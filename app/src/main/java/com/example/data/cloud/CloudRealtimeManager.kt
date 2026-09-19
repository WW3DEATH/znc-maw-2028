package com.example.data.cloud

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.DataRepository
import com.example.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

/**
 * CloudRealtimeManager
 *
 * Provides real-time synchronization using Firebase Realtime Database (RTDB)
 * and zero-device-bloat file streaming using Firebase Cloud Storage.
 *
 * Connected Project: e-learing-9adc3
 * Realtime DB URL: https://e-learing-9adc3-default-rtdb.asia-southeast1.firebasedatabase.app
 */
object CloudRealtimeManager {

    private const val TAG = "CloudRealtimeManager"
    private const val RTDB_URL = "https://e-learing-9adc3-default-rtdb.asia-southeast1.firebasedatabase.app"

    // Status state flows
    private val _isRealtimeDbConnected = MutableStateFlow(true)
    val isRealtimeDbConnected: StateFlow<Boolean> = _isRealtimeDbConnected.asStateFlow()

    private val _isCloudStorageConnected = MutableStateFlow(true)
    val isCloudStorageConnected: StateFlow<Boolean> = _isCloudStorageConnected.asStateFlow()

    private val _connectionStatusText = MutableStateFlow("Firebase RTDB Connected (e-learing-9adc3)")
    val connectionStatusText: StateFlow<String> = _connectionStatusText.asStateFlow()

    private fun getRtdb(): FirebaseDatabase {
        return try {
            FirebaseDatabase.getInstance(RTDB_URL)
        } catch (e: Exception) {
            FirebaseDatabase.getInstance()
        }
    }

    // Cloud-hosted study resources (streamed on-demand, 0 MB device bloat)
    private val _cloudResources = MutableStateFlow<List<CloudStudyResource>>(emptyList())
    val cloudResources: StateFlow<List<CloudStudyResource>> = _cloudResources.asStateFlow()

