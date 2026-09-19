package com.example.model

import java.util.Calendar

data class SyllabusUnit(
    val unitNumber: Int,
    val unitName: String,
    val description: String,
    val totalEstimatedQuestions: Int = 10
)

data class SyllabusSubject(
    val id: String,
    val name: String,
    val code: String,
    val streams: List<AcademicStream>,
    val units: List<SyllabusUnit>
)

data class QuizQuestion(
    val id: String,
    val subjectId: String,
    val subjectName: String,
    val unitNumber: Int,
    val unitName: String,
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class QuizSubmission(
    val id: String,
    val studentId: String,
    val studentName: String,
    val stream: AcademicStream,
    val subjectName: String,
    val unitNumber: Int,
    val score: Int,
    val totalQuestions: Int,
    val pointsEarned: Int,
    val spAwarded: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class LeaderboardEntry(
    val rank: Int,
    val studentId: String,
    val studentName: String,
    val stream: AcademicStream,
    val score: Int,
    val timeTakenSeconds: Int,
    val spEarned: Int,
    val isCurrentUser: Boolean = false
)

object QuizScheduleHelper {
    // Wednesday night 7:30pm (19:30) to 10:00pm (22:00)
    fun isWednesdayLiveWindow(): Boolean {
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val timeInMinutes = hour * 60 + minute

        val isWednesday = dayOfWeek == Calendar.WEDNESDAY
        val isWindowTime = timeInMinutes in (19 * 60 + 30)..(22 * 60)
        return isWednesday && isWindowTime
    }

    // Results and Top 10 users are visible after Wednesday 10:00 PM (22:00)
    fun isWednesdayResultsAvailable(): Boolean {
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val timeInMinutes = hour * 60 + minute

        // Available on Wednesday after 10:00 PM (22:00)
        return dayOfWeek == Calendar.WEDNESDAY && timeInMinutes >= (22 * 60)
    }

    fun getNextLiveTimeText(): String {
        return "Wednesday 7:30 PM – 10:00 PM (SLST)"
    }

    fun getResultsTimeText(): String {
        return "Wednesday after 10:00 PM (SLST)"
    }
}
