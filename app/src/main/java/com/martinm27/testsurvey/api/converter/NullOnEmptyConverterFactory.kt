package com.martinm27.testsurvey.api.converter

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * Used to avoid an error if backend returns an empty content or empty Object "{}" but we expect an object.
 */
class NullOnEmptyConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *> {
        return Converter<ResponseBody, Any> { responseBody ->
            if (responseBody.contentLength() == 0L || isEmptyObject(responseBody)) {
                null
            } else {
                retrofit.nextResponseBodyConverter<Any>(this, type, annotations)
                    .convert(responseBody)
            }
        }
    }

    private fun isEmptyObject(responseBody: ResponseBody): Boolean {
        val peeked = responseBody.source().peek()
        peeked.request(8) // check for byte length of response
        val responseString =
            peeked.readString(byteCount = minOf(8, peeked.buffer.size), Charsets.UTF_8) // read max of 8 bytes as string
        return responseString == "{}"
    }
}