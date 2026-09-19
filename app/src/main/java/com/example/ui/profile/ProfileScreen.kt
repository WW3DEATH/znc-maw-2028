package com.example.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DataRepository
import com.example.model.MilestoneReward
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUser: UserProfile,
    onLogout: () -> Unit
) {
    val milestones by DataRepository.milestoneRewards.collectAsState()
    val scrollState = rememberScrollState()

    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(currentUser.fullName) }
    var editBio by remember { mutableStateOf(currentUser.bio) }
    var editIndex by remember { mutableStateOf(currentUser.indexNumber) }
    var editAcademicHistory by remember { mutableStateOf(currentUser.academicHistory) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showAddMilestoneDialog by remember { mutableStateOf(false) }
    var showCloudStudyHub by remember { mutableStateOf(false) }
    var actionMessage by remember { mutableStateOf<String?>(null) }

    val isAdmin = currentUser.role == UserRole.ADMIN

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("User Profile & Milestones", fontWeight = FontWeight.Bold, color = Color.White)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ZahiraMaroon),
                actions = {
                    IconButton(
                        onClick = { isEditing = !isEditing },
                        modifier = Modifier.testTag("btn_toggle_edit_profile")
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Done" else "Edit",
                            tint = ZahiraGold
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SurfaceLight)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Profile Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar with Edit Overlay
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(ZahiraMaroon, ZahiraMaroonDark)
                                    )
                                )
                                .border(3.dp, ZahiraGold, CircleShape)
                                .clickable { showAvatarPicker = true }
                                .testTag("btn_change_avatar"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (currentUser.profilePic) {
                                    "avatar_admin" -> Icons.Default.AdminPanelSettings
                                    "avatar_teacher" -> Icons.Default.School
                                    "avatar_science" -> Icons.Default.Biotech
                                    "avatar_math" -> Icons.Default.Functions
                                    else -> Icons.Default.Person
                                },
                                contentDescription = "Profile Avatar",
                                tint = Color.White,
                                modifier = Modifier.size(50.dp)
                            )
                        }

                        // Avatar change badge
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ZahiraGold)
                                .clickable { showAvatarPicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF451A03), modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentUser.fullName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Role & Stream Tag
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ZahiraMaroon.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = currentUser.role.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ZahiraMaroon,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF92400E), modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentUser.stream.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    }

                    Text(
                        text = currentUser.bio,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Level Up System Display (Required by user)
                    // "There should be a level up system which levels up students when they do quizzes and gets answers correct the level of the user also need to shown in the profile"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(ZahiraGold),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Lv ${currentUser.level}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF451A03)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Level ${currentUser.level} Scholar",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "XP: ${currentUser.xp} / ${currentUser.xpForNextLevel()}",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Text(
                                        text = "${currentUser.spPoints} SP",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF166534),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val progress = (currentUser.xp.toFloat() / currentUser.xpForNextLevel()).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = ZahiraMaroon,
                                trackColor = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Edit Profile Form or Display Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "PERSONAL & ACADEMIC DETAILS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZahiraMaroon
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isEditing) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = editBio,
                            onValueChange = { editBio = it },
                            label = { Text("Bio / Status") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = editIndex,
                            onValueChange = { editIndex = it },
                            label = { Text("Index Number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = editAcademicHistory,
                            onValueChange = { editAcademicHistory = it },
                            label = { Text("Academic History & Exam Marks") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                val updated = currentUser.copy(
                                    fullName = editName.trim(),
                                    bio = editBio.trim(),
                                    indexNumber = editIndex.trim(),
                                    academicHistory = editAcademicHistory.trim()
                                )
                                DataRepository.updateProfile(updated)
                                isEditing = false
                                actionMessage = "Profile updated successfully!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ZahiraMaroon),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Changes")
                        }
                    } else {
                        ProfileInfoRow(label = "School", value = currentUser.school)
                        ProfileInfoRow(label = "Medium", value = currentUser.medium)
                        ProfileInfoRow(label = "Target Class", value = currentUser.targetClass())
                        ProfileInfoRow(label = "Academic Stream", value = "${currentUser.stream.displayName} (Locked)")
                        ProfileInfoRow(label = "Index Number", value = currentUser.indexNumber)
                        ProfileInfoRow(label = "Email Address", value = currentUser.email)
                        ProfileInfoRow(label = "Academic History", value = currentUser.academicHistory)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // MILESTONE SYSTEM
            // As explicitly requested: "there should be a milestone system in the profile when levelling up those milestones should be unlocked the first milestone starts at level 5 and it should continue with a differecence of 5 like 5, 10 ,15 etc. the milestone rewards can only be added by the admin"
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LEVEL MILESTONES (Lv 5, 10, 15...)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ZahiraMaroon
                            )
                            Text(
                                text = "Unlocks automatically as you level up",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        if (isAdmin) {
                            OutlinedButton(
                                onClick = { showAddMilestoneDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_admin_add_milestone")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Milestone", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    milestones.forEach { milestone ->
                        val isUnlocked = currentUser.level >= milestone.levelRequired
                        MilestoneRowItem(
                            milestone = milestone,
                            isUnlocked = isUnlocked,
                            onClaim = {
                                val res = DataRepository.claimMilestone(milestone)
                                actionMessage = res.getOrNull() ?: res.exceptionOrNull()?.message
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CLOUD REAL-TIME DATABASE & ZERO-STORAGE FILE STREAMING CARD
            com.example.ui.components.CloudStatusManagerCard(
                onOpenCloudHub = { showCloudStudyHub = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // DELETE PROFILE BUTTON
            // As explicitly requested: "and create a delete button to delete the profile"
            Button(
                onClick = { showDeleteConfirmDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_delete_profile")
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Profile", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Avatar Picker Sheet
    if (showAvatarPicker) {
        ModalBottomSheet(onDismissRequest = { showAvatarPicker = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Profile Picture",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZahiraMaroon
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AvatarPickerOption(icon = Icons.Default.Person, title = "Standard", onClick = {
                        DataRepository.updateProfile(currentUser.copy(profilePic = "avatar_1"))
                        showAvatarPicker = false
                    })
                    AvatarPickerOption(icon = Icons.Default.Biotech, title = "Bio Scientist", onClick = {
                        DataRepository.updateProfile(currentUser.copy(profilePic = "avatar_science"))
                        showAvatarPicker = false
                    })
                    AvatarPickerOption(icon = Icons.Default.Functions, title = "Mathematician", onClick = {
                        DataRepository.updateProfile(currentUser.copy(profilePic = "avatar_math"))
                        showAvatarPicker = false
                    })
                    AvatarPickerOption(icon = Icons.Default.School, title = "Scholar", onClick = {
                        DataRepository.updateProfile(currentUser.copy(profilePic = "avatar_teacher"))
                        showAvatarPicker = false
                    })
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Admin Add Milestone Reward Dialog
    if (showAddMilestoneDialog) {
        var levelStr by remember { mutableStateOf("25") }
        var titleStr by remember { mutableStateOf("") }
        var descStr by remember { mutableStateOf("") }
        var spStr by remember { mutableStateOf("500") }

        AlertDialog(
            onDismissRequest = { showAddMilestoneDialog = false },
            title = {
                Text("Add Milestone Reward (Admin)", fontWeight = FontWeight.Bold, color = ZahiraMaroon)
            },
            text = {
                Column {
                    Text("Milestones unlock at level 5, 10, 15, 20, 25...", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = levelStr,
                        onValueChange = { levelStr = it },
                        label = { Text("Required Level (e.g. 25)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = titleStr,
                        onValueChange = { titleStr = it },
                        label = { Text("Milestone Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = descStr,
                        onValueChange = { descStr = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = spStr,
                        onValueChange = { spStr = it },
                        label = { Text("SP Points Reward") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val lvl = levelStr.toIntOrNull() ?: 25
                        val sp = spStr.toIntOrNull() ?: 500
                        if (titleStr.isNotBlank()) {
                            DataRepository.adminAddMilestoneReward(
                                MilestoneReward(
                                    id = "ms_$lvl",
                                    levelRequired = lvl,
                                    title = titleStr.trim(),
                                    description = descStr.trim(),
                                    rewardSp = sp,
                                    badgeTitle = titleStr.trim()
                                )
                            )
                            showAddMilestoneDialog = false
                            actionMessage = "Added Level $lvl milestone reward!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZahiraMaroon)
                ) {
                    Text("Save Milestone")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMilestoneDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444)) },
            title = { Text("Permanently Delete Profile?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This action is irreversible. All your quiz progress, SP points, and academic details will be erased from ZSP-28.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        DataRepository.deleteProfile(currentUser.id)
                        showDeleteConfirmDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.testTag("btn_confirm_delete")
                ) {
                    Text("Delete My Profile", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCloudStudyHub) {
        com.example.ui.components.CloudStudyHubDialog(
            currentUser = currentUser,
            onDismiss = { showCloudStudyHub = false }
        )
    }
}

@Composable
fun MilestoneRowItem(
    milestone: MilestoneReward,
    isUnlocked: Boolean,
    onClaim: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xFFFEF9C3).copy(alpha = 0.5f) else Color(0xFFF1F5F9)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isUnlocked) ZahiraGold else Color(0xFFE2E8F0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) ZahiraGold else Color(0xFFCBD5E1)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUnlocked) Icons.Default.MilitaryTech else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isUnlocked) Color(0xFF451A03) else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Level ${milestone.levelRequired}: ${milestone.title}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                Text(
                    text = "${milestone.description} • +${milestone.rewardSp} SP",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            if (isUnlocked && !milestone.isClaimed) {
                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = ZahiraGold),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Claim SP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF451A03))
                }
            } else if (milestone.isClaimed) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFDCFCE7)
                ) {
                    Text("Claimed", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE2E8F0)
                ) {
                    Text("Locked", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = Color(0xFFF1F5F9))
    }
}

@Composable
fun AvatarPickerOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(ZahiraMaroon.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = ZahiraMaroon, modifier = Modifier.size(30.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
