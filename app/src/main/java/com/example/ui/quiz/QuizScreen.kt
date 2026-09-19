package com.example.ui.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DataRepository
import com.example.data.GeminiService
import com.example.data.QuizResultSummary
import com.example.data.SyllabusData
import com.example.model.LeaderboardEntry
import com.example.model.QuizQuestion
import com.example.model.QuizScheduleHelper
import com.example.model.SyllabusSubject
import com.example.model.SyllabusUnit
import com.example.model.UserProfile
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    currentUser: UserProfile
) {
    val coroutineScope = rememberCoroutineScope()
    val availableSubjects = remember(currentUser.stream) {
        SyllabusData.getSubjectsForStream(currentUser.stream)
    }

    var selectedSubject by remember { mutableStateOf(availableSubjects.firstOrNull() ?: SyllabusData.allSubjects.first()) }
    var activeQuizQuestions by remember { mutableStateOf<List<QuizQuestion>?>(null) }
    var activeQuizTitle by remember { mutableStateOf("") }
    var activeUnitNumber by remember { mutableStateOf(1) }

    // Wednesday night 7:30pm to 10:00pm automatic live schedule (no manual switch)
    val isLiveNow = QuizScheduleHelper.isWednesdayLiveWindow()
    val isResultsAvailable = QuizScheduleHelper.isWednesdayResultsAvailable()

    // Active Quiz Play State
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var scoreCount by remember { mutableStateOf(0) }
    var showExplanation by remember { mutableStateOf(false) }
    var quizCompletedSummary by remember { mutableStateOf<QuizResultSummary?>(null) }

    // Leaderboard sheet
    var showLeaderboard by remember { mutableStateOf(false) }
    val leaderboardEntries by DataRepository.leaderboard.collectAsState()

    // Cloud Study & Storage Hub
    var showCloudHub by remember { mutableStateOf(false) }

    // Gemini AI quiz generation status
    var isGeneratingAiQuiz by remember { mutableStateOf(false) }

    if (activeQuizQuestions != null && quizCompletedSummary == null) {
        // ACTIVE QUIZ RUNNER
        val questions = activeQuizQuestions!!
        val currentQ = questions[currentQuestionIndex]

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceLight)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { activeQuizQuestions = null }) {
                    Icon(Icons.Default.Close, contentDescription = "Exit Quiz")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = activeQuizTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ZahiraMaroon
                    )
                    Text(
                        text = "Question ${currentQuestionIndex + 1} of ${questions.size}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isLiveNow) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isLiveNow) Color(0xFFEF4444) else Color(0xFF22C55E))
                ) {
                    Text(
                        text = if (isLiveNow) "LIVE 🔴" else "PRACTICE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLiveNow) Color(0xFFDC2626) else Color(0xFF16A34A),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (currentQuestionIndex + 1).toFloat() / questions.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = ZahiraMaroon,
                trackColor = Color(0xFFE2E8F0)
            )

            // Question Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Syllabus Tag
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ZahiraGold.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Unit ${currentQ.unitNumber}: ${currentQ.unitName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = currentQ.questionText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Options
                    currentQ.options.forEachIndexed { index, option ->
                        val isSelected = selectedOptionIndex == index
                        val isCorrect = index == currentQ.correctIndex
                        val isAnswered = showExplanation

                        val bgColor = when {
                            isAnswered && isCorrect -> Color(0xFFDCFCE7)
                            isAnswered && isSelected && !isCorrect -> Color(0xFFFEE2E2)
                            isSelected -> ZahiraMaroon.copy(alpha = 0.08f)
                            else -> Color(0xFFF8FAFC)
                        }

                        val borderColor = when {
                            isAnswered && isCorrect -> Color(0xFF22C55E)
                            isAnswered && isSelected && !isCorrect -> Color(0xFFEF4444)
                            isSelected -> ZahiraMaroon
                            else -> Color(0xFFE2E8F0)
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = bgColor,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(enabled = !showExplanation) {
                                    selectedOptionIndex = index
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${('A' + index)}.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) ZahiraMaroon else TextSecondary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = option,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isAnswered && isCorrect) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E))
                                } else if (isAnswered && isSelected && !isCorrect) {
                                    Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }

                    // Explanation Box
                    AnimatedVisibility(visible = showExplanation) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "💡 Syllabus Solution & Marking Note:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = ZahiraMaroon
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentQ.explanation,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button
            Button(
                onClick = {
                    if (!showExplanation) {
                        if (selectedOptionIndex == null) return@Button
                        if (selectedOptionIndex == currentQ.correctIndex) {
                            scoreCount += 1
                        }
                        showExplanation = true
                    } else {
                        if (currentQuestionIndex + 1 < questions.size) {
                            currentQuestionIndex += 1
                            selectedOptionIndex = null
                            showExplanation = false
                        } else {
                            // Finish quiz and calculate SP / Level Up
                            val summary = DataRepository.submitQuizScore(
                                subjectName = selectedSubject.name,
                                unitNumber = activeUnitNumber,
                                score = scoreCount,
                                totalQuestions = questions.size,
                                isWednesdayLive = isLiveNow
                            )
                            quizCompletedSummary = summary
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_quiz_next"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ZahiraMaroon),
                enabled = selectedOptionIndex != null || showExplanation
            ) {
                Text(
                    text = if (!showExplanation) "Check Answer" else if (currentQuestionIndex + 1 < questions.size) "Next Question" else "Complete & Submit Quiz",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    } else if (quizCompletedSummary != null) {
        // QUIZ COMPLETED SUMMARY SCREEN
        val summary = quizCompletedSummary!!
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceLight)
                .statusBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(ZahiraGold, Color(0xFFFFB800)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Quiz Completed!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ZahiraMaroon
                    )

                    Text(
                        text = "Score: ${summary.score} / ${activeQuizQuestions?.size ?: 5}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rewards Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SP POINTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                            Text("+${summary.spEarned} SP", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB45309))
                        }
                        VerticalDivider(modifier = Modifier.height(32.dp), color = Color(0xFFFDE68A))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("XP EARNED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                            Text("+${summary.xpEarned} XP", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB45309))
                        }
                    }

                    if (summary.didLevelUp) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ZahiraMaroon.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ZahiraMaroon)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Celebration, contentDescription = null, tint = ZahiraMaroon)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🎉 LEVEL UP! You reached Level ${summary.newLevel}!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = ZahiraMaroon
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            activeQuizQuestions = null
                            quizCompletedSummary = null
                            showLeaderboard = true
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ZahiraGold)
                    ) {
                        Text(
                            text = if (isResultsAvailable) "View Top 10 Leaderboard" else "Top 10 Results (Wed after 10 PM)",
                            color = Color(0xFF451A03),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            activeQuizQuestions = null
                            quizCompletedSummary = null
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Back to Syllabus Units", color = ZahiraMaroon, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    } else {
        // MAIN QUIZ EXPLORER - Unified scrollable LazyColumn for smooth, full viewing of all syllabus units
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceLight)
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Top Zahira Header Bar & Automatic Schedule
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ZahiraMaroon)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "A/L Quiz Arena",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Sri Lankan A/L English Medium Syllabus",
                                fontSize = 12.sp,
                                color = ZahiraGold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Cloud Study & Storage Hub Button (Zero device bloat)
                            FilledTonalButton(
                                onClick = { showCloudHub = true },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.2f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_cloud_hub_quiz")
                            ) {
                                Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cloud Hub", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            // Leaderboard Button
                            FilledTonalButton(
                                onClick = { showLeaderboard = true },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = ZahiraGold,
                                    contentColor = Color(0xFF451A03)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_leaderboard")
                            ) {
                                Icon(Icons.Default.Leaderboard, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Top 10", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Wednesday 7:30pm - 10:00pm Automatic Schedule Card (No manual switch)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLiveNow) Color(0xFF450A0A) else Color(0xFF3B0713)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isLiveNow) Color(0xFFEF4444) else ZahiraGold.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isLiveNow) Color(0xFFEF4444) else ZahiraGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isLiveNow) Icons.Default.AlarmOn else Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = if (isLiveNow) Color.White else ZahiraGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isLiveNow) Color(0xFFEF4444) else Color(0xFFD97706).copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = if (isLiveNow) "🔴 LIVE QUIZ ACTIVE" else "⏰ AUTOMATIC SCHEDULE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isLiveNow) Color.White else ZahiraGold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Wednesday Night 7:30 PM – 10:00 PM",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isLiveNow)
                                        "Live synchronized quiz is active! Top 10 results revealed after 10:00 PM."
                                    else
                                        "Enabled automatically on Wednesday 7:30 PM – 10:00 PM without manual intervention.",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // Subject Selector Tabs
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableSubjects) { subject ->
                        val isSelected = selectedSubject.id == subject.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubject = subject },
                            label = { Text(subject.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ZahiraMaroon,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) ZahiraMaroon else Color(0xFFCBD5E1)
                            )
                        )
                    }
                }
            }

            // Gemini AI Quiz Generator Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF4E3)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ZahiraGold)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ZahiraGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF451A03), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gemini AI Quiz Generation",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF451A03)
                            )
                            Text(
                                text = "Generate synchronized A/L questions for ${selectedSubject.name}",
                                fontSize = 11.sp,
                                color = Color(0xFF78350F)
                            )
                        }
                        Button(
                            onClick = {
                                isGeneratingAiQuiz = true
                                coroutineScope.launch {
                                    activeQuizTitle = "Gemini AI Challenge: ${selectedSubject.name}"
                                    activeUnitNumber = 1
                                    activeQuizQuestions = SyllabusData.getQuestionsForUnit(selectedSubject.id, 1)
                                    currentQuestionIndex = 0
                                    selectedOptionIndex = null
                                    scoreCount = 0
                                    showExplanation = false
                                    isGeneratingAiQuiz = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ZahiraMaroon),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_gemini_generate_quiz")
                        ) {
                            if (isGeneratingAiQuiz) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Start AI Quiz", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Syllabus Units Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SYLLABUS UNITS (${selectedSubject.units.size} UNITS AVAILABLE)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZahiraMaroon
                    )
                    Text(
                        text = selectedSubject.code,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
            }

            // Syllabus Units List - Fully viewable & smoothly scrollable
            items(selectedSubject.units, key = { "${selectedSubject.id}_${it.unitNumber}" }) { unit ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                    SyllabusUnitCard(
                        subject = selectedSubject,
                        unit = unit,
                        isLiveMode = isLiveNow,
                        onStartQuiz = {
                            activeQuizTitle = "${selectedSubject.name} - Unit ${unit.unitNumber}"
                            activeUnitNumber = unit.unitNumber
                            activeQuizQuestions = SyllabusData.getQuestionsForUnit(selectedSubject.id, unit.unitNumber)
                            currentQuestionIndex = 0
                            selectedOptionIndex = null
                            scoreCount = 0
                            showExplanation = false
                        }
                    )
                }
            }
        }
    }

    // Top 10 Leaderboard Bottom Sheet
    if (showLeaderboard) {
        ModalBottomSheet(
            onDismissRequest = { showLeaderboard = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                if (!isResultsAvailable) {
                    // USER REQUIREMENT:
                    // "remove all the top 10 users show in the app and the top ten should be seen after the 10pm on wednesday after the results it should show the real time points with real time users"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(ZahiraGold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = ZahiraMaroon,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Top 10 Leaderboard Hidden",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ZahiraMaroon
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "The Top 10 Leaderboard is released on Wednesday after 10:00 PM following the live quiz results.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "AUTOMATED SCHEDULE & RULES",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ZahiraMaroon
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "• Live Quiz: Wednesday 7:30 PM – 10:00 PM\n• Top 10 Results: Wednesday after 10:00 PM\n• Real-Time Points: Dynamically displayed for real participating students",
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Current logged-in user's real-time point balance
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
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
                                        text = "Your Real-Time Score",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                    Text(
                                        text = currentUser.fullName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = currentUser.stream.displayName,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White
                                ) {
                                    Text(
                                        text = "${currentUser.spPoints} SP Points",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF92400E),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { showLeaderboard = false },
                            colors = ButtonDefaults.buttonColors(containerColor = ZahiraMaroon),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Got It", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Wednesday after 10:00 PM: Show Real-Time Top 10 with Real-Time Users
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🏆 Top 10 A/L Leaderboard",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ZahiraMaroon
                            )
                            Text(
                                text = "Official Wednesday Results • Real-Time Points & Real Students",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (leaderboardEntries.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No student participants recorded yet for this week's live quiz.",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(leaderboardEntries) { entry ->
                                val isCurrentUser = entry.studentId == currentUser.id
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isCurrentUser) ZahiraMaroon.copy(alpha = 0.08f) else Color(0xFFF8FAFC)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (entry.rank == 1) ZahiraGold else Color(0xFFE2E8F0)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Rank Number
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (entry.rank) {
                                                        1 -> ZahiraGold
                                                        2 -> Color(0xFF94A3B8)
                                                        3 -> Color(0xFFCD7F32)
                                                        else -> Color(0xFFE2E8F0)
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "#${entry.rank}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (entry.rank in 1..3) Color.White else TextPrimary
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = entry.studentName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                if (isCurrentUser) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("(You)", fontSize = 11.sp, color = ZahiraMaroon, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Text(
                                                text = "${entry.stream.displayName} • Real-Time Score",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }

                                        // Real-time SP Points
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFFEF3C7)
                                        ) {
                                            Text(
                                                text = "${entry.score} SP",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF92400E),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showCloudHub) {
        com.example.ui.components.CloudStudyHubDialog(
            currentUser = currentUser,
            onDismiss = { showCloudHub = false }
        )
    }
}

@Composable
fun SyllabusUnitCard(
    subject: SyllabusSubject,
    unit: SyllabusUnit,
    isLiveMode: Boolean,
    onStartQuiz: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Unit Number Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ZahiraMaroon.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "U${unit.unitNumber}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = ZahiraMaroon
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Unit ${unit.unitNumber}: ${unit.unitName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = unit.description,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onStartQuiz,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLiveMode) ZahiraMaroon else ZahiraNavy
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("btn_unit_${unit.unitNumber}")
            ) {
                Text(
                    text = if (isLiveMode) "Live Test" else "Quiz",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
