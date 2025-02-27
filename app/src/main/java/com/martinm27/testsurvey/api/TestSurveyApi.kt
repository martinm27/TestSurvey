package com.martinm27.testsurvey.api

import com.martinm27.testsurvey.api.model.Answer
import com.martinm27.testsurvey.api.model.Question
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TestSurveyApi {

    @GET("/questions")
    fun getQuestions(): Single<List<Question>>

    @POST("/question/submit")
    fun postAnswer(@Body answer: Answer): Single<Void>
}