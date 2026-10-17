package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Doctor

@Entity(tableName = "doctors")
data class DoctorEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val speciality: String,
    val degree: String,
    val chamberDay: String,
    val chamberTime: String,
    val roomNo: String,
    val chamberDay2: String = "",
    val chamberTime2: String = "",
    val roomNo2: String = ""
) {
    fun toDomain(): Doctor = Doctor(
        id = id,
        name = name,
        speciality = speciality,
        degree = degree,
        chamberDay = chamberDay,
        chamberTime = chamberTime,
        roomNo = roomNo,
        chamberDay2 = chamberDay2,
        chamberTime2 = chamberTime2,
        roomNo2 = roomNo2
    )

    companion object {
        fun fromDomain(doc: Doctor): DoctorEntity = DoctorEntity(
            id = doc.effectiveId,
            name = doc.name,
            speciality = doc.speciality,
            degree = doc.degree,
            chamberDay = doc.chamberDay,
            chamberTime = doc.chamberTime,
            roomNo = doc.roomNo,
            chamberDay2 = doc.chamberDay2,
            chamberTime2 = doc.chamberTime2,
            roomNo2 = doc.roomNo2
        )
    }
}
