package com.example.watchlist.data.domain

data class StreamingService (
    val id: Int,
    val name: String,
    val type: String?,
    val logo: String,
    var isChecked: Boolean = false
)