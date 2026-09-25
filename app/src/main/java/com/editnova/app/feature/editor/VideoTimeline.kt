package com.editnova.app.feature.editor

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/** Visual width of each trim handle. */
private val HANDLE_WIDTH = 14.dp

/** Visual width of the playhead line. */
private val PLAYHEAD_WIDTH = 3.dp

private val TIMELINE_HEIGHT = 64.dp

/**
 * Functional video timeline: a real-thumbnail strip with a draggable playhead and
 * draggable start/end trim handles, all positioned by mapping time (ms) to horizontal
 * pixels across the strip's measured width.
 *
 * All three drag handles use the same correctness pattern to avoid a well-known Compose
 * pitfall: a `pointerInput(key) { detectDragGestures { ... } }` block only restarts
 * when `key` changes, so any external state (durationMs, trimStartMs, ...) read
 * directly inside its lambda would go STALE across recomposition — the block would
 * keep using whatever those values were when the gesture detector was first installed.
 * To avoid that, each handle here:
 *   1. keys pointerInput on Unit, so the gesture detector installs once and never
 *      restarts (removing the "restarts and resets mid-drag" bug),
 *   2. wraps the external values it needs in [rememberUpdatedState], so it can read the
 *      LATEST value at the moment a new drag begins,
 *   3. accumulates position purely from local drag deltas (`dragAmount`) once a drag has
 *      started, rather than re-reading external state mid-gesture — deltas are reliable
 *      regardless of what recomposed in between.
 *
 * @param thumbnails Real decoded video frames from [generateVideoThumbnails]. An empty
 *                    list (e.g. while generating, or if generation failed) falls back to
 *                    a plain filled strip — never fake/placeholder thumbnail images.
 */
@Composable
fun VideoTimeline(
    durationMs: Long,
    currentPositionMs: Long,
    trimStartMs: Long,
    trimEndMs: Long,
    thumbnails: List<Bitmap>,
    onScrub: (Long) -> Unit,
    onTrimStartChange: (Long) -> Unit,
    onTrimEndChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (durationMs <= 0L) return // Nothing meaningful to draw until duration is known.

    val density = LocalDensity.current
    val handleWidthPx = with(density) { HANDLE_WIDTH.toPx() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(TIMELINE_HEIGHT)
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        if (widthPx <= 0f) return@BoxWithConstraints

        fun msToX(ms: Long): Float = ((ms.toFloat() / durationMs.toFloat()) * widthPx).coerceIn(0f, widthPx)
        fun xToMs(x: Float): Long = ((x / widthPx) * durationMs).toLong().coerceIn(0L, durationMs)

        // --- Real thumbnail strip (or a plain fallback if none are available yet) ---
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        ) {
            if (thumbnails.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            } else {
                thumbnails.forEach { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                        
                            .fillMaxHeight()
                    )
                }
            }
        }

        val startX = msToX(trimStartMs)
        val endX = msToX(trimEndMs)

        // --- Dim the parts of the video that are outside the selected trim range ---
        if (startX > 0f) {
            Box(
                modifier = Modifier
                    .width(with(density) { startX.toDp() })
                    .fillMaxHeight()
                    .background(Color.Black.copy(alpha = 0.55f))
            )
        }
        if (endX < widthPx) {
            Box(
                modifier = Modifier
                    .offset(x = with(density) { endX.toDp() }, y = 0.dp)
                    .width(with(density) { (widthPx - endX).toDp() })
                    .fillMaxHeight()
                    .background(Color.Black.copy(alpha = 0.55f))
            )
        }

        // --- Start trim handle ---
        val latestTrimStart = rememberUpdatedState(trimStartMs)
        val latestDuration = rememberUpdatedState(durationMs)
        val latestWidthPx = rememberUpdatedState(widthPx)
        var startDragX by remember { mutableStateOf(startX) }
        Box(
            modifier = Modifier
                .offset(x = with(density) { (startX - handleWidthPx / 2f).toDp() }, y = 0.dp)
                .width(HANDLE_WIDTH)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(3.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            startDragX = (latestTrimStart.value.toFloat() / latestDuration.value.toFloat()) * latestWidthPx.value
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            startDragX = (startDragX + dragAmount.x).coerceIn(0f, latestWidthPx.value)
                            onTrimStartChange(((startDragX / latestWidthPx.value) * latestDuration.value).toLong())
                        }
                    )
                }
        )

        // --- End trim handle ---
        val latestTrimEnd = rememberUpdatedState(trimEndMs)
        var endDragX by remember { mutableStateOf(endX) }
        Box(
            modifier = Modifier
                .offset(x = with(density) { (endX - handleWidthPx / 2f).toDp() }, y = 0.dp)
                .width(HANDLE_WIDTH)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(3.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            endDragX = (latestTrimEnd.value.toFloat() / latestDuration.value.toFloat()) * latestWidthPx.value
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            endDragX = (endDragX + dragAmount.x).coerceIn(0f, latestWidthPx.value)
                            onTrimEndChange(((endDragX / latestWidthPx.value) * latestDuration.value).toLong())
                        }
                    )
                }
        )

        // --- Playhead (draggable — dragging it scrubs the video preview) ---
        val latestPosition = rememberUpdatedState(currentPositionMs)
        val playheadX = msToX(currentPositionMs)
        var playheadDragX by remember { mutableStateOf(playheadX) }
        Box(
            modifier = Modifier
                .offset(x = with(density) { playheadX.toDp() }, y = 0.dp)
                .width(PLAYHEAD_WIDTH)
                .fillMaxHeight()
                .background(Color.White)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            playheadDragX = (latestPosition.value.toFloat() / latestDuration.value.toFloat()) * latestWidthPx.value
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            playheadDragX = (playheadDragX + dragAmount.x).coerceIn(0f, latestWidthPx.value)
                            onScrub(((playheadDragX / latestWidthPx.value) * latestDuration.value).toLong())
                        }
                    )
                }
        )
    }
}
