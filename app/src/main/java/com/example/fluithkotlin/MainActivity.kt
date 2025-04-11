package com.example.fluithkotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.example.fluithkotlin.data.LeaveDatabase
import com.example.fluithkotlin.data.LeaveRepository
import com.example.fluithkotlin.model.LeaveType
import com.example.fluithkotlin.uiFolder.LeaveViewModel
import com.example.fluithkotlin.uiFolder.LeaveViewModelFactory
import com.example.fluithkotlin.uiFolder.screens.LeaveFormScreen
import com.example.fluithkotlin.uiFolder.screens.LeaveListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = LeaveDatabase.getDatabase(this)
        val repository = LeaveRepository(database)
        val viewModel: LeaveViewModel by viewModels { LeaveViewModelFactory(repository) }

        setContent {
            HRAppTheme {
                var showForm by remember { mutableStateOf(false) }
                var selectedLeaveType by remember { mutableStateOf<LeaveType?>(null) }

                if (showForm && selectedLeaveType != null) {
                    viewModel.selectedLeaveType = selectedLeaveType!!
                    LeaveFormScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            showForm = false
                            viewModel.resetForm()
                        }
                    )
                } else {
                    LeaveListScreen(
                        viewModel = viewModel,
                        onAddLeave = { type ->
                            selectedLeaveType = type
                            showForm = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HRAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4285F4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3367D6),
    secondary = Color(0xFF34A853),
    onSecondary = Color.White,
    tertiary = Color(0xFFEA4335)
)

