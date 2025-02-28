package com.martinm27.testsurvey.api

import com.martinm27.testsurvey.api.model.Answer
import com.martinm27.testsurvey.api.model.ApiQuestion
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TestSurveyApi {

    @GET("/questions")
    suspend fun getQuestions(): List<ApiQuestion>?

    @POST("/question/submit")
    suspend fun postAnswer(@Body answer: Answer): Response<Unit>
}