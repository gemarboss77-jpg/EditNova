package com.editnova.app.feature.editor

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Number of frames pulled for the timeline strip — fixed regardless of video length. */
private const val THUMBNAIL_COUNT = 8

/** Requested frame width in px — small on purpose, this is a scrubber strip, not a gallery. */
private const val THUMBNAIL_WIDTH_PX = 160

/** Decodes one downscaled frame at [timeMs] using whichever retriever API is available. */
private fun decodeFrameAt(retriever: MediaMetadataRetriever, timeMs: Long): Bitmap? {
    val timeUs = timeMs.coerceAtLeast(0L) * 1000L
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            retriever.getScaledFrameAtTime(
                timeUs,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                THUMBNAIL_WIDTH_PX,
                THUMBNAIL_WIDTH_PX
            )
        } else {
            @Suppress("DEPRECATION")
            retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        }
    }.getOrNull()
}

/**
 * Generates a small, FIXED number of evenly-spaced, downscaled thumbnails from the real
 * video at [uri] for the timeline's thumbnail strip.
 *
 * Deliberately bounded: a 10-second clip and a 2-hour clip both produce exactly
 * [count] small bitmaps, never "one frame per second" or similar — this is what keeps
 * memory use small and predictable regardless of video length (see Step 4A performance
 * requirement). Frames are requested pre-scaled via [MediaMetadataRetriever.getScaledFrameAtTime]
 * on API 27+ so decoding never allocates a full-resolution bitmap just to discard it.
 *
 * Runs entirely on [Dispatchers.IO] so decoding never blocks the UI thread. Never
 * throws: any failure (corrupt file, unsupported codec, retriever error) results in an
 * empty list rather than a crash — the timeline still functions without thumbnails,
 * falling back to a plain strip (see VideoTimeline.kt).
 */
suspend fun generateVideoThumbnails(
    context: Context,
    uri: Uri,
    durationMs: Long,
    count: Int = THUMBNAIL_COUNT
): List<Bitmap> = withContext(Dispatchers.IO) {
    if (durationMs <= 0L || count <= 0) return@withContext emptyList()

    val retriever = MediaMetadataRetriever()
    val thumbnails = try {
        retriever.setDataSource(context, uri)
        val stepMs = durationMs / count
        (0 until count).mapNotNull { index -> decodeFrameAt(retriever, stepMs * index) }
    } catch (e: Exception) {
        // Invalid/unsupported video, corrupt file, permission issue, etc. — fail
        // gracefully with no thumbnails rather than crashing.
        emptyList()
    } finally {
        runCatching { retriever.release() }
    }

    thumbnails
}

/**
 * Generates a SINGLE representative thumbnail at [atMs] — used for Step 4B's per-segment
 * chip in [SegmentStrip], since each segment (post-Split) needs its own small preview
 * rather than sharing the whole-video strip from [generateVideoThumbnails]. Same
 * never-throws, IO-dispatched, pre-scaled-decode approach as above; returns null (never
 * crashes) on any failure.
 */
suspend fun generateSingleThumbnail(context: Context, uri: Uri, atMs: Long): Bitmap? =
    withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            decodeFrameAt(retriever, atMs)
        } catch (e: Exception) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }
