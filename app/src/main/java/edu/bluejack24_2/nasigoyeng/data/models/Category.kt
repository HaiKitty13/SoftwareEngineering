package edu.bluejack24_2.nasigoyeng.data.models

import java.util.UUID

data class Category(
    val name: String = "",
    val imageUrl: String = "",
    val id: String = UUID.randomUUID().toString()
)
