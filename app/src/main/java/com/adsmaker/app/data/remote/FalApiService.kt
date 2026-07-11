package com.adsmaker.app.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Url

/**
 * Retrofit interface for the fal.ai queue API. The base URL is
 * https://queue.fal.run/ ; [modelPath] selects the Seedance endpoint, e.g.
 * "bytedance/seedance-2.0-fast/image-to-video".
 *
 * Status/result are polled through the absolute URLs fal returns in the submit
 * response, so those methods take a full @Url.
 */
interface FalApiService {

    @POST("{modelPath}")
    suspend fun submit(
        @Path("modelPath", encoded = true) modelPath: String,
        @Body request: SeedanceRequest,
    ): QueueSubmitResponse

    @GET
    suspend fun status(@Url statusUrl: String): QueueStatusResponse

    @GET
    suspend fun result(@Url responseUrl: String): SeedanceResult
}
