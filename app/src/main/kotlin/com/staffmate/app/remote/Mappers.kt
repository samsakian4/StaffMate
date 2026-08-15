package com.staffmate.app.remote

import com.staffmate.app.data.DisciplinaryRecord
import com.staffmate.app.data.Employee
import com.staffmate.app.data.NegativeNote
import com.staffmate.app.data.PersonalityNote
import com.staffmate.app.data.PositiveNote
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    timeZone = TimeZone.getDefault()
}

fun msToDbDate(ms: Long): String = dbDateFormat.format(Date(ms))
fun dbDateToMs(str: String?): Long {
    if (str.isNullOrBlank()) return System.currentTimeMillis()
    return try { dbDateFormat.parse(str)?.time ?: System.currentTimeMillis() } catch (e: Exception) { System.currentTimeMillis() }
}

private fun JsonObject.str(key: String): String = this[key]?.jsonPrimitive?.contentOrNull ?: ""
private fun JsonObject.strOrNull(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull
private fun JsonObject.longVal(key: String): Long = this[key]?.jsonPrimitive?.long ?: 0L
private fun JsonObject.boolVal(key: String): Boolean = this[key]?.jsonPrimitive?.boolean ?: true

// ---------- Employee ----------
fun Employee.toInsertJson(): JsonObject = buildJsonObject {
    put("personnel_code", JsonPrimitive(personnelCode))
    put("first_name", JsonPrimitive(firstName))
    put("last_name", JsonPrimitive(lastName))
    put("position", JsonPrimitive(position))
    put("workplace", JsonPrimitive(workplace))
    put("shift", JsonPrimitive(shift))
    put("active", JsonPrimitive(active))
}
fun Employee.toUpdateJson(): JsonObject = toInsertJson()

fun JsonObject.toEmployee(): Employee = Employee(
    id = longVal("id"),
    personnelCode = str("personnel_code"),
    firstName = str("first_name"),
    lastName = str("last_name"),
    position = str("position"),
    workplace = str("workplace"),
    shift = str("shift"),
    active = boolVal("active")
)

// ---------- PositiveNote ----------
fun PositiveNote.toJson(): JsonObject = buildJsonObject {
    put("employee_id", JsonPrimitive(employeeId))
    put("title", JsonPrimitive(title))
    put("description", JsonPrimitive(description))
    put("importance", JsonPrimitive(importance))
    put("date", JsonPrimitive(msToDbDate(date)))
}
fun JsonObject.toPositiveNote(): PositiveNote = PositiveNote(
    id = longVal("id"),
    employeeId = longVal("employee_id"),
    title = str("title"),
    description = str("description"),
    importance = str("importance"),
    date = dbDateToMs(strOrNull("date"))
)

// ---------- NegativeNote ----------
fun NegativeNote.toJson(): JsonObject = buildJsonObject {
    put("employee_id", JsonPrimitive(employeeId))
    put("title", JsonPrimitive(title))
    put("description", JsonPrimitive(description))
    put("importance", JsonPrimitive(importance))
    put("status", JsonPrimitive(status))
    put("date", JsonPrimitive(msToDbDate(date)))
}
fun JsonObject.toNegativeNote(): NegativeNote = NegativeNote(
    id = longVal("id"),
    employeeId = longVal("employee_id"),
    title = str("title"),
    description = str("description"),
    importance = str("importance"),
    status = str("status"),
    date = dbDateToMs(strOrNull("date"))
)

// ---------- PersonalityNote ----------
fun PersonalityNote.toJson(): JsonObject = buildJsonObject {
    put("employee_id", JsonPrimitive(employeeId))
    put("type", JsonPrimitive(type))
    put("title", JsonPrimitive(title))
    put("description", JsonPrimitive(description))
    put("date", JsonPrimitive(msToDbDate(date)))
}
fun JsonObject.toPersonalityNote(): PersonalityNote = PersonalityNote(
    id = longVal("id"),
    employeeId = longVal("employee_id"),
    type = str("type"),
    title = str("title"),
    description = str("description"),
    date = dbDateToMs(strOrNull("date"))
)

// ---------- DisciplinaryRecord ----------
fun DisciplinaryRecord.toJson(): JsonObject = buildJsonObject {
    put("employee_id", JsonPrimitive(employeeId))
    put("date", JsonPrimitive(msToDbDate(date)))
    put("violation_type", JsonPrimitive(violationType))
    put("description", JsonPrimitive(description))
    put("severity", JsonPrimitive(severity))
    put("action_taken", JsonPrimitive(actionTaken))
    put("additional_notes", JsonPrimitive(additionalNotes))
}
fun JsonObject.toDisciplinaryRecord(): DisciplinaryRecord = DisciplinaryRecord(
    id = longVal("id"),
    employeeId = longVal("employee_id"),
    date = dbDateToMs(strOrNull("date")),
    violationType = str("violation_type"),
    description = str("description"),
    severity = str("severity"),
    actionTaken = str("action_taken"),
    additionalNotes = str("additional_notes")
)
