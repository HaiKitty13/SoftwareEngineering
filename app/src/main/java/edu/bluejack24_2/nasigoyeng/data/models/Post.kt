package edu.bluejack24_2.nasigoyeng.data.models

import java.util.UUID

data class Post(
    val id: String = UUID.randomUUID().toString(),
    val user_id: String = "",
    val media: String = "",
    val description: String = "",
)
