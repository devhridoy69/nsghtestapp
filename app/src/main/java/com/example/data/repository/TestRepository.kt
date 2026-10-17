package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.DefaultDoctorData
import com.example.data.local.DefaultTestData
import com.example.data.local.DoctorEntity
import com.example.data.local.TestEntity
import com.example.data.model.DiagnosticTest
import com.example.data.model.Doctor
import com.example.data.remote.SupabaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

import androidx.compose.runtime.Immutable

@Immutable
sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    data class Success(val count: Int, val time: Long) : SyncStatus()
    data class Info(val message: String) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}

class TestRepository(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val testDao = database.testDao()
    private val doctorDao = database.doctorDao()
    val supabaseService = SupabaseService(context)

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus = _syncStatus.asStateFlow()

    fun getAllTests(
        query: String = "",
        sortBy: String = "name_asc"
    ): Flow<List<DiagnosticTest>> {
        return testDao.searchTests(query, sortBy).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getAllDoctors(): Flow<List<Doctor>> {
        return doctorDao.getAllDoctors().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun initializeDatabase() = withContext(Dispatchers.IO) {
        val testCount = testDao.getTestCount()
        if (testCount == 0) {
            val entities = DefaultTestData.initialTests.map { TestEntity.fromDomain(it) }
            testDao.insertAll(entities)
            Log.d("TestRepository", "Seeded ${entities.size} initial tests into Room DB.")
        }

        val doctorCount = doctorDao.getCount()
        if (doctorCount == 0) {
            val docEntities = DefaultDoctorData.initialDoctors.map { DoctorEntity.fromDomain(it) }
            doctorDao.insertAll(docEntities)
            Log.d("TestRepository", "Seeded ${docEntities.size} initial doctors into Room DB.")
        }
    }

    suspend fun syncFromRemote(isAutoSync: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        _syncStatus.value = SyncStatus.Syncing

        val url = supabaseService.getSupabaseUrl()
        val key = supabaseService.getSupabaseKey()

        if (url.isBlank() || key.isBlank()) {
            if (isAutoSync) {
                _syncStatus.value = SyncStatus.Info("Database ready. Configure Supabase credentials to sync with your cloud server.")
            } else {
                _syncStatus.value = SyncStatus.Info("Please enter your Supabase URL & Anon Key in Sync Settings.")
            }
            return@withContext false
        }

        // Sync tests
        val testResult = supabaseService.fetchTests()
        // Sync doctors
        val doctorResult = supabaseService.fetchDoctors()
        // Sync notice
        supabaseService.fetchNotice()

        var totalItems = 0
        var testSuccess = false

        testResult.fold(
            onSuccess = { remoteTests ->
                if (remoteTests.isNotEmpty()) {
                    val entities = remoteTests.map { TestEntity.fromDomain(it) }
                    testDao.deleteAll()
                    testDao.insertAll(entities)
                    totalItems += remoteTests.size
                }
                testSuccess = true
            },
            onFailure = { error ->
                Log.e("TestRepository", "Failed to sync tests: ${error.message}")
            }
        )

        doctorResult.fold(
            onSuccess = { remoteDoctors ->
                if (remoteDoctors.isNotEmpty()) {
                    val docEntities = remoteDoctors.map { DoctorEntity.fromDomain(it) }
                    doctorDao.clearAll()
                    doctorDao.insertAll(docEntities)
                    totalItems += remoteDoctors.size
                    Log.d("TestRepository", "Synced ${remoteDoctors.size} doctors from Supabase.")
                }
            },
            onFailure = { error ->
                Log.e("TestRepository", "Failed to sync doctors: ${error.message}")
            }
        )

        val testCount = testResult.getOrNull()?.size ?: 0
        val docCount = doctorResult.getOrNull()?.size ?: 0

        if (testSuccess) {
            if (docCount == 0 && testCount > 0) {
                _syncStatus.value = SyncStatus.Info(
                    "Synced $testCount tests. 'doctors' table returned 0 rows (Check RLS in Supabase)."
                )
            } else {
                _syncStatus.value = SyncStatus.Success(totalItems, System.currentTimeMillis())
            }
            true
        } else {
            val errorMsg = testResult.exceptionOrNull()?.localizedMessage ?: "Sync failed"
            _syncStatus.value = SyncStatus.Error(errorMsg)
            false
        }
    }

    suspend fun resetToDefaultCatalog() = withContext(Dispatchers.IO) {
        testDao.deleteAll()
        val entities = DefaultTestData.initialTests.map { TestEntity.fromDomain(it) }
        testDao.insertAll(entities)

        doctorDao.clearAll()
        val docEntities = DefaultDoctorData.initialDoctors.map { DoctorEntity.fromDomain(it) }
        doctorDao.insertAll(docEntities)

        _syncStatus.value = SyncStatus.Success(entities.size + docEntities.size, System.currentTimeMillis())
    }

    fun clearSyncStatus() {
        _syncStatus.value = SyncStatus.Idle
    }
}
