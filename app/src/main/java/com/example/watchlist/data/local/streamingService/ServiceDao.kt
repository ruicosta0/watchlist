package com.example.watchlist.data.local.streamingService

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ServiceDao {
    // Insert a single service
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(services: List<ServiceEntity>)

    @Query("select * from service_table")
    suspend fun getAllServices() : List<ServiceEntity>

    // Insert a list of regions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegions(regions: List<RegionEntity>)

    // Get all services with their regions
    @Transaction
    @Query("SELECT * FROM service_table")
    fun getServicesWithRegions(): LiveData<List<ServiceWithRegions>>

    @Transaction
    @Query("""
    SELECT * FROM service_table 
    WHERE (:regionCode IS NULL OR :regionCode = '') 
    OR watchId IN (
        SELECT serviceOwnerId FROM region_table WHERE regions = :regionCode
    )
""")
    fun getServicesByRegion(regionCode: String?): LiveData<List<ServiceWithRegions>>

    //retrieve service by network id
    @Query("SELECT logoUrl FROM service_table WHERE watchId = :serviceId")
    fun getStreamingServiceLogo(serviceId: Int?) : String?

    // Delete a service (optional)
    @Delete
    suspend fun deleteService(service: ServiceEntity)

    //count rows in database
    @Query("SELECT COUNT(name) from service_table")
    suspend fun getRowCountService() : Int

    @Query("SELECT COUNT(regionId) from region_table")
    suspend fun getRowCountRegion() : Int
}