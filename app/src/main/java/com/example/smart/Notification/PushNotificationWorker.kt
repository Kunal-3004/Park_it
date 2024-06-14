package com.example.smart.Notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PushNotificationWorker(context:Context,parameters:WorkerParameters):CoroutineWorker(context,parameters){

    override suspend fun doWork(): Result= withContext(Dispatchers.IO) {

        try {

            val title = inputData.getString("Booking Alert !")
            val body = inputData.getString("Your Booking time is over.\nRebooked your slot.")
            val deepLink = inputData.getString("deepLink")

            val uid = inputData.getString("uid") ?: return@withContext Result.failure()
            val transactionId = inputData.getString("transactionId") ?: return@withContext Result.failure()
            val bookingRef = FirebaseDatabase.getInstance().reference
                .child("bookings")
                .child(uid)
                .child(transactionId)

            bookingRef.child("endTime").addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val endTimeString=snapshot.getValue(String::class.java)
                    if(endTimeString!=null){
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val endTime = sdf.parse(endTimeString) ?: return

                        val currentTime = Calendar.getInstance().time
                        if (currentTime >= endTime) {
                            sendNotification(title!!, body!!, deepLink!!, applicationContext)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
            Result.success()
        }catch (e:Exception){
            Result.failure()
        }
    }
}