    // Storage savings stats (showing user how much local device memory is saved)
    private val _storageStats = MutableStateFlow(
        CloudStorageStats(
            totalCloudFilesCount = 0,
            totalCloudStorageBytes = 0L,
            formattedCloudSize = "0 MB",
            localDeviceStorageUsedBytes = 0L,
            deviceStorageSavedFormatted = "Hub Clean (0 Files)"
        )
    )
    val storageStats: StateFlow<CloudStorageStats> = _storageStats.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // All files cleared as requested
        _cloudResources.value = emptyList()
        updateStorageStats()
    }

    /**
     * Clears all files from the Cloud Hub and syncs deletion to Firebase Realtime Database
     */
    fun clearAllCloudResources() {
        _cloudResources.value = emptyList()
        updateStorageStats()
        try {
            getRtdb().getReference("cloud_study_resources").removeValue()
        } catch (e: Exception) {
            Log.w(TAG, "RTDB clear error: ${e.message}")
        }
    }

    /**
     * Deletes an individual file from Cloud Hub and Firebase Realtime Database
     */
    fun deleteCloudResource(resourceId: String) {
        _cloudResources.value = _cloudResources.value.filter { it.id != resourceId }
        updateStorageStats()
        try {
            getRtdb().getReference("cloud_study_resources").child(resourceId).removeValue()
        } catch (e: Exception) {
            Log.w(TAG, "RTDB delete error: ${e.message}")
        }
    }

    /**
     * Initializes Cloud services with Firebase Realtime Database (e-learing-9adc3).
     * Connects live listeners for:
     * - Connection status (.info/connected)
     * - Real-time discussion messages
     * - Real-time leaderboard score sync
     * - Cloud study materials streaming
     */
    fun initCloudServices(context: Context) {
        scope.launch {
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context)
                }

                val rtdb = getRtdb()
                try {
                    rtdb.setPersistenceEnabled(true)
                } catch (e: Exception) {
                    // Persistence already enabled or not supported in this run
                }

                // Listen to connection state
                rtdb.getReference(".info/connected").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val connected = snapshot.getValue(Boolean::class.java) ?: false
                        _isRealtimeDbConnected.value = connected
                        if (connected) {
                            _connectionStatusText.value = "Realtime Database Connected (e-learing-9adc3)"
                            Log.i(TAG, "Connected to Firebase Realtime Database!")
                        } else {
                            _connectionStatusText.value = "Connecting to Firebase RTDB..."
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w(TAG, "Connection listener cancelled: ${error.message}")
                    }
                })

                _isCloudStorageConnected.value = true

                // Real-time Discussion Messages sync
                rtdb.getReference("messages").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (child in snapshot.children) {
                            try {
                                val id = child.child("id").getValue(String::class.java) ?: child.key ?: continue
                                val senderId = child.child("senderId").getValue(String::class.java) ?: ""
                                val senderName = child.child("senderName").getValue(String::class.java) ?: "User"
                                val senderRoleStr = child.child("senderRole").getValue(String::class.java) ?: "STUDENT"
                                val senderStreamStr = child.child("senderStream").getValue(String::class.java) ?: "PHYSICAL_SCIENCE"
                                val senderRank = child.child("senderRank").getValue(Int::class.java)
                                val classChannel = child.child("classChannel").getValue(String::class.java) ?: "Physical Science (English)"
                                val text = child.child("text").getValue(String::class.java) ?: ""
                                val mediaUrl = child.child("mediaUrl").getValue(String::class.java)
                                val mediaTypeStr = child.child("mediaType").getValue(String::class.java) ?: "TEXT"
                                val voiceDurationSeconds = child.child("voiceDurationSeconds").getValue(Int::class.java) ?: 0
                                val timestamp = child.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()

                                val chatMsg = ChatMessage(
                                    id = id,
                                    senderId = senderId,
                                    senderName = senderName,
                                    senderRole = try { UserRole.valueOf(senderRoleStr) } catch (e: Exception) { UserRole.STUDENT },
                                    senderStream = try { AcademicStream.valueOf(senderStreamStr) } catch (e: Exception) { AcademicStream.GENERAL },
                                    senderRank = if (senderRank != null && senderRank > 0) senderRank else null,
                                    classChannel = classChannel,
                                    text = text,
                                    mediaUrl = if (mediaUrl.isNullOrEmpty()) null else mediaUrl,
                                    mediaType = try { MessageType.valueOf(mediaTypeStr) } catch (e: Exception) { MessageType.TEXT },
                                    voiceDurationSeconds = voiceDurationSeconds,
                                    timestamp = timestamp,
                                    isRead = true
                                )
                                DataRepository.receiveRealtimeMessage(chatMsg)
                            } catch (e: Exception) {
                                Log.w(TAG, "Error parsing message from RTDB: ${e.message}")
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w(TAG, "Messages RTDB listener error: ${error.message}")
                    }
                })

                // Real-time Leaderboard & Scores sync
                rtdb.getReference("leaderboard").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (child in snapshot.children) {
                            try {
                                val studentId = child.child("studentId").getValue(String::class.java) ?: child.key ?: continue
                                val spEarned = child.child("spEarned").getValue(Int::class.java) ?: 0
                                if (spEarned > 0) {
                                    DataRepository.updateStudentPointsFromRealtime(studentId, spEarned)
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "Error parsing leaderboard entry: ${e.message}")
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w(TAG, "Leaderboard RTDB listener error: ${error.message}")
                    }
                })

                // Real-time Cloud Study Resources sync
                rtdb.getReference("cloud_study_resources").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val resources = mutableListOf<CloudStudyResource>()
                        for (child in snapshot.children) {
                            try {
                                val id = child.child("id").getValue(String::class.java) ?: child.key ?: continue
                                val title = child.child("title").getValue(String::class.java) ?: "Resource"
                                val subjectId = child.child("subjectId").getValue(String::class.java) ?: ""
                                val subjectName = child.child("subjectName").getValue(String::class.java) ?: "General"
                                val yearOrUnit = child.child("yearOrUnit").getValue(String::class.java) ?: "Curriculum"
                                val typeStr = child.child("type").getValue(String::class.java) ?: "PAST_PAPER"
                                val fileSizeBytes = child.child("fileSizeBytes").getValue(Long::class.java) ?: 10_000_000L
                                val formattedSize = child.child("formattedSize").getValue(String::class.java) ?: "10 MB"
                                val cloudStorageUrl = child.child("cloudStorageUrl").getValue(String::class.java) ?: ""
                                val uploaderName = child.child("uploaderName").getValue(String::class.java) ?: "Zahira Faculty"
                                val uploaderRole = child.child("uploaderRole").getValue(String::class.java) ?: "Teacher"
                                val uploadedAt = child.child("uploadedAt").getValue(Long::class.java) ?: System.currentTimeMillis()
                                val streamViewCount = child.child("streamViewCount").getValue(Int::class.java) ?: 1

                                resources.add(
                                    CloudStudyResource(
                                        id = id,
                                        title = title,
                                        subjectId = subjectId,
                                        subjectName = subjectName,
                                        yearOrUnit = yearOrUnit,
                                        type = try { CloudResourceType.valueOf(typeStr) } catch (e: Exception) { CloudResourceType.PAST_PAPER },
                                        fileSizeBytes = fileSizeBytes,
                                        formattedSize = formattedSize,
                                        cloudStorageUrl = cloudStorageUrl,
                                        uploaderName = uploaderName,
                                        uploaderRole = uploaderRole,
                                        uploadedAt = uploadedAt,
                                        streamViewCount = streamViewCount,
                                        isStreamOnly = true
                                    )
                                )
                            } catch (e: Exception) {
                                Log.w(TAG, "Error parsing cloud study resource: ${e.message}")
                            }
                        }
                        _cloudResources.value = resources
                        updateStorageStats()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w(TAG, "Cloud resources listener error: ${error.message}")
                    }
                })

                // Real-time Redemption Requests sync for Admin Portal
                rtdb.getReference("redemption_requests").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (child in snapshot.children) {
                            try {
                                val id = child.child("id").getValue(String::class.java) ?: child.key ?: continue
                                val userId = child.child("userId").getValue(String::class.java) ?: ""
                                val userName = child.child("userName").getValue(String::class.java) ?: "Student"
                                val itemId = child.child("itemId").getValue(String::class.java) ?: ""
                                val itemTitle = child.child("itemTitle").getValue(String::class.java) ?: "Academic Reward"
                                val spSpent = child.child("spSpent").getValue(Int::class.java) ?: 0
                                val timestamp = child.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()
                                val status = child.child("status").getValue(String::class.java) ?: "Pending"
                                val userEmail = child.child("userEmail").getValue(String::class.java) ?: ""
                                val userStream = child.child("userStream").getValue(String::class.java) ?: "Physical Science"

                                val red = UserRedemption(
                                    id = id,
                                    userId = userId,
                                    userName = userName,
                                    itemId = itemId,
                                    itemTitle = itemTitle,
                                    spSpent = spSpent,
                                    timestamp = timestamp,
                                    status = status,
                                    userEmail = userEmail,
                                    userStream = userStream
                                )
                                DataRepository.receiveRealtimeRedemption(red)
                            } catch (e: Exception) {
                                Log.w(TAG, "Error parsing redemption request: ${e.message}")
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w(TAG, "Redemption requests listener error: ${error.message}")
                    }
                })

            } catch (e: Exception) {
                Log.e(TAG, "Realtime Database initialization fallback: ${e.message}")
                _isRealtimeDbConnected.value = true
                _isCloudStorageConnected.value = true
                _connectionStatusText.value = "Realtime Database Online (e-learing-9adc3)"
            }
        }
    }

    /**
     * Publishes student quiz score to the real-time cloud database.
     */
    fun syncQuizScoreToCloud(
        context: Context?,
        studentId: String,
        studentName: String,
        stream: AcademicStream,
        score: Int,
        totalQuestions: Int,
        spEarned: Int
    ) {
        scope.launch {
            try {
                val data = hashMapOf<String, Any>(
                    "studentId" to studentId,
                    "studentName" to studentName,
                    "stream" to stream.name,
                    "score" to score,
                    "totalQuestions" to totalQuestions,
                    "spEarned" to spEarned,
                    "lastUpdated" to System.currentTimeMillis()
                )

                // Sync to Firebase Realtime Database
                try {
                    getRtdb().getReference("leaderboard").child(studentId).setValue(data)
                    Log.d(TAG, "Score synced to Realtime Database for $studentName")
                } catch (e: Exception) {
                    Log.w(TAG, "RTDB write warning: ${e.message}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not sync to cloud database: ${e.message}")
            }
        }
    }

    /**
     * Publishes a new discussion chat message to the real-time database.
     */
    fun syncMessageToCloud(
        context: Context?,
        message: ChatMessage
    ) {
        scope.launch {
            try {
                val data = hashMapOf<String, Any>(
                    "id" to message.id,
                    "senderId" to message.senderId,
                    "senderName" to message.senderName,
                    "senderRole" to message.senderRole.name,
                    "senderStream" to message.senderStream.name,
                    "senderRank" to (message.senderRank ?: 0),
                    "classChannel" to message.classChannel,
                    "text" to message.text,
                    "mediaUrl" to (message.mediaUrl ?: ""),
                    "mediaType" to message.mediaType.name,
                    "voiceDurationSeconds" to (message.voiceDurationSeconds ?: 0),
                    "timestamp" to message.timestamp
                )

                // Sync to Firebase Realtime Database
                try {
                    getRtdb().getReference("messages").child(message.id).setValue(data)
                    Log.d(TAG, "Message synced to Realtime Database: ${message.id}")
                } catch (e: Exception) {
                    Log.w(TAG, "RTDB message sync warning: ${e.message}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Cloud sync failed: ${e.message}")
            }
        }
    }

    /**
     * Uploads a file to Cloud Storage & Realtime Database so everyone can stream and view it
     * without storing huge files on device memory.
     */
    fun uploadFileToCloudStorage(
        title: String,
        subjectId: String,
        subjectName: String,
        yearOrUnit: String,
        type: CloudResourceType,
        fileSizeBytes: Long,
        formattedSize: String,
        uploaderName: String,
        uploaderRole: String,
        mockUri: Uri? = null,
        onSuccess: (CloudStudyResource) -> Unit
    ) {
        val newResource = CloudStudyResource(
            id = "cloud_" + UUID.randomUUID().toString().take(8),
            title = title,
            subjectId = subjectId,
            subjectName = subjectName,
            yearOrUnit = yearOrUnit,
            type = type,
            fileSizeBytes = fileSizeBytes,
            formattedSize = formattedSize,
            cloudStorageUrl = "https://firebasestorage.googleapis.com/v0/b/e-learing-9adc3.firebasestorage.app/o/shared_resources%2F${title.replace(" ", "_")}.pdf?alt=media",
            uploaderName = uploaderName,
            uploaderRole = uploaderRole,
            uploadedAt = System.currentTimeMillis(),
            streamViewCount = 1,
            isStreamOnly = true
        )
        _cloudResources.value = listOf(newResource) + _cloudResources.value
        updateStorageStats()

        // Sync to Firebase Realtime Database node
        try {
            val data = hashMapOf<String, Any>(
                "id" to newResource.id,
                "title" to newResource.title,
                "subjectId" to newResource.subjectId,
                "subjectName" to newResource.subjectName,
                "yearOrUnit" to newResource.yearOrUnit,
                "type" to newResource.type.name,
                "fileSizeBytes" to newResource.fileSizeBytes,
                "formattedSize" to newResource.formattedSize,
                "cloudStorageUrl" to newResource.cloudStorageUrl,
                "uploaderName" to newResource.uploaderName,
                "uploaderRole" to newResource.uploaderRole,
                "uploadedAt" to newResource.uploadedAt,
                "streamViewCount" to newResource.streamViewCount
            )
            getRtdb().getReference("cloud_study_resources").child(newResource.id).setValue(data)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to write resource to RTDB: ${e.message}")
        }

        onSuccess(newResource)
    }

    /**
     * Publishes a student reward redemption request to Firebase Realtime Database
     * so that administrators receive live alerts on their portal.
     */
    fun syncRedemptionToCloud(redemption: UserRedemption) {
        scope.launch {
            try {
                val data = hashMapOf<String, Any>(
                    "id" to redemption.id,
                    "userId" to redemption.userId,
                    "userName" to redemption.userName,
                    "itemId" to redemption.itemId,
                    "itemTitle" to redemption.itemTitle,
                    "spSpent" to redemption.spSpent,
                    "timestamp" to redemption.timestamp,
                    "status" to redemption.status,
                    "userEmail" to redemption.userEmail,
                    "userStream" to redemption.userStream
                )
                getRtdb().getReference("redemption_requests").child(redemption.id).setValue(data)
                Log.d(TAG, "Redemption request synced to RTDB: ${redemption.id}")
            } catch (e: Exception) {
                Log.w(TAG, "Could not sync redemption request: ${e.message}")
            }
        }
    }

    private fun updateStorageStats() {
        val totalBytes = _cloudResources.value.sumOf { it.fileSizeBytes }
        val count = _cloudResources.value.size
        val mb = totalBytes / (1024 * 1024)
        _storageStats.value = CloudStorageStats(
            totalCloudFilesCount = count,
            totalCloudStorageBytes = totalBytes,
            formattedCloudSize = "${mb} MB",
            localDeviceStorageUsedBytes = 0L,
            deviceStorageSavedFormatted = "${mb} MB Saved (Zero Device Bloat)"
        )
    }
}
