package com.example.watchlist.data.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Movie (val id: Int, val sourceId: Int, val title: String, val service: String,
    val type: String, val date: String, val watchlist: Int
) : Parcelable
