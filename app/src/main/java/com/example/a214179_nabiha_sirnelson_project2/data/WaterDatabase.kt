package com.example.a214179_nabiha_sirnelson_project2

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WaterEntry::class],
    version = 1
)
abstract class WaterDatabase : RoomDatabase() {

    abstract fun waterDao(): WaterDao
}