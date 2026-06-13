package com.example.a214179_nabiha_sirnelson_project2

import androidx.room.*

@Dao
interface WaterDao {

    @Query("SELECT * FROM water_entries WHERE userId = :uid ORDER BY id DESC")
    fun getAll(uid: String): kotlinx.coroutines.flow.Flow<List<WaterEntry>>

    // 🔥 for TODAY screen
    @Query("SELECT * FROM water_entries WHERE userId = :uid AND date = :date ORDER BY id DESC")
    fun getByDate(uid: String, date: String): kotlinx.coroutines.flow.Flow<List<WaterEntry>>

    // 🔥 for MONTH (ACHIEVEMENTS)
    @Query("SELECT * FROM water_entries WHERE userId = :uid AND date LIKE :month || '%'")
    fun getByMonth(uid: String, month: String): kotlinx.coroutines.flow.Flow<List<WaterEntry>>

    @Delete
    suspend fun delete(entry: WaterEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WaterEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<WaterEntry>)


}