package com.example.model

enum class CloudResourceType(val displayName: String, val badgeColorHex: Long) {
    PAST_PAPER("Past Paper", 0xFF0D9488),
    MARKING_SCHEME("Marking Scheme", 0xFFE11D48),
    MODEL_PAPER("Model Paper", 0xFF7C3AED),
    SHORT_NOTE("Summary Notes", 0xFF2563EB),
    FORMULA_SHEET("Formula Sheet", 0xFFD97706),
    TEACHER_SLIDE("Lecture Slides", 0xFF059669)
}

data class CloudStudyResource(
    val id: String,
    val title: String,
    val subjectId: String,
    val subjectName: String,
    val yearOrUnit: String,
    val type: CloudResourceType,
    val fileSizeBytes: Long,
    val formattedSize: String,
    val cloudStorageUrl: String,
    val uploaderName: String,
    val uploaderRole: String = "Zahira Academic Dept",
    val uploadedAt: Long = System.currentTimeMillis(),
    val streamViewCount: Int = 128,
    val isStreamOnly: Boolean = true // Stored in Cloud Storage; streamed on demand so device storage remains free
)

data class CloudStorageStats(
    val totalCloudFilesCount: Int,
    val totalCloudStorageBytes: Long,
    val formattedCloudSize: String,
    val localDeviceStorageUsedBytes: Long, // 0 bytes because files stream from cloud!
    val deviceStorageSavedFormatted: String
)
