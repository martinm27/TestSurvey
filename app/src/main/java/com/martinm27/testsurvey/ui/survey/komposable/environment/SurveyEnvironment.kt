package com.martinm27.testsurvey.ui.survey.komposable.environment

import com.martinm27.testsurvey.api.SurveyClient
import com.martinm27.testsurvey.base.SchedulerProvider

class SurveyEnvironment(
    val surveyClient: SurveyClient,
    val schedulerProvider: SchedulerProvider
)