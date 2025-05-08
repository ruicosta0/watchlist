package com.example.watchlist.data.network

import android.util.Log
import androidx.room.ColumnInfo
import com.example.watchlist.data.domain.TitleDetailDomain
import com.example.watchlist.data.local.movies.DatabaseMovie
import com.example.watchlist.data.local.streamingService.RegionEntity
import com.example.watchlist.data.local.streamingService.ServiceEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Url

@JsonClass(generateAdapter = true)
data class NetworkMovieContainer(val releases: List<NetworkMovie>) //releases is the key item in JSON response

@JsonClass(generateAdapter = true)
data class NetworkMovie(
    val id: Int,
    val title: String,
    val type: String,
    @Json(name = "tmdb_id") val tmdbId: Int,
    @Json(name = "imdb_id")val imdbId: String,
    @Json(name = "tmdb_type") val tmdbType: String,
    @Json(name = "season_number")val seasonNumber: String?,
    @Json(name = "poster_url")val posterUrl: String,
    @Json(name = "source_release_date") val sourceReleaseDate: String,
    @Json(name = "source_id")val sourceId: Int,
    @Json(name = "source_name")val sourceName: String,
    @Json(name = "is_original")val isOriginal: Int
)

/**
 * Convert Network results to Database Objects
 */

fun NetworkMovieContainer.asDatabaseModel(): Array<DatabaseMovie> {
    return releases.map { //takes in a NetWorkContainer and creates a collection of array<DatabaseMovie>
        DatabaseMovie(
            id = it.id,
            sourceId = it.sourceId,
            title = it.title,
            service = it.sourceName,
            type = it.type,
            date = it.sourceReleaseDate,
            watchlist = 0
        )
    }.toTypedArray()
}

@JsonClass(generateAdapter = true) //no key item so can map directly to NetworkService. an andapter handles serializing/deserializing to and from JSON
data class NetworkService(
    val id: Int,
    var name: String,
    @Json(name = "type") var type: String? = "Unknown", //default to unknown
    @Json(name = "logo_100px") var logoUrl: String, // url for logo
    @Json(name = "ios_appstore_url") var iosAppUrl: String?, // optional ios store link
    @Json(name = "android_playstore_url") var androidAppUrl: String?, // android play store
    @Json(name = "android_scheme") var androidScheme: String?, // android scheme
    @Json(name = "ios_scheme")var iosScheme: String?, // ios scheme
    val regions: List<String>
)

fun List<NetworkService>.asDatabaseModel(): Pair<List<ServiceEntity>, List<RegionEntity>> {

    val serviceEntities = mutableListOf<ServiceEntity>()
    val regionEntities = mutableListOf<RegionEntity>()

    //do not use map as not creating a new collection

    this.forEach { networkService ->
        // Map NetworkService to service entity
        Log.d("ServiceProcessing","ServiceProcessing ServiceEntity Id ${networkService.id}, Service Entity Regions: ${networkService.regions}")
        val serviceEntity = ServiceEntity(
            watchId = networkService.id,
            name = networkService.name,
            type = networkService.type,
            logoUrl= networkService.logoUrl,
            iosAppUrl= networkService.iosAppUrl,
            androidAppUrl = networkService.androidAppUrl,
            androidScheme = networkService.androidScheme,
            iosScheme = networkService.iosScheme
        )
        serviceEntities.add(serviceEntity)
        // map regions to RegionEntity

        networkService.regions.forEach { regionCode ->
            Log.d("RegionProcessing","RegionProcessing, ServiceOwnerId: ${networkService.id}, Processing Region:${networkService.regions}")
            val regionEntity = RegionEntity(
                regionCode = regionCode,
                serviceOwnerId = networkService.id
            )
            regionEntities.add(regionEntity)
        }
    }
    return Pair(serviceEntities, regionEntities)
}

@JsonClass(generateAdapter = true)
data class TitleDetail(
    val id: Int,
    val title: String,
    @Json(name = "original_title") val originalTitle: String?,
    @Json(name = "plot_overview") val plotOverview: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "runtime_minutes") val runtimeMinutes: Long?,
    val year: Int?,
    @Json(name = "end_year") val endYear: Int?,
    @Json(name = "release_date") val releaseDate: String?,
    @Json(name = "imdb_id") val imdbId: String?,
    @Json(name = "tmdb_id") val tmdbId: Int?,
    @Json(name = "tmdb_type") val tmdbType: String?,
    val genres: List<Int?>?,
    @Json(name = "genre_names") val genreNames: List<String?>?,
    @Json(name = "user_rating") val userRating: Double?,
    @Json(name = "critic_score") val criticScore: Double?,
    @Json(name = "us_rating") val usRating:String?,
    val poster: String?,
    val posterMedium: String?,
    val posterLarge: String?,
    val backdrop: String?,
    @Json(name = "original_language") val originalLanguage: String?,
    @Json(name="similar_titles") val similarTitles: List<Int?>?,
    val networks: List<Int?>?,
    @Json(name="network_names") val networkNames: List<String?>?,
    @Json(name="relevance_percentile") val relevancePercentile: Double?,
    @Json(name="popularity_percentile") val popularityPercentile: Double?,
    val trailer: String?,
    @Json(name="trailer_thumbnail") val trailerThumbnail: String?
)

fun TitleDetail.asDomainModelTitleDetail() : TitleDetailDomain {
    return TitleDetailDomain(
        id = this.id,
        title = this.title,
        plotOverview = this.plotOverview,
        year = this.year,
        criticScore = this.criticScore,
        language = this.originalLanguage,
        genres = this.genreNames?: emptyList(),
        trailer = this.trailer,
        poster = this.poster
    )
}