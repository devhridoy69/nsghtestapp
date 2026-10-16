package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.DiagnosticTest
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
        const val PREF_LAST_SYNC_TIME = "last_sync_time"

        const val DEFAULT_URL = "https://lrhlmalvppklxandtwhk.supabase.co"
        const val DEFAULT_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxyaGxtYWx2cHBrbHhhbmR0d2hrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTE1NjU0ODMsImV4cCI6MjEwNzE0MTQ4M30.TNVfzGHEwh20sJtLJMl2O1Dl4R8rDpLhV5isMUX3Kmk"
        const val DEFAULT_TABLE = "tests"
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

    fun getLastSyncTime(): Long = prefs.getLong(PREF_LAST_SYNC_TIME, 0L)

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
                        401, 403 -> "Authentication failed (HTTP $code). Please verify your Supabase anon API Key and Row Level Security (RLS) policies."
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

    private fun parseNameAndPriceJson(jsonString: String): List<DiagnosticTest> {
        val list = mutableListOf<DiagnosticTest>()
        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)

            val name = optStringAny(obj, "name", "test_name", "title", "test") ?: "Unnamed Test"
            val price = optDoubleAny(obj, "price", "test_price", "rate", "cost", "amount")
            val id = optStringAny(obj, "id", "test_id") ?: "test_${name.lowercase().trim().replace(Regex("[^a-z0-9]"), "_")}"

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
                val str = obj.optString(key, "").trim()
                if (str.isNotBlank()) return str
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
