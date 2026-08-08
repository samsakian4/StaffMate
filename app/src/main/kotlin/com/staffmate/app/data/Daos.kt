package com.staffmate.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(employee: Employee): Long

    @Update
    suspend fun update(employee: Employee)

    @Query("UPDATE employees SET active = :active, updatedAt = :ts WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean, ts: Long = System.currentTimeMillis())

    @Query("SELECT * FROM employees ORDER BY firstName, lastName")
    fun observeAll(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE active = 1 ORDER BY firstName, lastName")
    fun observeActive(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE id = :id")
    fun observeById(id: Long): Flow<Employee?>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getById(id: Long): Employee?

    @Query("""
        SELECT * FROM employees
        WHERE (:query = '' OR firstName LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%' OR personnelCode LIKE '%' || :query || '%')
        AND (:onlyActive = 0 OR active = 1)
        ORDER BY firstName, lastName
    """)
    fun search(query: String, onlyActive: Int): Flow<List<Employee>>

    @Query("SELECT COUNT(*) FROM employees WHERE active = 1")
    fun observeActiveCount(): Flow<Int>
}

@Dao
interface PositiveNoteDao {
    @Insert suspend fun insert(note: PositiveNote): Long
    @Update suspend fun update(note: PositiveNote)
    @Delete suspend fun delete(note: PositiveNote)

    @Query("SELECT * FROM positive_notes WHERE employeeId = :employeeId ORDER BY date DESC")
    fun observeForEmployee(employeeId: Long): Flow<List<PositiveNote>>

    @Query("SELECT * FROM positive_notes WHERE id = :id")
    suspend fun getById(id: Long): PositiveNote?

    @Query("SELECT * FROM positive_notes WHERE employeeId = :employeeId ORDER BY date DESC")
    suspend fun getAllForEmployee(employeeId: Long): List<PositiveNote>

    @Query("SELECT COUNT(*) FROM positive_notes WHERE employeeId = :employeeId")
    fun countForEmployee(employeeId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM positive_notes")
    fun observeTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM positive_notes WHERE employeeId = :employeeId AND date >= :since")
    suspend fun countSince(employeeId: Long, since: Long): Int
}

@Dao
interface NegativeNoteDao {
    @Insert suspend fun insert(note: NegativeNote): Long
    @Update suspend fun update(note: NegativeNote)
    @Delete suspend fun delete(note: NegativeNote)

    @Query("SELECT * FROM negative_notes WHERE employeeId = :employeeId ORDER BY date DESC")
    fun observeForEmployee(employeeId: Long): Flow<List<NegativeNote>>

    @Query("SELECT * FROM negative_notes WHERE id = :id")
    suspend fun getById(id: Long): NegativeNote?

    @Query("SELECT * FROM negative_notes WHERE employeeId = :employeeId ORDER BY date DESC")
    suspend fun getAllForEmployee(employeeId: Long): List<NegativeNote>

    @Query("SELECT COUNT(*) FROM negative_notes WHERE employeeId = :employeeId")
    fun countForEmployee(employeeId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM negative_notes")
    fun observeTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM negative_notes WHERE employeeId = :employeeId AND date >= :since")
    suspend fun countSince(employeeId: Long, since: Long): Int
}

@Dao
interface PersonalityNoteDao {
    @Insert suspend fun insert(note: PersonalityNote): Long
    @Update suspend fun update(note: PersonalityNote)
    @Delete suspend fun delete(note: PersonalityNote)

    @Query("SELECT * FROM personality_notes WHERE employeeId = :employeeId ORDER BY date DESC")
    fun observeForEmployee(employeeId: Long): Flow<List<PersonalityNote>>

    @Query("SELECT * FROM personality_notes WHERE id = :id")
    suspend fun getById(id: Long): PersonalityNote?

    @Query("SELECT * FROM personality_notes WHERE employeeId = :employeeId ORDER BY date DESC")
    suspend fun getAllForEmployee(employeeId: Long): List<PersonalityNote>

    @Query("SELECT COUNT(*) FROM personality_notes WHERE employeeId = :employeeId")
    fun countForEmployee(employeeId: Long): Flow<Int>
}

@Dao
interface DisciplinaryDao {
    @Insert suspend fun insert(record: DisciplinaryRecord): Long
    @Update suspend fun update(record: DisciplinaryRecord)
    @Delete suspend fun delete(record: DisciplinaryRecord)

    @Query("SELECT * FROM disciplinary_records WHERE employeeId = :employeeId ORDER BY date DESC")
    fun observeForEmployee(employeeId: Long): Flow<List<DisciplinaryRecord>>

    @Query("SELECT * FROM disciplinary_records WHERE id = :id")
    suspend fun getById(id: Long): DisciplinaryRecord?

    @Query("SELECT COUNT(*) FROM disciplinary_records WHERE employeeId = :employeeId")
    fun countForEmployee(employeeId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM disciplinary_records")
    fun observeTotalCount(): Flow<Int>

    @Query("SELECT * FROM disciplinary_records WHERE employeeId = :employeeId AND date >= :since")
    suspend fun getSince(employeeId: Long, since: Long): List<DisciplinaryRecord>
}

@Dao
interface SettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(setting: SettingEntity)

    @Query("SELECT * FROM settings WHERE key = :key")
    suspend fun get(key: String): SettingEntity?

    @Query("SELECT * FROM settings")
    fun observeAll(): Flow<List<SettingEntity>>
}
