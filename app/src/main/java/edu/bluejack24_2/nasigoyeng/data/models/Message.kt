package edu.bluejack24_2.nasigoyeng.data.models

data class Message(
    val id: String = "",
    val senderId: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val seen: Boolean = false
)
