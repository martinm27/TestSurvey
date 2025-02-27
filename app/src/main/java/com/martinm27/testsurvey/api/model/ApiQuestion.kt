package com.martinm27.testsurvey.api.model

import com.google.gson.annotations.SerializedName

data class ApiQuestion(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("question")
    val question: String? = null
)
