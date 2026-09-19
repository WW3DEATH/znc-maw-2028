package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DataRepository
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.ui.admin.AdminDashboardScreen
import com.example.ui.auth.LoginScreen
import com.example.ui.components.GeminiVoiceAssistantFAB
import com.example.ui.discussion.DiscussionScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.quiz.QuizScreen
import com.example.ui.redemption.RedemptionScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ZahiraGold
import com.example.ui.theme.ZahiraMaroon

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataRepository.initSession(applicationContext)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentUser by DataRepository.currentUser.collectAsState()

                if (currentUser == null) {
                    LoginScreen(
                        onLoginSuccess = {
                            // User logged in
                        }
                    )
                } else {
                    MainAppScaffold(
                        currentUser = currentUser!!,
                        onLogout = {
                            DataRepository.logout(applicationContext)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppScaffold(
    currentUser: UserProfile,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val isAdmin = currentUser.role == UserRole.ADMIN

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                // Discussion Tab (WhatsApp Style)
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Discussion") },
                    label = { Text("Discussion", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ZahiraMaroon,
                        selectedTextColor = ZahiraMaroon,
                        indicatorColor = ZahiraMaroon.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_tab_discussion")
                )

                // Quiz Tab
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Quiz, contentDescription = "Quiz") },
                    label = { Text("Quiz", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ZahiraMaroon,
                        selectedTextColor = ZahiraMaroon,
                        indicatorColor = ZahiraMaroon.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_tab_quiz")
                )

                // Redemption Tab
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.CardGiftcard, contentDescription = "Redeem") },
                    label = { Text("Redemption", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ZahiraMaroon,
                        selectedTextColor = ZahiraMaroon,
                        indicatorColor = ZahiraMaroon.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_tab_redemption")
                )

                // Profile Tab
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ZahiraMaroon,
                        selectedTextColor = ZahiraMaroon,
                        indicatorColor = ZahiraMaroon.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_tab_profile")
                )

                // Admin Tab (Always accessible to admin, or as management portal)
                if (isAdmin) {
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                        label = { Text("Admin", fontSize = 11.sp, fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ZahiraMaroon,
                            selectedTextColor = ZahiraMaroon,
                            indicatorColor = ZahiraMaroon.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("nav_tab_admin")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Content Area
            when (selectedTab) {
                0 -> DiscussionScreen(currentUser = currentUser)
                1 -> QuizScreen(currentUser = currentUser)
                2 -> RedemptionScreen(currentUser = currentUser)
                3 -> ProfileScreen(currentUser = currentUser, onLogout = onLogout)
                4 -> AdminDashboardScreen()
            }

            // Gemini Intelligence Voice & Command Assistant
            // As explicitly requested: "gemini intelligence that can do works in the app when a command is given through voice chatting that gemini intelligence should be shown at the right bottom of the app in the home screen only when clicked this functions can be done."
            GeminiVoiceAssistantFAB(
                currentUser = currentUser,
                onNavigateTab = { newTab -> selectedTab = newTab },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
            )
        }
    }
}
