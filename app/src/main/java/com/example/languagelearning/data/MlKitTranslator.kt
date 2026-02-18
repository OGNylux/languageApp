package com.example.languagelearning.data

import android.content.Context
import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

class MlKitTranslator() {
    private var translator: Translator? = null
    private var currentSource: String? = null
    private var currentTarget: String? = null

    suspend fun prepareTranslator(sourceLang: String, targetLang: String, requireWifi: Boolean = true): Boolean {
        try {
            translator?.close()
        } catch (_: Exception) { }

        val sourceCode = mapLanguageCode(sourceLang)
        val targetCode = mapLanguageCode(targetLang)

        Log.d("MlKitTranslator", "prepareTranslator called with sourceLang='$sourceLang' targetLang='$targetLang' -> resolved $sourceCode -> $targetCode")

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceCode)
            .setTargetLanguage(targetCode)
            .build()
        translator = Translation.getClient(options)
        currentSource = sourceCode
        currentTarget = targetCode

        val conditionsBuilder = DownloadConditions.Builder()
        if (requireWifi) conditionsBuilder.requireWifi()
        val conditions = conditionsBuilder.build()

        return try {
            translator!!.downloadModelIfNeeded(conditions).await()
            Log.d("MlKitTranslator", "Model downloaded for $sourceCode -> $targetCode")
            true
        } catch (e: Exception) {
            Log.w("MlKitTranslator", "Model download failed: ${e.message}")
            false
        }
    }

    suspend fun detectLanguage(text: String): String? {
        return try {
            val id = LanguageIdentification.getClient()
            val tag = id.identifyLanguage(text).await()
            Log.d("MlKitTranslator", "detectLanguage: text='$text' detected='$tag'")
            if (tag == "und") null else tag
        } catch (e: Exception) {
            Log.w("MlKitTranslator", "detectLanguage failed: ${e.message}")
            null
        }
    }

    private fun mapLanguageCode(langCode: String): String {
        val code = langCode.lowercase()
        try {
            TranslateLanguage.fromLanguageTag(code)?.let { return it }
        } catch (_: Exception) { }

        return when (code) {
            "auto" -> TranslateLanguage.GERMAN
            "en" -> TranslateLanguage.ENGLISH
            "de" -> TranslateLanguage.GERMAN
            "es" -> TranslateLanguage.SPANISH
            "fr" -> TranslateLanguage.FRENCH
            "it" -> TranslateLanguage.ITALIAN
            "pt" -> TranslateLanguage.PORTUGUESE
            "ru" -> TranslateLanguage.RUSSIAN
            "ja" -> TranslateLanguage.JAPANESE
            "ko" -> TranslateLanguage.KOREAN
            "zh" -> TranslateLanguage.CHINESE
            "ar" -> TranslateLanguage.ARABIC
            "nl" -> TranslateLanguage.DUTCH
            "pl" -> TranslateLanguage.POLISH
            "tr" -> TranslateLanguage.TURKISH
            "sv" -> TranslateLanguage.SWEDISH
            "da" -> TranslateLanguage.DANISH
            "fi" -> TranslateLanguage.FINNISH
            "no" -> TranslateLanguage.NORWEGIAN
            "cs" -> TranslateLanguage.CZECH
            "sk" -> TranslateLanguage.SLOVAK
            "bg" -> TranslateLanguage.BULGARIAN
            "ro" -> TranslateLanguage.ROMANIAN
            "hu" -> TranslateLanguage.HUNGARIAN
            "uk" -> TranslateLanguage.UKRAINIAN
            "el" -> TranslateLanguage.GREEK
            "he" -> TranslateLanguage.HEBREW
            "hi" -> TranslateLanguage.HINDI
            "th" -> TranslateLanguage.THAI
            "vi" -> TranslateLanguage.VIETNAMESE
            else -> {
                Log.w("MlKitTranslator", "Unknown language code: $langCode, defaulting to ENGLISH")
                TranslateLanguage.ENGLISH
            }
        }
    }

    suspend fun translate(text: String): String {
        val t = translator ?: throw IllegalStateException("Translator not prepared")
        Log.d("MlKitTranslator", "translate called with text='$text' using $currentSource -> $currentTarget")
        return try {
            val out = t.translate(text).await()
            Log.d("MlKitTranslator", "translate returned='$out' (len=${out?.length ?: 0}) for input='$text'")
            if (out.equals(text, ignoreCase = true)) {
                Log.w("MlKitTranslator", "Translated text equals input — possible model/language mismatch or missing model")
            }
            out
        } catch (e: Exception) {
            Log.e("MlKitTranslator", "translate failed: ${e.message}")
            throw e
        }
    }
}
