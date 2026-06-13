package com.example.a214179_nabiha_sirnelson_project2

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch

class WaterRepository(
    private val dao: WaterDao,
    private val firestore: FirebaseFirestore
) {

    // 🔥 ROOM → UI (single source of truth)
    fun observeWater(uid: String): Flow<List<WaterEntry>> {
        return dao.getAll(uid).map { list ->
            list.map {
                WaterEntry(
                    id = it.id,
                    userId = it.userId,
                    amount = it.amount,
                    label = it.label,
                    time = it.time,
                    date = it.date
                )
            }
        }
    }

    // 💧 ADD WATER (ROOM + FIRESTORE sync)
    suspend fun addWater(entry: WaterEntry) {

        // 1. SAVE LOCAL (instant UI)
        dao.insert(entry)

        // 2. SAVE CLOUD (Firestore)
        firestore.collection("waterLogs")
            .document(entry.id)
            .set(entry)
    }

    // ☁️ SYNC CLOUD → LOCAL
    fun syncFromFirestore(uid: String) {
        firestore.collection("waterLogs")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, _ ->

                snapshot?.documents?.forEach { doc ->
                    val data = doc.toObject(WaterEntry::class.java) ?: return@forEach

                    CoroutineScope(Dispatchers.IO).launch {
                        dao.insert(data)
                    }
                }
            }
    }
}