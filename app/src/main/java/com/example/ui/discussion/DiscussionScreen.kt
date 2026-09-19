package com.example.ui.discussion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DataRepository
import com.example.model.AcademicStream
import com.example.model.ChatMessage
import com.example.model.MessageType
import com.example.model.SchoolClasses
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionScreen(
    currentUser: UserProfile
) {
    val allMessages by DataRepository.messages.collectAsState()

    val canViewBothStreams = currentUser.role == UserRole.ADMIN || currentUser.role == UserRole.TEACHER

    // Stream-locked default channel for regular students
    val studentDefaultChannel = if (currentUser.stream == AcademicStream.BIO_SCIENCE) {
        SchoolClasses.BIO_SCIENCE_ENG
    } else {
        SchoolClasses.PHYSICAL_SCIENCE_ENG
    }

    // Active channel: Teachers and Admins can switch between both streams; students are locked to their chosen stream
    var selectedChannel by remember { mutableStateOf(studentDefaultChannel) }
    val effectiveChannel = if (canViewBothStreams) selectedChannel else studentDefaultChannel

    var messageInput by remember { mutableStateOf("") }
    var isSearchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showCloudHubDialog by remember { mutableStateOf(false) }
    var isRecordingAudio by remember { mutableStateOf(false) }

    // Users view only messages in their active/assigned stream
    val filteredMessages = remember(allMessages, effectiveChannel, searchQuery) {
        allMessages.filter { msg ->
            (msg.classChannel == effectiveChannel) &&
                    (searchQuery.isBlank() || msg.text.contains(searchQuery, ignoreCase = true) || msg.senderName.contains(searchQuery, ignoreCase = true))
        }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(filteredMessages.size) {
        if (filteredMessages.isNotEmpty()) {
            listState.animateScrollToItem(filteredMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WhatsAppDarkGreen)
                    .statusBarsPadding()
            ) {
                // WhatsApp Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Group Icon
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ZahiraMaroon)
                            .border(1.5.dp, ZahiraGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (effectiveChannel == SchoolClasses.PHYSICAL_SCIENCE_ENG) Icons.Default.Calculate else Icons.Default.Biotech,
                            contentDescription = "Class Icon",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Group Title and Subtitle
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = effectiveChannel,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(WhatsAppLightGreen)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = when (currentUser.role) {
                                    UserRole.TEACHER -> "Teacher Supervision • Switch Streams"
                                    UserRole.ADMIN -> "Administrator Supervision • Switch Streams"
                                    UserRole.STUDENT -> "Stream Locked • Official Discussion"
                                },
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Cloud Files Hub Button (0 MB device bloat)
                    IconButton(
                        onClick = { showCloudHubDialog = true },
                        modifier = Modifier.testTag("btn_cloud_hub_discussion")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = "Cloud Past Papers & Files",
                            tint = ZahiraGold
                        )
                    }

                    // Search Button
                    IconButton(
                        onClick = { isSearchOpen = !isSearchOpen },
                        modifier = Modifier.testTag("btn_search_chat")
                    ) {
                        Icon(
                            imageVector = if (isSearchOpen) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                }

                // Search Bar Expanded
                AnimatedVisibility(visible = isSearchOpen) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search messages...", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF064E46),
                            unfocusedContainerColor = Color(0xFF064E46),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                // Channel Control: Tabs for Admin and Teacher; Locked Stream Banner for Students
                if (canViewBothStreams) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF064E46))
                    ) {
                        ClassChannelTab(
                            title = "Physical Science",
                            subtitle = "Maths • Physics • Chem • ICT",
                            isSelected = effectiveChannel == SchoolClasses.PHYSICAL_SCIENCE_ENG,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedChannel = SchoolClasses.PHYSICAL_SCIENCE_ENG }
                        )
                        ClassChannelTab(
                            title = "Bio Science",
                            subtitle = "Bio • Physics • Chem • ICT",
                            isSelected = effectiveChannel == SchoolClasses.BIO_SCIENCE_ENG,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedChannel = SchoolClasses.BIO_SCIENCE_ENG }
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF064E46)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = ZahiraGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Viewing ${if (currentUser.stream == AcademicStream.BIO_SCIENCE) "Bio Science" else "Physical Science"} Stream Messages Only",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // WhatsApp Message Input Bar
            WhatsAppInputBar(
                messageText = messageInput,
                onMessageChange = { messageInput = it },
                onSend = {
                    if (messageInput.isNotBlank()) {
                        DataRepository.sendMessage(
                            classChannel = effectiveChannel,
                            text = messageInput.trim()
                        )
                        messageInput = ""
                    }
                },
                onAttachClick = { showAttachmentSheet = true },
                onVoiceRecord = {
                    isRecordingAudio = true
                    // Simulate WhatsApp voice note recording
                    DataRepository.sendMessage(
                        classChannel = effectiveChannel,
                        text = "🎤 Voice Note (0:14) - ${if (currentUser.stream == AcademicStream.BIO_SCIENCE) "Bio revision" else "Physics Doppler"} note",
                        mediaType = MessageType.VOICE_NOTE,
                        voiceDurationSeconds = 14
                    )
                    isRecordingAudio = false
                }
            )
        }
    ) { padding ->
        // Chat Canvas (WhatsApp Classic Theme)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(WhatsAppChatBackground)
        ) {
            if (filteredMessages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(WhatsAppGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No messages yet in $effectiveChannel",
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Send a question or note to start collaborating!",
                        color = TextSecondary.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredMessages, key = { it.id }) { msg ->
                        val isMe = msg.senderId == currentUser.id
                        val canDelete = when (currentUser.role) {
                            UserRole.ADMIN, UserRole.TEACHER -> true
                            UserRole.STUDENT -> isMe
                        }
                        WhatsAppMessageItem(
                            message = msg,
                            isMe = isMe,
                            canDelete = canDelete,
                            currentUserRole = currentUser.role,
                            onReact = { emoji ->
                                DataRepository.toggleReaction(msg.id, emoji)
                            },
                            onDelete = {
                                DataRepository.deleteMessage(msg.id, currentUser)
                            }
                        )
                    }
                }
            }
        }
    }

    // Attachment Bottom Sheet
    if (showAttachmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Share with Class",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZahiraMaroon
                )
                Text(
                    text = "☁️ Powered by Cloud Storage (Files streamed on-demand, 0 MB device bloat)",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttachmentOption(
                        icon = Icons.Default.CameraAlt,
                        label = "Camera",
                        color = Color(0xFFE91E63),
                        onClick = {
                            DataRepository.sendMessage(
                                classChannel = effectiveChannel,
                                text = "📷 Lab Apparatus Diagram (Physics Practical 4)",
                                mediaType = MessageType.IMAGE
                            )
                            showAttachmentSheet = false
                        }
                    )
                    AttachmentOption(
                        icon = Icons.Default.Description,
                        label = "Document",
                        color = Color(0xFF5C6BC0),
                        onClick = {
                            DataRepository.sendMessage(
                                classChannel = effectiveChannel,
                                text = "📄 Combined Maths 2024 Marking Scheme.pdf (1.8 MB)",
                                mediaType = MessageType.DOCUMENT
                            )
                            showAttachmentSheet = false
                        }
                    )
                    AttachmentOption(
                        icon = Icons.Default.AudioFile,
                        label = "Voice Note",
                        color = Color(0xFFFF9800),
                        onClick = {
                            DataRepository.sendMessage(
                                classChannel = effectiveChannel,
                                text = "🎙️ Teacher Audio Note: Chemistry Le Chatelier review",
                                mediaType = MessageType.VOICE_NOTE,
                                voiceDurationSeconds = 22
                            )
                            showAttachmentSheet = false
                        }
                    )
                    AttachmentOption(
                        icon = Icons.Default.CloudQueue,
                        label = "Cloud Hub",
                        color = Color(0xFF0D9488),
                        onClick = {
                            showAttachmentSheet = false
                            showCloudHubDialog = true
                        }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showCloudHubDialog) {
        com.example.ui.components.CloudStudyHubDialog(
            currentUser = currentUser,
            onDismiss = { showCloudHubDialog = false }
        )
    }
}

