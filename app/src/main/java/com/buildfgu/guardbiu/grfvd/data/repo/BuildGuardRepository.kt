package com.buildfgu.guardbiu.grfvd.data.repo

import android.util.Log
import com.buildfgu.guardbiu.grfvd.domain.model.BuildGuardEntity
import com.buildfgu.guardbiu.grfvd.domain.model.BuildGuardParam
import com.buildfgu.guardbiu.grfvd.presentation.app.BuildGuardApplication.Companion.BUILD_GUARD_MAIN_TAG
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface BuildGuardApi {
    @Headers("Content-Type: application/json")
    @POST("config.php")
    fun buildGuardGetClient(
        @Body jsonString: JsonObject,
    ): Call<BuildGuardEntity>
}


private const val BUILD_GUARD_MAIN = "https://builldguard.com/"
class BuildGuardRepository {

    suspend fun buildGuardGetClient(
        buildGuardParam: BuildGuardParam,
        buildGuardConversion: MutableMap<String, Any>?
    ): BuildGuardEntity? {
        val gson = Gson()
        val api = buildGuardGetApi(BUILD_GUARD_MAIN, null)

        val buildGuardJsonObject = gson.toJsonTree(buildGuardParam).asJsonObject
        buildGuardConversion?.forEach { (key, value) ->
            val element: JsonElement = gson.toJsonTree(value)
            buildGuardJsonObject.add(key, element)
        }
        return try {
            val buildGuardRequest: Call<BuildGuardEntity> = api.buildGuardGetClient(
                jsonString = buildGuardJsonObject,
            )
            val buildGuardResult = buildGuardRequest.awaitResponse()
            Log.d(BUILD_GUARD_MAIN_TAG, "Retrofit: Result code: ${buildGuardResult.code()}")
            if (buildGuardResult.code() == 200) {
                Log.d(BUILD_GUARD_MAIN_TAG, "Retrofit: Get request success")
                Log.d(BUILD_GUARD_MAIN_TAG, "Retrofit: Code = ${buildGuardResult.code()}")
                Log.d(BUILD_GUARD_MAIN_TAG, "Retrofit: ${buildGuardResult.body()}")
                buildGuardResult.body()
            } else {
                null
            }
        } catch (e: java.lang.Exception) {
            Log.d(BUILD_GUARD_MAIN_TAG, "Retrofit: Get request failed")
            Log.d(BUILD_GUARD_MAIN_TAG, "Retrofit: ${e.message}")
            null
        }
    }


    private fun buildGuardGetApi(url: String, client: OkHttpClient?) : BuildGuardApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client ?: OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }


}
