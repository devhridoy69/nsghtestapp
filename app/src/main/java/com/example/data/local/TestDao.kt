package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TestDao {

    @Query("SELECT * FROM diagnostic_tests ORDER BY name ASC")
    fun getAllTests(): Flow<List<TestEntity>>

    @Query("""
        SELECT * FROM diagnostic_tests 
        WHERE (:query IS NULL OR :query = '' OR name LIKE '%' || :query || '%')
        ORDER BY 
            CASE WHEN :sortBy = 'price_asc' THEN price END ASC,
            CASE WHEN :sortBy = 'price_desc' THEN price END DESC,
            name ASC
    """)
    fun searchTests(
        query: String?,
        sortBy: String?
    ): Flow<List<TestEntity>>

    @Query("SELECT * FROM diagnostic_tests WHERE id = :id LIMIT 1")
    suspend fun getTestById(id: String): TestEntity?

    @Query("SELECT COUNT(*) FROM diagnostic_tests")
    suspend fun getTestCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tests: List<TestEntity>)

    @Upsert
    suspend fun upsertTest(test: TestEntity)

    @Delete
    suspend fun deleteTest(test: TestEntity)

    @Query("DELETE FROM diagnostic_tests")
    suspend fun deleteAll()
}
