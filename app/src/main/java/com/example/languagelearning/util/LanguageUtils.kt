package com.example.languagelearning.util

import java.util.Locale

fun languageName(code: String): String {
    if (code.isBlank()) return code
    return try {
        val locale = Locale.forLanguageTag(code)
        val display = locale.getDisplayLanguage(Locale.ENGLISH)
        if (display.isNullOrBlank()) code else display.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
    } catch (_: Exception) {
        code
    }
}

