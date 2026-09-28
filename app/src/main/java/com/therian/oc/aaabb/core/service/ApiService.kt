package com.therian.oc.aaabb.core.service
import com.therian.oc.aaabb.data.model.PartAPI
import retrofit2.Response
import retrofit2.http.GET
interface ApiService {
    @GET("api/app/ST300_TherianOCMaker")
    suspend fun getAllData(): Response<Map<String, List<PartAPI>>>
}