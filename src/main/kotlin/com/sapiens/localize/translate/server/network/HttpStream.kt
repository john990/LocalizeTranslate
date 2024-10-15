package com.sapiens.localize.translate.server.network

import com.sapiens.localize.translate.server.network.interceptor.HeaderInterceptor
import com.sapiens.localize.translate.utils.PolicyViolationRequestException
import com.sapiens.localize.translate.utils.gson
import com.sapiens.localize.translate.utils.logi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit

private const val TAG = "HttpStream"


suspend fun executeHttpStream(url: String, data: Any? = null, callback: suspend (String?) -> Unit) {
    logi(TAG, "executeHttpStream:$url")
    executeHttpStreamInternal(url, data, callback)
}

private suspend fun executeHttpStreamInternal(url: String, data: Any? = null, callback: suspend (String?) -> Unit) {
    val response = call(url, data)

    if (!response.isSuccessful) {
        logi(TAG, response.toString())
        if (response.code == 400) {
            throw PolicyViolationRequestException()
        }
        callback.invoke(null)
        return
    }

    val respBody = response.body
    if (respBody == null) {
        callback.invoke(null)
        return
    }

    val source = respBody.source()
    while (!source.exhausted()) {
        val str = source.readUtf8LineStrict()
        callback.invoke(str)
    }
    callback.invoke(null)
}

@Suppress("SameParameterValue")
private fun call(url: String, data: Any? = null): Response {
    val client = okHttpClient()
    val body =
        if (data == null) data else (if (data is String) data else gson.toJson(data))

    logi(TAG, "executeHttpStream:$body")

    val request = Request.Builder().url(url)
        .post(body.orEmpty().toRequestBody("application/json; charset=utf-8".toMediaType()))
        .build()
    return client.newCall(request).execute()
}

private fun okHttpClient(): OkHttpClient {
    val client = OkHttpClient.Builder().apply {

        callTimeout(300, TimeUnit.SECONDS)
        connectTimeout(300, TimeUnit.SECONDS)
        readTimeout(300, TimeUnit.SECONDS)
        writeTimeout(300, TimeUnit.SECONDS)

        addInterceptor(HeaderInterceptor())
    }.build()
    return client
}
