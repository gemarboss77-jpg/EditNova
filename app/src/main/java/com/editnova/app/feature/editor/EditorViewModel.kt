package com.editnova.app.feature.editor

import androidx.lifecycle.ViewModel
import com.editnova.app.domain.media.MediaType
import com.editnova.app.domain.media.SelectedMedia
import com.editnova.app.domain.project.ClipSegment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * EditorViewModel — owns [EditorUiState] for the Editor Preview screen.
 *
 * This ViewModel does NOT create or hold the ExoPlayer instance itself — the player is
 * an Android framework resource tied to a Composable's lifecycle (see
 * PlayerSurface.kt), which reports events back here via the functions below. Keeping
 * the player out of the ViewModel avoids leaking a heavyweight Android object and keeps
 * this class simple, plain Kotlin state.
 *
 * UNDO/REDO ARCHITECTURE NOTE (Step 4A, still true in 4B): every state change here goes
 * through a single path — `_uiState.update { ... }` producing a new immutable
 * [EditorUiState] snapshot. That's deliberate groundwork for undo/redo: a future
 * history feature can record these snapshots (or the specific command that produced
 * them) without this class needing to change shape. No history list or Undo/Redo
 * actions are wired up yet — this is architecture-readiness only, not a working undo
 * feature.
 */
class EditorViewModel : ViewModel() {

    companion object {
        /** Smallest allowed trim/segment range — prevents creating an unusably tiny/invalid clip. */
        const val MIN_TRIM_DURATION_MS = 500L
    }

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    /** Called once, when the screen first receives the media the user picked. */
    fun setMedia(media: SelectedMedia) {
        _uiState.update {
            EditorUiState(
                media = media,
                // Images render immediately once loaded; video waits for the player to
                // report it's ready before we stop showing a loading indicator.
                isLoading = media.type == MediaType.VIDEO
            )
        }
    }

    /** Called when the screen has no valid media to show (missing/invalid selection). */
    fun setMissingMedia() {
        _uiState.update { EditorUiState(error = "No media selected.") }
    }

    /**
     * Called by the player once it knows the video's total ORIGINAL duration.
     * The first time this fires for a given media item, the trim range is initialized
     * to the full video (trimEndMs starts at 0 in a fresh [EditorUiState]).
     */
    fun onDurationResolved(durationMs: Long) {
        _uiState.update {
            it.copy(
                durationMs = durationMs,
                isLoading = false,
                trimEndMs = if (it.trimEndMs == 0L) durationMs else it.trimEndMs
            )
        }
    }

    fun onPositionChanged(positionMs: Long) {
        _uiState.update { it.copy(currentPositionMs = positionMs) }
    }

    fun onPlayingChanged(isPlaying: Boolean) {
        _uiState.update { it.copy(isPlaying = isPlaying) }
    }

    /**
     * Called when the user drags the playhead on the timeline.
     * Updates [EditorUiState.currentPositionMs] immediately so the UI feels responsive,
     * and sets [EditorUiState.pendingSeekMs] so PlayerSurface applies it to the real
     * player on its next recomposition — see [onSeekHandled].
     */
    fun seekTo(positionMs: Long) {
        _uiState.update { it.copy(currentPositionMs = positionMs, pendingSeekMs = positionMs) }
    }

    /** Called by PlayerSurface once it has applied a pending seek to the real player. */
    fun onSeekHandled() {
        _uiState.update { it.copy(pendingSeekMs = null) }
    }

    /**
     * Called while the user drags the Step 4A START trim handle.
     * Clamped so it can never go negative and can never cross within
     * [MIN_TRIM_DURATION_MS] of the current end handle.
     */
    fun setTrimStart(newStartMs: Long) {
        _uiState.update { state ->
            val maxAllowed = (state.trimEndMs - MIN_TRIM_DURATION_MS).coerceAtLeast(0L)
            val clampedStart = newStartMs.coerceIn(0L, maxAllowed)
            val newPosition = state.currentPositionMs.coerceAtLeast(clampedStart)
            state.copy(
                trimStartMs = clampedStart,
                currentPositionMs = newPosition,
                pendingSeekMs = if (newPosition != state.currentPositionMs) newPosition else state.pendingSeekMs
            )
        }
    }

    /**
     * Called while the user drags the Step 4A END trim handle.
     * Clamped so it can never exceed the original duration and can never cross within
     * [MIN_TRIM_DURATION_MS] of the current start handle.
     */
    fun setTrimEnd(newEndMs: Long) {
        _uiState.update { state ->
            val minAllowed = state.trimStartMs + MIN_TRIM_DURATION_MS
            val maxAllowed = state.durationMs.coerceAtLeast(minAllowed)
            val clampedEnd = newEndMs.coerceIn(minAllowed, maxAllowed)
            val newPosition = state.currentPositionMs.coerceAtMost(clampedEnd)
            state.copy(
                trimEndMs = clampedEnd,
                currentPositionMs = newPosition,
                pendingSeekMs = if (newPosition != state.currentPositionMs) newPosition else state.pendingSeekMs
            )
        }
    }

