package com.sapiens.localize.translate.utils

import com.google.gson.GsonBuilder
import java.io.File


fun safeRun(printLog: Boolean = true, block: () -> Unit) {
    try {
        block()
    } catch (e: Throwable) {
        if (printLog) {
            e.printStackTrace()
        }
    }
}

val isDebug: Boolean by lazy { !ClassLoader.getSystemResource("MainKt.class").toString().contains(".jar") }

fun getProjectDir(): String = if (isDebug) System.getProperty("user.dir") else jarPath()

private fun jarPath(): String {
/// 获取当前的 class path
    val jarFilePath = object {}.javaClass.protectionDomain.codeSource.location.toURI().path
    val jarFile = File(jarFilePath)

    // 获取父目录路径
    val parentFile = jarFile.parentFile ?: throw RuntimeException("Can't find parent directory for jar file")

    return parentFile.canonicalPath
}

val gson = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()