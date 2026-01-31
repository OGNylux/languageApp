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

class MlKitTranslator(private val context: Context) {
    private var translator: Translator? = null
    private var currentSource: String? = null
    private var currentTarget: String? = null

    suspend fun prepareTranslator(sourceLang: String, targetLang: String, requireWifi: Boolean = true): Boolean {
        try {
            translator?.close()
        } catch (_: Exception) { }

        val sourceCode = when (sourceLang) {
            "auto" -> TranslateLanguage.GERMAN
            else -> TranslateLanguage.fromLanguageTag(sourceLang) ?: sourceLang
        }
        val targetCode = TranslateLanguage.fromLanguageTag(targetLang) ?: targetLang

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
