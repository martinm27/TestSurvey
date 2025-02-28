package com.martinm27.testsurvey.ui.survey.komposable.reducer

import com.martinm27.testsurvey.api.model.ApiQuestion
import com.martinm27.testsurvey.api.model.toDomainQuestion
import com.martinm27.testsurvey.domain.Answer
import com.martinm27.testsurvey.domain.Question
import com.martinm27.testsurvey.ui.survey.komposable.action.SurveyAction
import com.martinm27.testsurvey.ui.survey.komposable.environment.SurveyEnvironment
import com.martinm27.testsurvey.ui.survey.komposable.state.SubmissionState
import com.martinm27.testsurvey.ui.survey.komposable.state.SurveyState
import com.xm.tka.Effects
import com.xm.tka.Reducer
import com.xm.tka.toEffect

val surveyReducer =
    Reducer<SurveyState, SurveyAction, SurveyEnvironment> { state, action, env ->
        when (action) {
            is SurveyAction.GetQuestions -> {
                state + env.surveyClient.getQuestions()
                    .subscribeOn(env.schedulerProvider.io())
                    .observeOn(env.schedulerProvider.mainThread())
                    .map<SurveyAction> { SurveyAction.QuestionsResponse(it) }
                    .toEffect()
            }

            is SurveyAction.QuestionsResponse -> {
                action.questions.fold(
                    onSuccess = { data ->
                        state.copy(questions = data.map(ApiQuestion::toDomainQuestion)) + Effects.none()
                    },
                    onFailure = { throwable ->
                        state.copy(errorMessage = throwable.message) + Effects.none()
                    }
                )
            }

            is SurveyAction.Back -> state.copy(navigateBack = Unit) + Effects.none()

            is SurveyAction.DismissNotificationBanner ->
                state.copy(submissionState = null) + Effects.none()

            is SurveyAction.Next ->
                state.copy(
                    selectedQuestionPosition = state.selectedQuestionPosition + 1,
                ) + Effects.just(SurveyAction.DismissNotificationBanner)

            is SurveyAction.Previous ->
                state.copy(
                    selectedQuestionPosition = state.selectedQuestionPosition - 1
                ) + Effects.just(SurveyAction.DismissNotificationBanner)

            is SurveyAction.RetrySubmit -> state + Effects.just(
                SurveyAction.Submit(
                    questionId = action.answer.id,
                    answerContent = action.answer.answer
                )
            )

            is SurveyAction.Submit -> {
                val answer = Answer(
                    id = action.questionId,
                    answer = action.answerContent
                )
                state + env.surveyClient.submitAnswer(apiAnswer = answer.toApiAnswer())
                    .subscribeOn(env.schedulerProvider.io())
                    .observeOn(env.schedulerProvider.mainThread())
                    .map<SurveyAction> { SurveyAction.SubmitAnswerResponse(answer, it) }
                    .toEffect()
            }

            is SurveyAction.SubmitAnswerResponse -> {
                val updatedQuestions = updatedQuestionsList(
                    questions = state.questions,
                    isSubmitSuccessful = action.result.isSuccess,
                    answer = action.answer
                )
                val submissionState = buildSubmissionState(
                    answer = action.answer,
                    isSubmitSuccessful = action.result.isSuccess
                )

                state.copy(
                    questions = updatedQuestions,
                    questionsSubmitted = updatedQuestions.count(Question::isAnswered),
                    submissionState = submissionState
                ) + Effects.none()
            }

            is SurveyAction.DismissErrorMessage -> {
                state.copy(errorMessage = null) + Effects.none()
            }
        }
    }

fun buildSubmissionState(answer: Answer, isSubmitSuccessful: Boolean): SubmissionState =
    if (isSubmitSuccessful) {
        SubmissionState(isSuccess = true, message = "Success")
    } else {
        SubmissionState(
            isSuccess = false,
            message = "Failure!",
            answerForRetry = answer
        )
    }

/**
 * Updates the current questions list with an answered questions.
 * - First, the list is converted to mutable list.
 * - Question is found via question ID (answer's ID is also a question ID)
 * - Question object is updated and replaced
 * - New list is emitted to the subscriber.
 */
fun updatedQuestionsList(
    questions: List<Question>,
    isSubmitSuccessful: Boolean,
    answer: Answer
): List<Question> {
    if (!isSubmitSuccessful) return questions

    val mutableQuestionsList = questions.toMutableList()

    val questionAnswered =
        mutableQuestionsList.find { question: Question -> question.id == answer.id }

    val updatedQuestion = questionAnswered?.copy(
        isAnswered = true,
        answer = answer
    )

    mutableQuestionsList.replaceAll { question ->
        if (question.id == updatedQuestion?.id) {
            updatedQuestion
        } else {
            question
        }
    }

    return mutableQuestionsList.toList()
}


