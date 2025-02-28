package com.martinm27.testsurvey.api.model

import com.google.gson.annotations.SerializedName
import com.martinm27.testsurvey.domain.Question

data class ApiQuestion(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("question")
    val question: String? = null
)

fun ApiQuestion.toDomainQuestion(): Question {
    return Question(
        id = id ?: -1,
        question = question ?: ""
    )
}