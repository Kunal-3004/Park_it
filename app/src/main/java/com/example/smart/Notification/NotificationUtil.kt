package com.example.smart.Notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.icu.text.CaseMap.Title
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.example.smart.R
import com.example.smart.User1
import kotlin.random.Random

fun sendNotification(
    title: String,
    body: String,
    deepLink:String,
    context: Context
){
    val notificationManager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val taskIntent= Intent(
        Intent.ACTION_VIEW,
        deepLink.toUri(),
        context,
        User1::class.java
    )
    val pendingIntent = TaskStackBuilder.create(context).run{
        addNextIntentWithParentStack(taskIntent)
        getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT  or PendingIntent.FLAG_IMMUTABLE)
    }

    val notificationBuilder=NotificationCompat.Builder(context,Constants.PUSH_NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.smart)
        .setContentTitle(title)
        .setContentText(body)
        .setContentIntent(pendingIntent)
        .setPriority(NotificationManager.IMPORTANCE_HIGH)
        .setAutoCancel(true)
        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

    notificationManager.notify(Random.nextInt(),notificationBuilder.build())

}