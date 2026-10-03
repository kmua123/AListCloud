package com.kmuaz.alistcloud.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kmuaz.alistcloud.data.network.model.FileItem

@Composable
fun FileItemRow(file: FileItem, onClick: () -> Unit, onLongClick: () -> Unit) {
    val tint = if (file.is_dir) Color(0xFFF0AC36) else MaterialTheme.colorScheme.primary
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(start = 14.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).background(tint.copy(alpha = .12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center) {
                Icon(getFileIcon(file), null, tint = tint, modifier = Modifier.size(27.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(file.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(5.dp))
                Text(if (file.is_dir) "文件夹" else "${formatFileSize(file.size)} · ${formatTime(file.modified)}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                file.parent?.takeIf { it != "/" }?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            IconButton(onClick = onLongClick) { Icon(Icons.Default.MoreVert, "${file.name} 的更多操作", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}
