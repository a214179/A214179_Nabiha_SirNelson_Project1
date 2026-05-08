package com.example.a214179_nabiha_sirnelson_project1

// ViewModel used to manage shared user profile and water intake across multiple screens

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

data class UserProfile(
    var name: String = "",
    val age: String = "",
    val weight: String = ""
)

class WaterViewModel : ViewModel() {

    var userProfile = mutableStateOf(UserProfile("", "", ""))

    /*var waterLog = mutableStateOf(
        listOf(
            WaterEntry("08:15 AM", "Morning Hydration", 250),
            WaterEntry("10:30 AM", "Post-Workout", 500)
        )
    )*/

    var showDialog = mutableStateOf(false)
    var inputText = mutableStateOf("")
    var inputLabel = mutableStateOf("")

    var waterLog = mutableStateOf<List<WaterEntry>>(emptyList())


    // 💧 Target calculation (IMPORTANT FOR VIVA)
    fun calculateTargetIntake(): Int {
        val weightValue = userProfile.value.weight.toFloatOrNull() ?: 0f
        return if (weightValue > 0f) {
            (weightValue * 30).toInt()
        } else {
            2000
        }
    }

    // 👤 Update profile safely
    fun updateProfile(age: String, weight: String, name: String? = null) {
        userProfile.value = userProfile.value.copy(
            age = age,
            weight = weight,
            name = name ?: userProfile.value.name
        )
    }

    // ➕ Add water entry
    fun addWater() {
        val added = inputText.value.toIntOrNull() ?: 0

        if (added > 0) {
            val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

            waterLog.value = waterLog.value + WaterEntry(
                time,
                inputLabel.value.ifBlank { "Custom" },
                added
            )
        }

        inputText.value = ""
        inputLabel.value = ""
        showDialog.value = false
    }
}