package com.example.smart


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smart.databinding.ActivitySlotsBinding

class Slots : AppCompatActivity() {
    private lateinit var binding: ActivitySlotsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySlotsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val location = intent.getStringExtra("location")
        val slots = intent.getStringExtra("slots")
        val price = intent.getStringExtra("price")


        binding.tvLocation.text = location
        binding.tvSlots.text = "Available Slots: $slots"
        binding.tvPrice.text = "Price: $price"
    }
}
