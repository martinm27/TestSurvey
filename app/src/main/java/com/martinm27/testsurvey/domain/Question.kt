package com.martinm27.testsurvey.domain

data class Question(
    val id: Int,
    val question: String,
    val isAnswered: Boolean = false,
    val answer: Answer? = null
)
