package com.example.fluithkotlin.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class LeaveRepository(
    private val database: LeaveDatabase
) {
    // Vacation Leave operations
    suspend fun insertVacationLeave(leave: VacationLeaveEntity) = database.vacationLeaveDao().insert(leave)
    private fun getAllVacationLeaves(): Flow<List<VacationLeaveEntity>> = database.vacationLeaveDao().getAll()
    suspend fun deleteVacationLeave(leave: VacationLeaveEntity) = database.vacationLeaveDao().delete(leave)

    // Medical Leave operations
    suspend fun insertMedicalLeave(leave: MedicalLeaveEntity) = database.medicalLeaveDao().insert(leave)
    private fun getAllMedicalLeaves(): Flow<List<MedicalLeaveEntity>> = database.medicalLeaveDao().getAll()
    suspend fun deleteMedicalLeave(leave: MedicalLeaveEntity) = database.medicalLeaveDao().delete(leave)

    // License Leave operations
    suspend fun insertLicenseLeave(leave: LicenseLeaveEntity) = database.licenseLeaveDao().insert(leave)
    private fun getAllLicenseLeaves(): Flow<List<LicenseLeaveEntity>> = database.licenseLeaveDao().getAll()
    suspend fun deleteLicenseLeave(leave: LicenseLeaveEntity) = database.licenseLeaveDao().delete(leave)

    // Combined flow of all leaves
    fun getAllLeaves(): Flow<List<LeaveEntity>> {
        return combine(
            getAllVacationLeaves(),
            getAllMedicalLeaves(),
            getAllLicenseLeaves()
        ) { vacations, medicals, licenses ->
            (vacations + medicals + licenses).sortedByDescending { it.startDate }
        }
    }
}