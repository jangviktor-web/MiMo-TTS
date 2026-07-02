package com.example.mimotts.data

val VOICE_LIBRARY = listOf(
    VoiceProfile("晓晓", "zh-CN-XiaoxiaoNeural", Gender.Female, "zh-CN", "中文", "温柔", "适合有声书、客服"),
    VoiceProfile("晓伊", "zh-CN-XiaoyiNeural", Gender.Female, "zh-CN", "中文", "活泼", "适合儿童内容"),
    VoiceProfile("晓涵", "zh-CN-XiaohanNeural", Gender.Female, "zh-CN", "中文", "知性", "适合新闻播报"),
    VoiceProfile("晓梦", "zh-CN-XiaomengNeural", Gender.Female, "zh-CN", "中文", "甜美", "适合广告配音"),
    VoiceProfile("晓秋", "zh-CN-XiaoqiuNeural", Gender.Female, "zh-CN", "中文", "沉稳", "适合纪录片"),
    VoiceProfile("晓睿", "zh-CN-XiaoruiNeural", Gender.Female, "zh-CN", "中文", "正式", "适合商务汇报"),
    VoiceProfile("晓颜", "zh-CN-XiaoyanNeural", Gender.Female, "zh-CN", "中文", "清冷", "适合古风内容"),
    VoiceProfile("晓悠", "zh-CN-XiaoyouNeural", Gender.Female, "zh-CN", "中文", "悠闲", "适合放松内容"),
    VoiceProfile("晓墨", "zh-CN-XiaomoNeural", Gender.Female, "zh-CN", "中文", "文艺", "适合诗词朗诵"),
    VoiceProfile("晓辰", "zh-CN-XiaochenNeural", Gender.Female, "zh-CN", "中文", "温暖", "适合睡前故事"),
    VoiceProfile("晓彤", "zh-CN-XiaotongNeural", Gender.Female, "zh-CN", "中文", "开朗", "适合娱乐节目"),
    VoiceProfile("晓萱", "zh-CN-XiaoxuanNeural", Gender.Female, "zh-CN", "中文", "高雅", "适合正式场合"),
    VoiceProfile("云希", "zh-CN-YunxiNeural", Gender.Male, "zh-CN", "中文", "阳光", "适合短视频"),
    VoiceProfile("云扬", "zh-CN-YunyangNeural", Gender.Male, "zh-CN", "中文", "播音", "适合新闻播报"),
    VoiceProfile("云皓", "zh-CN-YunhaoNeural", Gender.Male, "zh-CN", "中文", "成熟", "适合商务场景"),
    VoiceProfile("云枫", "zh-CN-YunfengNeural", Gender.Male, "zh-CN", "中文", "沉稳", "适合纪录片"),
    VoiceProfile("云杰", "zh-CN-YunjianNeural", Gender.Male, "zh-CN", "中文", "磁性", "适合悬疑小说"),
    VoiceProfile("云夏", "zh-CN-YunxiaNeural", Gender.Male, "zh-CN", "中文", "少年", "适合动漫配音"),
    VoiceProfile("曉佳", "zh-HK-HiuGaaiNeural", Gender.Female, "zh-HK", "粤语", "温柔", "粤语女声"),
    VoiceProfile("雲松", "zh-HK-WanLungNeural", Gender.Male, "zh-HK", "粤语", "沉稳", "粤语男声"),
    VoiceProfile("曉臻", "zh-TW-HsiaoChenNeural", Gender.Female, "zh-TW", "台湾", "甜美", "台湾腔女声"),
    VoiceProfile("雲哲", "zh-TW-YunJheNeural", Gender.Male, "zh-TW", "台湾", "温和", "台湾腔男声"),
    VoiceProfile("Jenny", "en-US-JennyNeural", Gender.Female, "en-US", "English", "Friendly", "American Female"),
    VoiceProfile("Guy", "en-US-GuyNeural", Gender.Male, "en-US", "English", "Casual", "American Male"),
    VoiceProfile("Aria", "en-US-AriaNeural", Gender.Female, "en-US", "English", "Professional", "Newscast"),
    VoiceProfile("Davis", "en-US-DavisNeural", Gender.Male, "en-US", "English", "Warm", "Narration"),
    VoiceProfile("Sonia", "en-GB-SoniaNeural", Gender.Female, "en-GB", "English", "Elegant", "British Female"),
    VoiceProfile("Ryan", "en-GB-RyanNeural", Gender.Male, "en-GB", "English", "Calm", "British Male"),
    VoiceProfile("七夏", "ja-JP-NanamiNeural", Gender.Female, "ja-JP", "日本語", "優しい", "日本語女性"),
    VoiceProfile("圭太", "ja-JP-KeitaNeural", Gender.Male, "ja-JP", "日本語", "落ち着いた", "日本語男性"),
    VoiceProfile("선히", "ko-KR-SunHiNeural", Gender.Female, "ko-KR", "한국어", "친근한", "한국어 여성"),
    VoiceProfile("인준", "ko-KR-InJoonNeural", Gender.Male, "ko-KR", "한국어", "차분한", "한국어 남성"),
)

fun voicesByLanguage(): Map<String, List<VoiceProfile>> = VOICE_LIBRARY.groupBy { it.language }
fun voicesByGender(): Map<Gender, List<VoiceProfile>> = VOICE_LIBRARY.groupBy { it.gender }

fun searchVoices(query: String): List<VoiceProfile> {
    val q = query.lowercase()
    return VOICE_LIBRARY.filter {
        it.name.lowercase().contains(q) ||
                it.shortName.lowercase().contains(q) ||
                it.language.lowercase().contains(q) ||
                it.style.lowercase().contains(q) ||
                it.description.lowercase().contains(q)
    }
}
