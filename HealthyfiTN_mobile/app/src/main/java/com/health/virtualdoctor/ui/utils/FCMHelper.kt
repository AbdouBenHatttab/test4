package com.health.virtualdoctor.ui.utils

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.health.virtualdoctor.ui.data.api.RetrofitClient
import com.health.virtualdoctor.ui.data.models.FCMTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FCMHelper {

    fun saveFCMToken(context: Context) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "🔑 FCM Token: $token")

                // ✅ Send to backend
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val request = FCMTokenRequest(
                            fcmToken = token,
                            deviceType = "ANDROID",
                            deviceModel = android.os.Build.MODEL
                        )

                        val authToken = "Bearer ${TokenManager(context).getAccessToken()}"

                        // ✅ TODO: Add this endpoint to your ApiService
                        // RetrofitClient.getNotificationService(context)
                        //     .saveFcmToken(authToken, request)

                        Log.d("FCM", "✅ Token saved to backend")
                    } catch (e: Exception) {
                        Log.e("FCM", "❌ Failed to save token: ${e.message}")
                    }
                }
            } else {
                Log.e("FCM", "❌ Failed to get FCM token", task.exception)
            }
        }
    }
}