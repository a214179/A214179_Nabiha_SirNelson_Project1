package com.example.a214179_nabiha_sirnelson_project2

// ViewModel used to manage shared user profile and water intake across multiple screens

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import androidx.room.Room
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flowOf
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.collections.emptyList

object DatabaseProvider {

    @Volatile
    private var INSTANCE: WaterDatabase? = null

    fun getDatabase(context: Context): WaterDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                WaterDatabase::class.java,
                "water_db"
            ).build()

            INSTANCE = instance
            instance
        }
    }
}

data class UserProfile(
    var name: String = "",
    val age: String = "",
    val weight: String = ""
)


class WaterViewModel(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    dao1: WaterDao
) : ViewModel() {

    private val dao = DatabaseProvider.getDatabase(context).waterDao()

    private val _uid = MutableStateFlow<String?>(null)
    val uid: StateFlow<String?> = _uid

    private val _waterLog = MutableStateFlow<List<WaterEntry>>(emptyList())
    val waterLog: StateFlow<List<WaterEntry>> = _waterLog

    var userProfile = mutableStateOf(UserProfile("", "", ""))

    var showDialog = mutableStateOf(false)
    var inputText = mutableStateOf("")
    var inputLabel = mutableStateOf("")

    init {
        auth.signOut()

        auth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val uid = auth.currentUser?.uid
                _uid.value = uid

                println("NEW UID = $uid")

                if (uid != null) {
                    observeFirestore(uid)
                }
            }
        }
    }

    // =========================
    // FIRESTORE SINGLE SOURCE
    // =========================
    private fun observeFirestore(uid: String) {

        firestore.collection("users")
            .document(uid)
            .collection("waterLogs")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    println("ERROR: $error")
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(WaterEntry::class.java)?.copy(id = it.id)
                } ?: emptyList()

                _waterLog.value = list

                // sync to ROOM
                viewModelScope.launch {
                    dao.insertAll(list)
                }
                println("FIRESTORE SIZE = ${list.size}")
            }
    }

    // =========================
    // ADD WATER
    // =========================
    fun addWater() {

        val uid = _uid.value ?: return
        val amount = inputText.value.toIntOrNull() ?: return

        val entry = WaterEntry(
            id = System.currentTimeMillis().toString(),
            userId = uid,
            amount = amount,
            label = inputLabel.value.ifBlank { "Water" },
            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )

        firestore.collection("users")
            .document(uid)
            .collection("waterLogs")
            .document(entry.id)
            .set(entry)

        inputText.value = ""
        inputLabel.value = ""
        showDialog.value = false
    }

    // =========================
    // TARGET
    // =========================
    fun calculateTargetIntake(): Int {
        val weight = userProfile.value.weight.toFloatOrNull() ?: 0f
        return if (weight > 0) (weight * 30).toInt() else 2000
    }

    // =========================
    // PROFILE SAVE
    // =========================
    fun saveProfile(age: String, weight: String) {
        val uid = _uid.value ?: return

        firestore.collection("users")
            .document(uid)
            .set(mapOf("age" to age, "weight" to weight))
    }

    fun updateProfile(age: String, weight: String, name: String? = null) {
        userProfile.value = userProfile.value.copy(
            age = age,
            weight = weight,
            name = name ?: userProfile.value.name
        )
    }

    // =========================
    // ACHIEVEMENT FLOW (ROOM)
    // =========================
    fun getAchievementState(): Flow<AchievementState> {

        val uid = _uid.value ?: return flowOf(
            AchievementState("", 0, 0, 0, emptyMap())
        )

        val month = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

        return dao.getByMonth(uid, month).map { list ->

            val grouped = list.groupBy { it.date }
            val target = calculateTargetIntake()

            val completedDays = grouped.count {
                it.value.sumOf { w -> w.amount } >= target
            }

            AchievementState(
                monthLabel = month,
                completedDays = completedDays,
                totalWater = list.sumOf { it.amount },
                target = target,
                groupedByDate = grouped
            )
        }
    }

    fun buildShareText(state: AchievementState): String {
        return """
            🏆 HydroBuddy Achievement
            Completed Days: ${state.completedDays}
            Total Water: ${state.totalWater} mL
            Target: ${state.target} mL
        """.trimIndent()
    }
}