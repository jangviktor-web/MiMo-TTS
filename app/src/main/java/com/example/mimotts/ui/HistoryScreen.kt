package com.example.mimotts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mimotts.audio.AudioManager
import com.example.mimotts.data.AppColors
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(audio: AudioManager) {
    var items by remember { mutableStateOf(audio.getHistory()) }
    var playingId by remember { mutableStateOf<Long?>(null) }
    val sdf = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.padding(start = 20.dp, end = 16.dp, top = 20.dp, bottom = 4.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("历史记录", fontSize = 26.sp, fontWeight = FontWeight.Black, color = AppColors.Text)
                Text("${items.size} 条记录", fontSize = 13.sp, color = AppColors.TextMuted, modifier = Modifier.padding(top = 2.dp))
            }
            if (items.isNotEmpty()) {
                TextButton(onClick = {
                    items.forEach { audio.deleteHistory(it.id) }
                    items = emptyList()
                }) {
                    Icon(Icons.Filled.DeleteSweep, null, tint = AppColors.Error, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("清空", color = AppColors.Error, fontSize = 13.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.History, null, modifier = Modifier.size(56.dp),
                        tint = AppColors.TextMuted.copy(alpha = 0.3f))
                    Spacer(Modifier.height(12.dp))
                    Text("暂无历史记录", fontSize = 15.sp, color = AppColors.TextMuted)
                    Text("合成语音后会自动保存在这里", fontSize = 12.sp, color = AppColors.TextMuted.copy(alpha = 0.6f))
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (playingId == item.id) AppColors.Accent.copy(0.08f) else AppColors.Card
                        )
                    ) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(36.dp).clip(CircleShape).background(AppColors.Accent.copy(0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.RecordVoiceOver, null, tint = AppColors.Accent,
                                        modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(item.voiceName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                        color = AppColors.Text)
                                    Text(sdf.format(Date(item.createdAt)), fontSize = 11.sp, color = AppColors.TextMuted)
                                }
                                IconButton(onClick = {
                                    val file = File(item.filePath)
                                    if (file.exists()) {
                                        if (playingId == item.id) {
                                            audio.stop(); playingId = null
                                        } else {
                                            audio.stop()
                                            audio.play(file) { playingId = null }
                                            playingId = item.id
                                        }
                                    }
                                }, Modifier.size(36.dp)) {
                                    Icon(
                                        if (playingId == item.id) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                        null,
                                        tint = if (playingId == item.id) AppColors.Error else AppColors.Accent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(item.text, fontSize = 13.sp, color = AppColors.Text.copy(alpha = 0.8f),
                                maxLines = 3, overflow = TextOverflow.Ellipsis, lineHeight = 20.sp)
                            Spacer(Modifier.height(6.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = {
                                    val file = File(item.filePath)
                                    if (file.exists()) audio.exportAudio(file, "MiMo_${item.id}.mp3")
                                }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                                    Icon(Icons.Filled.SaveAlt, null, modifier = Modifier.size(14.dp),
                                        tint = AppColors.TextMuted)
                                    Spacer(Modifier.width(4.dp))
                                    Text("导出", fontSize = 11.sp, color = AppColors.TextMuted)
                                }
                                TextButton(onClick = {
                                    audio.deleteHistory(item.id)
                                    items = audio.getHistory()
                                }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                                    Icon(Icons.Filled.Delete, null, modifier = Modifier.size(14.dp),
                                        tint = AppColors.Error.copy(alpha = 0.7f))
                                    Spacer(Modifier.width(4.dp))
                                    Text("删除", fontSize = 11.sp, color = AppColors.Error.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}
