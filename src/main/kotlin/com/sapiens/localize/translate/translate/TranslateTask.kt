package com.sapiens.localize.translate.translate

import com.sapiens.localize.translate.env.ConvertedStrings
import com.sapiens.localize.translate.utils.Command
import com.sapiens.localize.translate.utils.logd
import com.sapiens.localize.translate.utils.logi
import com.sapiens.localize.translate.utils.safeRun

class TranslateTask(
    private val command: Command,
    private val strings: ConvertedStrings
) {

    fun execute() {
        command.targetLanguages.forEach {
            logi("TranslateTask", "start translate $it")
            safeRun { translateJob(it, command, strings) }
        }
    }
}