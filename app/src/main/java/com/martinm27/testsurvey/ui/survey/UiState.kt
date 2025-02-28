package com.martinm27.testsurvey.ui.survey

import com.martinm27.testsurvey.domain.Answer
import com.martinm27.testsurvey.domain.Question

data class UiState(
    val selectedQuestionPosition: Int = 0,
    val questions: List<Question> = emptyList(),
    val questionsSubmittedCount: Int = 0,
    val isLoading: Boolean = true,
    val submissionState: SubmissionState? = null,
    val errorMessage: String? = null,
    val navigateBack: Unit? = null
)

data class SubmissionState(
    val isSuccess: Boolean,
    val message: String,
    val answerForRetry: Answer? = null
)

