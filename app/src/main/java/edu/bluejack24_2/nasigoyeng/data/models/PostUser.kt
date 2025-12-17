package edu.bluejack24_2.nasigoyeng.data.models

import java.util.UUID

data class PostUser(
    val user_id: String = "",
    val media: String = "",
    val description: String = "",
    val username: String,
    val name: String,
    val profile_picture: String,
    val id: String = UUID.randomUUID().toString(),
)
