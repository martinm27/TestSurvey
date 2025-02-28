package com.martinm27.testsurvey.data

import android.util.Log
import com.martinm27.testsurvey.api.TestSurveyApi
import com.martinm27.testsurvey.domain.Answer
import com.martinm27.testsurvey.domain.Question
import kotlinx.coroutines.flow.MutableStateFlow

private const val OK_STATUS_CODE = 200

class SurveyRepository(private val surveyApi: TestSurveyApi) {

    private val _questionsFlow = MutableStateFlow<Result<List<Question>>>(Result.success(emptyList()))
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

    /**
     * Submits the answer to the API and returns [Result.success] if successful.
     *
     * If there are any errors, exception is thrown and [Result.failure] will be returned.
     */
    suspend fun postAnswer(answer: Answer): Result<Unit> {
        return try {
            val response = surveyApi.postAnswer(answer.toApiAnswer())

            if (response.code() == OK_STATUS_CODE) {
                updateQuestionsList(answer)
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("Unexpected HTTP code returned: ${response.code()}"))
            }
        } catch (exception: Exception) {
            Log.e("SurveyRepository", exception.message ?: "")
            Result.failure(exception)
        }
    }

    /**
     * Updates the current questions list with an answered questions.
     * - First, the list is converted to mutable list.
     * - Question is found via question ID (answer's ID is also a question ID)
     * - Question object is updated and replaced
     * - New list is emitted to the Flow.
     */
    private suspend fun updateQuestionsList(answer: Answer) {
        val currentList = _questionsFlow.value.getOrNull()?.toMutableList()

        currentList?.let {
            val questionAnswered = it.find { question: Question -> question.id == answer.id }

            val updatedQuestion = questionAnswered?.copy(
                isAnswered = true,
                answer = answer
            )

            it.replaceAll { question ->
                if (question.id == updatedQuestion?.id) {
                    updatedQuestion
                } else {
                    question
                }
            }
        } ?: return

        _questionsFlow.emit(Result.success(currentList.toList()))
    }

    /**
     * Resets the whole survey progress.
     */
    suspend fun reset() {
        _questionsFlow.emit(Result.success(emptyList()))
    }
}