package com.sapiens.localize.translate.env

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sapiens.localize.translate.parser.XmlParser
import com.sapiens.localize.translate.utils.alignWithSource
import com.sapiens.localize.translate.utils.getProjectDir
import com.sapiens.localize.translate.utils.gson
import com.sapiens.localize.translate.utils.logi
import java.io.File

data class ConvertedStrings(
    val items: List<StringItem>,
) {
    companion object {
        private val path = File(getProjectDir(), "strings_converted.json")

        fun read(sourceFile: File): ConvertedStrings {
            val file = path.apply {
                prepareFile(sourceFile)
                syncStringWithSource(sourceFile)
            }

            return ConvertedStrings(
                items = Gson().fromJson(
                    file.readText(),
                    object : TypeToken<List<StringItem>>() {}.type
                )
            )
        }
    }
}

data class StringItem(
    val name: String,
    val text: String,
    val prompt: String? = null,
)

private fun File.prepareFile(sourceFile: File) {
    if (!this.exists()) {
        this.createNewFile()
        logi("", "The file strings_converted.json does not exist, create a new one.")
        val strings = sourceFile.xmlToStrings()
        this.writeText(gson.toJson(strings))
    }
}

// 检查是否数据有变化
private fun File.syncStringWithSource(sourceFile: File) {
    val localStrings = toConvertedStrings().items.toMutableList()
    val remoteStrings = sourceFile.xmlToStrings()

    val newStrings = localStrings.alignWithSource(remoteStrings)

    if (!localStrings.areListsEqual(newStrings)) {
        logi("", "found diff, so update strings_converted.json")
        this.writeText(gson.toJson(newStrings))
    }
}

private fun File.toConvertedStrings(): ConvertedStrings {
    return ConvertedStrings(
        items = Gson().fromJson(
            this.readText(),
            object : TypeToken<List<StringItem>>() {}.type
        )
    )
}

fun File.xmlToStrings(): List<StringItem> {
    return kotlin.runCatching {
        XmlParser.parse(this.path).map {
            StringItem(
                name = it.first,
                text = it.second,
                prompt = ""
            )
        }
    }.getOrElse { emptyList() }
}

private fun List<StringItem>.areListsEqual(list2: List<StringItem>): Boolean {
    return this.size == list2.size && this.zip(list2).all { (item1, item2) ->
        item1.name == item2.name && item1.text == item2.text
    }
}