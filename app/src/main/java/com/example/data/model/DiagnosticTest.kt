package com.example.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class DiagnosticTest(
    val id: String = "",
    val name: String,
    val price: Double
) {
    val effectiveId: String
        get() = if (id.isNotBlank()) id else "test_${name.lowercase().trim().replace(Regex("[^a-z0-9]"), "_")}"
}
