package com.example.mimotts.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mimotts.audio.AudioManager
import com.example.mimotts.data.AppColors
import com.example.mimotts.data.ParagraphStatus
import com.example.mimotts.data.VoiceProfile
import com.example.mimotts.engine.BatchSynthesizer
import kotlinx.coroutines.CoroutineScope

@Composable
fun BatchScreen(
    batch: BatchSynthesizer, audio: AudioManager, text: String,
    voice: VoiceProfile, speed: String, pitch: String, scope: CoroutineScope
) {
    val paragraphs by batch.paragraphs.collectAsState()
    val isProcessing by batch.isProcessing.collectAsState()
    val progress by batch.progress.collectAsState()
    val files by batch.completedFiles.collectAsState()
    var hasLoaded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Text(
            "播音文稿", fontSize = 26.sp, fontWeight = FontWeight.Black, color = AppColors.Text,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp)
        )
        Text(
            "长文本自动分段，逐段合成，顺序播放", fontSize = 13.sp, color = AppColors.TextMuted,
            modifier = Modifier.padding(start = 20.dp, top = 2.dp, bottom = 16.dp)
        )

        Row(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { batch.loadText(text); hasLoaded = true },
                modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Accent),
                enabled = text.isNotBlank() && !isProcessing
            ) {
                Icon(Icons.Filled.AutoFixHigh, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp)); Text("解析段落")
            }
            Button(
                onClick = { batch.startBatch(voice = voice.shortName, rate = speed, pitch = pitch, scope = scope) },
                modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent, contentColor = AppColors.Bg),
                enabled = hasLoaded && !isProcessing && paragraphs.isNotEmpty()
            ) {
                if (isProcessing) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = AppColors.Bg)
                else Icon(Icons.Filled.PlaylistPlay, null, Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp)); Text("合成全部")
            }
        }

        AnimatedVisibility(visible = isProcessing || (hasLoaded && paragraphs.isNotEmpty())) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = AppColors.Accent, trackColor = AppColors.Divider
                )
                if (isProcessing) Text(
                    "${(progress * 100).toInt()}%", fontSize = 12.sp, color = AppColors.TextMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        AnimatedVisibility(visible = files.isNotEmpty() && !isProcessing, enter = fadeIn() + slideInVertically()) {
            Button(
                onClick = { batch.playAllSequential() },
                Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Green, contentColor = AppColors.Bg)
            ) {
                Icon(Icons.Filled.PlaylistPlay, null, Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp)); Text("顺序播放全部 (${files.size}段)", fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(8.dp))

        if (paragraphs.isNotEmpty()) {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(paragraphs, key = { it.index }) { para ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.Card)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(28.dp).clip(CircleShape).background(
                                    when (para.status) {
                                        ParagraphStatus.Done -> AppColors.Green.copy(0.15f)
                                        ParagraphStatus.Processing -> AppColors.Accent.copy(0.15f)
                                        ParagraphStatus.Error -> AppColors.Error.copy(0.15f)
                                        ParagraphStatus.Pending -> AppColors.CardHover
                                    }
                                ), contentAlignment = Alignment.Center
                            ) {
                                when (para.status) {
                                    ParagraphStatus.Done -> Icon(
                                        Icons.Filled.Check, null, tint = AppColors.Green,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    ParagraphStatus.Processing -> CircularProgressIndicator(
                                        Modifier.size(14.dp), strokeWidth = 2.dp, color = AppColors.Accent
                                    )
                                    ParagraphStatus.Error -> Icon(
                                        Icons.Filled.ErrorOutline, null, tint = AppColors.Error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    ParagraphStatus.Pending -> Text(
                                        "${para.index + 1}", fontSize = 11.sp, color = AppColors.TextMuted
                                    )
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                para.text, fontSize = 13.sp,
                                color = AppColors.Text.copy(alpha = if (para.status == ParagraphStatus.Pending) 0.6f else 1f),
                                maxLines = 2, modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}
