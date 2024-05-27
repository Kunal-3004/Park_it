package com.example.smart.Splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.smart.R
import com.example.smart.User1
import com.example.smart.databinding.ActivitySplashScreenBinding
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {
    private lateinit var auth:FirebaseAuth
    private lateinit var binding: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth=FirebaseAuth.getInstance()
        val currentUser=auth.currentUser

        Handler().postDelayed({
            if(currentUser!=null) {
                val intent = Intent(this, User1::class.java)
                startActivity(intent)
                finish()
            }
            else{
                val intent = Intent(this, Splash2::class.java)
                startActivity(intent)
                finish()
            }
        },2000)
    }
}