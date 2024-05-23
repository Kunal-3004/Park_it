package com.example.smart

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smart.databinding.ActivityViewParkingBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class viewParking: AppCompatActivity() {
    private lateinit var binding: ActivityViewParkingBinding

    private lateinit var locationname: String
    private lateinit var price: String
    private lateinit var slotno: String
    private lateinit var locationid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityViewParkingBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val intent = intent
        locationname = intent.getStringExtra("locationname") ?: ""
        price = intent.getStringExtra("price") ?: ""
        locationid = intent.getStringExtra("locationid") ?: ""
        slotno = intent.getStringExtra("slotno") ?: ""

        binding.txt1.text = "Location Name: $locationname"
        binding.txt2.text = "Slot No: $slotno"
        binding.txt3.text = "Price: $price"

        binding.btnpay.setOnClickListener {
            val i = Intent(this, Payment::class.java)
            startActivity(i)
        }

        binding.btncan.setOnClickListener {
            resetSlot()
        }
    }

    private fun resetSlot() {
        val database = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference = database.getReference("slots").child(locationid).child(slotno)

        myRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "Slot reset successfully", Toast.LENGTH_LONG).show()
                val intent = Intent(this, User1::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(applicationContext, "Failed to reset slot", Toast.LENGTH_LONG).show()
            }
        }
    }
}
