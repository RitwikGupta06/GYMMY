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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymmy.ui.theme.GYMMYTheme
import com.example.gymmy.ui.theme.GoldAccent
import com.example.gymmy.ui.theme.PrimaryRed
import com.example.gymmy.ui.theme.TextSecondary
import kotlinx.coroutines.delay
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
                AppDestinations.HOME -> HomeScreen()
                AppDestinations.GYMS -> PlaceholderScreen("Gyms")
                AppDestinations.RECORD -> RecordWorkoutScreen { currentDestination = AppDestinations.HOME }
                AppDestinations.PLAN -> PlaceholderScreen("Gym Plan")
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
    PLAN("Gym ...", Icons.Outlined.FitnessCenter, Icons.Default.FitnessCenter),
    PROFILE("Profile", Icons.Outlined.AccountCircle, Icons.Default.AccountCircle)
}

@Composable
fun HomeScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { HomeTopBar() }
        item { StartWorkoutCard() }
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
fun StartWorkoutCard() {
    Card(
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

@Composable
fun RecordWorkoutScreen(onClose: () -> Unit) {
    var isRecording by rememberSaveable { mutableStateOf(false) }
    var secondsElapsed by rememberSaveable { mutableStateOf(0L) }
    var startTimeStr by rememberSaveable { mutableStateOf("") }

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
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "Workout Session",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1.2f))
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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

            Spacer(modifier = Modifier.height(80.dp))

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

        // Session Details Card at the bottom
        AnimatedVisibility(
            visible = isRecording,
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
                    Text("Session Details", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Started", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(startTimeStr, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Location", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Verified", color = Color(0xFF4CAF50), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

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
