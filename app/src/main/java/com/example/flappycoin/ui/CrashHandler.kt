package com.example.flappycoin.utils

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object CrashHandler : Thread.UncaughtExceptionHandler {
    private val TAG = "CrashHandler"
    private lateinit var context: Context
    private val crashLogDir by lazy {
        File(context.getExternalFilesDir(null), "crash_logs").apply {
            if (!exists()) mkdirs()
        }
    }

    fun init(ctx: Context) {
        context = ctx
        Thread.setDefaultUncaughtExceptionHandler(this)
        Log.d(TAG, "CrashHandler initialized")
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            Log.e(TAG, "UNCAUGHT EXCEPTION DETECTED!", throwable)
            
            // 🔴 Afficher le crash sur l'écran
            val errorMessage = getCrashMessage(throwable)
            showCrashDialog(errorMessage, throwable)
            
            // 💾 Sauvegarder le crash dans un fichier
            saveCrashLog(throwable)
            
            // ⏱️ Attendre un peu puis fermer l'app
            Thread.sleep(3000)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in crash handler: ${e.message}", e)
        }
        
        // Terminer l'application
        System.exit(1)
    }

    private fun getCrashMessage(throwable: Throwable): String {
        val cause = throwable.cause?.let { "\nCause: ${it.javaClass.simpleName}: ${it.message}" } ?: ""
        val stackTrace = throwable.stackTrace.take(5).joinToString("\n") { 
            "  at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})"
        }
        
        return """
            ╔════════════════════════════════════╗
            ║     🔴 APPLICATION CRASH 🔴        ║
            ╚════════════════════════════════════╝
            
            Exception: ${throwable.javaClass.simpleName}
            Message: ${throwable.message ?: "No message"}
            $cause
            
            Stack Trace:
            $stackTrace
            
            Device: ${Build.MANUFACTURER} ${Build.MODEL}
            Android: ${Build.VERSION.RELEASE}
            App: FlappyCoin v1.0
        """.trimIndent()
    }

    private fun showCrashDialog(message: String, throwable: Throwable) {
        try {
            // Afficher le crash dans les logs Android
            Log.e(TAG, message)
            Log.e(TAG, "Full stacktrace:", throwable)
            
            // Afficher un Toast
            val shortMessage = "CRASH: ${throwable.javaClass.simpleName}\n${throwable.message}"
            Toast.makeText(context, shortMessage, Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing crash dialog: ${e.message}")
        }
    }

    private fun saveCrashLog(throwable: Throwable) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val filename = "crash_${System.currentTimeMillis()}.log"
            val file = File(crashLogDir, filename)
            
            BufferedWriter(FileWriter(file)).use { writer ->
                writer.write("=== CRASH LOG ===\n")
                writer.write("Timestamp: $timestamp\n")
                writer.write("Device: ${Build.MANUFACTURER} ${Build.MODEL}\n")
                writer.write("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})\n")
                writer.write("Exception: ${throwable.javaClass.name}\n")
                writer.write("Message: ${throwable.message}\n\n")
                
                writer.write("Stack Trace:\n")
                writer.write(throwable.stackTraceToString())
                writer.write("\n\nCause:\n")
                throwable.cause?.let {
                    writer.write("${it.javaClass.name}: ${it.message}\n")
                    writer.write(it.stackTraceToString())
                }
                
                writer.write("\n\n=== FULL LOGCAT ===\n")
                try {
                    val process = Runtime.getRuntime().exec("logcat -d")
                    val reader = process.inputStream.bufferedReader()
                    reader.useLines { lines ->
                        lines.take(200).forEach { writer.write(it + "\n") }
                    }
                } catch (e: Exception) {
                    writer.write("Could not capture logcat: ${e.message}\n")
                }
            }
            
            Log.d(TAG, "Crash log saved to: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving crash log: ${e.message}", e)
        }
    }
}