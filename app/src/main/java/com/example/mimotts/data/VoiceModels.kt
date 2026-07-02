package com.example.mimotts.data

import androidx.compose.ui.graphics.Color

data class VoiceProfile(
    val name: String,
    val shortName: String,
    val gender: Gender,
    val locale: String,
    val language: String,
    val style: String = "默认",
    val description: String = "",
)

enum class Gender { Male, Female }

data class SpeedPreset(val label: String, val value: String, val icon: String)

val SPEED_PRESETS = listOf(
    SpeedPreset("极慢", "-60%", ""),
    SpeedPreset("慢速", "-30%", ""),
    SpeedPreset("正常", "+0%", ""),
    SpeedPreset("快速", "+30%", ""),
    SpeedPreset("极快", "+60%", ""),
)

val PITCH_PRESETS = listOf(
    SpeedPreset("极低", "-50Hz", ""),
    SpeedPreset("低沉", "-25Hz", ""),
    SpeedPreset("正常", "+0Hz", ""),
    SpeedPreset("明亮", "+25Hz", ""),
    SpeedPreset("尖锐", "+50Hz", ""),
)

data class HistoryItem(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val voiceName: String,
    val voiceShortName: String,
    val filePath: String,
    val duration: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
)

data class ScriptParagraph(
    val index: Int,
    val text: String,
    val status: ParagraphStatus = ParagraphStatus.Pending,
)

enum class ParagraphStatus { Pending, Processing, Done, Error }

data class CloneVoiceRequest(
    val name: String,
    val audioBase64: String,
    val format: String = "wav",
)

data class CloneVoiceResponse(
    val success: Boolean,
    val voiceId: String? = null,
    val message: String = "",
)

object AppColors {
    val Bg        = Color(0xFF080C18)
    val Surface   = Color(0xFF0F1528)
    val Card      = Color(0xFF162038)
    val CardHover = Color(0xFF1C2848)
    val Accent    = Color(0xFF00D4FF)
    val AccentDim = Color(0xFF007A99)
    val Pink      = Color(0xFFFF4D8D)
    val Orange    = Color(0xFFFF9F43)
    val Green     = Color(0xFF2ED573)
    val Text      = Color(0xFFECF0F6)
    val TextMuted = Color(0xFF5A6B8A)
    val Divider   = Color(0xFF1E2D4A)
    val Error     = Color(0xFFFF6B6B)
}
