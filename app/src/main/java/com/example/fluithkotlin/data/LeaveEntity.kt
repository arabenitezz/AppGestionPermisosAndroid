package com.example.fluithkotlin.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fluithkotlin.model.LicenseSubtype

sealed class LeaveEntity {
    abstract val id: Int
    abstract val employeeName: String
    abstract val startDate: String
    abstract val endDate: String
    abstract val documentUri: String?
}

@Entity(tableName = "vacation_leaves")
data class VacationLeaveEntity(
    @PrimaryKey(autoGenerate = true) override val id: Int = 0,
    override val employeeName: String,
    override val startDate: String,
    override val endDate: String,
    override val documentUri: String? = null,
    val status: String = "PENDIENTE"
) : LeaveEntity()

@Entity(tableName = "medical_leaves")
data class MedicalLeaveEntity(
    @PrimaryKey(autoGenerate = true) override val id: Int = 0,
    override val employeeName: String,
    override val startDate: String,
    override val endDate: String,
    override val documentUri: String?,
    val doctorName: String? = null
) : LeaveEntity()

@Entity(tableName = "license_leaves")
data class LicenseLeaveEntity(
    @PrimaryKey(autoGenerate = true) override val id: Int = 0,
    override val employeeName: String,
    override val startDate: String,
    override val endDate: String,
    override val documentUri: String? = null,
    val subtype: LicenseSubtype,
    val deathRelationship: String? = null,
    val gender: String? = null
) : LeaveEntity()
