package com.sapiens.localize.translate.utils

import java.util.Locale

fun Locale.isSimpleChinese(): Boolean = language == Locale.CHINA.language && country == Locale.CHINA.country

fun Locale.isChinese(): Boolean = Locale.CHINESE.language == language

fun String.languageName(): String {
    return Locale.forLanguageTag(this.replace("-r", "-")).localeLanguageName(isNativeLanguage = false)
}

fun Locale.localeLanguageName(isNativeLanguage: Boolean = false): String {
    return if (isNativeLanguage) {
        if (isChinese()) {
            return this.chineseLocaleName()
        }
        getDisplayLanguage(Locale(language, country))
    } else {
        if (isChinese()) {
            if(country.lowercase() == "cn") "Simplified Chinese" else "Traditional Chinese"
        } else getDisplayLanguage(Locale.US)
    }
}

private fun Locale.chineseLocaleName(): String {
    return if (isSimpleChinese()) {
        "简体中文"
    } else "繁體中文"
}