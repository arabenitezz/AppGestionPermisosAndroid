package com.example.fluithkotlin.uiFolder

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fluithkotlin.data.*
import com.example.fluithkotlin.model.DeathRelationship
import com.example.fluithkotlin.model.Gender
import com.example.fluithkotlin.model.LeaveType
import com.example.fluithkotlin.model.LicenseSubtype
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class LeaveViewModel(
    private val leaveRepository: LeaveRepository
) : ViewModel() {

    // compos para el formulario
    var selectedLeaveType by mutableStateOf<LeaveType?>(null)
    var employeeName by mutableStateOf("")
    var startDate by mutableStateOf("")
    var endDate by mutableStateOf("")
    var documentUri by mutableStateOf<Uri?>(null)
    var licenseSubtype by mutableStateOf<LicenseSubtype?>(null)
    var deathRelationship by mutableStateOf<DeathRelationship?>(null)
    var gender by mutableStateOf<Gender?>(null)
    var error by mutableStateOf<String?>(null)
    var warning by mutableStateOf<String?>(null)

    val allLeaves: Flow<List<LeaveEntity>> = leaveRepository.getAllLeaves()

    fun resetForm() {
        selectedLeaveType = null
        employeeName = ""
        startDate = ""
        endDate = ""
        documentUri = null
        licenseSubtype = null
        deathRelationship = null
        gender = null
        error = null
        warning = null
    }

    fun submitLeaveRequest() {
        if (!isFormValid()) return

        viewModelScope.launch {
            when (selectedLeaveType) {
                LeaveType.VACATION -> {
                    val vacationLeave = VacationLeaveEntity(
                        employeeName = employeeName,
                        startDate = startDate,
                        endDate = endDate
                    )
                    leaveRepository.insertVacationLeave(vacationLeave)
                }
                LeaveType.MEDICAL -> {
                    val medicalLeave = MedicalLeaveEntity(
                        employeeName = employeeName,
                        startDate = startDate,
                        endDate = endDate,
                        documentUri = documentUri?.toString(),
                    )
                    leaveRepository.insertMedicalLeave(medicalLeave)
                }
                LeaveType.LICENSE -> {
                    licenseSubtype?.let { subtype ->
                        val licenseLeave = LicenseLeaveEntity(
                            employeeName = employeeName,
                            startDate = startDate,
                            endDate = endDate,
                            documentUri = documentUri?.toString(),
                            subtype = subtype,
                            deathRelationship = if (subtype == LicenseSubtype.LUTO)
                                deathRelationship?.name else null,
                            gender = if (subtype == LicenseSubtype.NACIMIENTO)
                                gender?.name else null
                        )
                        leaveRepository.insertLicenseLeave(licenseLeave)
                    }
                }
                LeaveType.MARRIAGE -> {

                    val licenseLeave = LicenseLeaveEntity(
                        employeeName = employeeName,
                        startDate = startDate,
                        endDate = endDate,
                        documentUri = documentUri?.toString(),
                        subtype = LicenseSubtype.MATRIMONIO
                    )
                    leaveRepository.insertLicenseLeave(licenseLeave)
                }
                LeaveType.BIRTH -> {

                    val licenseLeave = LicenseLeaveEntity(
                        employeeName = employeeName,
                        startDate = startDate,
                        endDate = endDate,
                        documentUri = documentUri?.toString(),
                        subtype = LicenseSubtype.NACIMIENTO,
                        gender = gender?.name
                    )
                    leaveRepository.insertLicenseLeave(licenseLeave)
                }
                LeaveType.DEATH -> {

                    val licenseLeave = LicenseLeaveEntity(
                        employeeName = employeeName,
                        startDate = startDate,
                        endDate = endDate,
                        documentUri = documentUri?.toString(),
                        subtype = LicenseSubtype.LUTO,
                        deathRelationship = deathRelationship?.name
                    )
                    leaveRepository.insertLicenseLeave(licenseLeave)
                }
                LeaveType.PREGNANCY, LeaveType.PATERNITY -> {

                    val licenseLeave = LicenseLeaveEntity(
                        employeeName = employeeName,
                        startDate = startDate,
                        endDate = endDate,
                        documentUri = documentUri?.toString(),
                        subtype = LicenseSubtype.NACIMIENTO,
                        gender = gender?.name
                    )
                    leaveRepository.insertLicenseLeave(licenseLeave)
                }
                null -> {
                    error = "Seleccionar un tipo"
                }
            }
            resetForm()
        }
    }

    // borrar leaveentity
    fun deleteLeave(leave: LeaveEntity) {
        viewModelScope.launch {
            when (leave) {
                is VacationLeaveEntity -> leaveRepository.deleteVacationLeave(leave)
                is MedicalLeaveEntity -> leaveRepository.deleteMedicalLeave(leave)
                is LicenseLeaveEntity -> leaveRepository.deleteLicenseLeave(leave)
            }
        }
    }

    // validar que los campos esten completos
    fun isFormValid(): Boolean {
        if (employeeName.isBlank()) {
            return false
        }

        if (startDate.isBlank()) {
            return false
        }

       if (endDate.isBlank()) {
           return false
        }

        // Validacion del formato de fecha

        val dateFormat = SimpleDateFormat("dd-MM-yyy", Locale.getDefault())
        dateFormat.isLenient = false

        try {
            val start = dateFormat.parse(startDate)
            val end = dateFormat.parse(endDate)

            // verificar que la fecha de fin es dps de la fecha de fin
            if (end != null && start != null && end.before(start)) {
                error = "La fecha de fin debe ser posterior a la fecha de inicio"
                return false
            }

            // verificar que la fecha de inicio no sea un fin de semana (solo se avisa)
            if (start != null && isWeekend(start)) {
                warning = "La fecha de inicio cae en fin de semana"
            }

            // validaciones para licencetypes
            when (selectedLeaveType) {
                LeaveType.VACATION -> {
                    // validacion de cantidad de dias
                    if (start != null && end != null) {
                        val diffInMillis = end.time - start.time
                        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)
                        if (diffInDays > 30) {
                            error = "Las vacaciones no pueden exceder 30 días"
                            return false
                        }
                    }
                }
                LeaveType.MEDICAL -> {
                    // se requiere un tipo de licencia
                    if (documentUri == null) {

                        return false
                    }
                }
                LeaveType.LICENSE -> {
                    // se requiere un tipo de licencia
                    if (licenseSubtype == null) {
                        error = "Se requiere seleccionar un tipo de licencia"
                        return false
                    }

                    when (licenseSubtype) {
                        LicenseSubtype.EXAMEN -> {
                            // validacion de fecha de examen
                            if (start != null && end != null) {
                                val diffInMillis = end.time - start.time
                                val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)
                                if (diffInDays > 2) {
                                    warning = "Los permisos para examen suelen ser por 1-2 días"
                                }
                            }
                        }
                        LicenseSubtype.LUTO -> {
                            if (deathRelationship == null) {
                                error = "Se requiere seleccionar el parentesco"
                                return false
                            }
                        }
                        LicenseSubtype.NACIMIENTO -> {
                            if (gender == null) {
                                error = "Se requiere seleccionar el género"
                                return false
                            }
                        }
                        LicenseSubtype.MATRIMONIO -> {
                        }
                        null -> {
                            error = "Se requiere seleccionar un tipo de licencia"
                            return false
                        }
                    }
                }
                LeaveType.MARRIAGE -> {
                    // validacion de genero
                }
                LeaveType.BIRTH -> {
                    if (gender == null) {
                        error = "Se requiere seleccionar el género"
                        return false
                    }
                }
                LeaveType.DEATH -> {
                    if (deathRelationship == null) {
                        error = "Se requiere seleccionar el parentesco"
                        return false
                    }
                }
                LeaveType.PREGNANCY -> {

                    gender = Gender.FEMENINO
                }
                LeaveType.PATERNITY -> {

                    gender = Gender.MASCULINO
                }
                null -> {
                    error = "Se requiere seleccionar un tipo de permiso"
                    return false
                }
            }

            return true
        } catch (e: ParseException) {
            return true
        }
    }

    // verificar si es fin de semana
    private fun isWeekend(date: Date): Boolean {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
    }

    // calcular las fechas de fin
    fun calculateLicenseEndDate(): String {
        if (startDate.isBlank()) return ""

        try {
            val dateFormat = SimpleDateFormat("dd-MM-yyy", Locale.getDefault())
            val start = dateFormat.parse(startDate) ?: return ""

            val calendar = Calendar.getInstance()
            calendar.time = start

            when (licenseSubtype) {
                LicenseSubtype.MATRIMONIO -> {
                    // matrimonio: 5
                    calendar.add(Calendar.DAY_OF_MONTH, 5)
                }
                LicenseSubtype.LUTO -> {
                    // luto
                    when (deathRelationship) {
                        DeathRelationship.PADRES, DeathRelationship.HIJOS, DeathRelationship.HERMANOS -> {
                            // padres, hijos, hermanos: 5
                            calendar.add(Calendar.DAY_OF_MONTH, 5)
                        }
                        DeathRelationship.ABUELOS -> {
                            // abuelos: 3
                            calendar.add(Calendar.DAY_OF_MONTH, 3)
                        }
                        DeathRelationship.OTROS -> {
                            // otros: 2
                            calendar.add(Calendar.DAY_OF_MONTH, 2)
                        }
                        null -> return ""
                    }
                }
                LicenseSubtype.NACIMIENTO -> {
                    // Nacimiento
                    when (gender) {
                        Gender.MASCULINO -> {
                            // Paternidad: 14 dias
                            calendar.add(Calendar.DAY_OF_MONTH, 14)
                        }
                        Gender.FEMENINO -> {
                            // Maternidad: 150 dias
                            calendar.add(Calendar.DAY_OF_MONTH, 150)
                        }
                        null -> return ""
                    }
                }
                LicenseSubtype.EXAMEN -> {

                    return ""
                }
                null -> return ""
            }

            return dateFormat.format(calendar.time)
        } catch (e: Exception) {
            return ""
        }
    }

    // actualizar las fechas de fin en licencetypes
    fun updateEndDateBasedOnLicenseType() {
        val calculatedEndDate = calculateLicenseEndDate()
        if (calculatedEndDate.isNotBlank()) {
            endDate = calculatedEndDate
        }
    }
}