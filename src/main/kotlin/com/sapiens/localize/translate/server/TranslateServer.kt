package com.sapiens.localize.translate.server

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.sapiens.localize.translate.env.Env
import com.sapiens.localize.translate.env.EnvConfig
import com.sapiens.localize.translate.env.isOpenAi
import com.sapiens.localize.translate.server.network.executeHttpStream
import com.sapiens.localize.translate.utils.Command
import com.sapiens.localize.translate.utils.languageName
import com.sapiens.localize.translate.utils.logd
import com.sapiens.localize.translate.utils.logi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TranslateServer(
    private val language: String,
    private val command: Command,
    private val text: String,
) {

    suspend fun translate(): String {
        val deferredResult = CompletableDeferred<String>()

        val job = CoroutineScope(Dispatchers.IO).launch {
            val sb = StringBuilder()
            executeHttpStream(url(), body()) { line ->
                logd("TranslateServer", "received origin line: $line")
                line ?: return@executeHttpStream
                val pureLine = line.trim()
                    .removePrefix("data:")
                    .removePrefix("```json")
                    .removePrefix(":")
                    .removeSuffix("```")
                    .trim()
                if (pureLine == "[DONE]" || pureLine == "null") {
                    deferredResult.complete(sb.removePrefix("```json").removeSuffix("```").trim().toString())
                    return@executeHttpStream
                } else if (pureLine.startsWith("OPENROUTER")) {
                    // do nothing
                } else if (pureLine.isNotBlank()) {
                    logi("TranslateServer", "received line: $pureLine")
                    val text = Gson().fromJson(pureLine, ChatResponse::class.java).choices.firstOrNull()?.delta?.content
                    sb.append(text)
                    logd("TranslateServer", "text: $sb")
                }
            }
        }

        job.invokeOnCompletion { throwable ->
            if (throwable != null) {
                deferredResult.completeExceptionally(throwable)
            }
        }

        return deferredResult.await()
    }

    private fun url() = if (command.server is EnvConfig.Azure) {
        Env.get().azure.url
    } else Env.get().openai.url

    private fun body(): String {
        val body = mutableMapOf(
            "messages" to listOf(
                mapOf("role" to "system", "content" to Env.get().prompt),
                mapOf("role" to "user", "content" to "Please translate into ${language.languageName()}:\n$text"),
            ),
            "stream" to true
        )

        if (command.server.isOpenAi()) {
            body["model"] = Env.get().openai.model
        }

        return Gson().toJson(body)
    }
}


private data class ChatResponse(
//    @SerializedName("id") val id: String,
//    @SerializedName("object") val `object`: String,
//    @SerializedName("created") val created: Long,
//    @SerializedName("model") val model: String,
    @SerializedName("choices") val choices: List<Choice>,
    // @SerializedName("system_fingerprint") val systemFingerprint: String
) {
    data class Delta(
        @SerializedName("content") val content: String?
    )

    data class Choice(
        // @SerializedName("index") val index: Int,
        @SerializedName("delta") val delta: Delta,
        // @SerializedName("finish_reason") val finishReason: String?
    )
}