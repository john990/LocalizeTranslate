package com.sapiens.localize.translate.translate

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sapiens.localize.translate.doc.dumpToTargetLanguageStrings
import com.sapiens.localize.translate.env.ConvertedStrings
import com.sapiens.localize.translate.env.EnvConfig
import com.sapiens.localize.translate.env.StringItem
import com.sapiens.localize.translate.env.xmlToStrings
import com.sapiens.localize.translate.server.TranslateServer
import com.sapiens.localize.translate.utils.*
import kotlinx.coroutines.runBlocking
import java.io.File

fun translateJob(
    language: String,
    command: Command,
    strings: ConvertedStrings
) {
    val targetStrings = extractTargetLanguageStrings(language, command, strings)

    if (targetStrings.isEmpty()) {
        logi("TranslateJob", "No text needs to be translated.")
        return
    }

    val json = Gson().toJson(targetStrings)
    val result = runBlocking { TranslateServer(language, command, json).translate() }

    logd("TranslateJob", "result:$result")

    val translatedStrings =
        Gson().fromJson<List<StringItem>>(result, object : TypeToken<List<StringItem>>() {}.type) ?: emptyList()

    if (translatedStrings.isEmpty()) {
        logi("TranslateJob", "translated strings is empty.")
    } else {
        logi("TranslateJob", "translated strings: $translatedStrings")
        language.languageFile(command.file).dumpToTargetLanguageStrings(translatedStrings)
    }

    logi("TranslateJob", "translate $language finish.")
}

private fun extractTargetLanguageStrings(
    language: String,
    command: Command,
    strings: ConvertedStrings
): List<StringItem> {
    val file = language.prepareFile(command.file)
    val targetStrings = if (command.executionMode == ExecutionMode.RENEW) {
        strings.items
    } else file.xmlToStrings().diffWithSource(strings.items)
    logi("TranslateJob", "start translate $language: ${targetStrings.size} lines")

    return targetStrings
}

private fun String.prepareFile(sourceFile: File): File {
    val targetFile = this.languageFile(sourceFile)

    logd("TranslateJob", "targetFile:${targetFile.path}")
    if (!targetFile.parentFile.exists()) {
        targetFile.parentFile.mkdirs()
    }

    if (!targetFile.exists()) {
        targetFile.createNewFile()

        targetFile.writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <resources xmlns:tools="http://schemas.android.com/tools">
            </resources>
        """.trimIndent()
        )
    }

    return targetFile
}

fun String.languageFile(sourceFile: File): File {
    return File(sourceFile.parentFile.parent + File.separator + "values-$this", sourceFile.name)
}