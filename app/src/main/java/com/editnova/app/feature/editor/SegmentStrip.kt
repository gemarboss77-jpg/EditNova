package com.editnova.app.feature.editor

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.editnova.app.core.util.formatDurationMs
import com.editnova.app.domain.project.ClipSegment

/**
 * A horizontally scrollable row of "chips", one per timeline segment — the Step 4B
 * Split/Delete unit. Tapping a chip selects it (as the Delete target); the selected
 * chip is highlighted with a border. Each chip shows a real representative thumbnail
 * (a single decoded frame from that segment's midpoint, see
 * [generateSingleThumbnail]) and the segment's own non-destructive duration.
 *
 * Uses [LazyRow] (not a plain Row) so repeated splits producing many segments stay
 * smooth to scroll rather than laying out everything eagerly — see Step 4B's
 * performance requirement.
 */
@Composable
fun SegmentStrip(
    segments: List<ClipSegment>,
    selectedSegmentId: String?,
    thumbnailsBySegmentId: Map<String, Bitmap?>,
    onSegmentClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(segments, key = { it.id }) { segment ->
            SegmentChip(
                segment = segment,
                isSelected = segment.id == selectedSegmentId,
                thumbnail = thumbnailsBySegmentId[segment.id],
                onClick = { onSegmentClick(segment.id) }
            )
        }
    }
}

@Composable
private fun SegmentChip(
    segment: ClipSegment,
    isSelected: Boolean,
    thumbnail: Bitmap?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Text(
            text = formatDurationMs(segment.durationMs),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
