package edu.bluejack24_2.nasigoyeng.data.models

import java.util.Date
import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val name: String,
    val profile_picture: String,
    val is_private: Boolean,
    val bio: String,
    val bookmark_recipes: List<String> = emptyList(),
    val dob: Date,
    val height: Double,
    val weight: Double,
    val followers: List<String>,
    val following: List<String>,
    val like_recipe: List<String> = emptyList(),
    val my_recipe: List<String> = emptyList(),
    val mobile_phone: String,
    val posts: List<String>,
    val like_post: List<String>,
    val preferences: List<String>,
    val allergies: List<String>
)