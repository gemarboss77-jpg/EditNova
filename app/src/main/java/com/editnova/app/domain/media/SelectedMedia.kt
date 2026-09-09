package com.editnova.app.domain.media

import android.net.Uri

/**
 * SelectedMedia — the single video or photo the user picked from their device for a
 * new project (see the Home screen's "New Project" button and the Editor Preview
 * screen in feature/editor/).
 *
 * This is a plain data holder — it doesn't touch ExoPlayer, Coil, or any other
 * playback/rendering library. Those live entirely in feature/editor/.
 */
data class SelectedMedia(
    val uri: Uri,
    val type: MediaType
)
