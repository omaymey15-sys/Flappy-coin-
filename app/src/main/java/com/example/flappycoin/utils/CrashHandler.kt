package com.example.flappycoin.utils

import android.app.Application
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Gère les crashes globaux de l'application
 * Capture et enregistre toutes les exceptions non gérées
 */
object CrashHandler {
    
    private const val TAG = "CrashHandler"
    private var isInitialized = false
    
    /**
     * Initialise le gestionnaire de crash global
     */
    fun setupGlobalCrashHandler(app: Application) {
        if (isInitialized) return
        
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val separator = "=".repeat(80)
                
                Log.e(TAG, separator)
                Log.e(TAG, "💥 CRASH GLOBAL DÉTECTÉ 💥")
                Log.e(TAG, separator)
                Log.e(TAG, "Thread: ${thread.name}")
                Log.e(TAG, "Exception: ${throwable::class.simpleName}")
                Log.e(TAG, "Message: ${throwable.message}")
                Log.e(TAG, "Cause: ${throwable.cause}")
                Log.e(TAG, "-".repeat(80))
                Log.e(TAG, throwable.stackTraceToString())
                Log.e(TAG, separator)
                
                // Sauvegarde le crash
                saveCrashLog(app, throwable)
                
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors du traitement du crash", e)
            }
            
            // Appelle le handler par défaut
            defaultHandler?.uncaughtException(thread, throwable)
        }
        
        isInitialized = true
        Log.d(TAG, "✅ Global Crash Handler initialisé")
    }
    
    /**
     * Sauvegarde les logs de crash dans un fichier
     */
    private fun saveCrashLog(app: Application, throwable: Throwable) {
        try {
            val logDir = File(app.getExternalFilesDir(null), "crash_logs")
            if (!logDir.exists()) {
                logDir.mkdirs()
                Log.d(TAG, "📁 Répertoire crash créé: ${logDir.absolutePath}")
            }
            
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
            val logFile = File(logDir, "crash_$timestamp.txt")
            
            FileWriter(logFile).use { writer ->
                val separator = "=".repeat(80)
                val dash = "-".repeat(80)
                
                writer.write("$separator\n")
                writer.write("CRASH FLAPPYCOIN\n")
                writer.write("$separator\n")
                writer.write("Timestamp: $timestamp\n")
                writer.write("Device: ${android.os.Build.DEVICE}\n")
                writer.write("Model: ${android.os.Build.MODEL}\n")
                writer.write("Android Version: ${android.os.Build.VERSION.SDK_INT}\n\n")
                
                writer.write("Exception Type: ${throwable::class.simpleName}\n")
                writer.write("Message: ${throwable.message}\n")
                writer.write("Cause: ${throwable.cause}\n\n")
                
                writer.write("Stack Trace:\n")
                writer.write("$dash\n")
                writer.write(throwable.stackTraceToString())
                writer.write("\n$separator\n")
            }
            
            Log.d(TAG, "✅ Crash log sauvegardé: ${logFile.absolutePath}")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur sauvegarde crash log", e)
        }
    }
}