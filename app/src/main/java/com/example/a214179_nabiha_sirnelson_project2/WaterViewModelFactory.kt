package com.example.a214179_nabiha_sirnelson_project2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import com.google.firebase.auth.FirebaseAuth

class WaterViewModelFactory(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val dao: WaterDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WaterViewModel(context, firestore, auth,dao) as T
    }
}