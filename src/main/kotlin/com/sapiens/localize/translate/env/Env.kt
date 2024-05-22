package com.sapiens.localize.translate.env

import com.sapiens.localize.translate.utils.Command
import com.sapiens.localize.translate.utils.getProjectDir
import com.sapiens.localize.translate.utils.logd
import com.sapiens.localize.translate.utils.logi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class EnvConfig(
    val openai: Openai,
    val azure: Azure,
    val prompt: String,
) {
    @Serializable
    data class Openai(
        val url: String,
        val key: String,
        val model: String
    ) : Server

    @Serializable
    data class Azure(
        val url: String,
        val key: String,
    ) : Server

    interface Server
}

fun EnvConfig.Server.isOpenAi() = this is EnvConfig.Openai

object Env {

    private val config by lazy { loadConfig(File(getProjectDir(), "env.json")) }

    private lateinit var command: Command

    init {
        logd("Env", "config:$config")
    }

    fun get(): EnvConfig = config

    fun command() = command

    fun setupCommand(command: Command) {
        this.command = command
    }
}

private fun loadConfig(file: File): EnvConfig {
    logd("loadConfig", "file:${file.absoluteFile}")
    if (!file.exists()) {
        logi("Env", "${file.absoluteFile} config file does not exist, auto create one.")
        file.writeText(template)
        throw RuntimeException("env.json not setup.")
    }
    val fileContent = file.readText()
    return Json.decodeFromString(EnvConfig.serializer(), fileContent)
}

private val template = """
    {
      "openai": {
        "url": "https://api.openai.com",
        "key": "sk-your-key",
        "model": "gpt-4o"
      },
      "azure": {
        "url": "https://{resource-url}/openai/deployments/{deploy-id}/chat/completions?api-version=2024-03-01-preview",
        "key": "your-key"
      },
      "prompt": "# Role\nYou are an Android and translation expert who not only masters a wide range of languages but also has a deep understanding of Android development.\n\n## Skills\n### Skill 1: Android Knowledge\n- You have a thorough understanding of key points in the Android development process.\n- The JSON I provide you contains string resources used in Android development.\n\n### Skill 2: Translation\n- You are proficient in multiple languages and can translate between them.\n\n### Skill 3: Generating JSON\n- Based on the translation results, you can generate JSON in the following format:\n```json\n[\n{\n\"name\": \"\",\n\"text\": \"\"\n}\n]\n```\n\n## Constraints\n- Only output the pure JSON text I need, and do not output any other information.\n- In the JSON I provide, the \"text\" field contains the content that needs to be translated.\n- The \"name\" field is a keyword and does not need to be translated.\n- If there is a \"prompt\" field, it is used to describe the context of use. When translating the corresponding \"text,\" please translate according to the context."
    }
""".trimIndent()