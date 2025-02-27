package com.martinm27.testsurvey.ui.survey.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.martinm27.testsurvey.ui.survey.SurveyViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreen(
    navigateBack: () -> Unit,
    viewModel: SurveyViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(pageCount = { uiState.questions.size })
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        viewModel.reset()
        navigateBack()
    }

    LoadingOverlay(
        isLoading = uiState.isLoading
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.LightGray,
                ),
                title = { Text("Question ${pagerState.currentPage + 1}/${uiState.questions.size}") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.reset()
                            navigateBack()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                if (pagerState.currentPage > 0) pagerState.animateScrollToPage(
                                    pagerState.currentPage - 1
                                )
                            }
                        },
                        enabled = pagerState.currentPage > 0
                    ) {
                        Text("Previous")
                    }
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                if (pagerState.currentPage < uiState.questions.size - 1) pagerState.animateScrollToPage(
                                    pagerState.currentPage + 1
                                )
                            }
                        },
                        enabled = pagerState.currentPage < uiState.questions.size - 1
                    ) {
                        Text("Next")
                    }
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Questions submitted: 0", modifier = Modifier.padding(16.dp))
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.background(Color.LightGray)
            ) { page ->
                val question = "What is your favourite food?"
                SurveyQuestionScreen(
                    question = question,
                    onSubmit = {
                        // TODO: Handle submit button action
                    },
                    isSubmitted = false,
                )
            }
        }
    }
}

@Composable
fun SurveyQuestionScreen(
    question: String,
    onSubmit: (String) -> Unit,
    isSubmitted: Boolean,
) {
    var answerInput by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = question,
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = answerInput,
            onValueChange = { newValue: String ->
                answerInput = newValue
            },
            label = {
                Text("Type here an answer...", color = Color.LightGray)
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium,
            enabled = !isSubmitted,
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onSubmit(answerInput)
            },
            enabled = answerInput.isNotBlank() && !isSubmitted
        ) {
            Text("Submit")
        }
    }
}
