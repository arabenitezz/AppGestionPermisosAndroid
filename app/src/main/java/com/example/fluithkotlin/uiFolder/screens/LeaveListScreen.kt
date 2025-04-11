package com.example.fluithkotlin.uiFolder.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DismissValue
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DismissDirection
import androidx.compose.material.ExperimentalMaterialApi
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.SwipeToDismiss
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.fluithkotlin.data.*
import com.example.fluithkotlin.model.LeaveType
import com.example.fluithkotlin.model.LicenseSubtype
import com.example.fluithkotlin.uiFolder.LeaveViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun LeaveListScreen(
    viewModel: LeaveViewModel,
    onAddLeave: (LeaveType) -> Unit
) {
    var showLeaveTypeSelection by remember { mutableStateOf(false) }
    val leaves by viewModel.allLeaves.collectAsState(initial = emptyList())

    var itemBeingDeleted by remember { mutableStateOf<Any?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Historial de Permisos") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showLeaveTypeSelection = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Leave")
            }
        }
    ) { padding ->
        if (leaves.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay solicitudes registradas")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(
                    items = leaves,
                    key = { leave ->
                        when (leave) {
                            is VacationLeaveEntity -> "vacation_${leave.id}"
                            is MedicalLeaveEntity -> "medical_${leave.id}"
                            is LicenseLeaveEntity -> "license_${leave.id}"
                        }
                    }
                ) { leave ->
                    SwipeableLeaveCard(
                        leave = leave,
                        onDelete = {
                            itemBeingDeleted = leave
                            viewModel.deleteLeave(leave)
                            // Reset the item being deleted after a short delay
                            kotlinx.coroutines.MainScope().launch {
                                kotlinx.coroutines.delay(800)
                                itemBeingDeleted = null
                            }
                        }
                    )
                }
            }
        }

        if (showLeaveTypeSelection) {
            AlertDialog(
                onDismissRequest = { showLeaveTypeSelection = false },
                title = { Text("Seleccionar tipo de permiso") },
                text = {
                    Column {
                        listOf(
                            LeaveType.VACATION,
                            LeaveType.MEDICAL,
                            LeaveType.LICENSE
                        ).forEach { type ->
                            Button(
                                onClick = {
                                    showLeaveTypeSelection = false
                                    onAddLeave(type)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(type.displayName)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showLeaveTypeSelection = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeableLeaveCard(
    leave: Any,
    onDelete: () -> Unit
) {
    val dismissState = rememberDismissState(
        initialValue = DismissValue.Default,
        confirmStateChange = { dismissValue ->
            if (dismissValue == DismissValue.DismissedToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    LaunchedEffect(leave) {
        dismissState.reset()
    }

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        background = {
            val isDismissed = dismissState.isDismissed(DismissDirection.EndToStart)
            val isSwiping = dismissState.offset.value != 0f
            val backgroundColor =
                if (dismissState.dismissDirection == DismissDirection.EndToStart) {
                    Color.Red
                } else {
                    Color.Transparent
                }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (isDismissed || isSwiping) {
                    Text("Eliminar", color = Color.White)
                }
            }
        },
        dismissContent = {
            when (leave) {
                is VacationLeaveEntity -> VacationLeaveCard(leave)
                is MedicalLeaveEntity -> MedicalLeaveCard(leave)
                is LicenseLeaveEntity -> LicenseLeaveCard(leave)
                else -> {}
            }
        }
    )
}

@Composable
private fun VacationLeaveCard(leave: VacationLeaveEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = leave.employeeName, style = MaterialTheme.typography.titleLarge)
            Text(text = "Desde: ${leave.startDate}")
            Text(text = "Hasta: ${leave.endDate}")
            Text(text = "Tipo: Vacaciones")
            Text(text = "Estado: ${leave.status}")
        }
    }
}

@Composable
private fun MedicalLeaveCard(leave: MedicalLeaveEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = leave.employeeName, style = MaterialTheme.typography.titleLarge)
            Text(text = "Desde: ${leave.startDate}")
            Text(text = "Hasta: ${leave.endDate}")
            Text(text = "Tipo: Reposo Médico")
            leave.doctorName?.let {
                Text(text = "Doctor: $it")
            }
            if (leave.documentUri != null) {
                Text("Documentación")
            }
        }
    }
}

@Composable
private fun LicenseLeaveCard(leave: LicenseLeaveEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = leave.employeeName, style = MaterialTheme.typography.titleLarge)
            Text(text = "Desde: ${leave.startDate}")
            Text(text = "Hasta: ${leave.endDate}")

            val typeText = when (leave.subtype) {
                LicenseSubtype.EXAMEN -> "Licencia: Exámenes"
                LicenseSubtype.MATRIMONIO -> "Licencia: Matrimonio"
                LicenseSubtype.LUTO -> "Licencia: Defunción"
                LicenseSubtype.NACIMIENTO -> "Licencia: Nacimiento"
            }
            Text(text = typeText)

            when (leave.subtype) {
                LicenseSubtype.LUTO -> leave.deathRelationship?.let {
                    Text(text = "Parentesco: $it")
                }
                LicenseSubtype.NACIMIENTO -> leave.gender?.let {
                    Text(text = when (it) {
                        "MASCULINO" -> "Papa Roshkero"
                        "FEMENINO" -> "Mama Roshkera"
                        else -> "Genero: $it"
                    })
                }
                else -> {}
            }
        }
    }
}