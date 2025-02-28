package com.martinm27.testsurvey.ui.survey.compose

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.martinm27.testsurvey.domain.Question
import com.martinm27.testsurvey.ui.survey.komposable.action.SurveyAction
import com.martinm27.testsurvey.ui.survey.komposable.state.SubmissionState
import com.martinm27.testsurvey.ui.survey.komposable.state.SurveyState
import com.xm.tka.ui.ViewStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreenContent(
    navigateBack: () -> Unit,
    viewStore: ViewStore<SurveyState, SurveyAction>
) {
    val uiState = viewStore.states.subscribeAsState(viewStore.currentState).value

    val pagerState = rememberPagerState(
        initialPage = uiState.selectedQuestionPosition,
        pageCount = { uiState.questions.size }
    )

    val context = LocalContext.current

    if (uiState.navigateBack != null) {
        navigateBack()
    }

    if (uiState.errorMessage != null) {
        Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_SHORT).show()
        viewStore.send(SurveyAction.DismissErrorMessage)
    }

    BackHandler {
        viewStore.send(SurveyAction.Back)
    }

    LaunchedEffect(uiState.selectedQuestionPosition) {
        pagerState.animateScrollToPage(uiState.selectedQuestionPosition)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.LightGray,
            ),
            title = { Text("Question ${pagerState.currentPage + 1}/${uiState.questions.size}") },
            navigationIcon = {
                IconButton(
                    onClick = {
                        viewStore.send(SurveyAction.Back)
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        viewStore.send(SurveyAction.Previous)
                    },
                    enabled = pagerState.currentPage > 0
                ) {
                    Text("Previous")
                }
                TextButton(
                    onClick = {
                        viewStore.send(SurveyAction.Next)
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
            Text(
                "Questions submitted: ${uiState.questionsSubmitted}",
                modifier = Modifier.padding(16.dp)
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.background(Color.LightGray),
            userScrollEnabled = false
        ) { page ->
            val question = uiState.questions[page]
            SurveyQuestionScreen(
                viewStore = viewStore,
                question = question,
                submissionState = uiState.submissionState,
            )
        }
    }
}

@Composable
fun SurveyQuestionScreen(
    viewStore: ViewStore<SurveyState, SurveyAction>,
    question: Question,
    submissionState: SubmissionState?
) {
    var answerInput by rememberSaveable { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    if (submissionState != null) {
        Popup(
            alignment = Alignment.TopCenter,
            properties = PopupProperties(focusable = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (submissionState.isSuccess) {
                            Color.Green
                        } else {
                            Color.Red
                        }
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = submissionState.message,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (submissionState.answerForRetry != null) {
                        Button(
                            onClick = {
                                viewStore.send(SurveyAction.RetrySubmit(submissionState.answerForRetry))
                            }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }

        LaunchedEffect(submissionState) {
            coroutineScope.launch {
                delay(5000)
                viewStore.send(SurveyAction.DismissNotificationBanner)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = question.question,
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
            enabled = !question.isAnswered,
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                viewStore.send(
                    SurveyAction.Submit(
                        questionId = question.id,
                        answerContent = answerInput
                    )
                )
            },
            enabled = answerInput.isNotBlank() && !question.isAnswered
        ) {
            Text("Submit")
        }
    }
}