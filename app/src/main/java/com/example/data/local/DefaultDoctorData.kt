package com.example.data.local

import com.example.data.model.Doctor

object DefaultDoctorData {
    val initialDoctors = listOf(
        Doctor(
            id = "doc_1",
            name = "Prof. Dr. Md. Rafiqul Islam",
            speciality = "Medicine & Cardiology",
            degree = "MBBS, FCPS (Medicine), MD (Cardiology)",
            chamberDay = "Saturday to Thursday",
            chamberTime = "4:00 PM - 9:00 PM",
            roomNo = "201",
            chamberDay2 = "Friday",
            chamberTime2 = "9:00 AM - 1:00 PM",
            roomNo2 = "201"
        ),
        Doctor(
            id = "doc_2",
            name = "Dr. Farhana Yasmin",
            speciality = "Gynecology & Obstetrics",
            degree = "MBBS, DGO, MCPS, FCPS (Gynae)",
            chamberDay = "Saturday to Thursday",
            chamberTime = "5:00 PM - 8:30 PM",
            roomNo = "105"
        ),
        Doctor(
            id = "doc_3",
            name = "Dr. Kazi Mahbubur Rahman",
            speciality = "General & Laparoscopic Surgery",
            degree = "MBBS, BCS (Health), FCPS (Surgery)",
            chamberDay = "Sunday, Tuesday, Thursday",
            chamberTime = "5:00 PM - 8:00 PM",
            roomNo = "204"
        ),
        Doctor(
            id = "doc_4",
            name = "Dr. Md. Nazmul Huda",
            speciality = "Orthopedics & Trauma Surgery",
            degree = "MBBS, MS (Ortho), Fellow Arthroscopy (India)",
            chamberDay = "Everyday",
            chamberTime = "6:00 PM - 9:30 PM",
            roomNo = "108"
        ),
        Doctor(
            id = "doc_5",
            name = "Dr. Nusrat Jahan",
            speciality = "Child & Pediatrics Specialist",
            degree = "MBBS, DCH, MD (Pediatrics)",
            chamberDay = "Saturday to Thursday",
            chamberTime = "10:00 AM - 1:00 PM",
            roomNo = "102",
            chamberDay2 = "Everyday Evening",
            chamberTime2 = "5:00 PM - 8:00 PM",
            roomNo2 = "102"
        ),
        Doctor(
            id = "doc_6",
            name = "Dr. S. M. Tanvir Ahmed",
            speciality = "Ear, Nose & Throat (ENT)",
            degree = "MBBS, DLO, MS (ENT)",
            chamberDay = "Monday, Wednesday, Friday",
            chamberTime = "5:30 PM - 8:30 PM",
            roomNo = "206"
        )
    )
}
