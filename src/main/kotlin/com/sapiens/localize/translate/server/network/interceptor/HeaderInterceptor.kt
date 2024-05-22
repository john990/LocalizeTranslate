package com.sapiens.localize.translate.server.network.interceptor

import com.sapiens.localize.translate.env.Env
import com.sapiens.localize.translate.env.EnvConfig
import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor(
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val builder = chain.request().newBuilder()
            .addHeader("Content-Type", "application/json")

        if (Env.command().server is EnvConfig.Azure) {
            builder.addHeader("api-key", Env.get().azure.key.trim())
        } else {
            builder.addHeader("Authorization", "Bearer ${Env.get().openai.key.trim()}")
        }

        return chain.proceed(builder.build())
    }
}