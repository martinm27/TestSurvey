package com.martinm27.testsurvey.ui.survey.komposable.state

import com.martinm27.testsurvey.domain.Question

data class SurveyState(
    val questions: List<Question> = emptyList(),
    val questionsSubmitted: Int = 0,
    val selectedQuestionPosition: Int = 0,
    val submissionState: SubmissionState? = null,
    val errorMessage: String? = null,
    val navigateBack: Unit? = null
)