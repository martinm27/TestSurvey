## General Info
This project is implemented in two different versions of MVI architecture:
* Using Kotlin Coroutines and Flow together with Clean Architecture - branch [complete-task-v1](https://github.com/martinm27/TestSurvey/tree/complete-task-v1)
* Using RxJava with TKA (The Komposable Architecture) - branch [complete-task-v2](https://github.com/martinm27/TestSurvey/tree/complete-task-v2)

### UI layer
UI part is implemented using **Single Activity approach** with underlying **Jetpack Compose** screens.

There are two screens:
* Landing screen
* Survey Screen

### Data model
There are two layers of data models: API model and domain model. 

**API models** are serialized as **GSON** objects and they are used in the API calls. 

**Domain models** are used in the domain layer and have respective mapping functions to/from the API models.

`Question` data model has an ID and content received from the API model. Question is not answered by default. Additionally, when the answer is submitted successfully, question object is updated with the provided answer and re-emitted back to the UI.

```kotlin
data class Question(
    val id: Int,
    val question: String,
    val isAnswered: Boolean = false,
    val answer: Answer? = null
)
```

`Answer` data model contains question's ID as identifier (it could have been an additional field, but to not overengineer it, it is done this way) and content as `String`.

```kotlin
data class Answer(
    /** To make things easier, this ID is questionsId from [Question] object.*/
    val id: Int,
    val answer: String
)
```

### API
**Retrofit** interface called `TestSurveyApi` with two functions:

```kotlin
    @GET("/questions")
    fun getQuestions(): Call<List<ApiQuestion>>

    @POST("/question/submit")
    fun postAnswer(@Body answer: ApiAnswer): Call<ResponseBody>
```

### Dependency Injection (Service locator)
**Koin** framework is used for **dependency injection** due to its ease of use.

### Testing
JUnit tests combined with [MockK](https://github.com/mockk/mockk) testing library for Kotlin.

## 1st Version (Kotlin Coroutines & Flow)

### Data layer
`SurveyRepository` is the central component of the **business logic**. In different circumstances, its functionality should be broken down into use cases, but for the sake of simplicity, all the logic is nested inside this component.

It communicates directly to the API via `TestSurveyApi` component as there is no need to have an intermediate layer of data sources to save the data locally. 
 
It "hosts" the state of the questions list wrapped in Kotlin's `Result` class and published to the UI through the `Kotlin StateFlow`.

### UI layer
`SurveyViewModel` orchestrates changes of the `UiState` via `StateFlow`. It is observing the `SurveyRepository.questionsFlow`, maps the new questions data and emits new UiState object to the Compose screens.

#### UI state
The following data class defines the UI state for the survey screen:

```kotlin
data class UiState(
    val selectedQuestionPosition: Int = 0,
    val questions: List<Question> = emptyList(),
    val questionsSubmittedCount: Int = 0,
    val isLoading: Boolean = true,
    val submissionState: SubmissionState? = null,
    val errorMessage: String? = null,
    val navigateBack: Unit? = null
)

data class SubmissionState(
    val isSuccess: Boolean,
    val message: String,
    val answerForRetry: Answer? = null
)
```
#### UI event
The following interface defines all the UI events that are generated from the Compose screen to the ViewModel.

```kotlin
sealed interface UiEvent {
    data object Next : UiEvent
    data object Previous : UiEvent
    data class Submit(val questionId: Int, val answerContent: String) : UiEvent
    data class RetrySubmit(val answer: Answer) : UiEvent
    data object DismissNotificationBanner : UiEvent
    data object Back : UiEvent
}
```


## 2nd Version (TKA)

### Data layer
`SurveyClient` handles the communication with the API `TestSurveyApi` component. It propagates result as `Single<Result<*>` to which UI can subscribe.

### TKA part

#### State
`SurveyState` defines the UI state. It could be probably also broken down into smaller state components, but as I was still learning the architecture, I didn't have time to play with it more.

```kotlin
data class SurveyState(
    val questions: List<Question> = emptyList(),
    val questionsSubmitted: Int = 0,
    val selectedQuestionPosition: Int = 0,
    val submissionState: SubmissionState? = null,
    val errorMessage: String? = null,
    val navigateBack: Unit? = null
)
```

`SubmissionState` is a part of `SurveyState` and encapsulates state of answer submission (success or failure) together with retry action.

```kotlin
data class SubmissionState(
    val isSuccess: Boolean,
    val message: String,
    val answerForRetry: Answer? = null
)
```

#### Action
`SurveyAction` defines the list of possible actions that can happen throughout the survey feature.

```kotlin
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
```

#### Environment
`SurveyEnvironment` communicates with `SurveyClient` and exposes the background/main schedulers.

```kotlin
class SurveyEnvironment(
    val surveyClient: SurveyClient,
    val schedulerProvider: SchedulerProvider
)
```

#### Reducer
`SurveyReducer` is a reducer that implements the logic for this domain. It describes how to change the current state to the next state, and describes what effects need to be executed. 

#### Store
Encapsulated by the `SurveyViewModel` on which the Compose screen has a reference to:

```kotlin
val viewStore = viewModel.store.view()

val uiState = viewStore.states.subscribeAsState(viewStore.currentState).value
```




