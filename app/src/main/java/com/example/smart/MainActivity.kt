package com.example.smart


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.example.smart.Authentication.LoginActivity
import com.example.smart.Authentication.Registration

class MainActivity : Activity() {

    private lateinit var btnUserSys: Button
    private lateinit var btnSignup: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSignup = findViewById(R.id.btnSign)
        btnUserSys = findViewById(R.id.btnUser)

        btnSignup.setOnClickListener {
            val intent = Intent(this@MainActivity, Registration::class.java)
            startActivity(intent)
        }

        btnUserSys.setOnClickListener {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
