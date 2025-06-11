package com.sapiens.localize.translate.doc

import com.sapiens.localize.translate.env.StringItem
import com.sapiens.localize.translate.env.xmlToStrings
import com.sapiens.localize.translate.translate.languageFile
import com.sapiens.localize.translate.utils.alignWithSource
import com.sapiens.localize.translate.utils.getProjectDir
import com.sapiens.localize.translate.utils.gson
import com.sapiens.localize.translate.utils.logi
import java.io.File

fun reorganizeFiles(sourcePath: String) {
    logi("reorganize", "Starting to reorganize translation files...")

    val sourceFile = File(sourcePath)
    if (!sourceFile.exists() || !sourceFile.name.endsWith(".xml")) {
        logi("reorganize", "Source file does not exist or is not an XML file: $sourcePath")
        return
    }

    // Get the parent directory of the source file (usually the values directory)
    val resDir = sourceFile.parentFile.parentFile

    if (resDir == null || !resDir.exists()) {
        logi("reorganize", "Cannot find res directory")
        return
    }

    // Read strings from the source file
    val sourceStrings = sourceFile.xmlToStrings()

    logi("reorganize", "Source file contains ${sourceStrings.size} strings")

    // Find all translation directories (values-*)
    val translationDirs = resDir.listFiles()?.filter {
        it.isDirectory && it.name.startsWith("values-")
    } ?: emptyList()

    logi("reorganize", "Found ${translationDirs.size} translation directories")

    // Reorganize files in each translation directory
    translationDirs.forEach { dir ->
        val language = dir.name.removePrefix("values-")
        reorganizeLanguageFile(language, sourceFile, sourceStrings)
    }

    // Regenerate strings_converted.json
    regenerateConvertedStrings(sourceFile, sourceStrings)

    logi("reorganize", "Reorganization completed!")
}

private fun reorganizeLanguageFile(language: String, sourceFile: File, sourceStrings: List<StringItem>) {
    val languageFile = language.languageFile(sourceFile)

    if (!languageFile.exists()) {
        logi("reorganize", "Skipping non-existent translation file: ${languageFile.path}")
        return
    }

    logi("reorganize", "Processing translation file: ${languageFile.path}")

    try {
        // Read existing translation file
        val existingTranslations = languageFile.xmlToStrings()

        // Use existing alignWithSource logic to align and clean translations
        val alignedTranslations = existingTranslations.alignWithSource(sourceStrings)

        // Filter out empty translations (only keep items with actual translation content)
        val validTranslations = alignedTranslations.filter { translation ->
            // Check if there is actual translation content (not the original text)
            val sourceItem = sourceStrings.find { it.name == translation.name }
            sourceItem != null && translation.text.isNotBlank() && translation.text != sourceItem.text
        }

        val removedCount = existingTranslations.size - validTranslations.size
        if (removedCount > 0) {
            logi("reorganize", "Removed $removedCount invalid translation items from $language")
        }

        // Completely rewrite file content
        rewriteXmlFile(languageFile, validTranslations)

    } catch (e: Exception) {
        logi("reorganize", "Error processing translation file: ${languageFile.path}, error: ${e.message}")
    }
}

private fun rewriteXmlFile(file: File, translations: List<StringItem>) {
    val content = StringBuilder()
    content.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
    content.append("<resources xmlns:tools=\"http://schemas.android.com/tools\">\n")

    translations.forEach { translation ->
        val text = translation.text
        val formatted = if (text.contains("'") && !(text.startsWith("\"") && text.endsWith("\""))) {
            "\"$text\""
        } else text
        content.append("    <string name=\"${translation.name}\">$formatted</string>\n")
    }

    content.append("</resources>\n")

    file.writeText(content.toString())
}

private fun regenerateConvertedStrings(sourceFile: File, sourceStrings: List<StringItem>) {
    val convertedFile = File(getProjectDir(), "strings_converted.json")

    // If file already exists, try to preserve existing prompt information
    val existingPrompts = mutableMapOf<String, String>()

    if (convertedFile.exists()) {
        try {
            val existingStrings = gson.fromJson(
                convertedFile.readText(),
                Array<StringItem>::class.java
            ).toList()

            existingStrings.forEach { item ->
                if (!item.prompt.isNullOrBlank()) {
                    existingPrompts[item.name] = item.prompt
                }
            }
        } catch (e: Exception) {
            logi("reorganize", "Error reading existing converted file: ${e.message}")
        }
    }

    // Generate new StringItem list, preserving existing prompts
    val newStringItems = sourceStrings.map { sourceItem ->
        StringItem(
            name = sourceItem.name,
            text = sourceItem.text,
            prompt = existingPrompts[sourceItem.name] ?: ""
        )
    }

    // Write new file
    convertedFile.writeText(gson.toJson(newStringItems))

    logi("reorganize", "Regenerated strings_converted.json with ${newStringItems.size} string items")
}