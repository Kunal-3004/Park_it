package com.example.smart

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smart.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        uid = intent.getStringExtra("uid") ?: ""
        val parkingId = intent.getStringExtra("parkingId")
        val location = intent.getStringExtra("location")
        val totalSlotsValue = intent.getIntExtra("totalSlots", 0)
        val emptySlotsValue = intent.getIntExtra("emptySlots", 0)
        val priceValue = intent.getDoubleExtra("price", 0.0)

        binding.locationName.text = location
        binding.totalSlots.text = "Total Slots: $totalSlotsValue"
        binding.emptySlots.text = "Empty Slots: $emptySlotsValue"
        binding.price.text = "Price: $priceValue"

        binding.bookSlotButton.setOnClickListener {
            if (emptySlotsValue > 0) {
                val intent = Intent(this, Payment::class.java)
                intent.putExtra("uid", uid)
                intent.putExtra("parkingId", parkingId)
                intent.putExtra("location", location)
                intent.putExtra("price", priceValue)
                intent.putExtra("emptySlots", emptySlotsValue)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No empty slots available", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
