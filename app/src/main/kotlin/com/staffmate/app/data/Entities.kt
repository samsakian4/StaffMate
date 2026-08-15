package com.staffmate.app.data

import kotlinx.serialization.Serializable

@Serializable
data class Employee(
    val id: Long = 0,
    val personnelCode: String,
    val firstName: String,
    val lastName: String,
    val position: String = "",
    val workplace: String = "",
    val shift: String = "",
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class PositiveNote(
    val id: Long = 0,
    val employeeId: Long,
    val title: String,
    val description: String = "",
    val importance: String,
    val date: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class NegativeNote(
    val id: Long = 0,
    val employeeId: Long,
    val title: String,
    val description: String = "",
    val importance: String,
    val status: String = "تکرار نشده",
    val date: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class PersonalityNote(
    val id: Long = 0,
    val employeeId: Long,
    val type: String, // مثبت / منفی
    val title: String,
    val description: String = "",
    val date: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class DisciplinaryRecord(
    val id: Long = 0,
    val employeeId: Long,
    val date: Long,
    val violationType: String,
    val description: String = "",
    val severity: String, // کم / متوسط / زیاد / بسیار زیاد
    val actionTaken: String = "",
    val additionalNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class SettingEntity(
    val key: String,
    val value: String
)
