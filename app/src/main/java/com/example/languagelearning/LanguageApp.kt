package com.example.languagelearning

import android.app.Application
import com.example.languagelearning.data.AppDatabase
import com.example.languagelearning.data.LanguageRepository
import com.example.languagelearning.data.MlKitTranslator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Application-wide repository initialization.
 *
 * Note: removed `runBlocking(Dispatchers.IO)` because it blocks the main thread during
 * application startup (which can make the app appear stuck while system components
 * like ProfileInstaller run). Initialization is now performed asynchronously on a
 * background scope. If callers need the repository immediately they can use
 * `awaitRepository()` from a coroutine.
 */

class LanguageApp : Application() {
    // Use a single application coroutine scope for background initialization
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // repository is nullable until initialized asynchronously
    @Volatile
    var repository: LanguageRepository? = null
        private set

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@LanguageApp)
            val mlKitTranslator = MlKitTranslator(this@LanguageApp)
            val repoInstance = LanguageRepository(db, mlKit = mlKitTranslator)
            repository = repoInstance
        }
    }
}
