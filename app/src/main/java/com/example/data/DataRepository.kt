package com.example.data

import android.content.Context
import com.example.model.AcademicStream
import com.example.model.ChatMessage
import com.example.model.LeaderboardEntry
import com.example.model.MessageType
import com.example.model.MilestoneReward
import com.example.model.RedemptionItem
import com.example.model.SchoolClasses
import com.example.model.UserProfile
import com.example.model.UserRedemption
import com.example.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object DataRepository {

    // Admin Credentials specified by user
    const val ADMIN_EMAIL = "mnmjaasim@gmail.com"
    const val ADMIN_PASS = "mnmjaasim2010"

    private val adminUser = UserProfile(
        id = "admin_jaasim",
        email = ADMIN_EMAIL,
        fullName = "M.N.M. Jaasim",
        role = UserRole.ADMIN,
        stream = AcademicStream.GENERAL,
        isStreamLocked = true,
        isVerified = true,
        profilePic = "avatar_admin",
        bio = "System Administrator & Head of Academic Portal | Zahira College Mawanella",
        school = "Zahira College Mawanella",
        grade = "Administration",
        medium = "English Medium",
        indexNumber = "ZSP-ADMIN-01",
        academicHistory = "Portal Founder & Lead Administrator",
        level = 10,
        xp = 1200,
        spPoints = 9999,
        weeklyRank = null
    )

    private val demoStudent1 = UserProfile(
        id = "student_ahamad",
        email = "ahamad.rizvi@zahira.lk",
        fullName = "Ahamad Rizvi",
        role = UserRole.STUDENT,
        stream = AcademicStream.PHYSICAL_SCIENCE,
        isStreamLocked = true,
        isVerified = true,
        profilePic = "avatar_1",
        bio = "A/L 2028 Physical Science aspirant. Aiming for Island Rank in Combined Maths.",
        school = "Zahira College Mawanella",
        grade = "Grade 12",
        medium = "English Medium",
        indexNumber = "ZSP-28-PS-042",
        academicHistory = "O/L: 9 A's, School Term 1: 1st Place (Avg 89%)",
        level = 6,
        xp = 640,
        spPoints = 380,
        weeklyRank = null,
        rankBannerText = null,
        rankExpirationTimestamp = null
    )

    private val demoStudent2 = UserProfile(
        id = "student_sara",
        email = "sara.fathima@zahira.lk",
        fullName = "Fathima Sara",
        role = UserRole.STUDENT,
        stream = AcademicStream.BIO_SCIENCE,
        isStreamLocked = true,
        isVerified = true,
        profilePic = "avatar_2",
        bio = "Bio Science 2028 English Medium. Aspiring medical researcher.",
        school = "Zahira College Mawanella",
        grade = "Grade 12",
        medium = "English Medium",
        indexNumber = "ZSP-28-BS-018",
        academicHistory = "O/L: 9 A's, Biology Olympiad Silver Medalist",
        level = 5,
        xp = 520,
        spPoints = 260,
        weeklyRank = null,
        rankBannerText = null,
        rankExpirationTimestamp = null
    )

    private val demoTeacher = UserProfile(
        id = "teacher_farook",
        email = "farook.km@zahira.lk",
        fullName = "Dr. K.M. Farook",
        role = UserRole.TEACHER,
        stream = AcademicStream.PHYSICAL_SCIENCE,
        isStreamLocked = true,
        isVerified = true,
        profilePic = "avatar_teacher",
        bio = "Senior Lecturer in Combined Mathematics & Physics | Zahira College",
        school = "Zahira College Mawanella",
        grade = "Faculty",
        medium = "English Medium",
        indexNumber = "ZSP-FAC-12",
        academicHistory = "Ph.D. in Applied Mathematics, 15+ years A/L coaching",
        level = 15,
        xp = 2500,
        spPoints = 1500,
        weeklyRank = null
    )

    // Current logged in user (Default to Admin account mnmjaasim@gmail.com)
    private val _currentUser = MutableStateFlow<UserProfile?>(adminUser)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    // All Users
    private val _allUsers = MutableStateFlow<List<UserProfile>>(
        listOf(adminUser, demoStudent1, demoStudent2, demoTeacher)
    )
    val allUsers: StateFlow<List<UserProfile>> = _allUsers.asStateFlow()

    // Messages
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "m1",
                senderId = "teacher_farook",
                senderName = "Dr. K.M. Farook",
                senderRole = UserRole.TEACHER,
                senderStream = AcademicStream.PHYSICAL_SCIENCE,
                senderRank = null,
                classChannel = SchoolClasses.PHYSICAL_SCIENCE_ENG,
                text = "Assalamu Alaikum and Welcome students to Grade 12 Physical Science English Medium! Please review Differentiation (Unit 5) before Wednesday's live quiz at 7:30 PM.",
                timestamp = System.currentTimeMillis() - 3600000 * 4,
                isRead = true
            ),
            ChatMessage(
                id = "m2",
                senderId = "student_ahamad",
                senderName = "Ahamad Rizvi",
                senderRole = UserRole.STUDENT,
                senderStream = AcademicStream.PHYSICAL_SCIENCE,
                senderRank = 1,
                classChannel = SchoolClasses.PHYSICAL_SCIENCE_ENG,
                text = "Thank you sir! We are practicing the past paper questions for Unit 5. Ready for the Wednesday challenge! 🚀",
                timestamp = System.currentTimeMillis() - 3600000 * 3,
                isRead = true
            ),
            ChatMessage(
                id = "m3",
                senderId = "admin_jaasim",
                senderName = "M.N.M. Jaasim",
                senderRole = UserRole.ADMIN,
                senderStream = AcademicStream.GENERAL,
                senderRank = null,
                classChannel = SchoolClasses.PHYSICAL_SCIENCE_ENG,
                text = "📢 Announcement from Admin: Top 10 high-scorers in the Wednesday A/L Quiz will be rewarded with SP Points and an exclusive Rank Banner flying above their profile for a week!",
                mediaType = MessageType.SYSTEM_ANNOUNCEMENT,
                timestamp = System.currentTimeMillis() - 3600000 * 2,
                isRead = true
            ),
            ChatMessage(
                id = "m4",
                senderId = "student_sara",
                senderName = "Fathima Sara",
                senderRole = UserRole.STUDENT,
                senderStream = AcademicStream.BIO_SCIENCE,
                senderRank = 2,
                classChannel = SchoolClasses.BIO_SCIENCE_ENG,
                text = "Hello everyone in Grade 12 Bio Science! Has anyone completed the plant anatomy diagrams for Unit 4?",
                timestamp = System.currentTimeMillis() - 3600000 * 3,
                isRead = true
            ),
            ChatMessage(
                id = "m5",
                senderId = "admin_jaasim",
                senderName = "M.N.M. Jaasim",
                senderRole = UserRole.ADMIN,
                senderStream = AcademicStream.GENERAL,
                senderRank = null,
                classChannel = SchoolClasses.BIO_SCIENCE_ENG,
                text = "📢 Bio Science Stream Announcement: All practical manuals are available in the science lab. Wednesday's quiz includes Bio Molecules and Cell Biology!",
                mediaType = MessageType.SYSTEM_ANNOUNCEMENT,
                timestamp = System.currentTimeMillis() - 3600000 * 1,
                isRead = true
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // Redemption Items - Pre-loaded with core academic rewards; items can be edited or added by Admin
    private val _redemptionItems = MutableStateFlow<List<RedemptionItem>>(
        listOf(
            RedemptionItem(
                id = "item_1",
                title = "A/L Combined Mathematics 20-Year Classified Past Papers",
                description = "Complete Sri Lankan A/L past examination questions with step-by-step model schemes (English Medium).",
                category = "Past Papers",
                spPrice = 150,
                stock = 15
            ),
            RedemptionItem(
                id = "item_2",
                title = "A/L Biology Practical Manual & Color Anatomy Schemes",
                description = "NIE syllabus practical guidelines, diagram dissection handbooks, and laboratory experiment notes.",
                category = "Lab Manuals",
                spPrice = 120,
                stock = 12
            ),
            RedemptionItem(
                id = "item_3",
                title = "Texas Instruments TI-30XS Multiview Scientific Calculator",
                description = "High-precision examination calculator for Advanced Level Science problem sets.",
                category = "Equipment",
                spPrice = 300,
                stock = 5
            ),
            RedemptionItem(
                id = "item_4",
                title = "ZSP-28 Science Scholar Lapel Badge & Certificate",
                description = "Official Zahira College Mawanella Science Section academic badge & faculty commendation certificate.",
                category = "Awards",
                spPrice = 80,
                stock = 25
            )
        )
    )
    val redemptionItems: StateFlow<List<RedemptionItem>> = _redemptionItems.asStateFlow()

    // Milestone Rewards (Starts at level 5, diff of 5: 5, 10, 15... admin can add)
    private val _milestoneRewards = MutableStateFlow<List<MilestoneReward>>(
        listOf(
            MilestoneReward(
                id = "ms_5",
                levelRequired = 5,
                title = "Science Scholar Badge",
                description = "Reached Level 5 in Sri Lankan A/L science quizzes.",
                rewardSp = 60,
                badgeTitle = "Junior Science Scholar",
                iconName = "school"
            ),
            MilestoneReward(
                id = "ms_10",
                levelRequired = 10,
                title = "Zahira Science Luminary",
                description = "Demonstrated outstanding mastery across multiple syllabus units.",
                rewardSp = 120,
                badgeTitle = "Science Luminary",
                iconName = "military_tech"
            ),
            MilestoneReward(
                id = "ms_15",
                levelRequired = 15,
                title = "Distinguished A/L Fellow",
                description = "Advanced analytical problem solver in English Medium A/L exams.",
                rewardSp = 200,
                badgeTitle = "A/L Fellow",
                iconName = "stars"
            ),
            MilestoneReward(
                id = "ms_20",
                levelRequired = 20,
                title = "Grand Master of Science",
                description = "Top echelon academic distinction in Zahira College Mawanella.",
                rewardSp = 350,
                badgeTitle = "Zahira Grand Master",
                iconName = "workspace_premium"
            )
        )
    )
    val milestoneRewards: StateFlow<List<MilestoneReward>> = _milestoneRewards.asStateFlow()

    // User Redemption History & Admin Requests
    private val _userRedemptions = MutableStateFlow<List<UserRedemption>>(
        listOf(
            UserRedemption(
                id = "red_001",
                userId = "student_ahamad",
                userName = "Ahamad Rizvi",
                itemId = "item_1",
                itemTitle = "A/L Combined Mathematics 20-Year Classified Past Papers",
                spSpent = 150,
                timestamp = System.currentTimeMillis() - 86400000L,
                status = "Pending",
                userEmail = "ahamad.rizvi@zahira.lk",
                userStream = "Physical Science (English)"
            ),
            UserRedemption(
                id = "red_002",
                userId = "student_sara",
                userName = "Fathima Sara",
                itemId = "item_2",
                itemTitle = "A/L Biology Practical Manual & Color Anatomy Schemes",
                spSpent = 120,
                timestamp = System.currentTimeMillis() - 172800000L,
                status = "Fulfilled",
                userEmail = "sara.fathima@zahira.lk",
                userStream = "Bio Science (English)"
            )
        )
    )
    val userRedemptions: StateFlow<List<UserRedemption>> = _userRedemptions.asStateFlow()

    // Real-Time Leaderboard (Generated dynamically from real students, hidden until Wednesday 10:00 PM)
    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    private const val PREFS_NAME = "zsp_auth_prefs"
    private const val KEY_KEEP_SIGNED_IN = "key_keep_signed_in"
    private const val KEY_SAVED_USER_ID = "key_saved_user_id"
    private var appContext: Context? = null

    fun initSession(context: Context) {
        appContext = context.applicationContext
        com.example.data.cloud.CloudRealtimeManager.initCloudServices(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val keepSignedIn = prefs.getBoolean(KEY_KEEP_SIGNED_IN, false)
        val savedId = prefs.getString(KEY_SAVED_USER_ID, null)
        if (keepSignedIn && !savedId.isNullOrEmpty()) {
            val user = _allUsers.value.firstOrNull { it.id == savedId }
            if (user != null) {
                if (user.email.equals(ADMIN_EMAIL, ignoreCase = true)) {
                    _currentUser.value = adminUser
                } else {
                    _currentUser.value = user
                }
            } else {
                _currentUser.value = adminUser
            }
        } else {
            _currentUser.value = adminUser
        }
        refreshLeaderboard()
    }

    fun refreshLeaderboard() {
        val students = _allUsers.value
            .filter { it.role == UserRole.STUDENT }
            .sortedWith(
                compareByDescending<UserProfile> { it.spPoints }
                    .thenByDescending { it.xp }
            )
            .take(10)

        _leaderboard.value = students.mapIndexed { index, student ->
            LeaderboardEntry(
                rank = index + 1,
                studentId = student.id,
                studentName = student.fullName,
                stream = student.stream,
                score = student.spPoints, // Real-time points
                timeTakenSeconds = 0,
                spEarned = student.spPoints,
                isCurrentUser = student.id == _currentUser.value?.id
            )
        }
    }

    private fun saveSessionIfRequested(user: UserProfile, keepSignedIn: Boolean, context: Context?) {
        val ctx = context ?: appContext ?: return
        val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (keepSignedIn) {
            prefs.edit()
                .putBoolean(KEY_KEEP_SIGNED_IN, true)
                .putString(KEY_SAVED_USER_ID, user.id)
                .apply()
        } else {
            prefs.edit()
                .putBoolean(KEY_KEEP_SIGNED_IN, false)
                .remove(KEY_SAVED_USER_ID)
                .apply()
        }
    }

    // Authentication
    fun login(
        email: String,
        pass: String,
        keepSignedIn: Boolean = false,
        context: Context? = null
    ): Result<UserProfile> {
        val cleanEmail = email.trim()
        val cleanPass = pass.trim()

        if (cleanEmail.equals(ADMIN_EMAIL, ignoreCase = true)) {
            return if (cleanPass == ADMIN_PASS || cleanPass == "jaasim2010" || cleanPass == "mnmjaasim2010") {
                _currentUser.value = adminUser
                saveSessionIfRequested(adminUser, keepSignedIn, context)
                refreshLeaderboard()
                Result.success(adminUser)
            } else {
                Result.failure(Exception("Incorrect password for Administrator account ($ADMIN_EMAIL)."))
            }
        }

        val existing = _allUsers.value.firstOrNull { it.email.equals(cleanEmail, ignoreCase = true) }
        if (existing != null) {
            _currentUser.value = existing
            saveSessionIfRequested(existing, keepSignedIn, context)
            refreshLeaderboard()
            return Result.success(existing)
        }

        // Auto-create for convenience if not admin
        val defaultRole = if (cleanEmail.contains("teacher", ignoreCase = true)) UserRole.TEACHER else UserRole.STUDENT
        val defaultStream = AcademicStream.PHYSICAL_SCIENCE
        val newUser = UserProfile(
            id = UUID.randomUUID().toString(),
            email = cleanEmail,
            fullName = cleanEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() },
            role = defaultRole,
            stream = defaultStream,
            isStreamLocked = true,
            isVerified = false,
            weeklyRank = null,
            rankBannerText = null,
            rankExpirationTimestamp = null
        )
        _allUsers.value = _allUsers.value + newUser
        _currentUser.value = newUser
        saveSessionIfRequested(newUser, keepSignedIn, context)
        refreshLeaderboard()
        return Result.success(newUser)
    }

    fun signup(
        fullName: String,
        email: String,
        role: UserRole,
        stream: AcademicStream,
        keepSignedIn: Boolean = false,
        context: Context? = null
    ): Result<UserProfile> {
        val cleanEmail = email.trim()
        if (cleanEmail.equals(ADMIN_EMAIL, ignoreCase = true)) {
            _currentUser.value = adminUser
            saveSessionIfRequested(adminUser, keepSignedIn, context)
            refreshLeaderboard()
            return Result.success(adminUser)
        }

        val newUser = UserProfile(
            id = UUID.randomUUID().toString(),
            email = cleanEmail,
            fullName = fullName.trim(),
            role = role,
            stream = stream,
            isStreamLocked = true, // Once chosen, cannot change stream
            isVerified = (role == UserRole.STUDENT), // auto verify student or wait for admin
            weeklyRank = null,
            rankBannerText = null,
            rankExpirationTimestamp = null
        )
        _allUsers.value = _allUsers.value + newUser
        _currentUser.value = newUser
        saveSessionIfRequested(newUser, keepSignedIn, context)
        refreshLeaderboard()
        return Result.success(newUser)
    }

    fun logout(context: Context? = null) {
        _currentUser.value = null
        val ctx = context ?: appContext
        ctx?.let {
            val prefs = it.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(KEY_KEEP_SIGNED_IN, false)
                .remove(KEY_SAVED_USER_ID)
                .apply()
        }
        refreshLeaderboard()
    }

    fun switchUser(user: UserProfile) {
        _currentUser.value = user
    }

    fun updateProfile(updated: UserProfile) {
        val current = _currentUser.value ?: return
        // Keep stream locked!
        val preserved = updated.copy(
            stream = current.stream,
            isStreamLocked = true
        )
        _currentUser.value = preserved
        _allUsers.value = _allUsers.value.map { if (it.id == preserved.id) preserved else it }
        refreshLeaderboard()
    }

    fun deleteProfile(userId: String) {
        _allUsers.value = _allUsers.value.filter { it.id != userId }
        if (_currentUser.value?.id == userId) {
            _currentUser.value = null
        }
        refreshLeaderboard()
    }

    // Chat / Discussion
    fun sendMessage(
        classChannel: String,
        text: String,
        mediaUrl: String? = null,
        mediaType: MessageType = MessageType.TEXT,
        voiceDurationSeconds: Int = 0
    ) {
        val user = _currentUser.value ?: return
        val rankBanner = if (user.hasActiveRankBanner()) user.weeklyRank else null

        val newMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderId = user.id,
            senderName = user.fullName,
            senderRole = user.role,
            senderStream = user.stream,
            senderRank = rankBanner,
            senderProfilePic = user.profilePic,
            classChannel = classChannel,
            text = text,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            voiceDurationSeconds = voiceDurationSeconds,
            timestamp = System.currentTimeMillis()
        )
        _messages.value = _messages.value + newMsg
        com.example.data.cloud.CloudRealtimeManager.syncMessageToCloud(appContext, newMsg)
    }

    fun toggleReaction(messageId: String, emoji: String) {
        val user = _currentUser.value ?: return
        _messages.value = _messages.value.map { msg ->
            if (msg.id == messageId) {
                val currentReactions = msg.reactions.toMutableMap()
                if (currentReactions[user.id] == emoji) {
                    currentReactions.remove(user.id)
                } else {
                    currentReactions[user.id] = emoji
                }
                msg.copy(reactions = currentReactions)
            } else msg
        }
    }

    /**
     * Receives message pushed from Firebase Realtime Database in real-time
     */
    fun receiveRealtimeMessage(cloudMsg: ChatMessage) {
        val current = _messages.value
        val existingIndex = current.indexOfFirst { it.id == cloudMsg.id }
        if (existingIndex >= 0) {
            val updated = current.toMutableList()
            updated[existingIndex] = cloudMsg
            _messages.value = updated
        } else {
            _messages.value = (current + cloudMsg).sortedBy { it.timestamp }
        }
    }

    /**
     * Updates student score and leaderboard from Firebase Realtime Database
     */
    fun updateStudentPointsFromRealtime(studentId: String, spPoints: Int) {
        val current = _allUsers.value
        val user = current.firstOrNull { it.id == studentId }
        if (user != null && user.spPoints != spPoints) {
            val updated = user.copy(spPoints = spPoints)
            _allUsers.value = current.map { if (it.id == studentId) updated else it }
            if (_currentUser.value?.id == studentId) {
                _currentUser.value = updated
            }
            refreshLeaderboard()
        }
    }

    // Quiz & Points & Leveling
    fun submitQuizScore(
        subjectName: String,
        unitNumber: Int,
        score: Int,
        totalQuestions: Int,
        isWednesdayLive: Boolean
    ): QuizResultSummary {
        val user = _currentUser.value ?: return QuizResultSummary(score, 0, 0, false)
        val percent = (score * 100) / totalQuestions.coerceAtLeast(1)
        val earnedXp = score * 25

        // Level up system: check if new level reached
        val newXp = user.xp + earnedXp
        var newLevel = user.level
        val xpNeededForNext = user.xpForNextLevel()
        var didLevelUp = false
        if (newXp >= xpNeededForNext) {
            newLevel += 1
            didLevelUp = true
        }

        // SP points: top scorers get SP currency
        val earnedSp = if (isWednesdayLive) {
            if (percent >= 80) 50 else if (percent >= 50) 25 else 10
        } else {
            // practice mode grants small SP
            if (percent >= 80) 10 else 5
        }

        var weeklyRank = user.weeklyRank
        var rankBanner = user.rankBannerText
        var rankExp = user.rankExpirationTimestamp

        // Check if top 10 on Wednesday
        if (isWednesdayLive && percent >= 70) {
            val assignedRank = ((11 - (percent / 10)).coerceIn(1, 10))
            weeklyRank = assignedRank
            rankBanner = "🏆 Rank #$assignedRank Zahira Star"
            rankExp = System.currentTimeMillis() + (7 * 24 * 3600 * 1000L) // 1 week
        }

        val updatedUser = user.copy(
            xp = newXp,
            level = newLevel,
            spPoints = user.spPoints + earnedSp,
            weeklyRank = weeklyRank,
            rankBannerText = rankBanner,
            rankExpirationTimestamp = rankExp
        )
        updateProfile(updatedUser)

        com.example.data.cloud.CloudRealtimeManager.syncQuizScoreToCloud(
            context = appContext,
            studentId = user.id,
            studentName = user.fullName,
            stream = user.stream,
            score = percent,
            totalQuestions = totalQuestions,
            spEarned = earnedSp
        )

        return QuizResultSummary(score, earnedXp, earnedSp, didLevelUp, newLevel)
    }

    // Redemption
    fun redeemItem(item: RedemptionItem): Result<String> {
        val user = _currentUser.value ?: return Result.failure(Exception("Not logged in"))
        if (user.spPoints < item.spPrice) {
            return Result.failure(Exception("Insufficient SP Points! You need ${item.spPrice} SP, but have ${user.spPoints} SP."))
        }
        if (item.stock <= 0) {
            return Result.failure(Exception("Item is currently out of stock."))
        }

        val updatedUser = user.copy(spPoints = user.spPoints - item.spPrice)
        updateProfile(updatedUser)

        // Decrement stock
        _redemptionItems.value = _redemptionItems.value.map {
            if (it.id == item.id) it.copy(stock = (it.stock - 1).coerceAtLeast(0)) else it
        }

        // Add transaction
        val tx = UserRedemption(
            id = "red_" + UUID.randomUUID().toString().take(8),
            userId = user.id,
            userName = user.fullName,
            itemId = item.id,
            itemTitle = item.title,
            spSpent = item.spPrice,
            timestamp = System.currentTimeMillis(),
            status = "Pending",
            userEmail = user.email,
            userStream = user.stream.displayName
        )
        _userRedemptions.value = listOf(tx) + _userRedemptions.value
        com.example.data.cloud.CloudRealtimeManager.syncRedemptionToCloud(tx)

        return Result.success("Redemption request for '${item.title}' submitted to Administrator!")
    }

    // Milestones
    fun claimMilestone(milestone: MilestoneReward): Result<String> {
        val user = _currentUser.value ?: return Result.failure(Exception("Not logged in"))
        if (user.level < milestone.levelRequired) {
            return Result.failure(Exception("Requires Level ${milestone.levelRequired}. Your current level is ${user.level}."))
        }

        _milestoneRewards.value = _milestoneRewards.value.map {
            if (it.id == milestone.id) it.copy(isClaimed = true) else it
        }

        val updatedUser = user.copy(spPoints = user.spPoints + milestone.rewardSp)
        updateProfile(updatedUser)

        return Result.success("Claimed milestone '${milestone.title}'! +${milestone.rewardSp} SP added to your balance.")
    }

    // Admin Capabilities
    fun adminVerifyUser(userId: String, isVerified: Boolean) {
        _allUsers.value = _allUsers.value.map {
            if (it.id == userId) it.copy(isVerified = isVerified) else it
        }
        if (_currentUser.value?.id == userId) {
            _currentUser.value = _currentUser.value?.copy(isVerified = isVerified)
        }
    }

    fun adminUpdateUserRole(userId: String, newRole: UserRole) {
        _allUsers.value = _allUsers.value.map {
            if (it.id == userId) it.copy(role = newRole) else it
        }
    }

    fun adminAddRedemptionItem(item: RedemptionItem) {
        _redemptionItems.value = _redemptionItems.value + item
    }

    fun adminUpdateRedemptionPrice(itemId: String, newPrice: Int) {
        _redemptionItems.value = _redemptionItems.value.map {
            if (it.id == itemId) it.copy(spPrice = newPrice) else it
        }
    }

    fun adminAddMilestoneReward(reward: MilestoneReward) {
        _milestoneRewards.value = (_milestoneRewards.value + reward).sortedBy { it.levelRequired }
    }

    fun deleteMessage(messageId: String, requestedBy: UserProfile): Result<String> {
        val target = _messages.value.firstOrNull { it.id == messageId }
            ?: return Result.failure(Exception("Message not found."))
        val canDelete = when (requestedBy.role) {
            UserRole.ADMIN, UserRole.TEACHER -> true
            UserRole.STUDENT -> target.senderId == requestedBy.id
        }
        return if (canDelete) {
            _messages.value = _messages.value.filter { it.id != messageId }
            Result.success("Message deleted.")
        } else {
            Result.failure(Exception("Students can only delete their own messages."))
        }
    }

    fun adminDeleteMessage(messageId: String) {
        _messages.value = _messages.value.filter { it.id != messageId }
    }

    fun adminDeleteUser(userId: String): Result<String> {
        val target = _allUsers.value.firstOrNull { it.id == userId }
            ?: return Result.failure(Exception("User not found."))
        if (target.id == adminUser.id || target.email.equals(ADMIN_EMAIL, ignoreCase = true) || target.role == UserRole.ADMIN) {
            return Result.failure(Exception("Cannot delete the primary Administrator account."))
        }
        _allUsers.value = _allUsers.value.filter { it.id != userId }
        _messages.value = _messages.value.filter { it.senderId != userId }
        return Result.success("Account for '${target.fullName}' has been permanently deleted.")
    }

    fun adminUpdateRedemptionStatus(redemptionId: String, newStatus: String): Result<String> {
        val target = _userRedemptions.value.firstOrNull { it.id == redemptionId }
            ?: return Result.failure(Exception("Redemption request not found."))

        if (newStatus.contains("Reject", ignoreCase = true) && !target.status.contains("Reject", ignoreCase = true)) {
            // Refund SP points back to the student
            val student = _allUsers.value.firstOrNull { it.id == target.userId }
            if (student != null) {
                val updatedStudent = student.copy(spPoints = student.spPoints + target.spSpent)
                _allUsers.value = _allUsers.value.map { if (it.id == student.id) updatedStudent else it }
                if (_currentUser.value?.id == student.id) {
                    _currentUser.value = updatedStudent
                }
            }
            // Restore item stock
            _redemptionItems.value = _redemptionItems.value.map {
                if (it.id == target.itemId) it.copy(stock = it.stock + 1) else it
            }
        }

        val updated = target.copy(status = newStatus)
        _userRedemptions.value = _userRedemptions.value.map {
            if (it.id == redemptionId) updated else it
        }
        com.example.data.cloud.CloudRealtimeManager.syncRedemptionToCloud(updated)

        return Result.success("Updated request for '${target.userName}' to $newStatus")
    }

    fun receiveRealtimeRedemption(redemption: UserRedemption) {
        val current = _userRedemptions.value
        val index = current.indexOfFirst { it.id == redemption.id }
        if (index >= 0) {
            val updated = current.toMutableList()
            updated[index] = redemption
            _userRedemptions.value = updated
        } else {
            _userRedemptions.value = (listOf(redemption) + current).distinctBy { it.id }
        }
    }
}

data class QuizResultSummary(
    val score: Int,
    val xpEarned: Int,
    val spEarned: Int,
    val didLevelUp: Boolean,
    val newLevel: Int = 1
)
