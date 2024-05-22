package com.sapiens.localize.translate.utils

import java.text.SimpleDateFormat
import java.util.*

private const val LEVEL_DEBUG = 1
private const val LEVEL_INFO = 2

fun logd(tag: String? = "", message: Any) {
    log(tag, message, LEVEL_DEBUG)
}

fun logi(tag: String? = "", message: Any) {
    log(tag, message, LEVEL_INFO)
}

private fun log(tag: String? = "", message: Any, level: Int) {
    if (level == LEVEL_DEBUG && !isDebug) {
        return
    }

    val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date())

    val name = if (tag.isNullOrBlank()) "" else "[$tag]"
    println("$time $name: $message")
}