package com.example.model

enum class UserRole(val displayName: String) {
    STUDENT("Student"),
    TEACHER("Teacher"),
    ADMIN("Administrator")
}

enum class AcademicStream(val displayName: String) {
    PHYSICAL_SCIENCE("Physical Science"),
    BIO_SCIENCE("Bio Science"),
    GENERAL("General / Faculty")
}

object SchoolClasses {
    const val PHYSICAL_SCIENCE_ENG = "Grade 12 Physical Science English Medium"
    const val BIO_SCIENCE_ENG = "Grade 12 Bio Science English Medium"
}

data class UserProfile(
    val id: String,
    val email: String,
    val fullName: String,
    val role: UserRole,
    val stream: AcademicStream,
    val isStreamLocked: Boolean = true,
    val isVerified: Boolean = false,
    val profilePic: String = "avatar_1",
    val bio: String = "Striving for excellence at Zahira College Mawanella",
    val school: String = "Zahira College Mawanella",
    val grade: String = "Grade 12",
    val medium: String = "English Medium",
    val indexNumber: String = "ZSP-28-000",
    val academicHistory: String = "O/L: 9A's, Term 1 Combined Maths: 88, Physics: 84, Chemistry: 86",
    val level: Int = 1,
    val xp: Int = 120,
    val spPoints: Int = 150,
    val weeklyRank: Int? = null, // 1 to 10 if rank banner is active
    val rankBannerText: String? = null,
    val rankExpirationTimestamp: Long? = null
) {
    fun hasActiveRankBanner(): Boolean {
        if (weeklyRank == null || weeklyRank <= 0 || weeklyRank > 10) return false
        val now = System.currentTimeMillis()
        val exp = rankExpirationTimestamp ?: return true
        return now <= exp
    }

    fun xpForNextLevel(): Int = level * 100

    fun targetClass(): String {
        return when (stream) {
            AcademicStream.PHYSICAL_SCIENCE -> SchoolClasses.PHYSICAL_SCIENCE_ENG
            AcademicStream.BIO_SCIENCE -> SchoolClasses.BIO_SCIENCE_ENG
            AcademicStream.GENERAL -> "All Streams"
        }
    }
}
