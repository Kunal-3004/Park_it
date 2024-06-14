package com.example.smart

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.smart.DataClass.Booking
import com.example.smart.Notification.Constants
import com.example.smart.Notification.PushNotificationWorker
import com.example.smart.databinding.ActivityPaymentBinding
import com.google.firebase.database.*
import java.util.*
import java.util.concurrent.TimeUnit

class Payment : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private lateinit var database: DatabaseReference
    private lateinit var parkingId: String
    private var price: Double = 0.0
    private lateinit var upiId: String

    companion object {
        private const val UPI_PAYMENT_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dateAndTime = getDateAndTimeFromIntent()
        val notificationManager=applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        parkingId=generateParkingId()

        parkingId = intent.getStringExtra("parkingId") ?: ""
        val location = intent.getStringExtra("location")
        price = intent.getDoubleExtra("price", 0.0)

        binding.txt3.text = "User ID: $parkingId"
        binding.txt1.text = "Location: $location"
        binding.txtpr.text = "Price: $price"
        binding.edtarr.setText("${dateAndTime.day}/${dateAndTime.month + 1}/${dateAndTime.year}")
        binding.edtarr1.setText("${dateAndTime.hour}:${dateAndTime.minute}")
        binding.edtid.setText(generateTransactionId())

        database = FirebaseDatabase.getInstance().reference.child("parking_locations").child(parkingId)

        fetchUPIId {}


        binding.btnpro.setOnClickListener {
            fetchUPIId {
                openPaymentApp()
            }
        }
        binding.btncan.setOnClickListener {
            finish()
        }
    }

    private fun fetchUPIId(onFetched: () -> Unit) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                upiId = snapshot.child("upiId").getValue(String::class.java) ?: ""
                onFetched()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Payment, "Failed to fetch UPI ID", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openPaymentApp() {
        if (upiId.isEmpty()) {
            Toast.makeText(this, "UPI ID not found, please update the parking location details", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = price.toString()
        val name = "Parking Service"
        val payeeUri = Uri.parse("upi://pay?pa=$upiId&pn=$name&am=$amount&cu=INR&tn=Parking%20Payment")
        Log.d("Payment", "UPI URI: $payeeUri")

        val paymentIntent = Intent(Intent.ACTION_VIEW, payeeUri)
        val chooser = Intent.createChooser(paymentIntent, "Pay with")

        if (paymentIntent.resolveActivity(packageManager) != null) {
            try {
                startActivityForResult(chooser, UPI_PAYMENT_REQUEST_CODE)
            } catch (e: ActivityNotFoundException) {
                Log.e("Payment", "No UPI app found", e)
                Toast.makeText(this, "No UPI app found, please install one to proceed", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("Payment", "No UPI app found")
            Toast.makeText(this, "No UPI app found, please install one to proceed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateTransactionId(): String {
        return UUID.randomUUID().toString()
    }
    private fun generateParkingId(): String {
        return UUID.randomUUID().toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UPI_PAYMENT_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    confirmBooking()
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Payment failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun confirmBooking() {
        val transactionId = generateTransactionId()
        val dateAndTime = getDateAndTimeFromIntent()

        val arrivalCalendar = Calendar.getInstance().apply {
            set(dateAndTime.year, dateAndTime.month, dateAndTime.day, dateAndTime.hour, dateAndTime.minute)
        }
        arrivalCalendar.add(Calendar.HOUR_OF_DAY, 1)

        val endTime = "${arrivalCalendar.get(Calendar.HOUR_OF_DAY)}:${arrivalCalendar.get(Calendar.MINUTE)}"

        val bookingRef = database.child("bookings").push()
        bookingRef.setValue(
            Booking(
                uid = intent.getStringExtra("uid") ?: "",
                location = intent.getStringExtra("location") ?: "",
                price = price,
                arrivalDate = "${dateAndTime.day}/${dateAndTime.month + 1}/${dateAndTime.year}",
                arrivalTime = "${dateAndTime.hour}:${dateAndTime.minute}",
                endTime=endTime,
                transactionId = transactionId
            )
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateSlotAvailability()
                scheduleBookingEndNotification()
            } else {
                Toast.makeText(this, "Failed to book slot", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSlotAvailability() {
        database.child("empty_slots").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentSlots = snapshot.getValue(Int::class.java) ?: 0
                database.child("empty_slots").setValue(currentSlots - 1)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@Payment, "Slot booked successfully", Toast.LENGTH_SHORT).show()
                            scheduleBookingEndNotification()
                            finish()
                        } else {
                            Toast.makeText(this@Payment, "Failed to update slot availability", Toast.LENGTH_SHORT).show()
                        }
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Payment, "Failed to retrieve empty slots", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun scheduleBookingEndNotification() {
        val workManager = WorkManager.getInstance(applicationContext)
        val bookingNotificationRequest = OneTimeWorkRequest.Builder(PushNotificationWorker::class.java)
            .build()
        workManager.enqueue(bookingNotificationRequest)
    }
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                Constants.PUSH_NOTIFICATION_CHANNEL_ID,
                Constants.PUSH_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        else {
            return
        }
    }


    private fun getDateAndTimeFromIntent(): DateAndTime {
        return DateAndTime(
            year = intent.getIntExtra("YEAR", 0),
            month = intent.getIntExtra("MONTH", 0),
            day = intent.getIntExtra("DAY", 0),
            hour = intent.getIntExtra("HOUR", 0),
            minute = intent.getIntExtra("MINUTE", 0)
        )
    }

    data class DateAndTime(
        val year: Int,
        val month: Int,
        val day: Int,
        val hour: Int,
        val minute: Int
    )
}
