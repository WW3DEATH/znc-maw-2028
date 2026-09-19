package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DataRepository
import com.example.model.AcademicStream
import com.example.model.UserRole
import com.example.ui.theme.ZahiraGold
import com.example.ui.theme.ZahiraMaroon
import com.example.ui.theme.ZahiraMaroonDark

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("mnmjaasim@gmail.com") }
    var password by remember { mutableStateOf("mnmjaasim2010") }
    var fullName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var selectedStream by remember { mutableStateOf(AcademicStream.PHYSICAL_SCIENCE) }
    var passwordVisible by remember { mutableStateOf(false) }
    var keepMeSignedIn by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ZahiraMaroon,
                        ZahiraMaroonDark,
                        Color(0xFF1A0207)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // School Logo / Crest Header
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, ZahiraGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.zsp_icon_1789110002401),
                    contentDescription = "Zahira College Crest",
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "ZSP - 28",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ZahiraGold,
                letterSpacing = 2.sp
            )

            Text(
                text = "ZAHIRA COLLEGE MAWANELLA",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f),
                letterSpacing = 1.2.sp
            )

            Text(
                text = "Discipline • Dynamism • Dexterity",
                fontSize = 11.sp,
                color = ZahiraGold.copy(alpha = 0.85f),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Sign In / Sign Up Tab Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(4.dp)
                    ) {
                        Button(
                            onClick = {
                                isSignUp = false
                                errorMessage = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tab_sign_in"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isSignUp) ZahiraMaroon else Color.Transparent,
                                contentColor = if (!isSignUp) Color.White else Color.Gray
                            ),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Text("Sign In", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                isSignUp = true
                                errorMessage = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tab_sign_up"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSignUp) ZahiraMaroon else Color.Transparent,
                                contentColor = if (isSignUp) Color.White else Color.Gray
                            ),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Text("Sign Up", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Role Picker & Full Name (Only for Sign Up)
                    AnimatedVisibility(visible = isSignUp) {
                        Column {
                            Text(
                                text = "SELECT YOUR ROLE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedRole == UserRole.STUDENT,
                                    onClick = { selectedRole = UserRole.STUDENT },
                                    label = { Text("Student") },
                                    leadingIcon = {
                                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp))
                                    },
                                    modifier = Modifier.weight(1f).testTag("role_student")
                                )
                                FilterChip(
                                    selected = selectedRole == UserRole.TEACHER,
                                    onClick = { selectedRole = UserRole.TEACHER },
                                    label = { Text("Teacher") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                                    },
                                    modifier = Modifier.weight(1f).testTag("role_teacher")
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Name") },
                                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth().testTag("input_fullname"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = { Text("Email Address") },
                        placeholder = { Text("student@zahira.lk") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("input_email"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("input_password"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Keep me signed in option (User request: Add a keep me signed in option when signing in)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { keepMeSignedIn = !keepMeSignedIn }
                            .padding(vertical = 4.dp, horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = keepMeSignedIn,
                            onCheckedChange = { keepMeSignedIn = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = ZahiraMaroon,
                                checkmarkColor = Color.White,
                                uncheckedColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.testTag("checkbox_keep_signed_in")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Keep me signed in until I log out",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155)
                        )
                    }

                    // Academic Stream Selection (Required on Sign Up)
                    AnimatedVisibility(visible = isSignUp) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "SELECT ACADEMIC STREAM (CANNOT BE CHANGED)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ZahiraMaroon,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Once selected during registration, your stream is locked.",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StreamChoiceCard(
                                    title = "Physical Science",
                                    subtitle = "Maths, Physics, Chem, ICT",
                                    isSelected = selectedStream == AcademicStream.PHYSICAL_SCIENCE,
                                    modifier = Modifier.weight(1f).testTag("stream_physical"),
                                    onClick = { selectedStream = AcademicStream.PHYSICAL_SCIENCE }
                                )

                                StreamChoiceCard(
                                    title = "Bio Science",
                                    subtitle = "Bio, Physics, Chem, ICT",
                                    isSelected = selectedStream == AcademicStream.BIO_SCIENCE,
                                    modifier = Modifier.weight(1f).testTag("stream_bio"),
                                    onClick = { selectedStream = AcademicStream.BIO_SCIENCE }
                                )
                            }
                        }
                    }

                    errorMessage?.let { err ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Action Button
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Please enter both email and password."
                                return@Button
                            }

                            if (isSignUp) {
                                if (fullName.isBlank()) {
                                    errorMessage = "Please enter your full name."
                                    return@Button
                                }
                                val result = DataRepository.signup(
                                    fullName = fullName,
                                    email = email,
                                    role = selectedRole,
                                    stream = selectedStream,
                                    keepSignedIn = keepMeSignedIn,
                                    context = context
                                )
                                if (result.isSuccess) {
                                    onLoginSuccess()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Signup failed"
                                }
                            } else {
                                val result = DataRepository.login(
                                    email = email,
                                    pass = password,
                                    keepSignedIn = keepMeSignedIn,
                                    context = context
                                )
                                if (result.isSuccess) {
                                    onLoginSuccess()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_submit_auth"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ZahiraMaroon)
                    ) {
                        Text(
                            text = if (isSignUp) "Create Account" else "Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Grade 12 Physical & Bio Science English Medium\nSyllabus Aligned • Real-Time Collaboration",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StreamChoiceCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ZahiraMaroon.copy(alpha = 0.1f) else Color(0xFFF8FAFC)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, ZahiraMaroon) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(selectedColor = ZahiraMaroon),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) ZahiraMaroon else Color(0xFF1E293B)
                )
            }
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(start = 26.dp, top = 2.dp)
            )
        }
    }
}
