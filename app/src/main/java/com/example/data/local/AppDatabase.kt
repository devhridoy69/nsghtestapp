package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [TestEntity::class, DoctorEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun testDao(): TestDao
    abstract fun doctorDao(): DoctorDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nsgh_tests_database.db"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val dbInstance = getInstance(context)
                            val testDao = dbInstance.testDao()
                            val initialTests = DefaultTestData.initialTests.map {
                                TestEntity.fromDomain(it)
                            }
                            testDao.insertAll(initialTests)

                            val doctorDao = dbInstance.doctorDao()
                            val initialDoctors = DefaultDoctorData.initialDoctors.map {
                                DoctorEntity.fromDomain(it)
                            }
                            doctorDao.insertAll(initialDoctors)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
