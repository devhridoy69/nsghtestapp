package com.example.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class Doctor(
    val id: String = "",
    val name: String,
    val speciality: String = "",
    val degree: String = "",
    val chamberDay: String = "",
    val chamberTime: String = "",
    val roomNo: String = "",
    val chamberDay2: String = "",
    val chamberTime2: String = "",
    val roomNo2: String = ""
) {
    val effectiveId: String
        get() = if (id.isNotBlank()) id else "doc_${name.lowercase().trim().replace(Regex("[^a-z0-9]"), "_")}"
}
