package com.martinm27.testsurvey.api.model

import com.google.gson.annotations.SerializedName
import com.martinm27.testsurvey.domain.Answer

data class ApiAnswer(
    @SerializedName("id")
    val id: Int,

    @SerializedName("answer")
    val answer: String
)

fun ApiAnswer.toDomainModel(): Answer {
    return Answer(id, answer)
}