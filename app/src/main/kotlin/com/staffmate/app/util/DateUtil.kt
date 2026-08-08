package com.staffmate.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateUtil {
    private val fmt = SimpleDateFormat("yyyy/MM/dd", Locale.US)

    fun format(millis: Long): String = fmt.format(millis)

    fun today(): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun daysAgo(days: Int): Long {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, -days)
        return c.timeInMillis
    }

    /** Parses yyyy/MM/dd, returns today's date on failure. */
    fun parse(text: String): Long = try {
        fmt.parse(text)?.time ?: today()
    } catch (e: Exception) {
        today()
    }
}
