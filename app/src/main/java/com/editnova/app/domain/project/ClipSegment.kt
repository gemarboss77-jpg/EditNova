package com.editnova.app.domain.project

/**
 * A single non-destructive segment of the ORIGINAL source media, described purely as a
 * [sourceStartMs, sourceEndMs) range within it.
 *
 * EditNova's edited timeline (after Split/Delete) is just an ORDERED LIST of these — it
 * never copies, re-encodes, or otherwise touches the underlying video file. Splitting a
 * clip means replacing one [ClipSegment] with two adjacent ones that reference the same
 * source ranges on either side of the split point; deleting a clip means removing one
 * entry from the list. Preview playback plays through the list in order, jumping
 * between each segment's source range as needed — see PlayerSurface.kt.
 */
data class ClipSegment(
    val id: String,
    val sourceStartMs: Long,
    val sourceEndMs: Long
) {
    val durationMs: Long get() = (sourceEndMs - sourceStartMs).coerceAtLeast(0L)
}
