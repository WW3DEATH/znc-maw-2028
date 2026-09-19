package com.example.model

data class RedemptionItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val spPrice: Int,
    val stock: Int,
    val imageUrl: String = "",
    val isAvailable: Boolean = true
)

data class MilestoneReward(
    val id: String,
    val levelRequired: Int, // 5, 10, 15, 20, etc.
    val title: String,
    val description: String,
    val rewardSp: Int,
    val badgeTitle: String,
    val iconName: String = "military_tech",
    val isClaimed: Boolean = false
)

data class UserRedemption(
    val id: String,
    val userId: String,
    val userName: String,
    val itemId: String,
    val itemTitle: String,
    val spSpent: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Pending", // Pending, Approved, Fulfilled, Rejected
    val userEmail: String = "",
    val userStream: String = "Physical Science"
)
