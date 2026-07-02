package com.example.mimotts.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mimotts.audio.AudioManager
import com.example.mimotts.data.AppColors
import com.example.mimotts.data.Gender
import com.example.mimotts.data.HistoryItem
import com.example.mimotts.data.PITCH_PRESETS
import com.example.mimotts.data.SPEED_PRESETS
import com.example.mimotts.data.VoiceProfile
import com.example.mimotts.network.RetrofitClient
import com.example.mimotts.network.TtsRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun ComposeScreen(
    text: String, onTextChange: (String) -> Unit,
    voice: VoiceProfile, speed: String, pitch: String,
    speedIndex: Int, pitchIndex: Int,
    onSpeedChange: (Int) -> Unit, onPitchChange: (Int) -> Unit,
    audio: AudioManager, scope: CoroutineScope,
    onPickVoice: () -> Unit, onImportFile: () -> Unit,
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var playState by remember { mutableStateOf(AudioManager.PlayState.IDLE) }
    var generatedFile by remember { mutableStateOf<File?>(null) }
    var charCount by remember { mutableIntStateOf(text.length) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.data?.let { uri ->
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
            if (!content.isNullOrBlank()) {
                onTextChange(content); charCount = content.length
            }
        }
    }
    audio.setOnStateChange { playState = it }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(
            Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                "文本合成", style = TextStyle(
                    fontSize = 28.sp, fontWeight = FontWeight.Black,
                    brush = Brush.linearGradient(listOf(AppColors.Accent, AppColors.Pink))
                )
            )
            Spacer(Modifier.weight(1f))
            Text("${charCount}字", fontSize = 13.sp, color = AppColors.TextMuted)
        }
        Text("输入文字，一键转为自然语音", fontSize = 13.sp, color = AppColors.TextMuted,
            modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(18.dp))

        // 输入框
        Card(
            Modifier.padding(horizontal = 16.dp).fillMaxWidth(), shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.Card)
        ) {
            Column {
                OutlinedTextField(
                    value = text, onValueChange = { onTextChange(it); charCount = it.length },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp),
                    placeholder = { Text("在这里输入要合成的文本...", color = AppColors.TextMuted) },
                    textStyle = TextStyle(fontSize = 15.sp, color = AppColors.Text, lineHeight = 24.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = AppColors.Accent
                    ),
                    maxLines = 15
                )
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = {
                        filePicker.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE); type = "text/*"
                        })
                    }, Modifier.size(36.dp)) {
                        Icon(Icons.Filled.FileOpen, "导入", tint = AppColors.TextMuted,
                            modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = {
                        val cb = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                                as android.content.ClipboardManager
                        val clip = cb.primaryClip
                        if (clip != null && clip.itemCount > 0) {
                            val p = clip.getItemAt(0).coerceToText(context).toString()
                            onTextChange(p); charCount = p.length
                        }
                    }, Modifier.size(36.dp)) {
                        Icon(Icons.Filled.ContentPaste, "粘贴", tint = AppColors.TextMuted,
                            modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = { onTextChange(""); charCount = 0 }, Modifier.size(36.dp)) {
                        Icon(Icons.Filled.ClearAll, "清空", tint = AppColors.TextMuted,
                            modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        // 音色选择
        Card(
            onClick = onPickVoice, modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.Card)
        ) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape)
                        .background(if (voice.gender == Gender.Female) AppColors.Pink.copy(0.15f) else AppColors.Accent.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (voice.gender == Gender.Female) Icons.Filled.Woman else Icons.Filled.Man,
                        null,
                        tint = if (voice.gender == Gender.Female) AppColors.Pink else AppColors.Accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(voice.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = AppColors.Text)
                    Text("${voice.language} · ${voice.style}", fontSize = 12.sp, color = AppColors.TextMuted)
                }
                Icon(Icons.Filled.ChevronRight, null, tint = AppColors.TextMuted)
            }
        }
        Spacer(Modifier.height(14.dp))

        // 语速
        Card(
            Modifier.padding(horizontal = 16.dp).fillMaxWidth(), shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.Card)
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Speed, null, tint = AppColors.Accent, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp)); Text("语速", fontSize = 13.sp, color = AppColors.Text)
                    Spacer(Modifier.weight(1f)); Text(SPEED_PRESETS[speedIndex].label, fontSize = 13.sp, color = AppColors.Accent)
                }
                Slider(
                    value = speedIndex.toFloat(), onValueChange = { onSpeedChange(it.toInt()) },
                    valueRange = 0f..4f, steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = AppColors.Accent, activeTrackColor = AppColors.Accent,
                        inactiveTrackColor = AppColors.Divider
                    )
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        // 音调
        Card(
            Modifier.padding(horizontal = 16.dp).fillMaxWidth(), shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.Card)
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Tune, null, tint = AppColors.Pink, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp)); Text("音调", fontSize = 13.sp, color = AppColors.Text)
                    Spacer(Modifier.weight(1f)); Text(PITCH_PRESETS[pitchIndex].label, fontSize = 13.sp, color = AppColors.Pink)
                }
                Slider(
                    value = pitchIndex.toFloat(), onValueChange = { onPitchChange(it.toInt()) },
                    valueRange = 0f..4f, steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = AppColors.Pink, activeTrackColor = AppColors.Pink,
                        inactiveTrackColor = AppColors.Divider
                    )
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        // 生成按钮
        Button(
            onClick = {
                if (text.isBlank()) return@Button
                scope.launch {
                    isLoading = true; audio.stop()
                    try {
                        val outFile = audio.createCacheFile()
                        val body = RetrofitClient.api.generateSpeech(
                            TtsRequest(text = text.trim(), voice = voice.shortName, rate = speed, pitch = pitch)
                        )
                        FileOutputStream(outFile).use { body.byteStream().copyTo(it) }
                        generatedFile = outFile; audio.play(outFile)
                        audio.saveHistory(
                            HistoryItem(
                                text = text.take(80), voiceName = voice.name,
                                voiceShortName = voice.shortName, filePath = outFile.absolutePath
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        isLoading = false
                    }
                }
            },
            Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent, contentColor = AppColors.Bg),
            enabled = text.isNotBlank() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = AppColors.Bg)
                Spacer(Modifier.width(10.dp)); Text("合成中...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            } else {
                Icon(Icons.Filled.PlayArrow, null, Modifier.size(26.dp))
                Spacer(Modifier.width(8.dp)); Text("生成并播放", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // 播放控制
        AnimatedVisibility(
            visible = generatedFile != null && !isLoading,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Card(
                Modifier.padding(16.dp).fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.Card)
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = { audio.stop() }) {
                        Icon(Icons.Filled.Stop, "停止", tint = AppColors.TextMuted, modifier = Modifier.size(26.dp))
                    }
                    FilledIconButton(
                        onClick = { audio.toggle() }, modifier = Modifier.size(50.dp), shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = AppColors.Accent, contentColor = AppColors.Bg
                        )
                    ) {
                        Icon(
                            if (playState == AudioManager.PlayState.PLAYING) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            null, Modifier.size(28.dp)
                        )
                    }
                    IconButton(onClick = {
                        generatedFile?.let { audio.exportAudio(it, "MiMo_${System.currentTimeMillis()}.mp3") }
                    }) {
                        Icon(Icons.Filled.SaveAlt, "导出", tint = AppColors.TextMuted, modifier = Modifier.size(26.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}
