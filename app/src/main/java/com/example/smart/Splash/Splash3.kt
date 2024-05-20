package com.example.smart.Splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.smart.R
import com.example.smart.SwipeGesture
import com.example.smart.databinding.ActivitySplash3Binding

class Splash3 : AppCompatActivity(),SwipeGesture.SwipeListener {
    private lateinit var binding: ActivitySplash3Binding
    private lateinit var swipeGestureDetector: SwipeGesture.SwipeGestureDetector
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySplash3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        swipeGestureDetector= SwipeGesture.SwipeGestureDetector(this, this)
        swipeGestureDetector.setOnTouchListener(binding.swipe2)
    }
    override fun onSwipeRight() {
        val intent = Intent(this, Splash2::class.java)
        startActivity(intent)
        finish()
    }

    override fun onSwipeLeft() {
        val intent= Intent(this,Splash4::class.java)
        startActivity(intent)
        finish()
    }
}