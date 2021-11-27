package team.everywhere.googlemaptest.network

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("getStationByName")
    suspend fun getStationByName(
        @Query("ServiceKey") sk: String,
        @Query("stSrch") search: String,
        @Query("resultType") rt: String //Json 고정
    ): Response<JsonObject>

    @GET("getStationByPos")
    suspend fun getStationByPos(
        @Query("ServiceKey") sk: String,
        @Query("tmX") lng: Double,
        @Query("tmY") lat: Double,
        @Query("radius") radius: Int,
        @Query("resultType") rt: String
    ): Response<JsonObject>

}