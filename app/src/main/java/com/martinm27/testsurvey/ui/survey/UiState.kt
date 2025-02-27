package com.martinm27.testsurvey.ui.survey

import com.martinm27.testsurvey.domain.Question

data class UiState(
    val questions: List<Question>,
    val isLoading: Boolean = true,
    val submissionState: SubmissionState? = null,
    val errorMessage: String? = null
)

enum class SubmissionState {
    Successful,
    Error
}
