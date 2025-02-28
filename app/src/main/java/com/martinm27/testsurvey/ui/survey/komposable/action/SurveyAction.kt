package com.martinm27.testsurvey.ui.survey.komposable.action

import com.martinm27.testsurvey.api.model.ApiQuestion
import com.martinm27.testsurvey.domain.Answer

sealed interface SurveyAction {
    data object GetQuestions : SurveyAction
    data class QuestionsResponse(val questions: Result<List<ApiQuestion>>) : SurveyAction
    data object Next : SurveyAction
    data object Previous : SurveyAction
    data class Submit(val questionId: Int, val answerContent: String) : SurveyAction
    data class RetrySubmit(val answer: Answer) : SurveyAction
    data class SubmitAnswerResponse(val answer: Answer, val result: Result<Unit>) : SurveyAction
    data object DismissNotificationBanner : SurveyAction
    data object Back : SurveyAction
    data object DismissErrorMessage : SurveyAction
}