@Composable
fun ClassChannelTab(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
            fontSize = 13.sp
        )
        Text(
            text = subtitle,
            color = if (isSelected) ZahiraGold else Color.White.copy(alpha = 0.4f),
            fontSize = 9.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .height(3.dp)
                .fillMaxWidth(0.8f)
                .background(if (isSelected) ZahiraGold else Color.Transparent)
        )
    }
}

@Composable
fun WhatsAppMessageItem(
    message: ChatMessage,
    isMe: Boolean,
    canDelete: Boolean,
    currentUserRole: UserRole,
    onReact: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showReactionPicker by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val timeStr = dateFormat.format(Date(message.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        // TOP 10 FLYING RANK BANNER OVER SENDER PROFILE
        // As explicitly requested: "top 10 students should have a banner flying upon there profile when sending messages saying there rank and it should last only for a week."
        if (message.senderRank != null && message.senderRank in 1..10) {
            FlyingRankBanner(
                rank = message.senderRank,
                senderName = message.senderName,
                isMe = isMe
            )
            Spacer(modifier = Modifier.height(2.dp))
        }

        // WhatsApp Bubble
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 310.dp)
                    .shadow(1.dp, shape = RoundedCornerShape(12.dp))
                    .clip(
                        RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomStart = if (isMe) 12.dp else 2.dp,
                            bottomEnd = if (isMe) 2.dp else 12.dp
                        )
                    )
                    .background(if (isMe) WhatsAppOutgoingBubble else WhatsAppIncomingBubble)
                    .clickable { showReactionPicker = !showReactionPicker }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column {
                    // Sender Name & Role Tag (for incoming or announcement)
                    if (!isMe) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Text(
                                text = message.senderName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (message.senderRole == UserRole.TEACHER) ZahiraMaroon else if (message.senderRole == UserRole.ADMIN) Color(0xFFD97706) else WhatsAppDarkGreen
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (message.senderRole) {
                                    UserRole.TEACHER -> ZahiraMaroon.copy(alpha = 0.12f)
                                    UserRole.ADMIN -> Color(0xFFFEF3C7)
                                    UserRole.STUDENT -> Color(0xFFE0F2FE)
                                }
                            ) {
                                Text(
                                    text = message.senderRole.displayName,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when (message.senderRole) {
                                        UserRole.TEACHER -> ZahiraMaroon
                                        UserRole.ADMIN -> Color(0xFFB45309)
                                        UserRole.STUDENT -> Color(0xFF0369A1)
                                    },
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    // Special Media Renderers
                    when (message.mediaType) {
                        MessageType.VOICE_NOTE -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(WhatsAppGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .background(WhatsAppGreen.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "0:${message.voiceDurationSeconds.toString().padStart(2, '0')}",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                        MessageType.IMAGE -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                                    Text(message.text, fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        MessageType.DOCUMENT -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFEF4444))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(message.text, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        else -> {
                            // Standard Text Message
                            Text(
                                text = message.text,
                                fontSize = 14.sp,
                                color = TextPrimary,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    // Timestamp and Delivery Status Tick
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timeStr,
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                        if (isMe) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Read",
                                tint = WhatsAppTickBlue,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        if (canDelete) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Message",
                                tint = Color(0xFFEF4444).copy(alpha = 0.75f),
                                modifier = Modifier
                                    .size(15.dp)
                                    .clickable { showDeleteConfirmDialog = true }
                                    .testTag("bubble_delete_${message.id}")
                            )
                        }
                    }
                }
            }
        }

        // Reactions Display
        if (message.reactions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(top = 2.dp, start = if (isMe) 0.dp else 8.dp, end = if (isMe) 8.dp else 0.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(0.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val emojiCounts = message.reactions.values.groupingBy { it }.eachCount()
                emojiCounts.forEach { (emoji, count) ->
                    Text(text = "$emoji $count", fontSize = 11.sp)
                }
            }
        }

        // Reaction Picker Popup
        AnimatedVisibility(visible = showReactionPicker) {
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .shadow(3.dp, RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("👍", "❤️", "😂", "😮", "🔥", "👏").forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .clickable {
                                onReact(emoji)
                                showReactionPicker = false
                            }
                            .padding(2.dp)
                    )
                }

                if (canDelete) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(18.dp)
                            .background(Color(0xFFCBD5E1))
                    )
                    Row(
                        modifier = Modifier
                            .clickable {
                                showDeleteConfirmDialog = true
                                showReactionPicker = false
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .testTag("popup_delete_${message.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Message",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Delete",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(28.dp)
                    )
                },
                title = {
                    Text(
                        text = "Delete Message",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Text(
                        text = if (currentUserRole == UserRole.TEACHER && !isMe) {
                            "As a Teacher, delete message by '${message.senderName}' for everyone in the class?"
                        } else if (currentUserRole == UserRole.ADMIN && !isMe) {
                            "As Administrator, delete message by '${message.senderName}'?"
                        } else {
                            "Delete your message for everyone in this class?"
                        },
                        fontSize = 13.sp,
                        color = Color(0xFF334155)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDelete()
                            showDeleteConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("confirm_delete_${message.id}")
                    ) {
                        Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirmDialog = false },
                        modifier = Modifier.testTag("cancel_delete_${message.id}")
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

/**
 * Top 10 Student Flying Rank Banner
 * As requested: "top 10 students should have a banner flying upon there profile when sending messages saying there rank and it should last only for a week."
 */
@Composable
fun FlyingRankBanner(
    rank: Int,
    senderName: String,
    isMe: Boolean
) {
    val (gradientColors, borderGold, iconEmoji) = when (rank) {
        1 -> Triple(listOf(Color(0xFFFFDF73), Color(0xFFFFB800)), Color(0xFFD4AF37), "👑")
        2 -> Triple(listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8)), Color(0xFF64748B), "🥈")
        3 -> Triple(listOf(Color(0xFFFFEDD5), Color(0xFFCD7F32)), Color(0xFF9A3412), "🥉")
        else -> Triple(listOf(Color(0xFFFEF9C3), Color(0xFFFACC15)), Color(0xFFCA8A04), "⭐")
    }

    Row(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(gradientColors))
            .border(1.dp, borderGold, RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = iconEmoji, fontSize = 11.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "TOP $rank • ZAHIRA QUIZ STAR",
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF451A03),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "(1-Wk Active)",
            fontSize = 8.sp,
            color = Color(0xFF78350F),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun WhatsAppInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachClick: () -> Unit,
    onVoiceRecord: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhatsAppChatBackground)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Message Input Capsule
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(horizontal = 10.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { /* Emoji dialog */ },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.SentimentSatisfiedAlt, contentDescription = "Emoji", tint = TextSecondary)
            }

            TextField(
                value = messageText,
                onValueChange = onMessageChange,
                placeholder = { Text("Message...", color = TextSecondary, fontSize = 15.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_chat_message"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 4
            )

            IconButton(
                onClick = onAttachClick,
                modifier = Modifier.size(36.dp).testTag("btn_attach_media")
            ) {
                Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = TextSecondary)
            }

            IconButton(
                onClick = onAttachClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // WhatsApp Round Mic or Send Action FAB
        FloatingActionButton(
            onClick = {
                if (messageText.isNotBlank()) {
                    onSend()
                } else {
                    onVoiceRecord()
                }
            },
            modifier = Modifier
                .size(46.dp)
                .testTag("btn_send_chat"),
            shape = CircleShape,
            containerColor = WhatsAppGreen,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
        ) {
            Icon(
                imageVector = if (messageText.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                contentDescription = if (messageText.isNotBlank()) "Send" else "Voice Note",
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}
