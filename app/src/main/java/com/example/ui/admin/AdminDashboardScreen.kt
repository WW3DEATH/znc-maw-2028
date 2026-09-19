package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DataRepository
import com.example.model.ChatMessage
import com.example.model.UserProfile
import com.example.model.UserRedemption
import com.example.model.UserRole
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen() {
    val allUsers by DataRepository.allUsers.collectAsState()
    val allMessages by DataRepository.messages.collectAsState()
    val allRedemptions by DataRepository.userRedemptions.collectAsState()

    var selectedAdminTab by remember { mutableStateOf(0) }
    var actionNotification by remember { mutableStateOf<String?>(null) }
    var userToDelete by remember { mutableStateOf<UserProfile?>(null) }
    var showCloudStudyHub by remember { mutableStateOf(false) }
    var redemptionSearchQuery by remember { mutableStateOf("") }
    var redemptionStatusFilter by remember { mutableStateOf("All") }
    val currentUserState by DataRepository.currentUser.collectAsState()

    val pendingUsersCount = remember(allUsers) { allUsers.count { !it.isVerified } }
    val pendingRedemptionsCount = remember(allRedemptions) { allRedemptions.count { it.status.equals("Pending", ignoreCase = true) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ZSP Administrator Portal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Supervised by M.N.M. Jaasim",
                            fontSize = 12.sp,
                            color = ZahiraGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ZahiraMaroonDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SurfaceLight)
        ) {
            // Admin Tabs (Scrollable to comfortably fit all administrative functions)
            ScrollableTabRow(
                selectedTabIndex = selectedAdminTab,
                containerColor = ZahiraMaroon,
                contentColor = Color.White,
                edgePadding = 8.dp
            ) {
                Tab(
                    selected = selectedAdminTab == 0,
                    onClick = { selectedAdminTab = 0 },
                    text = { Text("Users", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedAdminTab == 1,
                    onClick = { selectedAdminTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Verifications", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            if (pendingUsersCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(shape = CircleShape, color = Color(0xFFEF4444)) {
                                    Text(
                                        text = "$pendingUsersCount",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedAdminTab == 2,
                    onClick = { selectedAdminTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Redemptions", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            if (pendingRedemptionsCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(shape = CircleShape, color = Color(0xFFF59E0B)) {
                                    Text(
                                        text = "$pendingRedemptionsCount",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedAdminTab == 3,
                    onClick = { selectedAdminTab = 3 },
                    text = { Text("Chat Monitor", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedAdminTab == 4,
                    onClick = { selectedAdminTab = 4 },
                    text = { Text("Cloud & Storage", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            actionNotification?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = msg,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            when (selectedAdminTab) {
                0 -> {
                    // Users & Roles
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "SYSTEM USER ACCOUNTS (${allUsers.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                        items(allUsers) { user ->
                            AdminUserCard(
                                user = user,
                                onRoleChange = { newRole ->
                                    DataRepository.adminUpdateUserRole(user.id, newRole)
                                    actionNotification = "Updated ${user.fullName} to ${newRole.displayName}"
                                },
                                onDeleteClick = {
                                    userToDelete = user
                                }
                            )
                        }
                    }
                }
                1 -> {
                    // Verifications
                    val pendingUsers = allUsers.filter { !it.isVerified }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "PENDING ACCOUNT VERIFICATIONS (${pendingUsers.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                        if (pendingUsers.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("All student & teacher accounts are verified! 🎉", color = TextSecondary)
                                }
                            }
                        } else {
                            items(pendingUsers) { user ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(user.email, fontSize = 12.sp, color = TextSecondary)
                                            Text("Stream: ${user.stream.displayName} • ${user.role.displayName}", fontSize = 11.sp, color = ZahiraMaroon)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick = { userToDelete = user },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.testTag("btn_verify_delete_${user.id}")
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Delete", fontSize = 12.sp)
                                            }
                                            Button(
                                                onClick = {
                                                    DataRepository.adminVerifyUser(user.id, true)
                                                    actionNotification = "Verified ${user.fullName}'s account!"
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                modifier = Modifier.testTag("btn_verify_confirm_${user.id}")
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Verify", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Redemption Items Requests
                    val filteredRedemptions = remember(allRedemptions, redemptionSearchQuery, redemptionStatusFilter) {
                        allRedemptions.filter { red ->
                            val matchesFilter = when (redemptionStatusFilter) {
                                "Pending" -> red.status.equals("Pending", ignoreCase = true)
                                "Approved" -> red.status.equals("Approved", ignoreCase = true)
                                "Fulfilled" -> red.status.equals("Fulfilled", ignoreCase = true)
                                "Rejected" -> red.status.contains("Reject", ignoreCase = true)
                                else -> true
                            }
                            val matchesQuery = redemptionSearchQuery.isBlank() ||
                                    red.userName.contains(redemptionSearchQuery, ignoreCase = true) ||
                                    red.itemTitle.contains(redemptionSearchQuery, ignoreCase = true) ||
                                    red.userEmail.contains(redemptionSearchQuery, ignoreCase = true)
                            matchesFilter && matchesQuery
                        }
                    }

                    val totalSpentSp = allRedemptions.sumOf { it.spSpent }
                    val pendingCount = allRedemptions.count { it.status.equals("Pending", ignoreCase = true) }
                    val fulfilledCount = allRedemptions.count { it.status.equals("Fulfilled", ignoreCase = true) }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            // Summary Cards Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = ZahiraMaroon.copy(alpha = 0.08f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Total Requests", fontSize = 10.sp, color = TextSecondary)
                                        Text("${allRedemptions.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ZahiraMaroon)
                                        Text("$totalSpentSp SP Redeemed", fontSize = 9.sp, color = Color(0xFFB45309), fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (pendingCount > 0) Color(0xFFFEF3C7) else Color(0xFFF1F5F9)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Pending Requests", fontSize = 10.sp, color = TextSecondary)
                                        Text("$pendingCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (pendingCount > 0) Color(0xFFD97706) else TextPrimary)
                                        Text(if (pendingCount > 0) "Needs Admin Review" else "All Reviewed", fontSize = 9.sp, color = if (pendingCount > 0) Color(0xFFB45309) else Color(0xFF10B981), fontWeight = FontWeight.Medium)
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFECFDF5)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Fulfilled", fontSize = 10.sp, color = TextSecondary)
                                        Text("$fulfilledCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                        Text("Issued to Students", fontSize = 9.sp, color = Color(0xFF047857), fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }

                        // Search Bar
                        item {
                            OutlinedTextField(
                                value = redemptionSearchQuery,
                                onValueChange = { redemptionSearchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_search_redemptions"),
                                placeholder = { Text("Search by student name or item...", fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                                },
                                trailingIcon = {
                                    if (redemptionSearchQuery.isNotBlank()) {
                                        IconButton(onClick = { redemptionSearchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }

                        // Filter Chips
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("All", "Pending", "Approved", "Fulfilled", "Rejected").forEach { filter ->
                                    FilterChip(
                                        selected = redemptionStatusFilter == filter,
                                        onClick = { redemptionStatusFilter = filter },
                                        label = {
                                            Text(
                                                text = filter,
                                                fontSize = 11.sp,
                                                fontWeight = if (redemptionStatusFilter == filter) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ZahiraMaroon,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                text = "STUDENT REDEMPTION REQUESTS (${filteredRedemptions.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }

                        if (filteredRedemptions.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.CardGiftcard,
                                            contentDescription = null,
                                            tint = Color(0xFFCBD5E1),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No redemption requests match your criteria.",
                                            color = TextSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filteredRedemptions, key = { it.id }) { redemption ->
                                AdminRedemptionCard(
                                    redemption = redemption,
                                    onApprove = {
                                        val res = DataRepository.adminUpdateRedemptionStatus(redemption.id, "Approved")
                                        actionNotification = res.getOrNull() ?: res.exceptionOrNull()?.message
                                    },
                                    onFulfill = {
                                        val res = DataRepository.adminUpdateRedemptionStatus(redemption.id, "Fulfilled")
                                        actionNotification = res.getOrNull() ?: res.exceptionOrNull()?.message
                                    },
                                    onReject = {
                                        val res = DataRepository.adminUpdateRedemptionStatus(redemption.id, "Rejected (Refunded)")
                                        actionNotification = res.getOrNull() ?: res.exceptionOrNull()?.message
                                    }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
                3 -> {
                    // Chat Monitor
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "MONITORING ALL CLASS DISCUSSIONS (${allMessages.size} MESSAGES)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                        items(allMessages) { msg ->
                            AdminMessageCard(
                                message = msg,
                                onDelete = {
                                    DataRepository.adminDeleteMessage(msg.id)
                                    actionNotification = "Deleted message by ${msg.senderName}"
                                }
                            )
                        }
                    }
                }
                4 -> {
                    // Cloud & Real-Time Storage Administration
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            com.example.ui.components.CloudStatusManagerCard(
                                onOpenCloudHub = { showCloudStudyHub = true }
                            )
                        }
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "ZERO-STORAGE CLOUD ARCHITECTURE",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = ZahiraMaroon
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "• All past papers, schemes, and notes are served from Cloud Storage.\n" +
                                                "• Files stream directly on demand without bloating student device internal storage.\n" +
                                                "• Real-time scores and leaderboards sync continuously across all devices via Firestore.\n" +
                                                "• Temporary cache is automatically managed and flushed by the streaming engine.",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // User Delete Confirmation Dialog
            userToDelete?.let { target ->
                AlertDialog(
                    onDismissRequest = { userToDelete = null },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "Delete User Account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    },
                    text = {
                        Text(
                            text = "Are you sure you want to delete '${target.fullName}' (${target.email})?\n\nThis will permanently remove the user from the system and clean up their messages.",
                            fontSize = 13.sp,
                            color = Color(0xFF334155)
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val res = DataRepository.adminDeleteUser(target.id)
                                actionNotification = res.getOrNull() ?: res.exceptionOrNull()?.message
                                userToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_confirm_delete_user")
                        ) {
                            Text("Delete User", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { userToDelete = null },
                            modifier = Modifier.testTag("btn_cancel_delete_user")
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (showCloudStudyHub && currentUserState != null) {
                com.example.ui.components.CloudStudyHubDialog(
                    currentUser = currentUserState!!,
                    onDismiss = { showCloudStudyHub = false }
                )
            }
        }
    }
}

@Composable
fun AdminUserCard(
    user: UserProfile,
    onRoleChange: (UserRole) -> Unit,
    onDeleteClick: () -> Unit
) {
    val isPrimaryAdmin = user.role == UserRole.ADMIN || user.email.equals(DataRepository.ADMIN_EMAIL, ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ZahiraMaroon.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPrimaryAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                        contentDescription = null,
                        tint = ZahiraMaroon
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (user.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                        }
                    }
                    Text(user.email, fontSize = 11.sp, color = TextSecondary)
                    Text("Stream: ${user.stream.displayName} • Level ${user.level} • ${user.spPoints} SP", fontSize = 10.sp, color = Color(0xFF64748B))
                }

                // Delete Button - only available for non-primary admin accounts
                if (!isPrimaryAdmin) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.testTag("btn_delete_user_${user.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete User",
                            tint = Color(0xFFEF4444)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Role Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Role:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                listOf(UserRole.STUDENT, UserRole.TEACHER, UserRole.ADMIN).forEach { role ->
                    FilterChip(
                        selected = user.role == role,
                        onClick = { onRoleChange(role) },
                        label = { Text(role.displayName, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ZahiraMaroon,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AdminMessageCard(
    message: ChatMessage,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(message.senderName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ZahiraMaroon.copy(alpha = 0.1f)
                    ) {
                        Text(message.classChannel, fontSize = 9.sp, color = ZahiraMaroon, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(message.text, fontSize = 12.sp, color = TextPrimary)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Moderate Message", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun AdminRedemptionCard(
    redemption: UserRedemption,
    onApprove: () -> Unit,
    onFulfill: () -> Unit,
    onReject: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(redemption.timestamp) { dateFormat.format(Date(redemption.timestamp)) }

    val statusColor = when (redemption.status.lowercase()) {
        "approved" -> Color(0xFF3B82F6)
        "fulfilled" -> Color(0xFF10B981)
        "rejected", "rejected (refunded)" -> Color(0xFFEF4444)
        else -> Color(0xFFF59E0B) // Pending
    }

    val statusBg = statusColor.copy(alpha = 0.12f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_redemption_${redemption.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Student Information Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Student Avatar Initial
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(ZahiraMaroon.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = redemption.userName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = ZahiraMaroon,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = redemption.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    if (redemption.userEmail.isNotBlank()) {
                        Text(
                            text = redemption.userEmail,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "Stream: ${redemption.userStream.ifBlank { "Physical Science" }}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = redemption.status.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Redeemed Item Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ZahiraGold.copy(alpha = 0.18f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = redemption.itemTitle,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${redemption.spSpent} SP Points",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFFB45309)
                        )
                        Text(
                            text = " • $formattedDate",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Admin Action Buttons
            when (redemption.status.lowercase()) {
                "pending" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onReject,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_reject_${redemption.id}")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reject & Refund", fontSize = 11.sp)
                        }

                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_approve_${redemption.id}")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve", fontSize = 11.sp)
                        }

                        Button(
                            onClick = onFulfill,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("btn_fulfill_${redemption.id}")
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hand Over", fontSize = 11.sp)
                        }
                    }
                }
                "approved" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onReject,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_cancel_approved_${redemption.id}")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancel & Refund", fontSize = 11.sp)
                        }

                        Button(
                            onClick = onFulfill,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("btn_fulfill_approved_${redemption.id}")
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Handed Over", fontSize = 11.sp)
                        }
                    }
                }
                "fulfilled" -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Item successfully issued & received by student.",
                                fontSize = 11.sp,
                                color = Color(0xFF065F46),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                else -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Request rejected and ${redemption.spSpent} SP points refunded to student.",
                                fontSize = 11.sp,
                                color = Color(0xFF991B1B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

