package com.example.a214179_nabiha_sirnelson_project2

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.navigation.NavController
import android.widget.Toast
import java.util.Locale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import java.util.Date
//import androidx.compose.ui.text.intl.Locale
import com.google.android.gms.location.LocationServices
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import kotlinx.coroutines.*
import com.google.android.gms.location.FusedLocationProviderClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.compose.material3.CheckboxDefaults.colors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.isSystemInDarkTheme
import kotlinx.coroutines.delay
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import android.content.pm.PackageManager
import com.example.a214179_nabiha_sirnelson_project2.ui.theme.A214179_Nabiha_SirNelson_Project2Theme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalConfiguration
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow

sealed class Screen(val route: String) {
    object Name : Screen("name")
    object Water : Screen("water")
    object Settings : Screen("settings")
    object Profile : Screen("profile")
    object Summary : Screen("summary")

    object Weather : Screen("weather")

    object Achievement : Screen("achievement")
}

fun NavController.safeNavigate(route: String) {
    this.navigate(route) {
        launchSingleTop = true
        restoreState = true
    }
}

val ScreenPadding = Modifier
    .fillMaxSize()


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val auth = FirebaseAuth.getInstance()
            var darkMode by rememberSaveable { mutableStateOf(false) }
            //var userName by remember { mutableStateOf("") }

            val context = LocalContext.current
            val dao = DatabaseProvider.getDatabase(context).waterDao()
            val firestore = FirebaseFirestore.getInstance()

            val viewModel: WaterViewModel = viewModel(
                factory = WaterViewModelFactory(context, firestore, auth, dao)
            )
            val profile = viewModel.userProfile.value


            A214179_Nabiha_SirNelson_Project2Theme(darkTheme = darkMode) {

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination =
                        if (profile.name.isBlank()) Screen.Name.route else Screen.Water.route
                ) {

                    composable(Screen.Name.route) {
                        NameScreen { name ->
                            viewModel.userProfile.value =
                                viewModel.userProfile.value.copy(name = name)

                            navController.navigate(Screen.Water.route) {
                                popUpTo(Screen.Name.route) { inclusive = true }
                            }
                        }
                    }

                    composable(Screen.Water.route) {
                        WaterScreen(
                            viewModel = viewModel,
                            userName = profile.name,
                            darkMode = darkMode,
                            onToggleTheme = { darkMode = !darkMode },
                            onSettings = { navController.safeNavigate(Screen.Settings.route) },
                            onGoWeather = { navController.safeNavigate(Screen.Weather.route) }
                        )
                    }

                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            darkMode = darkMode,
                            onToggleTheme = { darkMode = !darkMode },
                            onBack = { navController.popBackStack() },
                            onGoProfile = { navController.safeNavigate(Screen.Profile.route) },
                            onGoSummary = { navController.safeNavigate(Screen.Summary.route) },
                            onGoAchievement = { navController.safeNavigate(Screen.Achievement.route) },
                            onGoWeather = { navController.safeNavigate(Screen.Weather.route) },
                            age = profile.age,
                            weight = profile.weight,
                            onSaveProfile = { a, w ->
                                viewModel.updateProfile(a, w)
                            }
                        )
                    }

                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.Summary.route) {
                        SummaryScreen(
                            viewModel = viewModel,
                            onBack = {
                                navController.navigate(Screen.Water.route) {
                                    popUpTo(Screen.Water.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable(Screen.Weather.route) {
                        WeatherScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.Achievement.route) {
                        AchievementScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterScreen(
    viewModel: WaterViewModel,
    userName: String,
    darkMode: Boolean,
    onToggleTheme: () -> Unit,
    onSettings: () -> Unit,
    onGoWeather: () -> Unit,
) {

    val context = LocalContext.current
    // val viewModel: WaterViewModel = viewModel()
    val profile = viewModel.userProfile.value

    val weightValue = profile.weight.toFloatOrNull() ?: 0f

    val targetIntake = viewModel.calculateTargetIntake()
    var showCelebration by remember { mutableStateOf(false) }
    var alreadyCelebrated by remember { mutableStateOf(false) }

    val waterLog by viewModel.waterLog.collectAsStateWithLifecycle()

    val showDialog = viewModel.showDialog.value
    val uid by viewModel.uid.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(true) }

    val currentIntake = waterLog.sumOf { it.amount }
    val remaining = (targetIntake - currentIntake).coerceAtLeast(0)

    LazyColumn {
        items(waterLog) { item ->
            Text("${item.label} - ${item.amount}")
        }
    }


    val animatedProgress by animateFloatAsState(
        targetValue =
            if (targetIntake > 0)
                currentIntake.toFloat() / targetIntake.toFloat()
            else 0f,
        animationSpec = tween(800),
        label = "progress"
    )

    val animatedRemaining by animateFloatAsState(
        targetValue = remaining.toFloat(),
        animationSpec = tween(800),
        label = "remaining"
    )

    LaunchedEffect(currentIntake) {
        if (currentIntake >= targetIntake && !alreadyCelebrated) {
            showCelebration = true
            alreadyCelebrated = true
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(if (userName.isNotBlank()) "$userName's HydroBuddy" else "HydroBuddy")
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = null)
                    }
                }
            )
        }

    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp)
            ) {
                WaterFillCircle(animatedProgress)

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(130.dp)
                ) {
                    Text(
                        text = "${animatedRemaining.toInt()} mL\nLeft",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onGoWeather,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Weather & Location")
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {

                    Text(
                        "Activity Log",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedVisibility(visible = expanded) {
                        Column {
                            waterLog.forEach {
                                RecordRowUnique(it.time, it.label, "${it.amount} mL")
                                Divider()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.showDialog.value = true },
                        enabled = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add Water")
                    }

                }
            }
        }

        if (showCelebration) {
            CelebrationOverlay { showCelebration = false }
        }
    }

    // ✅ DIALOG FIXED
    if (viewModel.showDialog.value) {

        val focusRequester = remember { FocusRequester() }

        AlertDialog(
            onDismissRequest = {
                viewModel.showDialog.value = false
            },
            title = { Text("Add Water") },
            text = {
                Column {

                    OutlinedTextField(
                        value = viewModel.inputText.value,
                        onValueChange = { viewModel.inputText.value = it },
                        label = { Text("Amount (mL)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = viewModel.inputLabel.value,
                        onValueChange = { viewModel.inputLabel.value = it },
                        label = { Text("Label") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addWater()
                    Toast.makeText(context, "Added", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.showDialog.value = false
                }) {
                    Text("Cancel")
                }
            }
        )

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    darkMode: Boolean,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    onGoProfile: () -> Unit,
    onGoSummary: () -> Unit,
    age: String,
    weight: String,
    onSaveProfile: (String, String) -> Unit,
    onGoWeather: () -> Unit,
    onGoAchievement: () -> Unit
) {

    var localAge by remember { mutableStateOf(age) }
    var localWeight by remember { mutableStateOf(weight) }
    val context = LocalContext.current

    val bg = MaterialTheme.colorScheme.background

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {

        // 🔹 TOP CONTENT (SCROLLABLE SAFE AREA)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            CenterAlignedTopAppBar(
                title = {
                    Text("Settings")
                }
            )

            Text(
                "Manage your profile & preferences",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp
            )

            // PROFILE CARD
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier.fillMaxWidth()
            ){
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text("Profile", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = localAge,
                        onValueChange = { localAge = it },
                        label = { Text("Age") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = localWeight,
                        onValueChange = { localWeight = it },
                        label = { Text("Weight") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            onSaveProfile(localAge, localWeight)
                            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Profile")
                    }
                }
            }

            // THEME CARD
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Dark Mode")

                    Switch(
                        checked = darkMode,
                        onCheckedChange = { onToggleTheme() }
                    )
                }
            }
        }

        // 🔹 BOTTOM FIXED BUTTONS (SAFE AREA)
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Button(
                onClick = onGoAchievement,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Achievements")
            }
            Button(
                onClick = onGoProfile,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Profile")
            }

            Button(
                onClick = onGoSummary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Summary")
            }

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Home")
            }
        }
    }
}
@Composable
fun NameScreen(onNext: (String) -> Unit) {

    val colors = OutlinedTextFieldDefaults.colors()
    var name by remember { mutableStateOf("") }
    val isDark =
        MaterialTheme.colorScheme.background.luminance() < 0.5f

    val background = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF0B1220),
                Color(0xFF111827),
                Color(0xFF0B1220)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFFEFF6FF),
                Color(0xFFF8FAFC)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {

        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    if (isDark)
                        Color.White.copy(alpha = 0.08f)
                    else
                        Color.White.copy(alpha = 0.85f)
            ),
            border = BorderStroke(
                1.dp,
                if (isDark)
                    Color.White.copy(alpha = 0.15f)
                else
                    Color.Black.copy(alpha = 0.08f)
            )
        ) {

            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                val titleColor =
                    if (isDark) Color.White else Color(0xFF0F172A)

                val subtitleColor =
                    if (isDark)
                        Color.White.copy(alpha = 0.7f)
                    else
                        Color(0xFF334155)

                Text(
                    "HydroBuddy",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = titleColor
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    "Track your daily hydration easily",
                    fontSize = 14.sp,
                    color = subtitleColor
                )

                Spacer(modifier = Modifier.height(28.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Your name") },
                    shape = RoundedCornerShape(18.dp),

                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = Color(0xFF38BDF8),

                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),

                        focusedLabelColor = Color(0xFF38BDF8),
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),

                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { onNext(name) },
                    enabled = name.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Start Tracking")
                }
            }
        }
    }
}
@Composable
fun SummaryScreen(viewModel: WaterViewModel, onBack: () -> Unit) {

    val waterLog by viewModel.waterLog.collectAsStateWithLifecycle()
    val target = viewModel.calculateTargetIntake()

    val total = waterLog.sumOf { it.amount ?: 0 }
    val remaining = (target - total).coerceAtLeast(0)

    val progress = if (target > 0) total.toFloat() / target.toFloat() else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // 🔙 HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    "Hydration Summary",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🌊 MAIN GLASS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {

                Column(modifier = Modifier.padding(20.dp)) {

                    Text(
                        "Today Progress",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = progress.coerceIn(0f, 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50)),
                        color = Color(0xFF38BDF8),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "${(progress * 100).toInt()}% completed",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 📊 STATS ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Goal",
                    value = "$target mL"
                )

                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Intake",
                    value = "$total mL"
                )

                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Left",
                    value = "$remaining mL"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 📜 HISTORY HEADER
            Text(
                "Recent Intake",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 📜 HISTORY LIST
            waterLog.reversed().forEach {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column {

                            Text(
                                it.label,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                it.time,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            "+${it.amount} mL",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Back to Home")
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: WaterViewModel,
    onBack: () -> Unit
) {

    val profile = viewModel.userProfile.value
    val target = viewModel.calculateTargetIntake()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {

            // TOP BAR
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Profile",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(12.dp))

                // PROFILE AVATAR
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(241378),
                                    Color(0xFF0EA5E9)
                                )
                            ),
                            shape = RoundedCornerShape(100.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = profile.name.take(1).uppercase(),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    profile.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    "HydroBuddy User",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(28.dp))

                // MAIN CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(10.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {

                        ModernProfileRow(
                            title = "Age",
                            value = profile.age
                        )

                        ModernProfileRow(
                            title = "Weight",
                            value = "${profile.weight} kg"
                        )

                        ModernProfileRow(
                            title = "Daily Goal",
                            value = "$target mL"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // GOAL CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    )
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth() // ✅ IMPORTANT
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF38BDF8),
                                        Color(0xFF0EA5E9)
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {

                        Column {

                            Text(
                                "Hydration Goal",
                                color = Color.White.copy(alpha = 0.85f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "$target mL Daily",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Back")
                }

            }
        }
    }
}

@Composable
fun ModernProfileRow(
    title: String,
    value: String
) {

    Surface(
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                title,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                value,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
@Composable
fun ProfileRow(label: String, value: String) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp
        )

        Text(
            value,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
@Composable
fun GradientHalfCircle(progress: Float) {

    val colorScheme = MaterialTheme.colorScheme  // ✅ MOVE OUTSIDE CANVAS

    val animatedSweep by animateFloatAsState(
        targetValue = 180f * progress,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "arcSweep"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val gradient = Brush.sweepGradient(
        listOf(
            colorScheme.primary,
            colorScheme.secondary,
            colorScheme.primary
        )
    )

    Canvas(modifier = Modifier.size(200.dp)) {

        val stroke = Stroke(
            width = 14.dp.toPx(),
            cap = StrokeCap.Round
        )

        val padding = 20.dp.toPx()

        val arcSize = Size(
            width = size.width - padding * 2,
            height = size.height - padding * 2
        )

        val topLeft = Offset(padding, padding)

        // Background ring (full circle base)
        drawArc(
            color = colorScheme.secondaryContainer,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke
        )

        // Progress arc
        drawArc(
            brush = gradient,
            startAngle = -90f, // 🔥 start from top (important for "circle feel")
            sweepAngle = 360f * (animatedSweep / 180f),
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke
        )
    }
}
@Composable
fun WeatherScreen(onBack: () -> Unit) {

    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()

    var locationText by remember { mutableStateOf("Loading location...") }
    var weatherText by remember { mutableStateOf("Loading weather...") }

    val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    LaunchedEffect(Unit) {

        val permissionGranted =
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->

                if (location != null) {

                    val city = getCityName(
                        context,
                        location.latitude,
                        location.longitude
                    )

                    locationText = city

                    CoroutineScope(Dispatchers.Main).launch {
                        weatherText = getWeather(city.split(",")[0])
                    }

                } else {
                    locationText = "Location unavailable"
                }
            }

        } else {
            locationText = "Permission not granted"
        }
    }

    // 🌌 MUCH PRETTIER BACKGROUND
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(20.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Weather",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 🌫 LOCATION CARD (GLASS STYLE)
            GlassCard {

                WeatherCardContent(
                    title = "Location",
                    value = locationText
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🌤 WEATHER CARD (ACCENT GRADIENT)
            GradientCard {

                WeatherCardContent(
                    title = "🌦 Weather",
                    value = weatherText
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 💡 TIP CARD (SOFT GLASS)
            GlassCard {

                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        "💡 Hydration Tip",
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        "Drink more water when temperature is above 30°C ️",
                        color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Back")
            }
        }
    }
}
@Composable
fun GlassCard(content: @Composable () -> Unit) {

    val isDark = isSystemInDarkTheme()

    val bgColor = if (isDark) {
        // dark glass tint
        Color(0xFF0B1220).copy(alpha = 0.75f)
    } else {
        // light glass tint (NOT white!)
        Color(0xFFEFF6FF).copy(alpha = 0.85f)
    }

    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.Black.copy(alpha = 0.06f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(24.dp))
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        content()
    }
}
@Composable
fun GradientCard(content: @Composable () -> Unit) {

    val isDark = isSystemInDarkTheme()

    val gradient = if (isDark) {
        Brush.horizontalGradient(
            listOf(
                Color(0xFF1E3A8A),
                Color(0xFF1D4ED8),
                Color(0xFF2563EB)
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                Color(0xFF38BDF8),
                Color(0xFF0EA5E9),
                Color(0xFF2563EB)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        content()
    }
}
@Composable
fun WeatherCardContent(title: String, value: String) {

    val isDark = isSystemInDarkTheme()

    val titleColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val valueColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Text(title, color = titleColor, fontSize = 14.sp)

        Spacer(Modifier.height(8.dp))

        Text(
            value,
            color = valueColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


data class AchievementState(
    val monthLabel: String,
    val completedDays: Int,
    val totalWater: Int,
    val target: Int,
    val groupedByDate: Map<String, List<WaterEntry>>
)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AchievementScreen(
    viewModel: WaterViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.getAchievementState()
        .collectAsStateWithLifecycle(
            initialValue = AchievementState(
                monthLabel = "",
                completedDays = 0,
                totalWater = 0,
                target = 0,
                groupedByDate = emptyMap()
            )
        )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Achievements",
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SUMMARY CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {

            Column(modifier = Modifier.padding(24.dp)) {

                Text(
                    "🏆 ${state.monthLabel}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "${state.completedDays}",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text("Completed Days")

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem("Total", "${state.totalWater}mL")
                    StatItem("Goal", "${state.target}mL")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Badges", color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold)



        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.completedDays >= 1) AchievementBadge("🥉 First Goal")
            if (state.completedDays >= 7) AchievementBadge("🥈 7 Day Streak")
            if (state.completedDays >= 30) AchievementBadge("🥇 Hydration Master")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("History", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(10.dp))

        state.groupedByDate.entries
            .sortedByDescending { it.key }
            .forEach { day ->

                val achieved = day.value.sumOf { it.amount } >= state.target

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(day.key)

                        Text(
                            if (achieved) "🏆 Achieved" else "❌ Not Met",
                            color = if (achieved)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

        Spacer(modifier = Modifier.height(20.dp))

        // ✅ SINGLE CLEAN SHARE BUTTON
        Button(
            onClick = {
                val shareText = viewModel.buildShareText(state)

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }

                context.startActivity(
                    Intent.createChooser(intent, "Share Achievement")
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Share Achievement")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}
@Composable
fun StatItem(
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            value,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Text(
            title,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
@Composable
fun AchievementBadge(text: String) {

    val container = MaterialTheme.colorScheme.primaryContainer
    val content = MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = Modifier
            .background(container, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = content,
            fontWeight = FontWeight.Medium
        )
    }
}

data class Bubble(
    val x: Float,
    val radius: Float,
    val speed: Float,
    val phase: Float
)

@Composable
fun WaterFillCircle(progress: Float) {

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000),
        label = "progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "wave")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing)
        ),
        label = "phase"
    )

    val waterBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF6EE7FF),
            Color(0xFF00B8D4),
            Color(0xFF005F73)
        )
    )

    // 🫧 BUBBLES (FIXED)
    val bubbles = remember {
        List(14) {
            Bubble(
                x = (0..220).random().toFloat(),
                radius = (3..8).random().toFloat(),
                speed = (20..60).random() / 100f,
                phase = (0..100).random().toFloat()
            )
        }
    }
    val colorScheme = MaterialTheme.colorScheme

    Canvas(modifier = Modifier.size(220.dp)) {

        val time = phase * 1000
        val radius = size.minDimension / 2f
        val centerY = size.height * (1f - animatedProgress)

        // outer ring
        drawCircle(
            color = colorScheme.primary.copy(alpha = 0.35f),
            radius = radius,
            style = Stroke(width = 8f)
        )

        val circlePath = Path().apply {
            addOval(Rect(0f, 0f, size.width, size.height))
        }

        val wavePath = Path().apply {
            moveTo(0f, size.height)

            for (x in 0..size.width.toInt() step 6) {

                val angle = (x * 0.035f) + phase

                val wave =
                    kotlin.math.sin(angle) * 10f +
                            kotlin.math.sin(angle * 1.7f) * 4f

                val y = centerY + wave

                lineTo(x.toFloat(), y)
            }

            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        clipPath(circlePath) {

            // 🫧 BUBBLES
            bubbles.forEach { bubble ->

                val animatedY =
                    (size.height - (time * bubble.speed + bubble.phase * 50f)) % size.height

                val wobbleX =
                    bubble.x + kotlin.math.sin((time * 0.002f) + bubble.phase) * 6f

                if (animatedY < centerY) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.25f),
                        radius = bubble.radius,
                        center = androidx.compose.ui.geometry.Offset(
                            wobbleX,
                            animatedY
                        )
                    )
                }
            }

            // water fill
            drawPath(
                path = wavePath,
                brush = waterBrush
            )

            // soft shine
            drawRect(
                color = Color.White.copy(alpha = 0.06f)
            )
        }
    }
}

@Composable
fun CelebrationOverlay(onFinish: () -> Unit) {

    LaunchedEffect(Unit) {
        delay(2000)
        onFinish()
    }

    val scale by animateFloatAsState( //bila air dah habis minom
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F1A)),
        contentAlignment = Alignment.Center
    ) {

        // 🌫 iOS-style soft blur glow background
        Canvas(modifier = Modifier.fillMaxSize()) {

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    radius = 800f
                ),
                center = Offset(size.width * 0.35f, size.height * 0.3f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    radius = 700f
                ),
                center = Offset(size.width * 0.7f, size.height * 0.65f)
            )
        }

        // 🧊 iOS GLASS CARD
        Column(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
                .background(
                    color = Color.White.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(32.dp)
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.12f),
                    RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 30.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🍏 subtle success ring (no emoji vibe)
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.20f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    "✓",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                "Goal Completed",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                "Daily hydration target reached",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 🧿 iOS style pill badge
            Box(
                modifier = Modifier
                    .background(
                        Color.White.copy(alpha = 0.08f),
                        RoundedCornerShape(50)
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.10f),
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    "COMPLETED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
fun RecordRowUnique(time: String, label: String, amount: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(label, fontWeight = FontWeight.Bold)
            Text(time, fontSize = 12.sp)
        }
        Text(amount, fontWeight = FontWeight.Bold)
    }
}