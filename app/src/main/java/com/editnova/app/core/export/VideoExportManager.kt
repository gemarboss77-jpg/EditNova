package com.editnova.app.core.export

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.editnova.app.domain.project.ClipSegment
import java.io.File

class VideoExportManager(
    context: Context
) {

    private val transformer: Transformer =
        Transformer.Builder(context.applicationContext).build()

    fun export(
        inputUri: android.net.Uri,
        segments: List<ClipSegment>,
        outputFile: File,
        onCompleted: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val editedItems = segments.map { segment ->
            val mediaItem = MediaItem.Builder()
                .setUri(inputUri)
                .setClipStartPositionMs(segment.sourceStartMs)
                .setClipEndPositionMs(segment.sourceEndMs)
                .build()

            EditedMediaItem.Builder(mediaItem).build()
        }

        if (editedItems.isEmpty()) {
            onError(IllegalArgumentException("No video segments to export."))
            return
        }

        val sequence = EditedMediaItemSequence(editedItems)

        val composition = Composition.Builder(sequence).build()

        transformer.setListener(
            object : Transformer.Listener {
                override fun onCompleted(
                    composition: Composition,
                    exportResult: ExportResult
                ) {
                    onCompleted()
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    onError(exportException)
                }
            }
        )

        outputFile.parentFile?.mkdirs()
        transformer.start(composition, outputFile.absolutePath)
    }

    fun cancel() {
        transformer.cancel()
    }
}
