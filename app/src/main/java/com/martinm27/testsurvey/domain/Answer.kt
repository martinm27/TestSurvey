package com.martinm27.testsurvey.domain

import com.martinm27.testsurvey.api.model.ApiAnswer

data class Answer(
    /** To make things easier, this ID is questionsId from [Question] object.*/
    val id: Int,
    val answer: String
) {
    fun toApiAnswer(): ApiAnswer = ApiAnswer(id, answer)
}