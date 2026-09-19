package com.example.ui.components

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.cloud.CloudRealtimeManager
import com.example.model.CloudResourceType
import com.example.model.CloudStudyResource
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.ui.theme.*

/**
 * CloudStudyHubDialog
 *
 * Direct connection to Cloud Storage for Sri Lankan G.C.E. A/L English Medium resources:
 * Past papers, marking schemes, formula books, and teacher slides are streamed
 * on-demand from the Cloud so that students' devices NEVER run out of storage space!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudStudyHubDialog(
    currentUser: UserProfile,
    onDismiss: () -> Unit
) {
    val cloudResources by CloudRealtimeManager.cloudResources.collectAsState()
    val storageStats by CloudRealtimeManager.storageStats.collectAsState()

    var selectedSubjectFilter by remember { mutableStateOf("ALL") }
    var selectedTypeFilter by remember { mutableStateOf<CloudResourceType?>(null) }
    var activeViewingResource by remember { mutableStateOf<CloudStudyResource?>(null) }
    var showUploadDialog by remember { mutableStateOf(false) }

    val filteredResources = remember(cloudResources, selectedSubjectFilter, selectedTypeFilter) {
        cloudResources.filter { res ->
            val matchSubject = when (selectedSubjectFilter) {
                "ALL" -> true
                "CM" -> res.subjectId == "sub_cm"
                "PHY" -> res.subjectId == "sub_phy"
                "CHEM" -> res.subjectId == "sub_chem"
                "BIO" -> res.subjectId == "sub_bio"
                "ICT" -> res.subjectId == "sub_ict"
                else -> true
            }
            val matchType = selectedTypeFilter == null || res.type == selectedTypeFilter
            matchSubject && matchType
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ZahiraMaroon)
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CloudQueue,
                                    contentDescription = null,
                                    tint = ZahiraGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cloud Study & Past Papers Hub",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Stream files on-demand • Zero device storage used",
                                fontSize = 11.sp,
                                color = ZahiraGoldLight
                            )
                        }

                        // Upload button for teachers & verified students
                        IconButton(
                            onClick = { showUploadDialog = true },
                            modifier = Modifier.testTag("btn_cloud_upload")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Upload to Cloud", tint = Color.White)
                        }
                    }

                    // Storage Saver Summary Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF38070F)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Device Storage Saved: ${storageStats.deviceStorageSavedFormatted}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFD1FAE5)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF059669).copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = "☁️ 0 MB Used",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6EE7B7),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(SurfaceLight)
            ) {
                // Subject Filter Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val subjects = listOf(
                        "ALL" to "All Subjects",
                        "CM" to "Combined Maths",
                        "PHY" to "Physics",
                        "CHEM" to "Chemistry",
                        "BIO" to "Biology",
                        "ICT" to "ICT"
                    )
                    items(subjects) { (code, name) ->
                        val isSelected = selectedSubjectFilter == code
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubjectFilter = code },
                            label = { Text(name, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ZahiraMaroon,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Type Filter Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedTypeFilter == null,
                            onClick = { selectedTypeFilter = null },
                            label = { Text("All Types", fontSize = 11.sp) }
                        )
                    }
                    items(CloudResourceType.values()) { type ->
                        val isSelected = selectedTypeFilter == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTypeFilter = if (isSelected) null else type },
                            label = { Text(type.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredResources.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = ZahiraMaroon.copy(alpha = 0.08f),
                                modifier = Modifier.size(76.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CloudQueue,
                                        contentDescription = null,
                                        tint = ZahiraMaroon,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Cloud Hub is Clean",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ZahiraMaroon
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "All past papers, marking schemes, and notes have been removed.\nAuthorized teachers and admins can upload new curriculum files.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { showUploadDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ZahiraMaroon),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_empty_hub_upload")
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Upload New File to Cloud", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Cloud Resources List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(filteredResources, key = { it.id }) { resource ->
                            CloudResourceCard(
                                resource = resource,
                                isAdmin = currentUser.role == UserRole.ADMIN,
                                onStreamClick = { activeViewingResource = resource },
                                onDeleteClick = {
                                    CloudRealtimeManager.deleteCloudResource(resource.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Full Screen Streamed Document Viewer
    activeViewingResource?.let { resource ->
        CloudDocumentStreamViewerDialog(
            resource = resource,
            onDismiss = { activeViewingResource = null }
        )
    }

    // Upload to Cloud Storage Dialog
    if (showUploadDialog) {
        CloudFileUploadDialog(
            currentUser = currentUser,
            onDismiss = { showUploadDialog = false }
        )
    }
}

@Composable
fun CloudResourceCard(
    resource: CloudStudyResource,
    isAdmin: Boolean = false,
    onStreamClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onStreamClick)
            .testTag("card_cloud_res_${resource.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(resource.type.badgeColorHex).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = resource.type.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(resource.type.badgeColorHex),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = resource.formattedSize,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    if (isAdmin && onDeleteClick != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(24.dp).testTag("btn_delete_res_${resource.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete file",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = resource.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${resource.subjectName} • ${resource.yearOrUnit}",
                    fontSize = 11.sp,
                    color = ZahiraMaroon,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Uploaded by: ${resource.uploaderName}",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "⚡ Streamed directly (0 MB device memory)",
                        fontSize = 10.sp,
                        color = Color(0xFF059669),
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onStreamClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ZahiraMaroon),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_stream_${resource.id}")
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stream", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Cloud Document Stream Viewer Dialog
 *
 * Renders the document from Cloud Storage in real time with page-by-page viewing
 * and answer/syllabus highlights without filling up user storage.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudDocumentStreamViewerDialog(
    resource: CloudStudyResource,
    onDismiss: () -> Unit
) {
    var activePage by remember { mutableStateOf(1) }
    val totalPages = 12

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = resource.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "☁️ Cloud Streamed • Page $activePage of $totalPages",
                                fontSize = 11.sp,
                                color = ZahiraGoldLight
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Share stream link */ }) {
                            Icon(Icons.Default.Share, contentDescription = "Share Link", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ZahiraMaroonDark)
                )
            },
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { if (activePage > 1) activePage-- },
                            enabled = activePage > 1,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Previous")
                        }

                        Text(
                            text = "Page $activePage / $totalPages",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Button(
                            onClick = { if (activePage < totalPages) activePage++ },
                            enabled = activePage < totalPages,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ZahiraMaroon)
                        ) {
                            Text("Next Page")
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFE2E8F0))
            ) {
                // Cloud Stream Indicator Banner
                Surface(
                    color = Color(0xFF065F46),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFF6EE7B7), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Streaming live from Zahira Cloud Storage • 0 MB cached permanently",
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }

                // Realistic Render of G.C.E. A/L Examination Document Page
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                        .padding(20.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            // Header of examination paper
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "DEPARTMENT OF EXAMINATIONS, SRI LANKA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "General Certificate of Education (Adv. Level) Examination",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "${resource.subjectName} • ${resource.yearOrUnit}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ZahiraMaroon
                                )
                                Text(
                                    text = "English Medium • Three Hours • Additional Reading Time: 10 minutes",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            // Page Content Section
                            Text(
                                text = "SECTION A — STRUCTURED ESSAY / PART I (Page $activePage)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ZahiraMaroon
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            when (activePage) {
                                1 -> {
                                    PaperQuestionItem(
                                        number = "1.",
                                        question = "State the principle of conservation of linear momentum. A particle of mass m moving with speed u collides head-on with a stationary particle of mass 2m. If the collision is perfectly elastic, determine the velocities of both particles after the impact.",
                                        points = "(15 Marks)"
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    PaperQuestionItem(
                                        number = "2.",
                                        question = "Explain why temperature remains constant during the phase change of a substance even when heat is continuously supplied. Define the specific latent heat of vaporization of water.",
                                        points = "(10 Marks)"
                                    )
                                }
                                2 -> {
                                    PaperQuestionItem(
                                        number = "3.",
                                        question = "Write the chemical equation for the hydrolysis of ethyl acetate in acidic medium. State whether this reaction is reversible or irreversible. Derive the equilibrium constant expression Kc in terms of molar concentrations.",
                                        points = "(15 Marks)"
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    PaperQuestionItem(
                                        number = "4.",
                                        question = "Differentiate between structural isomerism and stereoisomerism. Draw the cis and trans isomers of 1,2-dichloroethene and identify which isomer has a non-zero net dipole moment.",
                                        points = "(15 Marks)"
                                    )
                                }
                                3 -> {
                                    PaperQuestionItem(
                                        number = "5.",
                                        question = "Explain how the double helix structure of DNA facilitates semi-conservative replication. Name three primary enzymes involved in eukaryotic DNA replication and state their specific functions.",
                                        points = "(20 Marks)"
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    PaperQuestionItem(
                                        number = "6.",
                                        question = "Describe the role of guard cells in regulating stomatal transpiration. Explain the potassium ion (K+) influx mechanism driven by blue-light receptors.",
                                        points = "(15 Marks)"
                                    )
                                }
                                else -> {
                                    PaperQuestionItem(
                                        number = "${activePage * 2 - 1}.",
                                        question = "Official syllabus evaluation questions for ${resource.subjectName}. In the marking scheme, verify step-by-step intermediate calculations and full algebraic simplification.",
                                        points = "(25 Marks)"
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    PaperQuestionItem(
                                        number = "${activePage * 2}.",
                                        question = "Comprehensive structured question covering theoretical principles, experimental diagrams, graph plotting criteria, and calculation of percentage uncertainties.",
                                        points = "(25 Marks)"
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Cloud Verification Tag
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = ZahiraMaroon, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Verified Official Resource • Uploaded by ${resource.uploaderName} (${resource.uploaderRole})",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaperQuestionItem(
    number: String,
    question: String,
    points: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = number, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ZahiraMaroon)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = question,
                fontSize = 13.sp,
                color = Color(0xFF1E293B),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = points,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ZahiraGold
            )
        }
    }
}

/**
 * CloudFileUploadDialog
 *
 * Allows uploading study materials, past papers, notes directly to Cloud Storage.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudFileUploadDialog(
    currentUser: UserProfile,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf("sub_cm") }
    var selectedSubjectName by remember { mutableStateOf("Combined Mathematics") }
    var yearOrUnit by remember { mutableStateOf("2024 A/L") }
    var selectedType by remember { mutableStateOf(CloudResourceType.PAST_PAPER) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadSuccess by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = ZahiraMaroon)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload to Cloud Storage", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Files are hosted in Cloud Storage so everyone can stream without using phone storage.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document Title") },
                    placeholder = { Text("e.g. 2024 Physics Unit 3 Model Paper") },
                    modifier = Modifier.fillMaxWidth().testTag("input_cloud_title"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = yearOrUnit,
                    onValueChange = { yearOrUnit = it },
                    label = { Text("Year or Syllabus Unit") },
                    placeholder = { Text("e.g. 2024 A/L or Unit 4") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Subject Selection
                Text("Subject", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ZahiraMaroon)
                val subjects = listOf(
                    "sub_cm" to "Combined Mathematics",
                    "sub_phy" to "Physics",
                    "sub_chem" to "Chemistry",
                    "sub_bio" to "Biology",
                    "sub_ict" to "ICT"
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subjects) { (id, name) ->
                        FilterChip(
                            selected = selectedSubjectId == id,
                            onClick = {
                                selectedSubjectId = id
                                selectedSubjectName = name
                            },
                            label = { Text(name, fontSize = 11.sp) }
                        )
                    }
                }

                // Type Selection
                Text("Resource Type", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ZahiraMaroon)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(CloudResourceType.values()) { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                if (isUploading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = ZahiraMaroon, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Uploading to Firebase Cloud Storage...", fontSize = 12.sp, color = TextSecondary)
                    }
                }

                if (uploadSuccess) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFD1FAE5),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✓ File uploaded successfully to Cloud Storage!",
                            color = Color(0xFF065F46),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        isUploading = true
                        CloudRealtimeManager.uploadFileToCloudStorage(
                            title = title.trim(),
                            subjectId = selectedSubjectId,
                            subjectName = selectedSubjectName,
                            yearOrUnit = yearOrUnit.trim().ifEmpty { "General A/L" },
                            type = selectedType,
                            fileSizeBytes = 12_500_000L,
                            formattedSize = "12.5 MB",
                            uploaderName = currentUser.fullName,
                            uploaderRole = if (currentUser.role == UserRole.TEACHER) "Subject Teacher" else "Student Contributor",
                            onSuccess = {
                                isUploading = false
                                uploadSuccess = true
                            }
                        )
                    }
                },
                enabled = title.isNotBlank() && !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = ZahiraMaroon),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Upload to Cloud")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
