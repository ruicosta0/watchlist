package com.example.watchlist.data.local.streamingService

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

// 1) entity containing streaming services
@Entity(tableName = "service_table")
data class ServiceEntity(
    @PrimaryKey val watchId: Int, // id from WatchMode
    var name: String = "unknown", // Name of the service (e.g., "Netflix", "Hulu")
    var type: String? = "unknown", // eg sub, free, purchase etc
    var logoUrl: String, // url for logo
    var iosAppUrl: String?, // optional ios store link
    var androidAppUrl: String?, // android play store
    var androidScheme: String?, // android scheme
    var iosScheme: String? // ios scheme
)


// 2) regions table and links to service table above
@Entity(
    tableName = "region_table",
    foreignKeys = [ForeignKey( //foreign key creates relationship to Service entity
        entity = ServiceEntity::class, // Links to ServiceEntity
        parentColumns = ["watchId"], //indicates column in ServiceEntity that is the primary key and holds the r/ship
        childColumns = ["serviceOwnerId"], //references the serviceId column in service table
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["serviceOwnerId"])]
)
data class RegionEntity(
    @PrimaryKey(autoGenerate = true) var regionId: Int = 0,
    @ColumnInfo(name = "regions") var regionCode: String, // Country code (e.g., "US", "GB")
    var serviceOwnerId: Int // Foreign key linking to ServiceEntity
)

// used to fetch service and associated regions in single query
data class ServiceWithRegions(
    @Embedded val service: ServiceEntity, // Embeds the ServiceEntity fields
    @Relation(
        parentColumn = "watchId",
        entityColumn = "serviceOwnerId"
    )
    val regions: List<RegionEntity> // List of regions linked to this service
)