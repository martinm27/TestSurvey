package com.martinm27.testsurvey.ui.survey.komposable.state

import com.martinm27.testsurvey.domain.Answer

data class SubmissionState(
    val isSuccess: Boolean,
    val message: String,
    val answerForRetry: Answer? = null
)
