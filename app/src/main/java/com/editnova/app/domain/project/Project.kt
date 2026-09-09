package com.editnova.app.domain.project

/**
 * Project — represents a single video editing project.
 *
 * Step 1 only uses this as an empty-state placeholder type on the Home screen (see
 * HomeScreen.kt) — no project is ever actually created, saved, or loaded yet. This
 * model is defined now so that when "New Project" / video import is built in a later
 * step, the Home screen's "Recent Projects" list already knows what shape of data
 * to expect and doesn't need to be restructured.
 */
data class Project(
    val id: String,
    val name: String,
    val thumbnailUrl: String? = null,
    val durationSeconds: Long = 0L,
    val lastEditedAtEpochMillis: Long
)
