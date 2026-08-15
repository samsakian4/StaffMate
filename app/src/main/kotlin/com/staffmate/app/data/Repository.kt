package com.staffmate.app.data

import com.staffmate.app.remote.SupabaseApi
import com.staffmate.app.remote.toDisciplinaryRecord
import com.staffmate.app.remote.toEmployee
import com.staffmate.app.remote.toInsertJson
import com.staffmate.app.remote.toJson
import com.staffmate.app.remote.toNegativeNote
import com.staffmate.app.remote.toPersonalityNote
import com.staffmate.app.remote.toPositiveNote
import com.staffmate.app.remote.toUpdateJson
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Online-first repository backed by Supabase (shared with the PWA — same
 * project, same tables, same RLS policies). Each collection is kept in an
 * in-memory cache refreshed after every mutation and on sign-in; screens
 * observe these caches the same way they previously observed Room Flows.
 */
class Repository(private val api: SupabaseApi) {

    private val employeeCache = MutableStateFlow<List<Employee>>(emptyList())
    private val positiveCache = MutableStateFlow<List<PositiveNote>>(emptyList())
    private val negativeCache = MutableStateFlow<List<NegativeNote>>(emptyList())
    private val personalityCache = MutableStateFlow<List<PersonalityNote>>(emptyList())
    private val disciplinaryCache = MutableStateFlow<List<DisciplinaryRecord>>(emptyList())
    private val settingsCache = MutableStateFlow<Map<String, String>>(emptyMap())

    suspend fun refreshAll() = coroutineScope {
        launch { refreshEmployees() }
        launch { refreshPositives() }
        launch { refreshNegatives() }
        launch { refreshPersonalities() }
        launch { refreshDisciplinary() }
        launch { refreshSettings() }
    }

    private suspend fun refreshEmployees() {
        employeeCache.value = api.select("employees").map { it.jsonObject.toEmployee() }
    }
    private suspend fun refreshPositives() {
        positiveCache.value = api.select("positive_notes").map { it.jsonObject.toPositiveNote() }
    }
    private suspend fun refreshNegatives() {
        negativeCache.value = api.select("negative_notes").map { it.jsonObject.toNegativeNote() }
    }
    private suspend fun refreshPersonalities() {
        personalityCache.value = api.select("personality_notes").map { it.jsonObject.toPersonalityNote() }
    }
    private suspend fun refreshDisciplinary() {
        disciplinaryCache.value = api.select("disciplinary_records").map { it.jsonObject.toDisciplinaryRecord() }
    }
    private suspend fun refreshSettings() {
        val rows = api.select("settings")
        settingsCache.value = rows.associate { row ->
            val obj = row.jsonObject
            (obj["key"]?.jsonPrimitive?.contentOrNull ?: "") to (obj["value"]?.jsonPrimitive?.contentOrNull ?: "")
        }
    }

    // ---------- Settings ----------
    suspend fun getSetting(key: String, default: String = ""): String =
        settingsCache.value[key] ?: default

    suspend fun setSetting(key: String, value: String) {
        val body = buildJsonObject { put("key", kotlinx.serialization.json.JsonPrimitive(key)); put("value", kotlinx.serialization.json.JsonPrimitive(value)) }
        api.upsert("settings", body, onConflict = "owner_id,key")
        refreshSettings()
    }

    suspend fun ensureDefaultSettings() {
        refreshSettings()
        for ((key, value) in DefaultSettings.values) {
            if (!settingsCache.value.containsKey(key)) setSetting(key, value)
        }
    }

    // ---------- Employees ----------
    val employees = EmployeeOps()
    inner class EmployeeOps {
        fun observeAll(): Flow<List<Employee>> = employeeCache
        fun observeActive(): Flow<List<Employee>> = employeeCache.map { it.filter { e -> e.active } }
        fun observeById(id: Long): Flow<Employee?> = employeeCache.map { it.find { e -> e.id == id } }
        fun observeActiveCount(): Flow<Int> = employeeCache.map { it.count { e -> e.active } }
        fun search(query: String, onlyActive: Int): Flow<List<Employee>> = employeeCache.map { list ->
            list.filter { e ->
                (onlyActive == 0 || e.active) &&
                    (query.isBlank() || e.firstName.contains(query) || e.lastName.contains(query) || e.personnelCode.contains(query))
            }.sortedBy { it.firstName + it.lastName }
        }
        suspend fun getById(id: Long): Employee? = employeeCache.value.find { e -> e.id == id }
        suspend fun insert(e: Employee): Long {
            val row = api.insert("employees", e.toInsertJson())
            refreshEmployees()
            return row.toEmployee().id
        }
        suspend fun update(e: Employee) {
            api.update("employees", "id", e.id.toString(), e.toUpdateJson())
            refreshEmployees()
        }
    }

