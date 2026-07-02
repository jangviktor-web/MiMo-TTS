package com.example.mimotts.audio

import android.content.ContentValues
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.mimotts.data.HistoryItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileInputStream

class AudioManager(private val context: Context) {

    private var player: MediaPlayer? = null
    private var onStateChange: ((PlayState) -> Unit)? = null

    enum class PlayState { IDLE, PLAYING, PAUSED }

    var currentState: PlayState = PlayState.IDLE
        private set

    val cacheDir: File = File(context.cacheDir, "tts_cache").apply { mkdirs() }
    private val gson = Gson()
    private val prefs = context.getSharedPreferences("tts_history", Context.MODE_PRIVATE)

    fun setOnStateChange(listener: (PlayState) -> Unit) { onStateChange = listener }

    fun play(file: File, onComplete: (() -> Unit)? = null) {
        stop()
        player = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            start()
            setOnCompletionListener {
                currentState = PlayState.IDLE
                onStateChange?.invoke(PlayState.IDLE)
                onComplete?.invoke()
            }
        }
        currentState = PlayState.PLAYING
        onStateChange?.invoke(PlayState.PLAYING)
    }

    fun pause() {
        player?.pause(); currentState = PlayState.PAUSED; onStateChange?.invoke(PlayState.PAUSED)
    }

    fun resume() {
        player?.start(); currentState = PlayState.PLAYING; onStateChange?.invoke(PlayState.PLAYING)
    }

    fun stop() {
        player?.let { try { it.stop() } catch (_: Exception) {}; it.release() }
        player = null
        currentState = PlayState.IDLE
        onStateChange?.invoke(PlayState.IDLE)
    }

    fun toggle() {
        when (currentState) {
            PlayState.PLAYING -> pause()
            PlayState.PAUSED -> resume()
            else -> {}
        }
    }

    val isPlaying get() = currentState == PlayState.PLAYING

    fun createCacheFile(name: String = "tts_${System.currentTimeMillis()}.mp3") = File(cacheDir, name)

    fun clearCache() { cacheDir.listFiles()?.forEach { it.delete() } }

    fun getCacheSize(): Long = cacheDir.listFiles()?.sumOf { it.length() } ?: 0L

    fun exportAudio(sourceFile: File, displayName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                    put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/MiMoTTS")
                }
                val uri = context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return false
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    FileInputStream(sourceFile).use { it.copyTo(out) }
                }
                true
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "MiMoTTS").apply { mkdirs() }
                sourceFile.copyTo(File(dir, displayName), overwrite = true)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace(); false
        }
    }

    fun saveHistory(item: HistoryItem) {
        val list = getHistory().toMutableList()
        list.add(0, item)
        if (list.size > 100) list.subList(100, list.size).clear()
        prefs.edit().putString("history", gson.toJson(list)).apply()
    }

    fun getHistory(): List<HistoryItem> {
        val json = prefs.getString("history", null) ?: return emptyList()
        val type = object : TypeToken<List<HistoryItem>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun deleteHistory(id: Long) {
        val list = getHistory().toMutableList()
        list.removeAll { it.id == id }
        prefs.edit().putString("history", gson.toJson(list)).apply()
        File(cacheDir, "${id}.mp3").delete()
    }

    fun release() { stop() }
}
