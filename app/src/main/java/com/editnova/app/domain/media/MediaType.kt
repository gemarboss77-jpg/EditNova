package com.editnova.app.domain.media

/**
 * MediaType — whether a piece of media the user picked is a video or a photo.
 *
 * Kept as a plain enum in the domain layer (no Android/Compose/player dependencies)
 * so it can be referenced from navigation, UI, and playback code alike.
 */
enum class MediaType {
    VIDEO,
    IMAGE
}
