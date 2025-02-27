package com.martinm27.testsurvey.data

import android.util.Log
import com.martinm27.testsurvey.api.TestSurveyApi
import com.martinm27.testsurvey.api.model.Answer
import com.martinm27.testsurvey.domain.Question
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.ResponseBody
import retrofit2.Call

class SurveyRepository(
    private val surveyApi: TestSurveyApi
) {

    private val _questionsFlow =
        MutableStateFlow<Result<List<Question>>>(Result.success(emptyList()))
    val questionsFlow = _questionsFlow

    /**
     * Tries to fetch the questions from the remote API and map them to domain model [Question].
     *
     * If there are any errors, exception is thrown and [Result.failure] will be emitted to the [questionsFlow].
     */
    suspend fun fetchQuestions() {
        try {
            val questions = getQuestions()

            _questionsFlow.emit(Result.success(questions))
        } catch (e: Exception) {
            Log.e("SurveyRepository", e.message ?: "")
            _questionsFlow.emit(Result.failure(e))
        }
    }

    /**
     * Gets questions from the remote API and maps them to domain model [Question].
     */
    private suspend fun getQuestions(): List<Question> {
        val apiQuestions =
            surveyApi.getQuestions() ?: throw IllegalStateException("Null returned from the API")
        return apiQuestions.map {
            Question(
                id = it.id ?: throw IllegalStateException("Question ID should not be null"),
                question = it.question ?: throw IllegalStateException("No question asked! :)")
            )
        }
    }

    suspend fun postAnswer(answer: Answer): Call<ResponseBody> {
        return surveyApi.postAnswer(answer)
    }

    suspend fun reset() {
        _questionsFlow.emit(Result.success(emptyList()))
    }
}