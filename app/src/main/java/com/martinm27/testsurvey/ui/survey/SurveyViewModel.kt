package com.martinm27.testsurvey.ui.survey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinm27.testsurvey.data.SurveyRepository
import com.martinm27.testsurvey.domain.Answer
import com.martinm27.testsurvey.domain.Question
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SurveyViewModel(
    private val surveyRepository: SurveyRepository,
    private val defaultDispatcher: CoroutineDispatcher,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeQuestions()
        fetchQuestions()
    }

    private fun observeQuestions() {
        viewModelScope.launch {
            surveyRepository.questionsFlow
                .collectLatest { updatedQuestionsResult ->
                    updatedQuestionsResult.fold(
                        onSuccess = {
                            updateQuestionsUiState(updatedQuestionsResult.getOrDefault(emptyList()))
                        },
                        onFailure = {
                            _uiState.update { it.copy(errorMessage = "Something went wrong. Try again later!") }
                        }
                    )
                }
        }
    }

    private fun fetchQuestions() {
        viewModelScope.launch(ioDispatcher) {
            surveyRepository.fetchQuestions()
        }
    }

    private suspend fun updateQuestionsUiState(updatedQuestions: List<Question>) {
        _uiState.update { state ->

            val questionsSubmittedCount = withContext(defaultDispatcher) {
                updatedQuestions.count(Question::isAnswered)
            }

            state.copy(
                questions = updatedQuestions,
                questionsSubmittedCount = questionsSubmittedCount,
                isLoading = false
            )
        }
    }

    fun onUiEvent(uiEvent: UiEvent) {
        when(uiEvent) {
            is UiEvent.Back -> {
                reset()
                _uiState.update { it.copy(navigateBack = Unit) }
            }

            is UiEvent.DismissNotificationBanner -> {
                _uiState.update { it.copy(submissionState = null) }
            }

            is UiEvent.Next -> {
                val currentPosition = _uiState.value.selectedQuestionPosition
                _uiState.update { it.copy(selectedQuestionPosition = currentPosition + 1) }
            }

            is UiEvent.Previous -> {
                val currentPosition = _uiState.value.selectedQuestionPosition
                _uiState.update { it.copy(selectedQuestionPosition = currentPosition - 1) }
            }

            is UiEvent.Submit -> {
                with(uiEvent) {
                    submitAnswer(questionId = questionId, answerContent = answerContent)
                }
            }

            is UiEvent.RetrySubmit -> {
                with (uiEvent.answer) {
                    submitAnswer(questionId = id, answerContent = answer)
                }
            }
        }
    }

    private fun reset() {
        viewModelScope.launch {
            surveyRepository.reset()
        }
    }

    private fun submitAnswer(questionId: Int, answerContent: String) {
        viewModelScope.launch(ioDispatcher) {
            val answer = Answer(id = questionId, answer = answerContent)
            val result = surveyRepository.postAnswer(answer = answer)

            showNotificationBanner(result.isSuccess, answer)
        }
    }

    private fun showNotificationBanner(isSubmittedSuccessfully: Boolean, answer: Answer) {
        _uiState.update {
            it.copy(
                submissionState = if (isSubmittedSuccessfully) {
                    SubmissionState(isSuccess = true, message = "Success")
                } else {
                    SubmissionState(isSuccess = false, message = "Failure!", answerForRetry = answer)
                }
            )
        }
    }
}