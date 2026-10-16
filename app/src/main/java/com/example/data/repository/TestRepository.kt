package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.DefaultTestData
import com.example.data.local.TestEntity
import com.example.data.model.DiagnosticTest
import com.example.data.remote.SupabaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

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

    suspend fun initializeDatabase() = withContext(Dispatchers.IO) {
        val count = testDao.getTestCount()
        if (count == 0) {
            val entities = DefaultTestData.initialTests.map { TestEntity.fromDomain(it) }
            testDao.insertAll(entities)
            Log.d("TestRepository", "Seeded ${entities.size} initial tests into Room DB.")
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

        val result = supabaseService.fetchTests()
        result.fold(
            onSuccess = { remoteTests ->
                if (remoteTests.isNotEmpty()) {
                    val entities = remoteTests.map { TestEntity.fromDomain(it) }
                    testDao.deleteAll()
                    testDao.insertAll(entities)
                    _syncStatus.value = SyncStatus.Success(remoteTests.size, System.currentTimeMillis())
                    true
                } else {
                    _syncStatus.value = SyncStatus.Info("Connected to Supabase, but the table returned 0 rows.")
                    true
                }
            },
            onFailure = { error ->
                _syncStatus.value = SyncStatus.Error(error.localizedMessage ?: "Sync failed")
                false
            }
        )
    }

    suspend fun resetToDefaultCatalog() = withContext(Dispatchers.IO) {
        testDao.deleteAll()
        val entities = DefaultTestData.initialTests.map { TestEntity.fromDomain(it) }
        testDao.insertAll(entities)
        _syncStatus.value = SyncStatus.Success(entities.size, System.currentTimeMillis())
    }

    fun clearSyncStatus() {
        _syncStatus.value = SyncStatus.Idle
    }
}
