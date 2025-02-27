package com.martinm27.testsurvey.ui.survey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinm27.testsurvey.data.SurveyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SurveyViewModel(
    private val surveyRepository: SurveyRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState(questions = emptyList()))
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
                            _uiState.update {
                                it.copy(
                                    questions = updatedQuestionsResult.getOrDefault(emptyList()),
                                    isLoading = false
                                )
                            }
                        },
                        onFailure = {
                            _uiState.update { it.copy(errorMessage = "Something went wrong. Try again later!") }
                        }
                    )

                }
        }
    }

    private fun fetchQuestions() {
        viewModelScope.launch(Dispatchers.IO) {
            surveyRepository.fetchQuestions()
        }
    }

    fun reset() {
        viewModelScope.launch {
            surveyRepository.reset()
        }
    }
}