package com.martinm27.testsurvey.ui.survey.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.martinm27.testsurvey.ui.survey.SurveyViewModel
import com.martinm27.testsurvey.ui.survey.UiState
import org.koin.androidx.compose.koinViewModel

@Composable
fun SurveyScreen(
    navigateBack: () -> Unit,
    viewModel: SurveyViewModel = koinViewModel()
) {

    val uiState: UiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.navigateBack != null) {
        navigateBack()
    }

    SurveyScreenContent(
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent
    )
}
