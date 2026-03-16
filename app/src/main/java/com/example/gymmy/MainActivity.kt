package com.example.gymmy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gymmy.ui.theme.GYMMYTheme
import com.example.gymmy.ui.theme.GoldAccent
import com.example.gymmy.ui.theme.PrimaryRed
import com.example.gymmy.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * MainActivity is the entry point of the app. 
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GYMMYTheme {
                GYMMYApp()
            }
        }
    }
}

/**
 * The main container for the app's UI.
 */
@Composable
fun GYMMYApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var gymPlan by remember { mutableStateOf(initialGymPlan) }

    Scaffold(
        bottomBar = {
            // Only show bottom bar on non-record screens to match reference
            if (currentDestination != AppDestinations.RECORD) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            val barHeight = 90.dp.roundToPx()
                            layout(placeable.width, barHeight) {
                                placeable.place(0, barHeight - placeable.height)
                            }
                        },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        color = Color(0xFF0F0F12),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                    ) {
                        Column {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppDestinations.entries.forEach { destination ->
                                    if (destination == AppDestinations.RECORD) {
                                        Spacer(modifier = Modifier.size(80.dp))
                                    } else {
                                        NavigationItem(
                                            destination = destination,
                                            isSelected = currentDestination == destination,
                                            onClick = { currentDestination = destination }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                            .size(84.dp)
                    ) {
                        Surface(
                            onClick = { currentDestination = AppDestinations.RECORD },
                            color = PrimaryRed,
                            shape = CircleShape,
                            modifier = Modifier.fillMaxSize(),
                            shadowElevation = 8.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Surface(
                                    color = Color.Transparent,
                                    shape = CircleShape,
                                    border = BorderStroke(3.5.dp, Color.White),
                                    modifier = Modifier.size(34.dp)
                                ) {}
                                Surface(
                                    color = Color.White,
                                    shape = CircleShape,
                                    modifier = Modifier.size(12.dp)
                                ) {}
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(if (currentDestination == AppDestinations.RECORD) PaddingValues(0.dp) else innerPadding)
            .fillMaxSize()
            .background(Color(0xFF0F0F12))) {
            when (currentDestination) {
                AppDestinations.HOME -> HomeScreen(onStartWorkout = { currentDestination = AppDestinations.RECORD })
                AppDestinations.GYMS -> PlaceholderScreen("Gyms")
                AppDestinations.RECORD -> {
                    val currentDay = java.time.LocalDate.now().dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.US)
                    val todayWorkout = gymPlan.find { it.day.equals(currentDay, ignoreCase = true) }
                    RecordWorkoutScreen(
                        todayExercises = todayWorkout?.exercises ?: emptyList(),
                        onClose = { currentDestination = AppDestinations.HOME }
                    )
                }
                AppDestinations.PLAN -> GymPlanScreen(
                    gymPlan = gymPlan,
                    onUpdatePlan = { gymPlan = it }
                )
                AppDestinations.PROFILE -> PlaceholderScreen("Profile")
            }
        }
    }
}

@Composable
fun NavigationItem(destination: AppDestinations, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(64.dp)
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            if (isSelected) destination.filledIcon else destination.outlinedIcon,
            contentDescription = destination.label,
            tint = if (isSelected) PrimaryRed else TextSecondary,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            destination.label,
            fontSize = 10.sp,
            color = if (isSelected) PrimaryRed else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

enum class AppDestinations(val label: String, val outlinedIcon: ImageVector, val filledIcon: ImageVector) {
    HOME("Home", Icons.Outlined.Home, Icons.Default.Home),
    GYMS("Gyms", Icons.Outlined.PinDrop, Icons.Default.PinDrop),
    RECORD("Record", Icons.Default.Circle, Icons.Default.Circle),
    PLAN("Gym", Icons.Outlined.FitnessCenter, Icons.Default.FitnessCenter),
    PROFILE("Profile", Icons.Outlined.AccountCircle, Icons.Default.AccountCircle)
}

@Composable
fun HomeScreen(onStartWorkout: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { HomeTopBar() }
        item { StartWorkoutCard(onStartWorkout = onStartWorkout) }
        item { SectionHeader("Friends Activity") }
        items(activityFeed) { activity -> ActivityCard(activity) }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun HomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text("GYMMY", fontSize = 34.sp, fontWeight = FontWeight.Black, color = Color.White)
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Text("FitZone Gym", color = TextSecondary, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = GoldAccent,
                        shape = CircleShape,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("#3", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rank", color = GoldAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box {
                Icon(Icons.Outlined.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
                Surface(
                    color = PrimaryRed,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(9.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 2.dp)
                ) {}
            }
        }
    }
}

@Composable
fun StartWorkoutCard(onStartWorkout: () -> Unit) {
    Card(
        onClick = onStartWorkout,
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF24E4E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Start Workout", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Track your session now", fontSize = 16.sp, color = Color.White.copy(alpha = 0.9f))
            }
            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}

@Composable
fun ActivityCard(activity: ActivityItem) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color(0xFF2C2C2E))) {
                // Image placeholder
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(activity.userName, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(activity.status, fontSize = 14.sp, color = TextSecondary)
                }
                Text(activity.timestamp, fontSize = 13.sp, color = TextSecondary)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color(0xFF2C2C2E))
                ) {
                    // This would be the gym image
                }
                
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(activity.workoutTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(activity.duration, fontSize = 14.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.width(20.dp))
                        Text(activity.gymName, fontSize = 14.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}

data class WorkoutSet(val reps: Int, val weight: Float, val exerciseName: String = "")

@Composable
fun RecordWorkoutScreen(todayExercises: List<String>, onClose: () -> Unit) {
    var isRecording by rememberSaveable { mutableStateOf(false) }
    var secondsElapsed by remember { mutableLongStateOf(0L) }
    var startTimeStr by rememberSaveable { mutableStateOf("") }
    
    // In-memory sets for the current recording session
    var activeSets by remember { mutableStateOf(listOf<WorkoutSet>()) }
    var selectedExercise by remember { mutableStateOf(todayExercises.firstOrNull() ?: "") }

    // Timer logic
    LaunchedEffect(isRecording) {
        if (isRecording) {
            if (startTimeStr.isEmpty()) {
                startTimeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            }
            while (isRecording) {
                delay(1000)
                secondsElapsed++
            }
        }
    }

    val hours = secondsElapsed / 3600
    val minutes = (secondsElapsed % 3600) / 60
    val seconds = secondsElapsed % 60
    val timeFormatted = String.format(
        Locale.US,
        "%02d:%02d:%02d",
        hours,
        minutes,
        seconds
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF8B1E1E).copy(alpha = 0.28f),
                            Color.Black
                        ),
                        center = Offset(size.width / 2f, size.height * 0.35f),
                        radius = size.minDimension * 0.9f
                    )
                )
            }
            .statusBarsPadding()
    ) {
        // Top Bar - Simplified structure to ensure clickability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                "Workout Session",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
            
            Surface(
                onClick = onClose,
                color = Color.Transparent,
                modifier = Modifier.align(Alignment.CenterEnd).size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 100.dp, bottom = 200.dp)
        ) {
            item {
                // Location Tag
                Surface(
                    color = Color(0xFF1C1C1E),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.padding(bottom = 60.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FitZone Gym", color = TextSecondary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            item {
                Text(
                    if (isRecording) "TIME ELAPSED" else "READY TO START",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    timeFormatted,
                    color = if (isRecording) PrimaryRed else Color.White,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp,
                    maxLines = 1,
                    softWrap = false
                )
            }

            if (isRecording) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("SELECT EXERCISE", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        items(todayExercises) { exercise ->
                            val isSelected = selectedExercise == exercise
                            Surface(
                                onClick = { selectedExercise = exercise },
                                color = if (isSelected) PrimaryRed else Color(0xFF1C1C1E),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isSelected) PrimaryRed else Color.White.copy(alpha = 0.1f))
                            ) {
                                Text(
                                    text = exercise,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                itemsIndexed(activeSets) { index, set ->
                    SetEditorItem(
                        setIndex = index + 1,
                        set = set,
                        onUpdate = { updatedSet ->
                            val newList = activeSets.toMutableList()
                            newList[index] = updatedSet
                            activeSets = newList
                        },
                        onDelete = {
                            activeSets = activeSets.filterIndexed { i, _ -> i != index }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Button(
                        onClick = { 
                            if (selectedExercise.isNotEmpty()) {
                                activeSets = activeSets + WorkoutSet(10, 20f, selectedExercise)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1E)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(vertical = 16.dp),
                        enabled = selectedExercise.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Set for $selectedExercise", color = Color.White)
                    }
                }
            } else {
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
                // Main Play/Stop Button
                Surface(
                    onClick = { isRecording = !isRecording },
                    color = PrimaryRed,
                    shape = CircleShape,
                    modifier = Modifier.size(120.dp),
                    border = if (isRecording) BorderStroke(2.dp, Color.White) else null
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isRecording) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                            contentDescription = if (isRecording) "Stop" else "Start",
                            tint = Color.White,
                            modifier = Modifier.size(if (isRecording) 48.dp else 60.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    if (isRecording) "Tap to stop and save your workout" else "Tap to start tracking your workout",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }

        // Session Details Card at the bottom (only when not recording to keep it clean)
        if (!isRecording) {
            AnimatedVisibility(
                visible = startTimeStr.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Session Summary", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Started", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(startTimeStr, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sets Logged", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("${activeSets.size}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetEditorItem(
    setIndex: Int,
    set: WorkoutSet,
    onUpdate: (WorkoutSet) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${set.exerciseName} - SET $setIndex", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Delete set", tint = PrimaryRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("REPS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (set.reps > 0) onUpdate(set.copy(reps = set.reps - 1)) }) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Text("${set.reps}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                        IconButton(onClick = { onUpdate(set.copy(reps = set.reps + 1)) }) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("WEIGHT (KG)", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (set.weight >= 2.5f) onUpdate(set.copy(weight = set.weight - 2.5f)) }) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Text("${set.weight}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                        IconButton(onClick = { onUpdate(set.copy(weight = set.weight + 2.5f)) }) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

// --- Gym Plan Screen Models & Data ---

enum class WorkoutStatus { DONE, UPCOMING, REST }

data class WorkoutDay(
    val day: String,
    val focus: String,
    val status: WorkoutStatus,
    val exercises: List<String>
)

val initialGymPlan = listOf(
    WorkoutDay("Monday", "Chest & Triceps", WorkoutStatus.DONE, listOf("Bench Press", "Incline Dumbbell Press", "Cable Flyes", "Tricep Dips")),
    WorkoutDay("Tuesday", "Back & Biceps", WorkoutStatus.DONE, listOf("Deadlifts", "Pull-ups", "Barbell Rows", "Bicep Curls")),
    WorkoutDay("Wednesday", "Rest Day", WorkoutStatus.REST, emptyList()),
    WorkoutDay("Thursday", "Legs & Core", WorkoutStatus.UPCOMING, listOf("Squats", "Leg Press", "Calf Raises", "Plank")),
    WorkoutDay("Friday", "Shoulders & Arms", WorkoutStatus.UPCOMING, listOf("Military Press", "Lateral Raises", "Face Pulls", "Bicep Curls")),
    WorkoutDay("Saturday", "Full Body", WorkoutStatus.UPCOMING, listOf("Clean & Press", "Lunges", "Push-ups", "Burpees")),
    WorkoutDay("Sunday", "Active Recovery", WorkoutStatus.UPCOMING, listOf("Yoga", "Light cardio", "Mobility work"))
)

val allAvailableExercises = listOf(
    "Bench Press", "Incline Dumbbell Press", "Cable Flyes", "Tricep Dips",
    "Deadlifts", "Pull-ups", "Barbell Rows", "Bicep Curls",
    "Squats", "Leg Press", "Calf Raises", "Plank",
    "Military Press", "Lateral Raises", "Face Pulls", "Tricep Pushdown",
    "Clean & Press", "Lunges", "Push-ups", "Burpees",
    "Yoga", "Light cardio", "Mobility work", "Hammer Curls",
    "Lat Pulldown", "Leg Extension", "Leg Curl", "Shoulder Press"
)

val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
val commonFocuses = listOf("Chest & Triceps", "Back & Biceps", "Legs & Core", "Shoulders & Arms", "Full Body", "Cardio", "Rest Day", "Active Recovery")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymPlanScreen(gymPlan: List<WorkoutDay>, onUpdatePlan: (List<WorkoutDay>) -> Unit) {
    var editingDayIndex by remember { mutableStateOf(-1) }
    val scope = rememberCoroutineScope()

    if (editingDayIndex != -1) {
        EditPlanDialog(
            workoutDay = gymPlan[editingDayIndex],
            onDismiss = { editingDayIndex = -1 },
            onSave = { updatedDay ->
                val newList = gymPlan.toMutableList()
                newList[editingDayIndex] = updatedDay
                
                // Auto-sort based on daysOfWeek order
                newList.sortBy { daysOfWeek.indexOf(it.day) }
                
                onUpdatePlan(newList)
                editingDayIndex = -1
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 48.dp, bottom = 8.dp)) {
                Text(
                    "Your Gym Plan",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    "AI-generated 7-day split",
                    fontSize = 16.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard(
                    label = "Goal",
                    value = "Muscle Gain",
                    icon = Icons.Default.Adjust,
                    iconColor = PrimaryRed,
                    modifier = Modifier.weight(1f)
                )
                InfoCard(
                    label = "This Week",
                    value = "2 / 5 Days",
                    icon = Icons.Default.CalendarToday,
                    iconColor = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            val currentDay = java.time.LocalDate.now().dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.US)
            val todayWorkout = gymPlan.find { it.day.equals(currentDay, ignoreCase = true) }
            
            todayWorkout?.let { workout ->
                HighlightWorkoutCard(
                    title = "Today's Workout",
                    focus = workout.focus,
                    exercises = workout.exercises.take(3) + if(workout.exercises.size > 3) listOf("+${workout.exercises.size - 3} more") else emptyList(),
                    onClick = { editingDayIndex = gymPlan.indexOf(workout) }
                )
            }
        }

        item {
            Text(
                "This Week's Schedule",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        items(items = gymPlan, key = { it.day }) { day ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = {
                    it == SwipeToDismissBoxValue.EndToStart
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {
                    val isDismissed = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
                    val color = if (isDismissed) PrimaryRed else Color.Transparent
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color, shape = RoundedCornerShape(20.dp))
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (isDismissed) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { 
                                    scope.launch { dismissState.reset() }
                                }) {
                                    Text("CANCEL", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { 
                                        onUpdatePlan(gymPlan.filter { it.day != day.day })
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text("DELETE", color = PrimaryRed, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        } else {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                    }
                },
                enableDismissFromStartToEnd = false
            ) {
                DayCard(workoutDay = day, onClick = { editingDayIndex = gymPlan.indexOf(day) })
            }
        }

        item {
            GeneratePlanCard(onClick = { /* Trigger AI generation */ })
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditPlanDialog(
    workoutDay: WorkoutDay,
    onDismiss: () -> Unit,
    onSave: (WorkoutDay) -> Unit
) {
    var dayName by remember { mutableStateOf(workoutDay.day) }
    var focus by remember { mutableStateOf(workoutDay.focus) }
    var selectedExercises by remember { mutableStateOf(workoutDay.exercises) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredExercises = allAvailableExercises.filter { 
        it.contains(searchQuery, ignoreCase = true) && it !in selectedExercises 
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0F0F12)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .statusBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                        Text(
                            "Edit Plan",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { 
                            val newStatus = if (focus.lowercase().contains("rest") || (focus.isEmpty() && selectedExercises.isEmpty())) {
                                WorkoutStatus.REST
                            } else {
                                workoutDay.status
                            }
                            onSave(workoutDay.copy(day = dayName, focus = focus, exercises = selectedExercises, status = newStatus)) 
                        }) {
                            Text("SAVE", color = PrimaryRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    Text("Select Day", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(daysOfWeek) { day ->
                            val isSelected = dayName == day
                            Surface(
                                onClick = { dayName = day },
                                color = if (isSelected) PrimaryRed else Color(0xFF1C1C1E),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isSelected) PrimaryRed else Color.White.copy(alpha = 0.1f))
                            ) {
                                Text(
                                    text = day,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                item {
                    Text("Select Focus", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        commonFocuses.forEach { f ->
                            val isSelected = focus == f
                            Surface(
                                onClick = { focus = f },
                                color = if (isSelected) PrimaryRed else Color(0xFF1C1C1E),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isSelected) PrimaryRed else Color.White.copy(alpha = 0.1f))
                            ) {
                                Text(
                                    text = f,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                item {
                    Text("Current Exercises", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedExercises.isEmpty()) {
                        Text("No exercises added. This will be shown as a rest day.", color = TextSecondary, fontSize = 14.sp)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(selectedExercises) { exercise ->
                                Surface(
                                    color = PrimaryRed.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, PrimaryRed.copy(alpha = 0.5f)),
                                    onClick = { selectedExercises = selectedExercises - exercise }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Text(exercise, color = Color.White, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Add Exercises", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search exercises...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1C1C1E),
                            unfocusedContainerColor = Color(0xFF1C1C1E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = PrimaryRed
                        )
                    )
                }

                items(filteredExercises) { exercise ->
                    Card(
                        onClick = { selectedExercises = selectedExercises + exercise },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(exercise, color = Color.White)
                            Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(label, color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HighlightWorkoutCard(
    title: String,
    focus: String,
    exercises: List<String>,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryRed)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background Dumbbell Icon
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.15f),
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 30.dp, y = 20.dp)
            )
            
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(focus, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            exercises.joinToString(" • "),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                    
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DayCard(workoutDay: WorkoutDay, onClick: () -> Unit) {
    val isRestDay = workoutDay.status == WorkoutStatus.REST || (workoutDay.exercises.isEmpty() && workoutDay.focus.lowercase().contains("rest"))
    val isDone = workoutDay.status == WorkoutStatus.DONE
    val borderColor = if (isDone) Color(0xFF4CAF50).copy(alpha = 0.5f) else Color.Transparent
    val backgroundColor = if (isDone) Color(0xFF122315) else if (isRestDay) Color(0xFF0F0F12) else Color(0xFF1C1C1E)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (isDone) BorderStroke(1.dp, borderColor) else if (isRestDay) BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        workoutDay.day,
                        color = if (isDone) Color(0xFF4CAF50) else if (isRestDay) TextSecondary else Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isDone) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(
                            color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("DONE", color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                
                Text(
                    if (workoutDay.focus.isEmpty() && workoutDay.exercises.isEmpty()) "Rest Day" else workoutDay.focus,
                    color = if (isDone) Color.White.copy(alpha = 0.7f) else TextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                if (workoutDay.exercises.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        workoutDay.exercises.forEach { exercise ->
                            ExerciseChip(exercise)
                        }
                    }
                }
            }
            
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ExerciseChip(text: String) {
    Surface(
        color = Color(0xFF2C2C2E),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun GeneratePlanCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFFBB86FC).copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFBB86FC), modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text("Generate New Plan", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Customize your workout schedule with AI", color = TextSecondary, fontSize = 13.sp)
            }
            
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// --- End of Gym Plan Screen ---

@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(name, color = Color.White, fontSize = 24.sp)
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(vertical = 4.dp))
}

data class ActivityItem(val userName: String, val status: String, val timestamp: String, val workoutTitle: String, val duration: String, val gymName: String)

val activityFeed = listOf(
    ActivityItem("Om Gupta", "Just completed a workout!", "10 minutes ago", "🔥 Chest Day! Bench Press & Incline Dumbbells 💪", "45 mins", "FitZone Gym")
)

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GYMMYTheme {
        GYMMYApp()
    }
}
