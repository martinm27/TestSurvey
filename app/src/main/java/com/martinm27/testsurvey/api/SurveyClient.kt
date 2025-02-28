package com.martinm27.testsurvey.api

import com.martinm27.testsurvey.api.model.ApiAnswer
import com.martinm27.testsurvey.api.model.ApiQuestion
import io.reactivex.rxjava3.core.Single

interface SurveyClient {

    fun getQuestions(): Single<Result<List<ApiQuestion>>>

    fun submitAnswer(apiAnswer: ApiAnswer): Single<Result<Unit>>
}

class SurveyClientLive(
    private val testSurveyApi: TestSurveyApi
) : SurveyClient {

    override fun getQuestions(): Single<Result<List<ApiQuestion>>> =
        Single.fromCallable {
            runCatching {
                testSurveyApi.getQuestions().execute().body() ?: emptyList()
            }
        }

    override fun submitAnswer(apiAnswer: ApiAnswer): Single<Result<Unit>> =
        Single.fromCallable {
            runCatching {
                val result = testSurveyApi.postAnswer(apiAnswer).execute()
                if (result.code() == 200) {
                   Unit
                } else {
                    throw IllegalStateException("Unexpected HTTP code ${result.code()}")
                }
            }
        }
}
