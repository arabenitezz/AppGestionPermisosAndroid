package com.example.fluithkotlin.model

enum class LeaveType(val displayName: String) {
    VACATION("Vacaciones"),
    MEDICAL("Reposo medico"),
    LICENSE("Licencia"),
    MARRIAGE("Matrimonio"),
    BIRTH("Nacimiento"),
    DEATH("Luto"),
    PREGNANCY("Maternidad"),
    PATERNITY("Paternidad")
}

enum class LicenseSubtype {
    EXAMEN,
    MATRIMONIO,
    LUTO,
    NACIMIENTO
}

enum class DeathRelationship {
    PADRES,
    HIJOS,
    HERMANOS,
    ABUELOS,
    OTROS
}

enum class Gender {
    MASCULINO,
    FEMENINO
}