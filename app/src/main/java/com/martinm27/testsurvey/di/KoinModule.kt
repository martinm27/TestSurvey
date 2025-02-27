package com.martinm27.testsurvey.di

import com.martinm27.testsurvey.BuildConfig
import com.martinm27.testsurvey.api.TestSurveyApi
import com.martinm27.testsurvey.api.converter.NullOnEmptyConverterFactory
import com.martinm27.testsurvey.ui.survey.SurveyViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val apiModule = module {
    single { provideTestSurveyApi(get()) }
    single { provideTestSurveyRetrofit(get()) }
    single { provideOkHttpClient() }
}

val featuresModule = module {
    viewModelOf(::SurveyViewModel)
}

private fun provideTestSurveyApi(retrofit: Retrofit) = retrofit.create(TestSurveyApi::class.java)

private fun provideTestSurveyRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return provideRetrofit(BuildConfig.BASE_URL, okHttpClient)
}

fun provideRetrofit(
    baseUrl: String,
    okHttpClient: OkHttpClient
): Retrofit {
    return Retrofit.Builder()
        .baseUrl(baseUrl).client(okHttpClient)
        .addConverterFactory(NullOnEmptyConverterFactory())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

fun provideOkHttpClient(): OkHttpClient {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    return createOkHttpClient {
        addNetworkInterceptor(loggingInterceptor)
    }
}

fun createOkHttpClient(configuration: OkHttpClient.Builder.() -> Unit = {}): OkHttpClient {
    return OkHttpClient.Builder().apply(configuration).build()
}
