package com.example.fluithkotlin.uiFolder.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fluithkotlin.model.DeathRelationship
import com.example.fluithkotlin.model.Gender
import com.example.fluithkotlin.model.LeaveType
import com.example.fluithkotlin.model.LicenseSubtype
import com.example.fluithkotlin.uiFolder.LeaveViewModel
import java.util.Calendar

@Composable
fun LeaveFormScreen(
    viewModel: LeaveViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.getType(it)?.let { mimeType ->
                if (mimeType.startsWith("image/")) {
                    viewModel.documentUri = it
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Button(
            onClick = onNavigateBack,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Volver")
        }

        val formTitle = when (viewModel.selectedLeaveType) {
            LeaveType.VACATION -> "Solicitud de Vacaciones"
            LeaveType.MEDICAL -> "Solicitud de Reposo Médico"
            LeaveType.LICENSE -> "Solicitud de Licencia"
            LeaveType.MARRIAGE -> "Solicitud de Matrimonio"
            LeaveType.BIRTH -> "Solicitud de Nacimiento"
            LeaveType.DEATH -> "Solicitud de Defunción"
            LeaveType.PREGNANCY -> "Solicitud de Embarazo"
            LeaveType.PATERNITY -> "Solicitud de Paternidad"
            null -> "Solicitud de Permiso"
        }

        Text(
            text = formTitle,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Nombre roshkero
        OutlinedTextField(
            value = viewModel.employeeName,
            onValueChange = { viewModel.employeeName = it },
            label = { Text("Nombre del funcionario") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Fecha inicio
        DatePickerField(
            label = "Fecha de inicio (DD-MM-YYY)",
            date = viewModel.startDate,
            onDateSelected = {
                viewModel.startDate = it

                // calcular fechas automaticas
                if (viewModel.selectedLeaveType == LeaveType.LICENSE &&
                    viewModel.licenseSubtype != null &&
                    viewModel.licenseSubtype != LicenseSubtype.EXAMEN) {
                    viewModel.updateEndDateBasedOnLicenseType()
                }
            }
        )

        when (viewModel.selectedLeaveType) {
            LeaveType.VACATION -> {
                // fecha de fin
                DatePickerField(
                    label = "Fecha de fin (DD-MM-YYY)",
                    date = viewModel.endDate,
                    onDateSelected = { viewModel.endDate = it }
                )
            }
            LeaveType.MEDICAL -> {
                // fecha de fin
                DatePickerField(
                    label = "Fecha de fin (DD-MM-YYY)",
                    date = viewModel.endDate,
                    onDateSelected = { viewModel.endDate = it }
                )

                // seleccion de imagen
                Button(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Añadir comprobante médico")
                }

                // mostrar imagen
                viewModel.documentUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Comprobante médico",
                        modifier = Modifier.size(150.dp)
                    )
                }
            }
            LeaveType.LICENSE -> {
                // dropdown para el tipo de licencia
                LicenseTypeDropdown(viewModel)

                when (viewModel.licenseSubtype) {
                    LicenseSubtype.EXAMEN -> {
                        // fecha final
                        DatePickerField(
                            label = "Fecha de fin (DD-MM-YYY)",
                            date = viewModel.endDate,
                            onDateSelected = { viewModel.endDate = it }
                        )

                        // error de fecha
                        viewModel.warning?.let { warning ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = warning,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    LicenseSubtype.LUTO -> {
                        // dd para tipo de relacion
                        DeathRelationshipDropdown(viewModel)

                        // fecha de fin calculada
                        if (viewModel.deathRelationship != null) {
                            Text(
                                text = "Fecha de fin calculada: ${viewModel.calculateLicenseEndDate()}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                    LicenseSubtype.NACIMIENTO -> {
                        // dd para genero
                        GenderDropdown(viewModel)

                        // fecha de fin calculada
                        if (viewModel.gender != null) {
                            Text(
                                text = "Fecha de fin calculada: ${viewModel.calculateLicenseEndDate()}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                    LicenseSubtype.MATRIMONIO -> {
                        // fecha de fin
                        Text(
                            text = "Fecha de fin calculada: ${viewModel.calculateLicenseEndDate()}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    null -> { }
                }
            }
            LeaveType.MARRIAGE -> {
                // fecha de fin calculada
                Text(
                    text = "Fecha de fin calculada: ${viewModel.calculateLicenseEndDate()}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            LeaveType.BIRTH -> {
                // dd para genero
                GenderDropdown(viewModel)

                // fecha de fin calculada
                if (viewModel.gender != null) {
                    Text(
                        text = "Fecha de fin calculada: ${viewModel.calculateLicenseEndDate()}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            LeaveType.DEATH -> {
                // dd para tipo de relacion
                DeathRelationshipDropdown(viewModel)

                // fecha de fin calculada
                if (viewModel.deathRelationship != null) {
                    Text(
                        text = "Fecha de fin calculada: ${viewModel.calculateLicenseEndDate()}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            LeaveType.PREGNANCY -> {
                // mostrar fecha de fin
                viewModel.gender = Gender.FEMENINO
                Text(
                    text = "Fecha de fin calculada: ${viewModel.calculateLicenseEndDate()}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            LeaveType.PATERNITY -> {
                // mostrar fecha de fin
                viewModel.gender = Gender.MASCULINO
                Text(
                    text = "Fecha de fin calculada: ${viewModel.calculateLicenseEndDate()}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            null -> { }
        }

        // boton para enviar solicitud
        Button(
            onClick = {
                if (viewModel.isFormValid()) {
                    viewModel.submitLeaveRequest()
                    onNavigateBack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = viewModel.isFormValid()
        ) {
            Text("Enviar solicitud")
        }

        // Error
        viewModel.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun LicenseTypeDropdown(viewModel: LeaveViewModel) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp)) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(viewModel.licenseSubtype?.name?.let {
                when (it) {
                    "EXAMEN" -> "Exámenes"
                    "MATRIMONIO" -> "Matrimonio"
                    "LUTO" -> "Defunción"
                    "NACIMIENTO" -> "Nacimiento"
                    else -> it
                }
            } ?: "Seleccionar tipo de licencia")
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            LicenseSubtype.entries.forEach { subtype ->
                val displayName = when (subtype) {
                    LicenseSubtype.EXAMEN -> "Exámenes"
                    LicenseSubtype.MATRIMONIO -> "Matrimonio"
                    LicenseSubtype.LUTO -> "Defunción"
                    LicenseSubtype.NACIMIENTO -> "Nacimiento"
                }

                DropdownMenuItem(
                    onClick = {
                        viewModel.licenseSubtype = subtype
                        expanded = false
                        if (subtype != LicenseSubtype.EXAMEN) {
                            viewModel.updateEndDateBasedOnLicenseType()
                        }
                    },
                    text = { Text(displayName) }
                )
            }
        }
    }
}

@Composable
fun DeathRelationshipDropdown(viewModel: LeaveViewModel) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp)) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(viewModel.deathRelationship?.name?.let {
                when (it) {
                    "PADRES" -> "Padre/Madre"
                    "HIJOS" -> "Hijo/Hija"
                    "HERMANOS" -> "Hermano/Hermana"
                    "ABUELOS" -> "Abuelo/Abuela"
                    "OTROS" -> "Otro Familiar"
                    else -> it
                }
            } ?: "Seleccionar parentesco")
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DeathRelationship.entries.forEach { relationship ->
                val displayName = when (relationship) {
                    DeathRelationship.PADRES -> "Padre/Madre"
                    DeathRelationship.HIJOS -> "Hijo/Hija"
                    DeathRelationship.HERMANOS -> "Hermano/Hermana"
                    DeathRelationship.ABUELOS -> "Abuelo/Abuela"
                    DeathRelationship.OTROS -> "Otro Familiar"
                }

                DropdownMenuItem(
                    onClick = {
                        viewModel.deathRelationship = relationship
                        expanded = false
                        viewModel.updateEndDateBasedOnLicenseType()
                    },
                    text = { Text(displayName) }
                )
            }
        }
    }
}

@Composable
fun GenderDropdown(viewModel: LeaveViewModel) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp)) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(viewModel.gender?.name?.let {
                when (it) {
                    "MASCULINO" -> "Masculino"
                    "FEMENINO" -> "Femenino"
                    else -> it
                }
            } ?: "Seleccionar género")
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Gender.entries.forEach { gender ->
                val displayName = when (gender) {
                    Gender.MASCULINO -> "Masculino"
                    Gender.FEMENINO -> "Femenino"
                }

                DropdownMenuItem(
                    onClick = {
                        viewModel.gender = gender
                        expanded = false
                        viewModel.updateEndDateBasedOnLicenseType()
                    },
                    text = { Text(displayName) }
                )
            }
        }
    }
}

@Composable
fun DatePickerField(
    label: String,
    date: String,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePicker = remember {
        android.app.DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "${selectedDay.toString().padStart(2, '0')}-${(selectedMonth + 1).toString().padStart(2, '0')}-$selectedYear"
                onDateSelected(formattedDate)
            },
            year,
            month,
            day
        )
    }

    OutlinedTextField(
        value = date,
        onValueChange = { },
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { datePicker.show() }) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Pick date")
            }
        }
    )
}