    // ---------- Positive notes ----------
    val positives = PositiveOps()
    inner class PositiveOps {
        fun observeForEmployee(employeeId: Long): Flow<List<PositiveNote>> =
            positiveCache.map { list -> list.filter { it.employeeId == employeeId }.sortedByDescending { it.date } }
        fun observeTotalCount(): Flow<Int> = positiveCache.map { it.size }
        suspend fun getById(id: Long): PositiveNote? = positiveCache.value.find { it.id == id }
        suspend fun getAllForEmployee(employeeId: Long): List<PositiveNote> =
            positiveCache.value.filter { it.employeeId == employeeId }.sortedByDescending { it.date }
        suspend fun insert(n: PositiveNote): Long {
            val row = api.insert("positive_notes", n.toJson())
            refreshPositives()
            return row.toPositiveNote().id
        }
        suspend fun update(n: PositiveNote) {
            api.update("positive_notes", "id", n.id.toString(), n.toJson())
            refreshPositives()
        }
        suspend fun delete(n: PositiveNote) {
            api.delete("positive_notes", "id", n.id.toString())
            refreshPositives()
        }
    }

    // ---------- Negative notes ----------
    val negatives = NegativeOps()
    inner class NegativeOps {
        fun observeForEmployee(employeeId: Long): Flow<List<NegativeNote>> =
            negativeCache.map { list -> list.filter { it.employeeId == employeeId }.sortedByDescending { it.date } }
        fun observeTotalCount(): Flow<Int> = negativeCache.map { it.size }
        suspend fun getById(id: Long): NegativeNote? = negativeCache.value.find { it.id == id }
        suspend fun getAllForEmployee(employeeId: Long): List<NegativeNote> =
            negativeCache.value.filter { it.employeeId == employeeId }.sortedByDescending { it.date }
        suspend fun insert(n: NegativeNote): Long {
            val row = api.insert("negative_notes", n.toJson())
            refreshNegatives()
            return row.toNegativeNote().id
        }
        suspend fun update(n: NegativeNote) {
            api.update("negative_notes", "id", n.id.toString(), n.toJson())
            refreshNegatives()
        }
        suspend fun delete(n: NegativeNote) {
            api.delete("negative_notes", "id", n.id.toString())
            refreshNegatives()
        }
    }

    // ---------- Personality notes ----------
    val personalities = PersonalityOps()
    inner class PersonalityOps {
        fun observeForEmployee(employeeId: Long): Flow<List<PersonalityNote>> =
            personalityCache.map { list -> list.filter { it.employeeId == employeeId }.sortedByDescending { it.date } }
        suspend fun getById(id: Long): PersonalityNote? = personalityCache.value.find { it.id == id }
        suspend fun getAllForEmployee(employeeId: Long): List<PersonalityNote> =
            personalityCache.value.filter { it.employeeId == employeeId }.sortedByDescending { it.date }
        suspend fun insert(n: PersonalityNote): Long {
            val row = api.insert("personality_notes", n.toJson())
            refreshPersonalities()
            return row.toPersonalityNote().id
        }
        suspend fun update(n: PersonalityNote) {
            api.update("personality_notes", "id", n.id.toString(), n.toJson())
            refreshPersonalities()
        }
        suspend fun delete(n: PersonalityNote) {
            api.delete("personality_notes", "id", n.id.toString())
            refreshPersonalities()
        }
    }

    // ---------- Disciplinary records ----------
    val disciplinary = DisciplinaryOps()
    inner class DisciplinaryOps {
        fun observeForEmployee(employeeId: Long): Flow<List<DisciplinaryRecord>> =
            disciplinaryCache.map { list -> list.filter { it.employeeId == employeeId }.sortedByDescending { it.date } }
        fun observeTotalCount(): Flow<Int> = disciplinaryCache.map { it.size }
        suspend fun getById(id: Long): DisciplinaryRecord? = disciplinaryCache.value.find { it.id == id }
        suspend fun getSince(employeeId: Long, since: Long): List<DisciplinaryRecord> =
            disciplinaryCache.value.filter { it.employeeId == employeeId && it.date >= since }
        suspend fun insert(n: DisciplinaryRecord): Long {
            val row = api.insert("disciplinary_records", n.toJson())
            refreshDisciplinary()
            return row.toDisciplinaryRecord().id
        }
        suspend fun update(n: DisciplinaryRecord) {
            api.update("disciplinary_records", "id", n.id.toString(), n.toJson())
            refreshDisciplinary()
        }
        suspend fun delete(n: DisciplinaryRecord) {
            api.delete("disciplinary_records", "id", n.id.toString())
            refreshDisciplinary()
        }
    }

