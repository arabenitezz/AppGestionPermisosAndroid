package com.example.fluithkotlin.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        VacationLeaveEntity::class,
        MedicalLeaveEntity::class,
        LicenseLeaveEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class LeaveDatabase : RoomDatabase() {
    abstract fun vacationLeaveDao(): VacationLeaveDao
    abstract fun medicalLeaveDao(): MedicalLeaveDao
    abstract fun licenseLeaveDao(): LicenseLeaveDao

    companion object {
        @Volatile
        private var Instance: LeaveDatabase? = null

        fun getDatabase(context: Context): LeaveDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    LeaveDatabase::class.java,
                    "leave_db"
                ).fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}