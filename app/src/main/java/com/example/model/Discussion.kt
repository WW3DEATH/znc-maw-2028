package com.example.model

enum class MessageType {
    TEXT,
    IMAGE,
    VOICE_NOTE,
    DOCUMENT,
    SYSTEM_ANNOUNCEMENT
}

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderRole: UserRole,
    val senderStream: AcademicStream,
    val senderRank: Int? = null, // 1 to 10 for top 10 banner
    val senderProfilePic: String = "avatar_1",
    val classChannel: String, // Grade 12 Physical Science English Medium or Grade 12 Bio Science English Medium
    val text: String,
    val mediaUrl: String? = null,
    val mediaType: MessageType = MessageType.TEXT,
    val voiceDurationSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = true,
    val reactions: Map<String, String> = emptyMap() // userId -> emoji
)

data class DiscussionChannel(
    val id: String,
    val name: String,
    val stream: AcademicStream,
    val description: String,
    val unreadCount: Int = 0,
    val lastMessage: String = "",
    val lastTimestamp: Long = System.currentTimeMillis()
)
