package com.example.smart

import BookingNotification
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.smart.databinding.ActivityPaymentBinding
import com.google.firebase.database.*
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.util.*

class Payment : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private lateinit var qrCodeImage: ImageView
    private lateinit var database: DatabaseReference
    private lateinit var parkingId: String
    private var price: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        qrCodeImage = binding.imageView1

        val dateAndTime = getDateAndTimeFromIntent()

        parkingId = intent.getStringExtra("parkingId") ?: ""
        val location = intent.getStringExtra("location")
        price = intent.getDoubleExtra("price", 0.0)

        binding.txt1.text = "Location: $location"
        binding.txtpr.text = "Price: $price"
        binding.edtarr.setText("${dateAndTime.day}/${dateAndTime.month + 1}/${dateAndTime.year}")
        binding.edtarr1.setText("${dateAndTime.hour}:${dateAndTime.minute}")

        val upiId = "bshsjggjfdhjijshgdhdudj@oksbi"
        val userName = "Parking Service"
        generateQRCode("upi://pay?pa=$upiId&pn=$userName&am=$price&cu=INR&tn=Parking%20Payment")

        database = FirebaseDatabase.getInstance().reference.child("parking_locations").child(parkingId)

        binding.btnpro.setOnClickListener {
            openPaymentApp()
            val transactionId = binding.edtid.text.toString().trim()
            if (transactionId.isNotEmpty()) {
                confirmBooking(transactionId)
            } else {
                Toast.makeText(this, "Please enter transaction ID", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btncan.setOnClickListener {
            finish()
        }
    }

    private fun generateQRCode(text: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 400, 400)
            qrCodeImage.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPaymentApp() {
        val upiId = "bshsjggjfdhjijshgdhdudj@oksbi"
        val amount = price.toString()
        val name = "Parking Service"
        val payeeUri = Uri.parse("upi://pay?pa=$upiId&pn=$name&am=$amount&cu=INR&tn=Parking%20Payment")
        Log.d("Payment", "UPI URI: $payeeUri")

        val paymentIntent = Intent(Intent.ACTION_VIEW).apply {
            data = payeeUri
        }
        val chooser = Intent.createChooser(paymentIntent, "Pay with")

        if (paymentIntent.resolveActivity(packageManager) != null) {
            startActivity(chooser)
        } else {
            Log.e("Payment", "No UPI app found")
            Toast.makeText(this, "No UPI app found, please install one to proceed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmBooking(transactionId: String) {
        val dateAndTime = getDateAndTimeFromIntent()
        val bookingRef = database.child("bookings").push()
        bookingRef.setValue(
            Booking(
                uid = intent.getStringExtra("uid") ?: "",
                location = intent.getStringExtra("location") ?: "",
                price = price,
                arrivalDate = "${dateAndTime.day}/${dateAndTime.month + 1}/${dateAndTime.year}",
                arrivalTime = "${dateAndTime.hour}:${dateAndTime.minute}",
                transactionId = transactionId
            )
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
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
            } else {
                Toast.makeText(this@Payment, "Failed to book slot", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scheduleBookingEndNotification() {
        val workManager = WorkManager.getInstance(applicationContext)
        val bookingNotificationRequest = OneTimeWorkRequest.Builder(BookingNotification::class.java)
            .build()
        workManager.enqueue(bookingNotificationRequest)
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

    data class Booking(
        val uid: String,
        val location: String,
        val price: Double,
        val arrivalDate: String,
        val arrivalTime: String,
        val transactionId: String
    )
    {
        constructor() : this("", "", 0.0, "", "", "")
    }
}
