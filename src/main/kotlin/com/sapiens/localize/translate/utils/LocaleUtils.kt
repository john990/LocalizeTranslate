package com.sapiens.localize.translate.utils

import java.util.Locale

fun Locale.isSimpleChinese(): Boolean = language == Locale.CHINA.language && country == Locale.CHINA.country

fun Locale.isTraditionalChinese(): Boolean = !isSimpleChinese() && language == Locale.TRADITIONAL_CHINESE.language

fun Locale.isChinese(): Boolean = Locale.CHINESE.language == language

fun Locale.isSameLanguage(other: Locale): Boolean {
    val otherLanguage = other.language
    val otherCountry = other.country

    // 检查语言代码是否相同
    if (language != otherLanguage) {
        return false
    }

    // 对于中文，特别处理香港和台湾的繁体中文
    if (language == Locale.CHINESE.language) {
        // 简体中文 (中国大陆)
        if (country == Locale.CHINA.country || otherCountry == Locale.CHINA.country) {
            return country == otherCountry
        }
        // 其他所有 "zh" 语言代码认为是繁体中文，包括香港和台湾
        return true
    }

    // 其他语言，语言代码相同即可
    return true
}

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