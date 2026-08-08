package com.staffmate.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Employee::class,
        PositiveNote::class,
        NegativeNote::class,
        PersonalityNote::class,
        DisciplinaryRecord::class,
        SettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StaffMateDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun positiveNoteDao(): PositiveNoteDao
    abstract fun negativeNoteDao(): NegativeNoteDao
    abstract fun personalityNoteDao(): PersonalityNoteDao
    abstract fun disciplinaryDao(): DisciplinaryDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        const val DB_NAME = "staffmate.db"

        @Volatile private var INSTANCE: StaffMateDatabase? = null

        fun getInstance(context: Context): StaffMateDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StaffMateDatabase::class.java,
                    DB_NAME
                ).build().also { INSTANCE = it }
            }

        fun closeAndReset() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }
}

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

suspend fun StaffMateDatabase.ensureDefaultSettings() {
    val dao = settingsDao()
    for ((k, v) in DefaultSettings.values) {
        if (dao.get(k) == null) dao.set(SettingEntity(k, v))
    }
}
