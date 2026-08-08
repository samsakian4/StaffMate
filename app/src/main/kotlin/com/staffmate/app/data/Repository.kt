package com.staffmate.app.data

import kotlinx.coroutines.flow.Flow

class Repository(private val db: StaffMateDatabase) {
    val employees get() = db.employeeDao()
    val positives get() = db.positiveNoteDao()
    val negatives get() = db.negativeNoteDao()
    val personalities get() = db.personalityNoteDao()
    val disciplinary get() = db.disciplinaryDao()
    val settings get() = db.settingsDao()

    suspend fun getSetting(key: String, default: String = ""): String =
        settings.get(key)?.value ?: default

    suspend fun setSetting(key: String, value: String) =
        settings.set(SettingEntity(key, value))

    fun observeSettings(): Flow<List<SettingEntity>> = settings.observeAll()

    suspend fun calculateScore(employeeId: Long, sinceMillis: Long = 0L): Int {
        val wPos = getSetting("weight_positive", "1").toIntOrNull() ?: 1
        val wNeg = getSetting("weight_negative", "-1").toIntOrNull() ?: -1
        val sevLow = getSetting("severity_low", "-1").toIntOrNull() ?: -1
        val sevMed = getSetting("severity_medium", "-3").toIntOrNull() ?: -3
        val sevHigh = getSetting("severity_high", "-5").toIntOrNull() ?: -5
        val sevVeryHigh = getSetting("severity_very_high", "-8").toIntOrNull() ?: -8

        val posCount = positives.countSince(employeeId, sinceMillis)
        val negCount = negatives.countSince(employeeId, sinceMillis)
        val discRecords = disciplinary.getSince(employeeId, sinceMillis)

        var score = posCount * wPos + negCount * wNeg
        for (record in discRecords) {
            score += when (record.severity) {
                "کم" -> sevLow
                "متوسط" -> sevMed
                "زیاد" -> sevHigh
                "بسیار زیاد" -> sevVeryHigh
                else -> 0
            }
        }
        return score
    }

    suspend fun totalRecordCount(employeeId: Long): Int {
        val since = 0L
        return positives.countSince(employeeId, since) +
            negatives.countSince(employeeId, since) +
            disciplinary.getSince(employeeId, since).size
    }
}
