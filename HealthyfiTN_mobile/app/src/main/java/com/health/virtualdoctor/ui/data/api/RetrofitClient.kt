package com.health.virtualdoctor.ui.data.api


import android.content.Context
import com.health.virtualdoctor.ui.data.models.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ✅ URLs des deux services
    private const val AUTH_BASE_URL = "https://stores-faq-looks-burner.trycloudflare.com" // Port 8082
    private const val DOCTOR_BASE_URL = "https://viking-game-dale-player.trycloudflare.com" // Port 8083
    private const val USER_BASE_URL = "https://YOUR_USER_SERVICE_URL.trycloudflare.com/" // Port 8085 - REPLACE THIS

    private var authRetrofit: Retrofit? = null
    private var doctorRetrofit: Retrofit? = null
    private var userRetrofit: Retrofit? = null

    private var authApiService: ApiService? = null
    private var doctorApiService: ApiService? = null
    private var userApiService: ApiService? = null

    private var appContext: Context? = null

    // ✅ Init function for compatibility
    fun init(context: Context) {
        appContext = context.applicationContext
        getAuthService(appContext!!)
    }

    // ✅ AUTH Service (port 8082)
    fun getAuthService(context: Context): ApiService {
        if (authApiService == null) {
            authRetrofit = createRetrofit(AUTH_BASE_URL, context)
            authApiService = authRetrofit!!.create(ApiService::class.java)
        }
        return authApiService!!
    }

    // ✅ DOCTOR Service (port 8083)
    fun getDoctorService(context: Context): ApiService {
        if (doctorApiService == null) {
            doctorRetrofit = createRetrofit(DOCTOR_BASE_URL, context)
            doctorApiService = doctorRetrofit!!.create(ApiService::class.java)
        }
        return doctorApiService!!
    }

    // ✅ USER Service (port 8085)
    fun getUserService(context: Context): ApiService {
        if (userApiService == null) {
            userRetrofit = createRetrofit(USER_BASE_URL, context)
            userApiService = userRetrofit!!.create(ApiService::class.java)
        }
        return userApiService!!
    }

    // ✅ Default service (for backward compatibility)
    @Deprecated("Use getAuthService(), getDoctorService(), or getUserService() instead")
    fun getApiService(context: Context): ApiService {
        return getAuthService(context)
    }

    private fun createRetrofit(baseUrl: String, context: Context): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = AuthInterceptor(context.applicationContext)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getAuthBaseUrl(): String = AUTH_BASE_URL
    fun getDoctorBaseUrl(): String = DOCTOR_BASE_URL
    fun getUserBaseUrl(): String = USER_BASE_URL
}