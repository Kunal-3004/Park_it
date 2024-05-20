package com.example.smart.Splash

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.smart.R
import com.example.smart.SwipeGesture
import com.example.smart.databinding.ActivitySplash2Binding

class Splash2 : AppCompatActivity(),SwipeGesture.SwipeListener {
    private lateinit var binding: ActivitySplash2Binding
    private lateinit var swipeGestureDetector: SwipeGesture.SwipeGestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySplash2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        swipeGestureDetector= SwipeGesture.SwipeGestureDetector(this, this)
        swipeGestureDetector.setOnTouchListener(binding.swipe)
    }
    override fun onSwipeRight() {
        (this as? Activity)?.finishAffinity()
    }

    override fun onSwipeLeft() {
        val intent= Intent(this,Splash3::class.java)
        startActivity(intent)
        finish()
    }
}