package com.aetnox.cooking.ui.util

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

object AppLogger {
    private const val MAIN_TAG = "CookingApp"
    private const val MAX_LOG_HISTORY = 500

    private val logHistory = ConcurrentLinkedQueue<String>()
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    fun d(subTag: String, message: String) {
        val formatted = formatEntry("DEBUG", subTag, message)
        Log.d("$MAIN_TAG:$subTag", message)
        record(formatted)
    }

    fun i(subTag: String, message: String) {
        val formatted = formatEntry("INFO", subTag, message)
        Log.i("$MAIN_TAG:$subTag", message)
        record(formatted)
    }

    fun w(subTag: String, message: String, throwable: Throwable? = null) {
        val formatted = formatEntry("WARN", subTag, if (throwable != null) "$message | Exception: ${throwable.localizedMessage}" else message)
        Log.w("$MAIN_TAG:$subTag", message, throwable)
        record(formatted)
    }

    fun e(subTag: String, message: String, throwable: Throwable? = null) {
        val formatted = formatEntry("ERROR", subTag, if (throwable != null) "$message | Exception: ${throwable.localizedMessage}" else message)
        Log.e("$MAIN_TAG:$subTag", message, throwable)
        record(formatted)
    }

    private fun formatEntry(level: String, subTag: String, message: String): String {
        val time = dateFormat.format(Date())
        return "[$time] [$level] [$subTag] $message"
    }

    private fun record(entry: String) {
        logHistory.add(entry)
        while (logHistory.size > MAX_LOG_HISTORY) {
            logHistory.poll()
        }
    }

    fun getLogHistory(): List<String> {
        return logHistory.toList()
    }

    fun clearHistory() {
        logHistory.clear()
    }
}