    /** Resets the trim range back to the full, original video AND clears any splits. */
    fun resetTrim() {
        _uiState.update {
            it.copy(
                trimStartMs = 0L,
                trimEndMs = it.durationMs,
                segments = emptyList(),
                selectedSegmentId = null,
                currentPositionMs = 0L,
                pendingSeekMs = 0L
            )
        }
    }

    /**
     * Step 4B: splits whichever segment currently contains the playhead into two,
     * exactly at the playhead's current position. A no-op (never crashes) if the
     * playhead isn't inside any segment, or is too close to that segment's own edges to
     * produce two valid (>= [MIN_TRIM_DURATION_MS]) pieces — this is what prevents
     * creating an invalid/zero-length clip.
     */
    fun splitAtPlayhead() {
        _uiState.update { state ->
            val current = state.effectiveSegments
            val playhead = state.currentPositionMs
            val index = current.indexOfFirst { segment ->
                playhead > segment.sourceStartMs + MIN_TRIM_DURATION_MS &&
                    playhead < segment.sourceEndMs - MIN_TRIM_DURATION_MS
            }
            if (index == -1) return@update state

            val target = current[index]
            val left = ClipSegment(
                id = UUID.randomUUID().toString(),
                sourceStartMs = target.sourceStartMs,
                sourceEndMs = playhead
            )
            val right = ClipSegment(
                id = UUID.randomUUID().toString(),
                sourceStartMs = playhead,
                sourceEndMs = target.sourceEndMs
            )
            val newSegments = current.toMutableList().also {
                it[index] = left
                it.add(index + 1, right)
            }
            state.copy(segments = newSegments, selectedSegmentId = null)
        }
    }

    /** Selects (or deselects, on a second tap) a segment as the Delete target. */
    fun selectSegment(id: String) {
        _uiState.update { it.copy(selectedSegmentId = if (it.selectedSegmentId == id) null else id) }
    }

    /**
     * Deletes the currently selected segment and closes the gap (remaining segments
     * simply play back-to-back in list order — see PlayerSurface.kt). A safe no-op if
     * nothing is selected, or if it's the only remaining segment — the app never allows
     * deleting a project down to zero clips. The UI additionally disables the Delete
     * button for both of these cases so this is a defensive backstop, not the primary
     * safeguard.
     */
    fun deleteSelectedSegment() {
        _uiState.update { state ->
            val current = state.effectiveSegments
            val id = state.selectedSegmentId
            if (id == null || current.size <= 1 || current.none { it.id == id }) {
                return@update state
            }

            val deleted = current.first { it.id == id }
            val remaining = current.filterNot { it.id == id }
            // If the playhead was inside the clip being deleted, snap it forward to
            // whatever now plays at that point instead of pointing at removed content.
            val newPosition = if (state.currentPositionMs in deleted.sourceStartMs until deleted.sourceEndMs) {
                remaining.firstOrNull { it.sourceStartMs >= deleted.sourceEndMs }?.sourceStartMs
                    ?: remaining.last().sourceStartMs
            } else {
                state.currentPositionMs
            }

            state.copy(
                segments = remaining,
                selectedSegmentId = null,
                currentPositionMs = newPosition,
                pendingSeekMs = if (newPosition != state.currentPositionMs) newPosition else state.pendingSeekMs
            )
        }
    }

    /**
     * Toggles play/pause. When starting playback, if the current position is before the
     * first segment or at/after the last segment's end, playback snaps back to the
     * start of the FIRST segment first — so pressing Play always starts inside the
     * edited timeline. (A position sitting inside a *gap* between two segments, e.g.
     * right after a delete, is left alone here — PlayerSurface's own per-tick check
     * naturally skips forward past it once playback starts; see PlayerSurface.kt.)
     */
    fun togglePlayPause() {
        _uiState.update { state ->
            if (state.isPlaying) {
                state.copy(isPlaying = false)
            } else {
                val segments = state.effectiveSegments
                val firstStart = segments.firstOrNull()?.sourceStartMs ?: state.trimStartMs
                val lastEnd = segments.lastOrNull()?.sourceEndMs ?: state.trimEndMs
                val outsideRange = state.currentPositionMs < firstStart || state.currentPositionMs >= lastEnd
                state.copy(
                    isPlaying = true,
                    currentPositionMs = if (outsideRange) firstStart else state.currentPositionMs,
                    pendingSeekMs = if (outsideRange) firstStart else state.pendingSeekMs
                )
            }
        }
    }

    /**
     * Called by PlayerSurface when playback reaches the end of the LAST segment in the
     * edited timeline — enforces "playback never continues past the selected range".
     */
    fun pauseAtSegmentsEnd() {
        _uiState.update { state ->
            val endPos = state.effectiveSegments.lastOrNull()?.sourceEndMs ?: state.trimEndMs
            state.copy(isPlaying = false, currentPositionMs = endPos, pendingSeekMs = endPos)
        }
    }

    /** Called when ExoPlayer reports a playback error, or Coil fails to load an image. */
    fun onError(message: String) {
        _uiState.update { it.copy(isLoading = false, error = message) }
    }

    /** Called once a still image has finished loading successfully. */
    fun onImageReady() {
        _uiState.update { it.copy(isLoading = false) }
    }
}
