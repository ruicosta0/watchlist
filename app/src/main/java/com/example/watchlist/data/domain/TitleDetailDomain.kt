package com.example.watchlist.data.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.intellij.lang.annotations.Language

@Parcelize
data class TitleDetailDomain(val id: Int, val title: String, val plotOverview: String?, val year: Int?, val criticScore: Double?,
    val language: String?, val genres: List<String?>, val trailer: String?,val poster:String?
) : Parcelable
