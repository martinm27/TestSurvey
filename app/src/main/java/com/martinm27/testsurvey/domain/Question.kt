package com.martinm27.testsurvey.domain

import com.martinm27.testsurvey.api.model.Answer

data class Question(
    val id: Int,
    val question: String,
    val isAnswered: Boolean = false,
    val answer: Answer? = null
)
