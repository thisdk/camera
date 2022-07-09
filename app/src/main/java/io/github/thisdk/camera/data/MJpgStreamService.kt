package io.github.thisdk.camera.data

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming

interface MJpgStreamService {

    @GET("/?action=stream")
    @Streaming
    suspend fun stream(): ResponseBody

}