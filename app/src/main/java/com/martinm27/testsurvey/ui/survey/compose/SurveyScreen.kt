package com.martinm27.testsurvey.ui.survey.compose

import androidx.compose.runtime.Composable
import com.martinm27.testsurvey.ui.survey.SurveyViewModel
import com.xm.tka.ui.ViewStore.Companion.view
import org.koin.androidx.compose.koinViewModel

@Composable
fun SurveyScreen(
    navigateBack: () -> Unit,
    viewModel: SurveyViewModel = koinViewModel()
) {

    val viewStore = viewModel.store.view()

    SurveyScreenContent(
        navigateBack,
        viewStore = viewStore,
    )
}