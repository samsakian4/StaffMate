package com.staffmate.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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

@Entity(
    tableName = "positive_notes",
    foreignKeys = [ForeignKey(
        entity = Employee::class,
        parentColumns = ["id"],
        childColumns = ["employeeId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("employeeId")]
)
data class PositiveNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val title: String,
    val description: String = "",
    val importance: String,
    val date: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "negative_notes",
    foreignKeys = [ForeignKey(
        entity = Employee::class,
        parentColumns = ["id"],
        childColumns = ["employeeId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("employeeId")]
)
data class NegativeNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val title: String,
    val description: String = "",
    val importance: String,
    val status: String = "تکرار نشده",
    val date: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "personality_notes",
    foreignKeys = [ForeignKey(
        entity = Employee::class,
        parentColumns = ["id"],
        childColumns = ["employeeId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("employeeId")]
)
data class PersonalityNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val type: String, // مثبت / منفی
    val title: String,
    val description: String = "",
    val date: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "disciplinary_records",
    foreignKeys = [ForeignKey(
        entity = Employee::class,
        parentColumns = ["id"],
        childColumns = ["employeeId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("employeeId")]
)
data class DisciplinaryRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val date: Long,
    val violationType: String,
    val description: String = "",
    val severity: String, // کم / متوسط / زیاد / بسیار زیاد
    val actionTaken: String = "",
    val additionalNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
