package com.editnova.app.feature.editor

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.editnova.app.domain.project.ClipSegment
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * PlayerSurface — creates, drives, and releases an ExoPlayer for a single video [uri].
 *
 * This is the ONLY place in the app that touches ExoPlayer directly. It reports what's
 * happening back to the caller via plain callbacks (onDurationReady, onPositionChanged,
 * onPlayingChanged, onError, onPlaybackEnded) instead of exposing the player object
 * itself, so [EditorPreviewScreen] only ever deals with [EditorUiState] — not an
 * Android media API.
 *
 * The player is created with [remember] (survives recomposition, tied to this
 * composable's lifecycle) and released inside [DisposableEffect]'s onDispose, so it's
 * always cleaned up when this leaves composition — avoiding a leaked native player.
 *
 * @param isPlaying The screen's desired play/pause state — this composable applies it
 *                   to the player rather than owning play/pause state itself, keeping
 *                   [EditorUiState] as the single source of truth for "should this be
 *                   playing right now".
 * @param seekToMs A one-shot seek command: when non-null, this composable seeks the
 *                  player to that position and immediately calls [onSeekHandled] so the
 *                  caller clears it back to null (see EditorViewModel.seekTo).
 * @param segments The caller's current edited timeline (Step 4A trim range, or Step 4B
 *                  split/delete result — see EditorUiState.effectiveSegments, which is
 *                  always non-empty by the time this is composed). While playing,
 *                  position polling finds whichever segment the current position falls
 *                  in, stops it from advancing past that segment's end, and jumps to the
 *                  next segment's start instead of continuing straight through — this is
 *                  what makes deleted/skipped ranges actually get skipped during preview,
 *                  and what enforces "never plays past the trim range" from Step 4A,
 *                  using the exact same mechanism (a single-element list reduces to the
 *                  old trim-only behavior).
 */
@Composable
fun PlayerSurface(
    uri: Uri,
    isPlaying: Boolean,
    seekToMs: Long?,
    segments: List<ClipSegment>,
    modifier: Modifier = Modifier,
    onDurationReady: (Long) -> Unit,
    onPositionChanged: (Long) -> Unit,
    onPlayingChanged: (Boolean) -> Unit,
    onError: (String) -> Unit,
    onPlaybackEnded: () -> Unit,
    onSeekHandled: () -> Unit,
    onAllSegmentsPlaybackEnded: () -> Unit
) {
    val context = LocalContext.current

    val exoPlayer = remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> onDurationReady(exoPlayer.duration.coerceAtLeast(0L))
                    Player.STATE_ENDED -> onPlaybackEnded()
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                onPlayingChanged(playing)
            }

            override fun onPlayerError(error: PlaybackException) {
                onError(error.message ?: "This video couldn't be played.")
            }
        }
        exoPlayer.addListener(listener)

        // Guaranteed to run when this composable leaves composition (screen closed,
        // media changed, etc.) — this is what prevents a leaked native player.
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Apply the screen's desired play/pause state to the real player whenever it changes.
    LaunchedEffect(isPlaying) {
        exoPlayer.playWhenReady = isPlaying
    }

    // Poll playback position while playing — ExoPlayer has no built-in position stream.
    // Also enforces the edited timeline: each tick, find whichever segment the current
    // position falls in (or most recently started before it, if it's sitting in a gap
    // left by a delete), and once position reaches THAT segment's end, jump straight to
    // the next segment's start instead of continuing into a gap or a deleted range. If
    // there's no next segment, this was the last one — stop.
    // Re-deriving "current segment" from the real position every tick (rather than
    // tracking it as separate mutable state) avoids any risk of that tracked index going
    // stale relative to `segments` after a Split/Delete changes the list mid-playback.
    // NOTE: 250ms polling means a boundary can be overshot by up to ~250ms before this
    // catches it — acceptable granularity for this foundation-level feature.
    LaunchedEffect(isPlaying, segments) {
        while (isActive && isPlaying) {
            val position = exoPlayer.currentPosition.coerceAtLeast(0L)
            val currentIndex = segments.indexOfLast { position >= it.sourceStartMs }.coerceAtLeast(0)
            val currentSegment = segments.getOrNull(currentIndex)
            if (currentSegment != null && position >= currentSegment.sourceEndMs) {
                val nextSegment = segments.getOrNull(currentIndex + 1)
                if (nextSegment != null) {
                    exoPlayer.seekTo(nextSegment.sourceStartMs)
                    onPositionChanged(nextSegment.sourceStartMs)
                } else {
                    onAllSegmentsPlaybackEnded()
                    break
                }
            } else {
                onPositionChanged(position)
            }
            delay(250)
        }
    }

    // Apply a pending seek command to the real player, then tell the caller it's done
    // so it can clear pendingSeekMs back to null (one-shot command pattern).
    LaunchedEffect(seekToMs) {
        if (seekToMs != null) {
            exoPlayer.seekTo(seekToMs)
            onSeekHandled()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false // EditorPreviewScreen draws its own minimal controls.
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT // Preserves aspect ratio, no stretching.
                setBackgroundColor(android.graphics.Color.BLACK)
            }
        }
    )
}
