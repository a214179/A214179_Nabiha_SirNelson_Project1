package com.example.a214179_nabiha_sirnelson_project2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_entries")
data class WaterEntry(

    @PrimaryKey
    val id: String = "",
    val userId: String = "",
    val date: String = "",
    val time: String = "",
    val label: String = "",
    val amount: Int = 0
)