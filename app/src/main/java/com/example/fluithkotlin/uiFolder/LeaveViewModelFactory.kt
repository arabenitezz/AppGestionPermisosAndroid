package com.example.fluithkotlin.uiFolder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fluithkotlin.data.LeaveRepository

class LeaveViewModelFactory(
    private val repository: LeaveRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeaveViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LeaveViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}