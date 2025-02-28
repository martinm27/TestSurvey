package com.martinm27.testsurvey.ui.survey

import androidx.lifecycle.ViewModel
import com.martinm27.testsurvey.ui.survey.komposable.action.SurveyAction
import com.martinm27.testsurvey.ui.survey.komposable.environment.SurveyEnvironment
import com.martinm27.testsurvey.ui.survey.komposable.reducer.surveyReducer
import com.martinm27.testsurvey.ui.survey.komposable.state.SurveyState
import com.xm.tka.Store

class SurveyViewModel(
    surveyEnvironment: SurveyEnvironment
) : ViewModel() {

    val store = Store(
        initialState = SurveyState(),
        reducer = surveyReducer,
        environment = surveyEnvironment
    )

    init {
        store.send(SurveyAction.GetQuestions)
    }
}







