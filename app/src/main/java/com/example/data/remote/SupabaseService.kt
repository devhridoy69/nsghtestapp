package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.DiagnosticTest
import com.example.data.model.Doctor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SupabaseService(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nsgh_supabase_prefs", Context.MODE_PRIVATE)

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    companion object {
        const val PREF_SUPABASE_URL = "supabase_url"
        const val PREF_SUPABASE_ANON_KEY = "supabase_anon_key"
        const val PREF_SUPABASE_TABLE = "supabase_table"
        const val PREF_DOCTOR_TABLE = "supabase_doctor_table"
        const val PREF_LAST_SYNC_TIME = "last_sync_time"
        const val PREF_SAVED_NOTICE = "saved_notice_text"

        const val DEFAULT_URL = "https://lrhlmalvppklxandtwhk.supabase.co"
        const val DEFAULT_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxyaGxtYWx2cHBrbHhhbmR0d2hrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTE1NjU0ODMsImV4cCI6MjEwNzE0MTQ4M30.TNVfzGHEwh20sJtLJMl2O1Dl4R8rDpLhV5isMUX3Kmk"
        const val DEFAULT_TABLE = "tests"
        const val DEFAULT_DOCTOR_TABLE = "doctors"
        const val DEFAULT_NOTICE = "হাসপাতাল কর্তৃপক্ষের সিদ্ধান্ত অনুযায়ী যেকোনো সময় টেস্টের মূল্য কম বা বেশি হতে পারে, যা তাৎক্ষণিকভাবে অ্যাপে আপডেট নাও হতে পারে। তাই চূড়ান্ত বিল সম্পর্কে নিশ্চিত হতে অনুগ্রহ করে আমাদের বিলিং সেকশনে যোগাযোগ করুন।"
    }

    fun getSupabaseUrl(): String {
        val saved = prefs.getString(PREF_SUPABASE_URL, "") ?: ""
        if (saved.isNotBlank()) return saved
        val buildUrl = try { BuildConfig.SUPABASE_URL } catch (_: Exception) { "" }
        if (buildUrl.isNotBlank() && !buildUrl.contains("your-project.supabase.co")) return buildUrl
        return DEFAULT_URL
    }

    fun getSupabaseKey(): String {
        val saved = prefs.getString(PREF_SUPABASE_ANON_KEY, "") ?: ""
        if (saved.isNotBlank()) return saved
        val buildKey = try { BuildConfig.SUPABASE_ANON_KEY } catch (_: Exception) { "" }
        if (buildKey.isNotBlank() && !buildKey.contains("your-anon-key")) return buildKey
        return DEFAULT_ANON_KEY
    }

    fun getSupabaseTable(): String {
        val saved = prefs.getString(PREF_SUPABASE_TABLE, "") ?: ""
        if (saved.isNotBlank()) return saved
        val buildTable = try { BuildConfig.SUPABASE_TABLE } catch (_: Exception) { "" }
        if (buildTable.isNotBlank()) return buildTable
        return DEFAULT_TABLE
    }

    fun getDoctorTable(): String {
        val saved = prefs.getString(PREF_DOCTOR_TABLE, "") ?: ""
        if (saved.isNotBlank()) return saved
        return DEFAULT_DOCTOR_TABLE
    }

    fun getLastSyncTime(): Long = prefs.getLong(PREF_LAST_SYNC_TIME, 0L)

    fun getSavedNotice(): String {
        val saved = prefs.getString(PREF_SAVED_NOTICE, DEFAULT_NOTICE) ?: DEFAULT_NOTICE
        if (saved.isBlank() || saved == "টেস্টের দাম যে কোনো সময় কমতে বা বাড়তে পারে। যে কোনো সমস্যায় 01711902062") {
            return DEFAULT_NOTICE
        }
        return saved
    }

    fun saveNotice(notice: String) {
        prefs.edit().putString(PREF_SAVED_NOTICE, notice.trim()).apply()
    }

    fun saveConfig(url: String, key: String, table: String) {
        prefs.edit()
            .putString(PREF_SUPABASE_URL, url.trim().trimEnd('/'))
            .putString(PREF_SUPABASE_ANON_KEY, key.trim())
            .putString(PREF_SUPABASE_TABLE, if (table.isBlank()) DEFAULT_TABLE else table.trim())
            .apply()
    }

    fun updateLastSyncTime() {
        prefs.edit().putLong(PREF_LAST_SYNC_TIME, System.currentTimeMillis()).apply()
    }

    suspend fun fetchNotice(): String = withContext(Dispatchers.IO) {
        val url = getSupabaseUrl()
        val key = getSupabaseKey()
        if (url.isBlank() || key.isBlank()) return@withContext getSavedNotice()

        val possibleTables = listOf("notices", "notice", "app_notice", "settings")
        for (tbl in possibleTables) {
            try {
                val request = Request.Builder()
                    .url("$url/rest/v1/$tbl?select=*")
                    .addHeader("apikey", key)
                    .addHeader("Authorization", "Bearer $key")
                    .addHeader("Accept", "application/json")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: ""
                        val arr = JSONArray(body)
                        if (arr.length() > 0) {
                            val noticesList = mutableListOf<String>()
                            for (i in 0 until arr.length()) {
                                val obj = arr.getJSONObject(i)
                                val text = optStringAny(
                                    obj,
                                    "notice_text",
                                    "notice",
                                    "text",
                                    "message",
                                    "title",
                                    "value",
                                    "content"
                                )
                                if (!text.isNullOrBlank()) {
                                    noticesList.add(text)
                                }
                            }
                            if (noticesList.isNotEmpty()) {
                                val combined = noticesList.joinToString("   •   ")
                                saveNotice(combined)
                                return@withContext combined
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Table might not exist yet, continue
            }
        }
        return@withContext getSavedNotice()
    }

    suspend fun fetchTests(): Result<List<DiagnosticTest>> = withContext(Dispatchers.IO) {
        val url = getSupabaseUrl()
        val key = getSupabaseKey()
        val table = getSupabaseTable()

        if (url.isBlank() || key.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Supabase Project URL and Anon API Key are required.")
            )
        }

        val requestUrl = "$url/rest/v1/$table?select=*"

        try {
            val request = Request.Builder()
                .url(requestUrl)
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .addHeader("Accept", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val code = response.code
                    val errorBody = response.body?.string() ?: ""
                    Log.e("SupabaseService", "HTTP $code: $errorBody")

                    val userMessage = when (code) {
                        401, 403 -> "Authentication failed (HTTP $code). Please verify your Supabase anon API Key."
                        404 -> "Table '$table' not found in Supabase (HTTP 404)."
                        else -> "Supabase connection error (HTTP $code): ${response.message}"
                    }
                    return@withContext Result.failure(Exception(userMessage))
                }

                val jsonString = response.body?.string() ?: "[]"
                val parsedTests = parseNameAndPriceJson(jsonString)

                updateLastSyncTime()
                Result.success(parsedTests)
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error syncing tests from Supabase", e)
            Result.failure(e)
        }
    }

    suspend fun fetchDoctors(): Result<List<Doctor>> = withContext(Dispatchers.IO) {
        val url = getSupabaseUrl()
        val key = getSupabaseKey()
        val table = getDoctorTable()

        if (url.isBlank() || key.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Supabase Project URL and Anon API Key are required.")
            )
        }

        val possibleTables = listOf(table, "doctors", "doctor_list", "hospital_doctors")
        for (tbl in possibleTables) {
            try {
                val requestUrl = "$url/rest/v1/$tbl?select=*"
                val request = Request.Builder()
                    .url(requestUrl)
                    .addHeader("apikey", key)
                    .addHeader("Authorization", "Bearer $key")
                    .addHeader("Accept", "application/json")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val jsonString = response.body?.string() ?: "[]"
                        val parsedDoctors = parseDoctorsJson(jsonString)
                        if (parsedDoctors.isNotEmpty()) {
                            return@withContext Result.success(parsedDoctors)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("SupabaseService", "Error trying table $tbl for doctors", e)
            }
        }
        Result.success(emptyList())
    }

    private fun parseDoctorsJson(jsonString: String): List<Doctor> {
        val list = mutableListOf<Doctor>()
        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)

            val name = optStringAny(obj, "name", "Name", "DoctorName", "doctor_name", "doctor") ?: continue
            val speciality = optStringAny(obj, "speciality", "Speciality", "specialty", "Specialty", "Department", "department", "designation") ?: ""
            val degree = optStringAny(obj, "degree", "Degree", "Qualification", "qualification", "degrees") ?: ""
            
            // Schedule 1
            val chamberDay = optStringAny(obj, "chamber_day", "chamberDay", "Chamber_Day", "ChamberDay", "day") ?: ""
            val chamberTime = optStringAny(obj, "chamber_time", "chamberTime", "Chamber_Time", "ChamberTime", "time", "visiting_hours", "visiting_time") ?: ""
            val roomNo = optStringAny(obj, "room_no", "roomNo", "Room_No", "RoomNo", "room", "Room", "chamber_no", "chamber") ?: ""
            
            // Schedule 2
            val chamberDay2 = optStringAny(obj, "chamber_day2", "chamberDay2", "Chamber_Day2", "ChamberDay2", "day2") ?: ""
            val chamberTime2 = optStringAny(obj, "chamber_time2", "chamberTime2", "Chamber_Time2", "ChamberTime2", "time2") ?: ""
            val roomNo2 = optStringAny(obj, "room_no2", "roomNo2", "Room_No2", "RoomNo2", "room2", "Room2") ?: ""

            val id = optStringAny(obj, "id", "doctor_id", "ID") ?: "doc_${name.lowercase().trim().replace(Regex("[^a-z0-9]"), "_")}"

            list.add(
                Doctor(
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
            )
        }
        return list
    }

    private fun parseNameAndPriceJson(jsonString: String): List<DiagnosticTest> {
        val list = mutableListOf<DiagnosticTest>()
        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)

            val name = optStringAny(obj, "name", "test_name", "title", "test", "Name") ?: "Unnamed Test"
            val price = optDoubleAny(obj, "price", "test_price", "rate", "cost", "amount", "Price")
            val id = optStringAny(obj, "id", "test_id", "ID") ?: "test_${name.lowercase().trim().replace(Regex("[^a-z0-9]"), "_")}"

            list.add(
                DiagnosticTest(
                    id = id,
                    name = name,
                    price = price
                )
            )
        }
        return list
    }

    private fun optStringAny(obj: JSONObject, vararg keys: String): String? {
        for (key in keys) {
            if (obj.has(key) && !obj.isNull(key)) {
                val str = obj.opt(key)?.toString()?.trim() ?: ""
                if (str.isNotBlank() && str != "null") return str
            }
        }
        return null
    }

    private fun optDoubleAny(obj: JSONObject, vararg keys: String): Double {
        for (key in keys) {
            if (obj.has(key) && !obj.isNull(key)) {
                val num = obj.optDouble(key, Double.NaN)
                if (!num.isNaN()) return num

                val rawStr = obj.optString(key, "")
                val clean = rawStr.replace(Regex("[^0-9.]"), "")
                val parsed = clean.toDoubleOrNull()
                if (parsed != null) return parsed
            }
        }
        return 0.0
    }
}
