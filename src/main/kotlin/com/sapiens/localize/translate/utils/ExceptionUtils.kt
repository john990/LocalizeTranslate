package com.sapiens.localize.translate.utils

import java.io.InterruptedIOException
import java.net.SocketException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

// 请求暴力色情的内容
class PolicyViolationRequestException : RuntimeException()

class OutOfQuotaException : RuntimeException()

fun Throwable?.isNetworkException(): Boolean {
    if (this == null) return false
    return this is SocketException
            || this is SSLHandshakeException
            || this is InterruptedIOException
            || this is UnknownHostException
}