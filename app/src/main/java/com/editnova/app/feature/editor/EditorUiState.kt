package com.editnova.app.feature.editor

import com.editnova.app.domain.media.SelectedMedia
import com.editnova.app.domain.project.ClipSegment

/**
 * EditorUiState — everything the Editor Preview screen needs to render, kept separate
 * from the Composable UI itself (see [EditorViewModel]) so it's easy to reason about.
 *
 * Trim/Split/Delete here are all non-destructive: they only describe *ranges within the
 * original media*, never modify the source file itself. No clip list beyond [segments],
 * no export settings — those belong to later steps.
 */
data class EditorUiState(
    val media: SelectedMedia? = null,
    /** The ORIGINAL, untrimmed duration of the media, as reported by the player. */
    val durationMs: Long = 0L,
    val currentPositionMs: Long = 0L,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    /**
     * A one-shot seek command: non-null while a seek to this position is pending, and
     * cleared back to null once [com.editnova.app.feature.editor.PlayerSurface] has
     * applied it to the real player (see EditorViewModel.seekTo / onSeekHandled).
     */
    val pendingSeekMs: Long? = null,
    /** Start of the Step 4A trim range, in ms from the start of the original media. */
    val trimStartMs: Long = 0L,
    /**
     * End of the Step 4A trim range, in ms. Defaults to 0L until [durationMs] is first
     * resolved, at which point EditorViewModel initializes it to the full duration —
     * see EditorViewModel.onDurationResolved.
     */
    val trimEndMs: Long = 0L,
    /**
     * Step 4B: the timeline as an ORDERED LIST of non-destructive segments, once the
     * user has performed at least one Split. Empty before that — see [effectiveSegments]
     * for the value that actually drives preview/playback either way.
     */
    val segments: List<ClipSegment> = emptyList(),
    /** The segment currently selected for Delete, or null if none is selected. */
    val selectedSegmentId: String? = null
) {
    /** The length of the Step 4A trim range — what would be exported before any split. */
    val trimmedDurationMs: Long get() = (trimEndMs - trimStartMs).coerceAtLeast(0L)

    /**
     * The segments that actually define the current edited timeline for preview and
     * playback purposes. Falls back to a single IMPLICIT segment spanning
     * [trimStartMs, trimEndMs] until the user's first Split, at which point [segments]
     * becomes the real, persisted source of truth. This lets playback/UI code treat
     * "one clip" and "several clips" uniformly instead of branching everywhere.
     */
    val effectiveSegments: List<ClipSegment>
        get() = segments.ifEmpty {
            listOf(ClipSegment(id = "implicit", sourceStartMs = trimStartMs, sourceEndMs = trimEndMs))
        }

    /** Total duration of the edited timeline right now — sum of every segment's length. */
    val totalTimelineDurationMs: Long get() = effectiveSegments.sumOf { it.durationMs }
}
