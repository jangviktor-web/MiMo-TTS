package com.example.mimotts.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mimotts.audio.AudioManager
import com.example.mimotts.data.AppColors
import com.example.mimotts.network.RetrofitClient

@Composable
fun SettingsScreen(audio: AudioManager) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("tts_settings", Context.MODE_PRIVATE) }
    var serverUrl by remember { mutableStateOf(prefs.getString("server_url", RetrofitClient.BASE_URL) ?: RetrofitClient.BASE_URL) }
    var cacheSize by remember { mutableStateOf(formatBytes(audio.getCacheSize())) }
    var showUrlDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("设置", fontSize = 26.sp, fontWeight = FontWeight.Black, color = AppColors.Text,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 16.dp))

        SectionHeader(icon = Icons.Filled.Dns, title = "服务器配置")
        Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.Card)) {
            Column(Modifier.padding(16.dp)) {
                Text("API 地址", fontSize = 12.sp, color = AppColors.TextMuted)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(serverUrl, fontSize = 14.sp, color = AppColors.Text, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showUrlDialog = true }, Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Edit, "修改", tint = AppColors.Accent, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "模拟器" to "http://10.0.2.2:5000",
                        "本机" to "http://127.0.0.1:5000",
                        "局域网" to "http://192.168.1.100:5000",
                    ).forEach { (label, url) ->
                        Chip(label = label, selected = serverUrl == url) {
                            serverUrl = url; applyUrl(prefs, url)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        SectionHeader(icon = Icons.Filled.Storage, title = "缓存管理")
        Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.Card)) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("音频缓存", fontSize = 14.sp, color = AppColors.Text)
                    Text(cacheSize, fontSize = 12.sp, color = AppColors.TextMuted, modifier = Modifier.padding(top = 2.dp))
                }
                OutlinedButton(onClick = { audio.clearCache(); cacheSize = formatBytes(0) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Error),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.DeleteOutline, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp)); Text("清除缓存", fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        SectionHeader(icon = Icons.Filled.Info, title = "关于")
        Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.Card)) {
            Column(Modifier.padding(16.dp)) {
                InfoRow("应用名称", "MiMo TTS")
                InfoRow("版本", "2.0")
                InfoRow("引擎", "Microsoft Azure TTS")
                InfoRow("音色数量", "${VOICE_COUNT}")
                Spacer(Modifier.height(8.dp))
                Text("由 MiMo 驱动的文本转语音客户端，支持多语言、多方言、多风格音色。",
                    fontSize = 12.sp, color = AppColors.TextMuted, lineHeight = 18.sp)
            }
        }
        Spacer(Modifier.height(80.dp))
    }

    if (showUrlDialog) {
        var input by remember { mutableStateOf(serverUrl) }
        AlertDialog(onDismissRequest = { showUrlDialog = false },
            containerColor = AppColors.Card, shape = RoundedCornerShape(18.dp),
            title = { Text("设置服务器地址", color = AppColors.Text, fontWeight = FontWeight.SemiBold) },
            text = {
                OutlinedTextField(value = input, onValueChange = { input = it },
                    placeholder = { Text("http://192.168.x.x:5000", color = AppColors.TextMuted) },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp, color = AppColors.Text),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Accent, unfocusedBorderColor = AppColors.Divider,
                        cursorColor = AppColors.Accent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    serverUrl = input.trim(); applyUrl(prefs, serverUrl); showUrlDialog = false
                }) { Text("确认", color = AppColors.Accent) }
            },
            dismissButton = {
                TextButton(onClick = { showUrlDialog = false }) { Text("取消", color = AppColors.TextMuted) }
            }
        )
    }
}

private const val VOICE_COUNT = 34

private fun applyUrl(prefs: android.content.SharedPreferences, url: String) {
    prefs.edit().putString("server_url", url).apply()
    RetrofitClient.BASE_URL = url
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
    bytes < 1024L * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024))
    else -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AppColors.Accent, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppColors.Text)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = AppColors.TextMuted)
        Text(value, fontSize = 13.sp, color = AppColors.Text, fontWeight = FontWeight.Medium)
    }
}
