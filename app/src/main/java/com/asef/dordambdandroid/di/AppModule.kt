package com.asef.dordambdandroid.di

import android.content.Context
import android.content.SharedPreferences
import com.asef.dordambdandroid.data.remote.DorDamBDAPI
import com.asef.dordambdandroid.repository.DorDamBDRepository
import com.asef.dordambdandroid.util.Configuration.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideDorDamBDRepository(api: DorDamBDAPI) =
        DorDamBDRepository(api)

//    @Singleton
//    @Provides
//    fun provideSettingsRepository(sharedPreferences: SharedPreferences) =
//        SettingsRepository(sharedPreferences)

    @Singleton
    @Provides
    fun provideDriverApi(client: OkHttpClient): DorDamBDAPI {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(client)
            .build()
            .create(DorDamBDAPI::class.java)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder().addInterceptor(interceptor).build()
    }

    @Singleton
    @Provides
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("DOR_DAM_BD_PREF", Context.MODE_PRIVATE)
    }
}