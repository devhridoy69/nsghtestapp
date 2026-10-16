package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DiagnosticTest

@Entity(tableName = "diagnostic_tests")
data class TestEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val price: Double
) {
    fun toDomain(): DiagnosticTest = DiagnosticTest(
        id = id,
        name = name,
        price = price
    )

    companion object {
        fun fromDomain(test: DiagnosticTest): TestEntity = TestEntity(
            id = test.effectiveId,
            name = test.name,
            price = test.price
        )
    }
}
