package com.sapiens.localize.translate.utils

import com.google.gson.GsonBuilder
import java.io.File
import java.nio.file.Paths


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

fun gson() = GsonBuilder().disableHtmlEscaping().create()

private fun jarPath(): String {
/// 获取当前的 class path
    val classPath = System.getProperty("java.class.path")
    // 获取运行的 JAR 文件的路径
    val jarPath = classPath.split(File.pathSeparator).firstOrNull { it.endsWith(".jar") }
    // 如果找不到 JAR 文件路径，则抛出异常
    if (jarPath == null) {
        throw RuntimeException("Can't find jar path")
    }
    // 返回 JAR 文件的规范化目录
    return File(jarPath).parentFile.canonicalPath
}