package com.editnova.app.domain.project

/**
 * A text layer placed over the editor preview.
 *
 * This stores the editable properties of a text overlay so the same
 * information can later be used by preview and export.
 */
data class TextLayer(
    val id: String,
    val text: String,
    val x: Float = 0.5f,
    val y: Float = 0.5f,
    val fontSizeSp: Float = 24f,
    val colorArgb: Long = 0xFFFFFFFF
)
