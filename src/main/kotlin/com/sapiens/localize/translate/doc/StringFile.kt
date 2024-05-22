package com.sapiens.localize.translate.doc

import com.sapiens.localize.translate.env.StringItem
import java.io.File

fun File.dumpToTargetLanguageStrings(strings: List<StringItem>) {
    addTranslationsToXml(strings)
}

private fun File.addTranslationsToXml(translations: List<StringItem>) {
    val content = if (this.exists()) {
        this.readText()
    } else {
        """<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools">
</resources>
"""
    }

    // Find the position to insert new strings
    val insertPosition = content.lastIndexOf("</resources>")
    val newContent = StringBuilder(content.substring(0, insertPosition).trim())

    // Insert each new translation before the closing </resources> tag
    for (translation in translations) {
        val text = translation.text
        val formatted = if(text.contains("'") && !(text.startsWith("\"") && text.endsWith("\""))) {
            "\"$text\""
        } else text
        val stringElement = """<string name="${translation.name}">${formatted}</string>"""
        newContent.append("\n    $stringElement")
    }

    newContent.append("\n</resources>")

    // Write the modified content back to the file
    this.writeText(newContent.toString())
}