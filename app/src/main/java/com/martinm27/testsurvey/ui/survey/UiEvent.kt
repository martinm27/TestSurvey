package com.martinm27.testsurvey.ui.survey

import com.martinm27.testsurvey.domain.Answer

sealed interface UiEvent {
    data object Next : UiEvent
    data object Previous : UiEvent
    data class Submit(val questionId: Int, val answerContent: String) : UiEvent
    data class RetrySubmit(val answer: Answer) : UiEvent
    data object DismissNotificationBanner : UiEvent
    data object Back : UiEvent
}