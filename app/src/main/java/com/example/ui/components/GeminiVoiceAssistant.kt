package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GeminiService
import com.example.model.UserProfile
import com.example.ui.theme.ZahiraGold
import com.example.ui.theme.ZahiraMaroon
import com.example.ui.theme.ZahiraMaroonDark
import kotlinx.coroutines.launch

data class GeminiVoiceMessage(
    val isUser: Boolean,
    val text: String,
    val isVoice: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiVoiceAssistantFAB(
    currentUser: UserProfile,
    onNavigateTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }

    // Pulsing animation for the AI assistant button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(modifier = modifier) {
        FloatingActionButton(
            onClick = { showSheet = true },
            modifier = Modifier
                .scale(scale)
                .size(60.dp)
                .border(2.dp, ZahiraGold, CircleShape)
                .testTag("fab_gemini_voice_assistant"),
            shape = CircleShape,
            containerColor = ZahiraMaroonDark,
            contentColor = ZahiraGold,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Gemini Voice Intelligence",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    if (showSheet) {
        GeminiVoiceModalSheet(
            currentUser = currentUser,
            onDismiss = { showSheet = false },
            onNavigateTab = { tab ->
                onNavigateTab(tab)
                showSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiVoiceModalSheet(
    currentUser: UserProfile,
    onDismiss: () -> Unit,
    onNavigateTab: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isListening by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var conversation by remember {
        mutableStateOf(
            listOf(
                GeminiVoiceMessage(
                    isUser = false,
                    text = "Assalamu Alaikum, ${currentUser.fullName}! I am your ZSP-28 Gemini Intelligence Assistant. Speak or type to ask Sri Lankan A/L syllabus questions or execute app commands (e.g. 'Check my SP points', 'Open quiz', 'Go to discussion')."
                )
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(ZahiraMaroon, Color(0xFF6B21A8))
                                )
                            )
                            .border(1.5.dp, ZahiraGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ZahiraGold, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Gemini Voice Intelligence",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ZahiraMaroon
                        )
                        Text(
                            text = "Powered by Google Gemini • Zahira Science Portal",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick suggestion chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SuggestionChip(
                    onClick = {
                        inputText = "What is my current SP balance and rank?"
                    },
                    label = { Text("My SP Points", fontSize = 11.sp) }
                )
                SuggestionChip(
                    onClick = {
                        inputText = "Explain Doppler effect formula in Physics"
                    },
                    label = { Text("Physics Doppler", fontSize = 11.sp) }
                )
                SuggestionChip(
                    onClick = {
                        inputText = "Open Wednesday Quiz space"
                    },
                    label = { Text("Open Quiz", fontSize = 11.sp) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Messages Conversation
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(conversation) { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 290.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 14.dp,
                                        topEnd = 14.dp,
                                        bottomStart = if (msg.isUser) 14.dp else 2.dp,
                                        bottomEnd = if (msg.isUser) 2.dp else 14.dp
                                    )
                                )
                                .background(
                                    if (msg.isUser) ZahiraMaroon else Color(0xFFF1F5F9)
                                )
                                .padding(12.dp)
                        ) {
                            Column {
                                if (msg.isVoice) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Mic,
                                            contentDescription = null,
                                            tint = if (msg.isUser) ZahiraGold else ZahiraMaroon,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Voice Command",
                                            fontSize = 10.sp,
                                            color = if (msg.isUser) ZahiraGold else ZahiraMaroon,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = if (msg.isUser) Color.White else Color(0xFF1E293B),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = ZahiraMaroon)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Gemini is analyzing syllabus...", fontSize = 12.sp, color = Color(0xFF64748B))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Voice Command Microphone Wave / Listening Mode
            if (isListening) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFFEF4444))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Listening... (Tap mic to stop)", color = Color(0xFF991B1B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        IconButton(
                            onClick = {
                                isListening = false
                                // Simulate voice command captured
                                val voicePrompt = "What are the rules for organic chemistry Markovnikov addition?"
                                conversation = conversation + GeminiVoiceMessage(isUser = true, text = voicePrompt, isVoice = true)
                                isLoading = true
                                coroutineScope.launch {
                                    val reply = GeminiService.queryGemini(voicePrompt)
                                    conversation = conversation + GeminiVoiceMessage(isUser = false, text = reply)
                                    isLoading = false
                                }
                            }
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice Mic Button
                FloatingActionButton(
                    onClick = {
                        if (!isListening) {
                            isListening = true
                        } else {
                            isListening = false
                        }
                    },
                    shape = CircleShape,
                    containerColor = if (isListening) Color(0xFFEF4444) else ZahiraMaroon,
                    contentColor = Color.White,
                    modifier = Modifier.size(46.dp).testTag("btn_voice_record_ai")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask syllabus question or command...", fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_gemini_voice"),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val text = inputText.trim()
                        if (text.isNotBlank()) {
                            inputText = ""
                            conversation = conversation + GeminiVoiceMessage(isUser = true, text = text)

                            // App command triggers:
                            if (text.contains("quiz", ignoreCase = true)) {
                                onNavigateTab(1) // Quiz tab
                            } else if (text.contains("discussion", ignoreCase = true) || text.contains("chat", ignoreCase = true)) {
                                onNavigateTab(0) // Discussion tab
                            } else if (text.contains("redeem", ignoreCase = true) || text.contains("point", ignoreCase = true)) {
                                onNavigateTab(2) // Redemption tab
                            }

                            isLoading = true
                            coroutineScope.launch {
                                val reply = GeminiService.queryGemini(
                                    userPrompt = text,
                                    systemContext = "User is ${currentUser.fullName}, role: ${currentUser.role}, stream: ${currentUser.stream}. SP balance: ${currentUser.spPoints}, Level: ${currentUser.level}."
                                )
                                conversation = conversation + GeminiVoiceMessage(isUser = false, text = reply)
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.testTag("btn_send_gemini_prompt")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = ZahiraMaroon)
                }
            }
        }
    }
}
