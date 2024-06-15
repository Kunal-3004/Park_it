package com.example.smart.Splash

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.smart.Authentication.LoginActivity
import com.example.smart.MainActivity
import com.example.smart.SwipeGesture
import com.example.smart.databinding.ActivitySplash4Binding

class Splash4 : AppCompatActivity(), SwipeGesture.SwipeListener {
    private lateinit var binding: ActivitySplash4Binding
    private lateinit var swipeGestureDetector: SwipeGesture.SwipeGestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplash4Binding.inflate(layoutInflater)
        setContentView(binding.root)

        swipeGestureDetector = SwipeGesture.SwipeGestureDetector(this, this)
        swipeGestureDetector.setOnTouchListener(binding.swipe3)

        binding.EnableLocation.setOnClickListener {
            startNextActivity()
        }
        binding.Continue.setOnClickListener {
            startNextActivity()
        }
    }

    override fun onSwipeRight() {
        val intent = Intent(this, Splash3::class.java)
        startActivity(intent)
        finish()
    }
    override fun onSwipeLeft() {
    }
    private fun startNextActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
