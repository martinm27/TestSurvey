package com.martinm27.testsurvey.api.model

import com.google.gson.annotations.SerializedName

data class Answer(
    @SerializedName("id")
    val id: Int,

    @SerializedName("answer")
    val answer: String
)
