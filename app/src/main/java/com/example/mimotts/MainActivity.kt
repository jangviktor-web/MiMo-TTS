package com.example.mimotts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mimotts.audio.AudioManager
import com.example.mimotts.data.AppColors
import com.example.mimotts.data.PITCH_PRESETS
import com.example.mimotts.data.SPEED_PRESETS
import com.example.mimotts.data.ROLE_LIBRARY
import com.example.mimotts.data.VOICE_LIBRARY
import com.example.mimotts.data.VoiceProfile
import com.example.mimotts.engine.BatchSynthesizer
import com.example.mimotts.network.RetrofitClient
import com.example.mimotts.ui.BatchScreen
import com.example.mimotts.ui.ComposeScreen
import com.example.mimotts.ui.HistoryScreen
import com.example.mimotts.ui.SettingsScreen
import com.example.mimotts.ui.VoiceScreen
import com.example.mimotts.ui.noRippleClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MainActivity : ComponentActivity() {
    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences("tts_settings", Context.MODE_PRIVATE)
        prefs.getString("server_url", null)?.let { RetrofitClient.BASE_URL = it }

        audioManager = AudioManager(this)

        val sharedText = if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        } else ""

        setContent { MiMoTTSApp(audioManager = audioManager, initialText = sharedText) }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioManager.release()
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "合成", Icons.Filled.AutoAwesome)
    data object Voice : Screen("voice", "音色", Icons.Filled.RecordVoiceOver)
    data object Batch : Screen("batch", "文稿", Icons.Filled.Article)
    data object History : Screen("history", "历史", Icons.Filled.History)
    data object Settings : Screen("settings", "设置", Icons.Filled.Settings)
}

@Composable
fun MiMoTTSApp(audioManager: AudioManager, initialText: String = "") {
    val scope = remember { CoroutineScope(Dispatchers.Main) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var text by remember { mutableStateOf(initialText) }
    var selectedVoice by remember { mutableStateOf(VOICE_LIBRARY.first()) }
    var speedIndex by remember { mutableIntStateOf(2) }
    var pitchIndex by remember { mutableIntStateOf(2) }
    val speed = SPEED_PRESETS[speedIndex].value
    val pitch = PITCH_PRESETS[pitchIndex].value
    val batch = remember { BatchSynthesizer(audioManager) }
    val screens = listOf(Screen.Home, Screen.Voice, Screen.Batch, Screen.History, Screen.Settings)

    MaterialTheme(
        colorScheme = darkColorScheme(
            surface = AppColors.Surface, onSurface = AppColors.Text,
            primary = AppColors.Accent, onPrimary = AppColors.Bg,
            background = AppColors.Bg, onBackground = AppColors.Text,
            surfaceVariant = AppColors.Card, onSurfaceVariant = AppColors.TextMuted,
        )
    ) {
        Box(Modifier.fillMaxSize().background(AppColors.Bg).systemBarsPadding()) {
            Column(Modifier.fillMaxSize()) {
                Box(
                    Modifier.fillMaxWidth().height(2.dp).background(
                        Brush.horizontalGradient(listOf(AppColors.Accent, AppColors.Pink, AppColors.Orange, AppColors.Green))
                    )
                )
                Box(Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(150))
                        },
                        label = "page"
                    ) { screen ->
                        when (screen) {
                            is Screen.Home -> ComposeScreen(
                                text = text, onTextChange = { text = it },
                                voice = selectedVoice, speed = speed, pitch = pitch,
                                speedIndex = speedIndex, pitchIndex = pitchIndex,
                                onSpeedChange = { speedIndex = it }, onPitchChange = { pitchIndex = it },
                                audio = audioManager, scope = scope,
                                onPickVoice = { currentScreen = Screen.Voice },
                                onImportFile = {}
                            )
                            is Screen.Voice -> VoiceScreen(
                                selected = selectedVoice,
                                onSelect = { selectedVoice = it; currentScreen = Screen.Home }
                            )
                            is Screen.Batch -> BatchScreen(
                                batch = batch, audio = audioManager, text = text,
                                voice = selectedVoice, speed = speed, pitch = pitch, scope = scope
                            )
                            is Screen.History -> HistoryScreen(audio = audioManager)
                            is Screen.Settings -> SettingsScreen(audio = audioManager)
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.Card.copy(alpha = 0.97f))
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    screens.forEach { screen ->
                        val selected = currentScreen == screen
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clip(RoundedCornerShape(16.dp))
                                .noRippleClick { currentScreen = screen }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(screen.icon, screen.label,
                                tint = if (selected) AppColors.Accent else AppColors.TextMuted,
                                modifier = Modifier.size(22.dp))
                            Spacer(Modifier.height(2.dp))
                            Text(screen.label, fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) AppColors.Accent else AppColors.TextMuted)
                            if (selected) {
                                Spacer(Modifier.height(2.dp))
                                Box(Modifier.size(4.dp).clip(CircleShape).background(AppColors.Accent))
                            }
                        }
                    }
                }
            }
        }
    }
}
