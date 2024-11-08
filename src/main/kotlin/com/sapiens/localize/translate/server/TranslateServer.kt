package com.sapiens.localize.translate.server

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.sapiens.localize.translate.env.Env
import com.sapiens.localize.translate.env.EnvConfig
import com.sapiens.localize.translate.env.isOpenAi
import com.sapiens.localize.translate.parser.XmlParser
import com.sapiens.localize.translate.server.network.executeHttpStream
import com.sapiens.localize.translate.translate.languageFile
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
        // 获取目标语言的文件路径
        val targetFile = language.languageFile(command.file)

        // 读取已存在的翻译内容
        val existingTranslations = if (targetFile.exists()) {
            XmlParser.parse(targetFile.path)
                .joinToString("\n") { "${it.first}: ${it.second}" }
        } else ""

        // 构建请求体
        val body = mutableMapOf(
            "messages" to listOf(
                // 系统提示，定义 AI 的角色和任务
                mapOf("role" to "system", "content" to Env.get().prompt),
                // 用户消息，包含已有翻译和待翻译文本
                mapOf(
                    "role" to "user",
                    "content" to """
                        // Provide existing translations as reference
                        These are some existing translations in ${language.languageName()}:
                        $existingTranslations
                        
                        // Request to translate new text
                        Please translate following text into ${language.languageName()}, 
                        and keep consistent with existing translations:
                        $text
                    """.trimIndent()
                ),
            ),
            // 启用流式响应
            "stream" to true
        )

        // 如果是 OpenAI，添加模型参数
        if (command.server.isOpenAi()) {
            body["model"] = Env.get().openai.model
        }

        // 将 Map 转换为 JSON 字符串
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