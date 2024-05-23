package com.example.smart

import View
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smart.databinding.ActivityPaymentBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class Payment : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private lateinit var qrCodeImage: ImageView
    private lateinit var database: DatabaseReference
    private lateinit var parkingId: String
    private var emptySlots: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        qrCodeImage = findViewById(R.id.imageView1)

        parkingId = intent.getStringExtra("parkingId") ?: ""
        val location = intent.getStringExtra("location")
        val price = intent.getDoubleExtra("price", 0.0)
        emptySlots = intent.getIntExtra("emptySlots", 0)

        binding.txt3.text = "User Id: " + intent.getStringExtra("uid")
        binding.txt1.text = "Location: $location"
        binding.txtpr.text = "Price: $price"

        val upiString = generateUPIString("recipient-upi-id@bank", "Recipient Name", price, "Parking Payment")
        generateQRCode(upiString)

        database = FirebaseDatabase.getInstance().reference.child("parking_locations").child(parkingId)

        binding.btnpro.setOnClickListener {
            val transactionId = binding.edtid.text.toString().trim()
            if (transactionId.isNotEmpty()) {
                if (emptySlots > 0) {
                    val bookingRef = database.child("bookings").push()
                    bookingRef.setValue(
                        Booking(
                            uid = intent.getStringExtra("uid") ?: "",
                            location = location ?: "",
                            price = price,
                            arrivalDate = binding.edtarr.text.toString(),
                            arrivalTime = binding.edtarr1.text.toString(),
                            transactionId = transactionId
                        )
                    ).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            database.child("empty_slots").setValue(emptySlots - 1).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Slot booked successfully", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, View::class.java)
                                    intent.putExtra("uid", intent.getStringExtra("uid"))
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this, "Failed to update slot availability", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(this, "Failed to book slot", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "No empty slots available", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter transaction ID", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btncan.setOnClickListener {
            finish()
        }
    }

    private fun generateUPIString(upiId: String, name: String, amount: Double, note: String): String {
        return "upi://pay?pa=$upiId&pn=$name&am=$amount&cu=INR&tn=$note"
    }

    private fun generateQRCode(text: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 400, 400)
            qrCodeImage.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show()
        }
    }

    data class Booking(
        val uid: String,
        val location: String,
        val price: Double,
        val arrivalDate: String,
        val arrivalTime: String,
        val transactionId: String
    )
}
