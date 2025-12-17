package edu.bluejack24_2.nasigoyeng.data.models

data class MessageList(
    val id: String,
    val userId: String,
    val name: String,
    val avatarUrl: String,
    val message: String,
    val timestamp: Long,
    val isOnline: Boolean = false
)
