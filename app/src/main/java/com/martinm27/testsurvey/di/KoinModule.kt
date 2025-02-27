package com.martinm27.testsurvey.di

import com.martinm27.testsurvey.BuildConfig
import com.martinm27.testsurvey.api.TestSurveyApi
import com.martinm27.testsurvey.api.converter.NullOnEmptyConverterFactory
import com.martinm27.testsurvey.ui.survey.SurveyViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

const val IO_SCHEDULER = "IO"
const val MAIN_SCHEDULER = "MAIN_THREAD"

val apiModule = module {
    single { provideTestSurveyApi(get()) }
    single { provideTestSurveyRetrofit(get()) }
    single { provideOkHttpClient() }

    single(named(IO_SCHEDULER)) {
        Schedulers.io()
    }

    single(named(MAIN_SCHEDULER)) {
        AndroidSchedulers.mainThread()
    }
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
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
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