    // ---------- Scoring ----------
    suspend fun calculateScore(employeeId: Long, sinceMillis: Long = 0L): Int {
        val wPos = getSetting("weight_positive", "1").toIntOrNull() ?: 1
        val wNeg = getSetting("weight_negative", "-1").toIntOrNull() ?: -1
        val sevLow = getSetting("severity_low", "-1").toIntOrNull() ?: -1
        val sevMed = getSetting("severity_medium", "-3").toIntOrNull() ?: -3
        val sevHigh = getSetting("severity_high", "-5").toIntOrNull() ?: -5
        val sevVeryHigh = getSetting("severity_very_high", "-8").toIntOrNull() ?: -8

        val posCount = positiveCache.value.count { it.employeeId == employeeId && it.date >= sinceMillis }
        val negCount = negativeCache.value.count { it.employeeId == employeeId && it.date >= sinceMillis }
        val discRecords = disciplinaryCache.value.filter { it.employeeId == employeeId && it.date >= sinceMillis }

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
        return positiveCache.value.count { it.employeeId == employeeId } +
            negativeCache.value.count { it.employeeId == employeeId } +
            disciplinaryCache.value.count { it.employeeId == employeeId }
    }

    // ---------- Backup / Restore (JSON, same shape as the PWA export) ----------
    suspend fun exportAllData(): BackupData {
        refreshAll()
        return BackupData(
            employees = employeeCache.value,
            positiveNotes = positiveCache.value,
            negativeNotes = negativeCache.value,
            personalityNotes = personalityCache.value,
            disciplinaryRecords = disciplinaryCache.value,
            settings = settingsCache.value.map { SettingEntity(it.key, it.value) }
        )
    }

    suspend fun clearAllRemoteData() {
        api.deleteAll("positive_notes")
        api.deleteAll("negative_notes")
        api.deleteAll("personality_notes")
        api.deleteAll("disciplinary_records")
        api.deleteAll("employees")
        api.deleteAll("settings")
    }

    suspend fun importAllData(data: BackupData) {
        clearAllRemoteData()
        val idMap = HashMap<Long, Long>()
        for (emp in data.employees) {
            val newId = employees.insert(emp.copy(id = 0))
            idMap[emp.id] = newId
        }
        for (n in data.positiveNotes) {
            positives.insert(n.copy(id = 0, employeeId = idMap[n.employeeId] ?: n.employeeId))
        }
        for (n in data.negativeNotes) {
            negatives.insert(n.copy(id = 0, employeeId = idMap[n.employeeId] ?: n.employeeId))
        }
        for (n in data.personalityNotes) {
            personalities.insert(n.copy(id = 0, employeeId = idMap[n.employeeId] ?: n.employeeId))
        }
        for (n in data.disciplinaryRecords) {
            disciplinary.insert(n.copy(id = 0, employeeId = idMap[n.employeeId] ?: n.employeeId))
        }
        for (s in data.settings) {
            setSetting(s.key, s.value)
        }
        refreshAll()
    }
}

@kotlinx.serialization.Serializable
data class BackupData(
    val employees: List<Employee> = emptyList(),
    val positiveNotes: List<PositiveNote> = emptyList(),
    val negativeNotes: List<NegativeNote> = emptyList(),
    val personalityNotes: List<PersonalityNote> = emptyList(),
    val disciplinaryRecords: List<DisciplinaryRecord> = emptyList(),
    val settings: List<SettingEntity> = emptyList()
)

object DefaultSettings {
    val values = mapOf(
        "weight_positive" to "1",
        "weight_negative" to "-1",
        "severity_low" to "-1",
        "severity_medium" to "-3",
        "severity_high" to "-5",
        "severity_very_high" to "-8",
        "shifts_list" to "صبح,عصر,شب",
        "positions_list" to "اپراتور,سرشیفت,کارگر ساده,تکنسین",
        "workplaces_list" to "خط تولید ۱,خط تولید ۲,انبار",
        "violation_types_list" to "غیبت,تاخیر,عدم رعایت ایمنی,نافرمانی,سایر",
        "importance_list" to "کم,متوسط,زیاد",
        "pin_enabled" to "0",
        "pin_hash" to "",
        "last_backup_date" to ""
    )
}
