package com.example.smart

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smart.databinding.ActivityUser1Binding
import com.google.firebase.auth.FirebaseAuth

class User1: AppCompatActivity() {

    private lateinit var binding: ActivityUser1Binding

    private var uid: String? = null
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityUser1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        handleIntent(intent)


        email = intent.getStringExtra("email")
        uid = intent.getStringExtra("uid")

        binding.btnbook.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("uid", uid)
            startActivity(intent)
        }

        binding.btnview.setOnClickListener {
            val intent = Intent(this, View::class.java)
            intent.putExtra("uid", uid)
            startActivity(intent)
        }

        binding.btnlog.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnmy.setOnClickListener {
            val intent = Intent(this, Data::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }

    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    private fun handleIntent(intent: Intent) {
        val data: Uri? = intent.data
        if (data != null) {
            email = data.getQueryParameter("email")
            uid = data.getQueryParameter("uid")
        } else {
            email = intent.getStringExtra("email")
            uid = intent.getStringExtra("uid")
        }
    }

}