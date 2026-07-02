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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mimotts.data.AppColors
import com.example.mimotts.data.Gender
import com.example.mimotts.data.ROLE_LIBRARY
import com.example.mimotts.data.VOICE_LIBRARY
import com.example.mimotts.data.VoiceProfile
import com.example.mimotts.data.searchVoices

@Composable
fun VoiceScreen(selected: VoiceProfile, onSelect: (VoiceProfile) -> Unit) {
    var query by remember { mutableStateOf("") }
    var filterLang by remember { mutableStateOf("全部") }
    var filterGender by remember { mutableStateOf("全部") }
    val languages = listOf("全部") + VOICE_LIBRARY.map { it.language }.distinct()
    val genders = listOf("全部", "男声", "女声")
    val filtered = remember(query, filterLang, filterGender) {
        var list = if (query.isBlank()) VOICE_LIBRARY else searchVoices(query)
        if (filterLang != "全部") list = list.filter { it.language == filterLang }
        if (filterGender == "男声") list = list.filter { it.gender == Gender.Male }
        if (filterGender == "女声") list = list.filter { it.gender == Gender.Female }
        list
    }

    Column(Modifier.fillMaxSize()) {
        Text(
            "音色库", fontSize = 26.sp, fontWeight = FontWeight.Black, color = AppColors.Text,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp)
        )
        Text(
            "${VOICE_LIBRARY.size} 个音色可选", fontSize = 13.sp, color = AppColors.TextMuted,
            modifier = Modifier.padding(start = 20.dp, top = 2.dp, bottom = 12.dp)
        )

        OutlinedTextField(
            value = query, onValueChange = { query = it },
            Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            placeholder = { Text("搜索音色...", color = AppColors.TextMuted) },
            leadingIcon = { Icon(Icons.Filled.Search, null, tint = AppColors.TextMuted) },
            trailingIcon = {
                if (query.isNotEmpty()) IconButton(onClick = { query = "" }) {
                    Icon(Icons.Filled.Clear, null, tint = AppColors.TextMuted)
                }
            },
            textStyle = TextStyle(fontSize = 14.sp, color = AppColors.Text),
            singleLine = true, shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Accent, unfocusedBorderColor = AppColors.Divider,
                cursorColor = AppColors.Accent
            )
        )

        Spacer(Modifier.height(10.dp))
        LazyRow(
            Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(languages) { lang ->
                Chip(label = lang, selected = filterLang == lang) { filterLang = lang }
            }
        }
        Spacer(Modifier.height(6.dp))
        LazyRow(
            Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(genders) { g ->
                Chip(label = g, selected = filterGender == g) { filterGender = g }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "${filtered.size} 个结果", fontSize = 12.sp, color = AppColors.TextMuted,
            modifier = Modifier.padding(start = 20.dp, bottom = 6.dp)
        )

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filtered, key = { it.shortName }) { voice ->
                val isSelected = voice.shortName == selected.shortName
                Card(
                    onClick = { onSelect(voice) }, shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) AppColors.Accent.copy(0.1f) else AppColors.Card
                    )
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(38.dp).clip(CircleShape)
                                .background(
                                    if (voice.gender == Gender.Female) AppColors.Pink.copy(0.15f)
                                    else AppColors.Accent.copy(0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (voice.gender == Gender.Female) Icons.Filled.Woman else Icons.Filled.Man,
                                null,
                                tint = if (voice.gender == Gender.Female) AppColors.Pink else AppColors.Accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                voice.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) AppColors.Accent else AppColors.Text
                            )
                            Text(
                                "${voice.language} · ${voice.style} · ${voice.description}",
                                fontSize = 11.sp, color = AppColors.TextMuted, maxLines = 1
                            )
                        }
                        if (isSelected) Icon(
                            Icons.Filled.CheckCircle, null, tint = AppColors.Accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}
