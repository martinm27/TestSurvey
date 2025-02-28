package com.martinm27.testsurvey

import com.martinm27.testsurvey.api.SurveyClient
import com.martinm27.testsurvey.api.model.ApiQuestion
import com.martinm27.testsurvey.api.model.toDomainQuestion
import com.martinm27.testsurvey.base.BaseSchedulerProvider
import com.martinm27.testsurvey.domain.Answer
import com.martinm27.testsurvey.domain.Question
import com.martinm27.testsurvey.ui.survey.komposable.action.SurveyAction
import com.martinm27.testsurvey.ui.survey.komposable.environment.SurveyEnvironment
import com.martinm27.testsurvey.ui.survey.komposable.reducer.surveyReducer
import com.martinm27.testsurvey.ui.survey.komposable.state.SubmissionState
import com.martinm27.testsurvey.ui.survey.komposable.state.SurveyState
import com.xm.tka.test.TestStore
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Test

class SurveyTest {

    private val surveyClient =  mockk<SurveyClient>()

    private val schedulerProvider = mockk<BaseSchedulerProvider>()

    @Test
    fun testGetQuestions() {
        val env = mockk<SurveyEnvironment>()
        val testScheduler = Schedulers.trampoline()

        val mockQuestions = listOf(ApiQuestion(1, "What is your favourite food?"))
        val mockQuestionsResult = Result.success(mockQuestions)

        every { surveyClient.getQuestions() } returns Single.just(mockQuestionsResult)

        every { env.surveyClient } returns surveyClient
        every { env.schedulerProvider } returns schedulerProvider
        every { env.schedulerProvider.mainThread() } returns testScheduler
        every { env.schedulerProvider.io() } returns testScheduler

        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        TestStore(SurveyState(), surveyReducer, env).assert {
            send(SurveyAction.GetQuestions) {
                it.copy()
            }
            receive(SurveyAction.QuestionsResponse(mockQuestionsResult)) {
                it.copy(questions = mockQuestions.map(ApiQuestion::toDomainQuestion))
            }
        }
    }

    @Test
    fun testSuccessSubmitAnswer() {
        val env = mockk<SurveyEnvironment>()
        val testScheduler = Schedulers.trampoline()

        val mockQuestions = listOf(Question(id = 1, question = "What is your favourite food?"))

        val mockAnswer = Answer(1, "Pizza")
        val mockAnswerResult = Result.success(Unit)

        every { surveyClient.submitAnswer(any()) } returns Single.just(mockAnswerResult)

        every { env.surveyClient } returns surveyClient
        every { env.schedulerProvider } returns schedulerProvider
        every { env.schedulerProvider.mainThread() } returns testScheduler
        every { env.schedulerProvider.io() } returns testScheduler

        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        TestStore(SurveyState(
            questions = mockQuestions
        ), surveyReducer, env).assert {
            send(SurveyAction.Submit(1, "Pizza")) {
                it.copy()
            }
            receive(SurveyAction.SubmitAnswerResponse(mockAnswer, mockAnswerResult)) {
                val newAnswer =  Answer(1, "Pizza")
                val newQuestions = listOf(
                    Question(
                        id = 1,
                        question = "What is your favourite food?",
                        isAnswered = true,
                        answer = newAnswer
                    )
                )
                val submissionState = SubmissionState(true, "Success")

                it.copy(
                    questions = newQuestions,
                    questionsSubmitted = 1,
                    submissionState = submissionState
                )
            }
        }
    }

    @Test
    fun testFailedSubmitAnswer() {
        val env = mockk<SurveyEnvironment>()
        val testScheduler = Schedulers.trampoline()

        val mockQuestions = listOf(Question(id = 1, question = "What is your favourite food?"))

        val mockAnswer = Answer(1, "Pizza")
        val mockAnswerResult = Result.failure<Unit>(IllegalStateException("Problem"))

        every { surveyClient.submitAnswer(any()) } returns Single.just(mockAnswerResult)

        every { env.surveyClient } returns surveyClient
        every { env.schedulerProvider } returns schedulerProvider
        every { env.schedulerProvider.mainThread() } returns testScheduler
        every { env.schedulerProvider.io() } returns testScheduler

        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        TestStore(SurveyState(
            questions = mockQuestions
        ), surveyReducer, env).assert {
            send(SurveyAction.Submit(1, "Pizza")) {
                it.copy()
            }
            receive(SurveyAction.SubmitAnswerResponse(mockAnswer, mockAnswerResult)) {
                val newAnswer =  Answer(1, "Pizza")
                val submissionState = SubmissionState(false, "Failure!", newAnswer)

                it.copy(submissionState = submissionState)
            }
        }
    }

