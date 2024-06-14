package com.example.smart.Notification

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushNotificationService: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println(token)
        storeTokenInFirebase(token)

    }
    private fun storeTokenInFirebase(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val tokenRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(uid)
            .child("fcmToken")

        tokenRef.setValue(token)
            .addOnSuccessListener {
                println("FCM Token stored successfully")
            }
            .addOnFailureListener { e ->
                println("Failed to store FCM Token: ${e.message}")
            }
    }


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if(message.data.isNotEmpty()){
            val title=message.data["Booking Alert !"]
            val body=message.data["Your Booking time is over.\nRebooked your slot."]
            val deepLink=message.data["deepLink"]?:""

            val workerData= workDataOf(
                "Booking Alert !" to title,
                "Your Booking time is over.\nRebooked your slot." to body,
                "deepLink" to deepLink

            )

            val notificationRequest = OneTimeWorkRequestBuilder<PushNotificationWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(workerData)
                .build()

            WorkManager.getInstance(applicationContext)
                .enqueue(notificationRequest)
        }

        // Respond to received messages
        // You can specify here what to do when the received notification is clicked
    }
}