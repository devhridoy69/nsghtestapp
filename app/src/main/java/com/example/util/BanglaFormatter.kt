package com.example.util

object BanglaFormatter {
    fun formatChamberDayToBangla(day: String): String {
        if (day.isBlank()) return ""
        var result = day.trim()

        val replacements = listOf(
            "Saturday to Thursday" to "শনিবার থেকে বৃহস্পতিবার",
            "Sunday to Thursday" to "রবিবার থেকে বৃহস্পতিবার",
            "Saturday to Wednesday" to "শনিবার থেকে বুধবার",
            "Friday and Saturday" to "শুক্রবার ও শনিবার",
            "Friday & Saturday" to "শুক্রবার ও শনিবার",
            "Sunday, Tuesday, Thursday" to "রবি, মঙ্গল ও বৃহস্পতিবার",
            "Monday, Wednesday, Friday" to "সোম, বুধ ও শুক্রবার",
            "Everyday Evening" to "প্রতিদিন সন্ধ্যা",
            "Everyday Morning" to "প্রতিদিন সকাল",
            "Everyday" to "প্রতিদিন",
            "Daily" to "প্রতিদিন",
            "Saturday" to "শনিবার",
            "Sunday" to "রবিবার",
            "Monday" to "সোমবার",
            "Tuesday" to "মঙ্গলবার",
            "Wednesday" to "বুধবার",
            "Thursday" to "বৃহস্পতিবার",
            "Friday" to "শুক্রবার",
            "Sat" to "শনিবার",
            "Sun" to "রবিবার",
            "Mon" to "সোমবার",
            "Tue" to "মঙ্গলবার",
            "Wed" to "বুধবার",
            "Thu" to "বৃহস্পতিবার",
            "Fri" to "শুক্রবার"
        )

        for ((en, bn) in replacements) {
            result = result.replace(Regex("(?i)\\b$en\\b"), bn)
        }
        return result
    }
}
