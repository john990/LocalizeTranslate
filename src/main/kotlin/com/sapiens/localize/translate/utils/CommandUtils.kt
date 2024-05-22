package com.sapiens.localize.translate.utils

import com.sapiens.localize.translate.env.Env
import com.sapiens.localize.translate.env.EnvConfig
import java.io.File


data class Command(
    // strings.xml 文件路径
    val file: File,
    // -server 使用哪个翻译服务
    val server: EnvConfig.Server,
    // -exeMode 执行方式（增量还是全量）
    val executionMode: ExecutionMode,
    // -lang 目标语言
    val targetLanguages: List<String>,
)

enum class ExecutionMode(name: String) {
    INCREMENTAL("incremental"), // 增量翻译，只翻译没有翻译过的行
    RENEW("renew"), // 全量翻译，所有语言所有行全部翻译
}

fun Array<String>.toCommand(): Command {
    if (this.isEmpty()) throw IllegalArgumentException("commands cannot be empty")

    val filePath = this[0]
    if (filePath.isBlank() || !filePath.endsWith(".xml")) throw IllegalArgumentException("Please select strings.xml")

    val serverParam = firstOrNull { it.startsWith("-server") }?.removePrefix("-server=") ?: "openai"
    val server = if (serverParam == "azure") {
        Env.get().azure
    } else Env.get().openai

    val executionModeParam = firstOrNull { it.startsWith("-exeMode") }?.removePrefix("-exeMode=") ?: "incremental"
    val executionMode = if (executionModeParam == "renew") ExecutionMode.RENEW else ExecutionMode.INCREMENTAL

    val targetLanguagesParam = firstOrNull { it.startsWith("-lang") }?.removePrefix("-lang=") ?: ""
    if (targetLanguagesParam.isBlank()) throw IllegalArgumentException("language(-lang) cannot be empty")
    val targetLanguages = targetLanguagesParam.split(",").map { it.trim() }

    return Command(
        file = File(filePath),
        server = server,
        executionMode = executionMode,
        targetLanguages = targetLanguages,
    )
}