import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.smart.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BookingNotification(ctx:Context,params:WorkerParameters):Worker(ctx,params) {
    override fun doWork(): Result {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "booking_notification_channel"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Booking Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.smart)
            .setContentTitle("Booking Alert")
            .setContentText("Your booking time has completed!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(1, notification)
        updateEmptySlots()

        return Result.success()
    }

    private fun updateEmptySlots() {
        val database: DatabaseReference = FirebaseDatabase.getInstance().reference

        database.child("parking_locations").child("your_parking_id").child("empty_slots")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val currentSlots = dataSnapshot.getValue(Int::class.java) ?: 0
                    database.child("parking_locations").child("your_parking_id").child("empty_slots")
                        .setValue(currentSlots + 1)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
    }
}
