package com.example.mimotts.engine

import com.example.mimotts.audio.AudioManager
import com.example.mimotts.data.ParagraphStatus
import com.example.mimotts.data.ScriptParagraph
import com.example.mimotts.network.RetrofitClient
import com.example.mimotts.network.TtsRequest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class BatchSynthesizer(private val audioManager: AudioManager) {

    private val _paragraphs = MutableStateFlow<List<ScriptParagraph>>(emptyList())
    val paragraphs: StateFlow<List<ScriptParagraph>> = _paragraphs
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress
    private val _completedFiles = MutableStateFlow<List<File>>(emptyList())
    val completedFiles: StateFlow<List<File>> = _completedFiles
    private var job: Job? = null

    fun loadText(text: String) {
        val parts = text.split(Regex("\n{2,}|[。！？；\n]+")).map { it.trim() }.filter { it.isNotBlank() }
        _paragraphs.value = parts.mapIndexed { i, s -> ScriptParagraph(index = i, text = s) }
    }

    fun startBatch(
        voice: String = "zh-CN-XiaoxiaoNeural",
        rate: String = "+0%",
        pitch: String = "+0Hz",
        scope: CoroutineScope
    ) {
        if (_isProcessing.value) return
        job = scope.launch(Dispatchers.IO) {
            _isProcessing.value = true
            _completedFiles.value = emptyList()
            val files = mutableListOf<File>()
            val total = _paragraphs.value.size
            for ((i, para) in _paragraphs.value.withIndex()) {
                updateParagraph(para.copy(status = ParagraphStatus.Processing))
                try {
                    val outFile = audioManager.createCacheFile("batch_${para.index}.mp3")
                    val body = RetrofitClient.api.generateSpeech(
                        TtsRequest(text = para.text, voice = voice, rate = rate, pitch = pitch)
                    )
                    FileOutputStream(outFile).use { fos -> body.byteStream().copyTo(fos) }
                    files.add(outFile)
                    updateParagraph(para.copy(status = ParagraphStatus.Done))
                } catch (e: Exception) {
                    e.printStackTrace()
                    updateParagraph(para.copy(status = ParagraphStatus.Error))
                }
                _progress.value = (i + 1).toFloat() / total
            }
            _completedFiles.value = files
            _isProcessing.value = false
        }
    }

    fun cancel() {
        job?.cancel()
        _isProcessing.value = false
        _progress.value = 0f
        _paragraphs.value = _paragraphs.value.map { it.copy(status = ParagraphStatus.Pending) }
    }

    fun playAllSequential(onComplete: (() -> Unit)? = null) {
        val files = _completedFiles.value
        if (files.isEmpty()) return
        CoroutineScope(Dispatchers.Main).launch {
            for (file in files) {
                val done = CompletableDeferred<Unit>()
                withContext(Dispatchers.Main) { audioManager.play(file) { done.complete(Unit) } }
                done.await()
            }
            onComplete?.invoke()
        }
    }

    private suspend fun updateParagraph(updated: ScriptParagraph) {
        _paragraphs.value = _paragraphs.value.map { if (it.index == updated.index) updated else it }
    }
}
