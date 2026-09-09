package com.editnova.app.feature.editor

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.editnova.app.core.theme.EditNovaSpacing
import com.editnova.app.core.util.formatDurationMs
import com.editnova.app.domain.media.MediaType
import com.editnova.app.domain.media.SelectedMedia

/**
 * EditorPreviewScreen — the foundation of EditNova's video editor.
 *
 * Step 4A: shows the picked video/photo with play/pause/seek for video, plus a
 * FUNCTIONAL timeline (real thumbnail strip, draggable playhead, draggable start/end
 * trim handles) for video. Trim is non-destructive.
 *
 * Step 4B adds Split and Delete: Split cuts whichever clip contains the playhead into
 * two at that exact point; Delete removes the selected clip and closes the gap. Both
 * are non-destructive — they only edit the in-memory [ClipSegment] list (see
 * EditorUiState.segments / effectiveSegments), never the source file. There is still no
 * merge, filters, effects, export, or any other editing feature here.
 *
 * @param media The video/photo the user picked on Home, or null if selection failed
 *              or media was otherwise missing — handled as an honest error state
 *              instead of crashing (see [EditorErrorState]).
 */
@Composable
fun EditorPreviewScreen(
    media: SelectedMedia?,
    onBack: () -> Unit,
    viewModel: EditorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(media) {
        if (media != null) viewModel.setMedia(media) else viewModel.setMissingMedia()
    }

    // Real video thumbnails for the whole-video timeline strip. Regenerated only when
    // the media changes or the (original) duration first becomes known — never
    // per-frame, never fake/placeholder images. See ThumbnailGenerator.kt.
    var thumbnails by remember(media?.uri) { mutableStateOf<List<Bitmap>>(emptyList()) }
    LaunchedEffect(media?.uri, uiState.durationMs) {
        if (media != null && media.type == MediaType.VIDEO && uiState.durationMs > 0L) {
            thumbnails = generateVideoThumbnails(context, media.uri, uiState.durationMs)
        }
    }

    // Step 4B: one representative thumbnail PER SEGMENT, for SegmentStrip's chips.
    // Regenerated whenever the segment list itself changes (after a Split or Delete) —
    // keyed on the list's content, not identity, so unrelated recompositions don't
    // trigger unnecessary re-decoding.
    var segmentThumbnails by remember(media?.uri) { mutableStateOf<Map<String, Bitmap?>>(emptyMap()) }
    LaunchedEffect(media?.uri, uiState.effectiveSegments) {
        if (media != null && media.type == MediaType.VIDEO && uiState.durationMs > 0L) {
            segmentThumbnails = uiState.effectiveSegments.associate { segment ->
                val midpoint = (segment.sourceStartMs + segment.sourceEndMs) / 2
                segment.id to generateSingleThumbnail(context, media.uri, midpoint)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { EditorTopBar(onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- Main preview area: dark background, aspect ratio preserved ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.error != null -> EditorErrorState(message = uiState.error!!)

                    media != null && media.type == MediaType.VIDEO -> {
                        PlayerSurface(
                            uri = media.uri,
                            isPlaying = uiState.isPlaying,
                            seekToMs = uiState.pendingSeekMs,
                            segments = uiState.effectiveSegments,
                            onDurationReady = viewModel::onDurationResolved,
                            onPositionChanged = viewModel::onPositionChanged,
                            onPlayingChanged = viewModel::onPlayingChanged,
                            onError = viewModel::onError,
                            onPlaybackEnded = { viewModel.onPlayingChanged(false) },
                            onSeekHandled = viewModel::onSeekHandled,
                            onAllSegmentsPlaybackEnded = { viewModel.pauseAtSegmentsEnd() }
                        )
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    media != null -> {
                        AsyncImage(
                            model = media.uri,
                            contentDescription = "Selected photo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize(),
                            onSuccess = { viewModel.onImageReady() },
                            onError = { viewModel.onError("This photo couldn't be loaded.") }
                        )
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }

            // --- Video controls: video only, and only once there's no error ---
            if (media != null && media.type == MediaType.VIDEO && uiState.error == null) {
                VideoControls(
                    isPlaying = uiState.isPlaying,
                    positionMs = uiState.currentPositionMs,
                    durationMs = uiState.durationMs,
                    onPlayPauseClick = { viewModel.togglePlayPause() }
                )
            }

            // --- Timeline: functional for a ready video, a simple placeholder otherwise ---
            if (media != null && media.type == MediaType.VIDEO && uiState.durationMs > 0L && uiState.error == null) {
                FunctionalTimelineSection(
                    uiState = uiState,
                    thumbnails = thumbnails,
                    segmentThumbnails = segmentThumbnails,
                    onScrub = { viewModel.seekTo(it) },
                    onTrimStartChange = { viewModel.setTrimStart(it) },
                    onTrimEndChange = { viewModel.setTrimEnd(it) },
                    onResetTrim = { viewModel.resetTrim() },
                    onSegmentClick = { viewModel.selectSegment(it) },
                    onSplitClick = { viewModel.splitAtPlayhead() },
                    onDeleteClick = { viewModel.deleteSelectedSegment() }
                )
            } else if (media != null) {
                // Image, or video whose duration hasn't resolved yet — trim/split don't
                // apply, so fall back to the simple non-interactive placeholder.
                TimelinePlaceholder(media = media, durationMs = uiState.durationMs)
            }
        }
    }
}

/** Top bar: back button, fixed "New Project" title, and an options placeholder icon. */
@Composable
private fun EditorTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = EditNovaSpacing.xs, vertical = EditNovaSpacing.xs)
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = "New Project",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Center)
        )
        IconButton(
            // Placeholder only — no options menu exists yet.
            onClick = { },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More options",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

/** Shown in the preview area when media is missing, invalid, or fails to load/play. */
@Composable
private fun EditorErrorState(message: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(EditNovaSpacing.sm))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = EditNovaSpacing.lg)
        )
    }
}

