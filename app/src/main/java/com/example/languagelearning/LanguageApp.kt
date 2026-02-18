package com.example.languagelearning

import android.app.Application
import com.example.languagelearning.data.AppDatabase
import com.example.languagelearning.data.LanguageRepository
import com.example.languagelearning.data.MlKitTranslator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class LanguageApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    var repository: LanguageRepository? = null
        private set

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@LanguageApp)
            val mlKitTranslator = MlKitTranslator()
            val repoInstance = LanguageRepository(db, mlKit = mlKitTranslator)
            repository = repoInstance
        }
    }
}
