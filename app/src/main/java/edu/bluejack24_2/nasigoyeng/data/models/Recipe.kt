package edu.bluejack24_2.nasigoyeng.data.models

data class Recipe(
    val country: String = "",
    val created_by: String = "",
    val description: String = "",
    val image_url: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val category: String = "",
    val calories: Int = 0,
    val time: Int = 0,
    val title: String = "",
    val id: String = "",
    val is_official: Boolean = false,
    var liked_by: List<String> = emptyList(),
    var bookmark_by: List<String> = emptyList()
)