/** Play/Pause button + "position / duration" text — video only. Scrubbing lives on the timeline's playhead (see [VideoTimeline]), so there's no separate slider here. */
@Composable
private fun VideoControls(
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onPlayPauseClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(EditNovaSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPlayPauseClick) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(modifier = Modifier.width(EditNovaSpacing.sm))
        Text(
            text = "${formatDurationMs(positionMs)} / ${formatDurationMs(durationMs)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * The functional timeline section: Step 4A's header + thumbnail-strip timeline with
 * draggable playhead/trim handles + trim-range readout + Reset Trim, PLUS Step 4B's
 * segment strip (one chip per clip, tap to select) and Split/Delete buttons.
 */
@Composable
private fun FunctionalTimelineSection(
    uiState: EditorUiState,
    thumbnails: List<Bitmap>,
    segmentThumbnails: Map<String, Bitmap?>,
    onScrub: (Long) -> Unit,
    onTrimStartChange: (Long) -> Unit,
    onTrimEndChange: (Long) -> Unit,
    onResetTrim: () -> Unit,
    onSegmentClick: (String) -> Unit,
    onSplitClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(EditNovaSpacing.md)
    ) {
        Text(
            text = "Timeline",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(EditNovaSpacing.sm))

        VideoTimeline(
            durationMs = uiState.durationMs,
            currentPositionMs = uiState.currentPositionMs,
            trimStartMs = uiState.trimStartMs,
            trimEndMs = uiState.trimEndMs,
            thumbnails = thumbnails,
            onScrub = onScrub,
            onTrimStartChange = onTrimStartChange,
            onTrimEndChange = onTrimEndChange
        )

        Spacer(modifier = Modifier.height(EditNovaSpacing.sm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${formatDurationMs(uiState.trimStartMs)} \u2013 ${formatDurationMs(uiState.trimEndMs)}" +
                    " (${formatDurationMs(uiState.trimmedDurationMs)})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onResetTrim) {
                Text("Reset Trim", color = MaterialTheme.colorScheme.secondary)
            }
        }

        Spacer(modifier = Modifier.height(EditNovaSpacing.md))

        // --- Step 4B: Clips strip + Split/Delete ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Clips (${formatDurationMs(uiState.totalTimelineDurationMs)} total)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(EditNovaSpacing.xs))

        SegmentStrip(
            segments = uiState.effectiveSegments,
            selectedSegmentId = uiState.selectedSegmentId,
            thumbnailsBySegmentId = segmentThumbnails,
            onSegmentClick = onSegmentClick
        )

        Spacer(modifier = Modifier.height(EditNovaSpacing.sm))

        val canDelete = uiState.selectedSegmentId != null && uiState.effectiveSegments.size > 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(EditNovaSpacing.sm)
        ) {
            OutlinedButton(onClick = onSplitClick) {
                Icon(
                    imageVector = Icons.Filled.ContentCut,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(EditNovaSpacing.xs))
                Text("Split", color = MaterialTheme.colorScheme.secondary)
            }
            Button(
                onClick = onDeleteClick,
                enabled = canDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(EditNovaSpacing.xs))
                Text("Delete")
            }
        }
    }
}

/**
 * A non-interactive placeholder for when the functional timeline doesn't apply yet:
 * a still photo (trim/split are video-only), or a video whose duration hasn't resolved
 * yet. Shows a thumbnail-style box, the media's duration (video) or "Photo" label
 * (image), and a "Timeline" section label — no scrubbing, no trim handles, no clips.
 */
@Composable
private fun TimelinePlaceholder(media: SelectedMedia, durationMs: Long) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(EditNovaSpacing.md)
    ) {
        Text(
            text = "Timeline",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(EditNovaSpacing.sm))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (media.type == MediaType.VIDEO) Icons.Filled.Movie else Icons.Filled.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(EditNovaSpacing.sm))
            Text(
                text = if (media.type == MediaType.VIDEO) formatDurationMs(durationMs) else "Photo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
