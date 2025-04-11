package com.example.fluithkotlin.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VacationLeaveDao {
    @Insert
    suspend fun insert(leave: VacationLeaveEntity)

    @Query("SELECT * FROM vacation_leaves ORDER BY startDate DESC")
    fun getAll(): Flow<List<VacationLeaveEntity>>

    @Delete
    suspend fun delete(leave: VacationLeaveEntity)
}

@Dao
interface MedicalLeaveDao {
    @Insert
    suspend fun insert(leave: MedicalLeaveEntity)

    @Query("SELECT * FROM medical_leaves ORDER BY startDate DESC")
    fun getAll(): Flow<List<MedicalLeaveEntity>>

    @Delete
    suspend fun delete(leave: MedicalLeaveEntity)
}

@Dao
interface LicenseLeaveDao {
    @Insert
    suspend fun insert(leave: LicenseLeaveEntity)

    @Query("SELECT * FROM license_leaves ORDER BY startDate DESC")
    fun getAll(): Flow<List<LicenseLeaveEntity>>

    @Delete
    suspend fun delete(leave: LicenseLeaveEntity)
}