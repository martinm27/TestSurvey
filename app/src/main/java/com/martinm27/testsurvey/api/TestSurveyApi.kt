package com.martinm27.testsurvey.api

import com.martinm27.testsurvey.api.model.ApiAnswer
import com.martinm27.testsurvey.api.model.ApiQuestion
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TestSurveyApi {

    @GET("/questions")
    fun getQuestions(): Call<List<ApiQuestion>>

    @POST("/question/submit")
    fun postAnswer(@Body answer: ApiAnswer): Call<ResponseBody>
}