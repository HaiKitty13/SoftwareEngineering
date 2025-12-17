package edu.bluejack24_2.nasigoyeng.data.models

import java.util.UUID

data class Step(
    var text: String = "",
    val id: String = UUID.randomUUID().toString()
)