    @Test
    fun testRetrySubmitAnswer() {
        val env = mockk<SurveyEnvironment>()
        val testScheduler = Schedulers.trampoline()

        val mockQuestions = listOf(Question(id = 1, question = "What is your favourite food?"))

        val mockAnswer = Answer(1, "Pizza")
        val mockAnswerResult = Result.failure<Unit>(IllegalStateException("Problem"))

        every { surveyClient.submitAnswer(any()) } returns Single.just(mockAnswerResult)

        every { env.surveyClient } returns surveyClient
        every { env.schedulerProvider } returns schedulerProvider
        every { env.schedulerProvider.mainThread() } returns testScheduler
        every { env.schedulerProvider.io() } returns testScheduler

        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        TestStore(SurveyState(
            questions = mockQuestions
        ), surveyReducer, env).assert {
            send(SurveyAction.Submit(1, "Pizza")) {
                it.copy()
            }
            receive(SurveyAction.SubmitAnswerResponse(mockAnswer, mockAnswerResult)) {
                val newAnswer = Answer(1, "Pizza")
                val submissionState = SubmissionState(false, "Failure!", newAnswer)

                it.copy(submissionState = submissionState)
            }

            val successfulMockResult = Result.success(Unit)
            every { surveyClient.submitAnswer(any()) } returns Single.just(successfulMockResult)

            send(SurveyAction.RetrySubmit(mockAnswer)) {
                it.copy()
            }

            receive(SurveyAction.Submit(1, "Pizza")) {
                it.copy()
            }

            receive(SurveyAction.SubmitAnswerResponse(mockAnswer, successfulMockResult)) {
                val newAnswer =  Answer(1, "Pizza")
                val newQuestions = listOf(
                    Question(
                        id = 1,
                        question = "What is your favourite food?",
                        isAnswered = true,
                        answer = newAnswer
                    )
                )
                val submissionState = SubmissionState(true, "Success")

                it.copy(
                    questions = newQuestions,
                    questionsSubmitted = 1,
                    submissionState = submissionState
                )
            }
        }
    }

    @Test
    fun testBack() {
        val env = mockk<SurveyEnvironment>()
        val testScheduler = Schedulers.trampoline()

        val mockQuestions = listOf(ApiQuestion(1, "What is your favourite food?"))
        val mockQuestionsResult = Result.success(mockQuestions)

        every { surveyClient.getQuestions() } returns Single.just(mockQuestionsResult)

        every { env.surveyClient } returns surveyClient
        every { env.schedulerProvider } returns schedulerProvider
        every { env.schedulerProvider.mainThread() } returns testScheduler
        every { env.schedulerProvider.io() } returns testScheduler

        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        TestStore(SurveyState(), surveyReducer, env).assert {
            send(SurveyAction.Back) {
                it.copy(navigateBack = Unit)
            }
        }
    }

    @Test
    fun testDismissNotificationBanner() {
        val env = mockk<SurveyEnvironment>()
        val testScheduler = Schedulers.trampoline()

        val mockQuestions = listOf(ApiQuestion(1, "What is your favourite food?"))
        val mockQuestionsResult = Result.success(mockQuestions)

        every { surveyClient.getQuestions() } returns Single.just(mockQuestionsResult)

        every { env.surveyClient } returns surveyClient
        every { env.schedulerProvider } returns schedulerProvider
        every { env.schedulerProvider.mainThread() } returns testScheduler
        every { env.schedulerProvider.io() } returns testScheduler

        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        TestStore(SurveyState(), surveyReducer, env).assert {
            send(SurveyAction.DismissNotificationBanner) {
                it.copy(submissionState = null)
            }
        }
    }

    @Test
    fun testDismissErrorMessage() {
        val env = mockk<SurveyEnvironment>()
        val testScheduler = Schedulers.trampoline()

        val mockQuestions = listOf(ApiQuestion(1, "What is your favourite food?"))
        val mockQuestionsResult = Result.success(mockQuestions)

        every { surveyClient.getQuestions() } returns Single.just(mockQuestionsResult)

        every { env.surveyClient } returns surveyClient
        every { env.schedulerProvider } returns schedulerProvider
        every { env.schedulerProvider.mainThread() } returns testScheduler
        every { env.schedulerProvider.io() } returns testScheduler

        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        TestStore(SurveyState(), surveyReducer, env).assert {
            send(SurveyAction.DismissErrorMessage) {
                it.copy(errorMessage = null)
            }
        }
    }

    @Test
    fun testNext() {
        val env = mockk<SurveyEnvironment>()
        val testScheduler = Schedulers.trampoline()

        val mockQuestions = listOf(ApiQuestion(1, "What is your favourite food?"))
        val mockQuestionsResult = Result.success(mockQuestions)

        every { surveyClient.getQuestions() } returns Single.just(mockQuestionsResult)

        every { env.surveyClient } returns surveyClient
        every { env.schedulerProvider } returns schedulerProvider
        every { env.schedulerProvider.mainThread() } returns testScheduler
        every { env.schedulerProvider.io() } returns testScheduler

        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        TestStore(SurveyState(), surveyReducer, env).assert {
            send(SurveyAction.Next) {
                it.copy(selectedQuestionPosition = it.selectedQuestionPosition + 1)
            }
            receive(SurveyAction.DismissNotificationBanner) {
                it.copy(submissionState = null)
            }
        }
    }

    @Test
    fun testPrevious() {
        val env = mockk<SurveyEnvironment>()
        val testScheduler = Schedulers.trampoline()

        val mockQuestions = listOf(ApiQuestion(1, "What is your favourite food?"))
        val mockQuestionsResult = Result.success(mockQuestions)

        every { surveyClient.getQuestions() } returns Single.just(mockQuestionsResult)

        every { env.surveyClient } returns surveyClient
        every { env.schedulerProvider } returns schedulerProvider
        every { env.schedulerProvider.mainThread() } returns testScheduler
        every { env.schedulerProvider.io() } returns testScheduler

        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        TestStore(SurveyState(), surveyReducer, env).assert {
            send(SurveyAction.Previous) {
                it.copy(selectedQuestionPosition = it.selectedQuestionPosition - 1)
            }
            receive(SurveyAction.DismissNotificationBanner) {
                it.copy(submissionState = null)
            }
        }
    }
}