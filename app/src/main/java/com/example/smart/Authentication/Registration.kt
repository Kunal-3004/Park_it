package com.example.smart.Authentication

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.smart.MainActivity
import com.example.smart.User
import com.example.smart.databinding.ActivityRegestrationBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Registration : AppCompatActivity() {
    private lateinit var binding: ActivityRegestrationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityRegestrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth=FirebaseAuth.getInstance()
        database=FirebaseDatabase.getInstance()

        binding.btnReg.setOnClickListener {
            registerUser()
        }
        binding.btncan.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    private fun registerUser() {
        val fname = binding.edtFname.text.toString()
        val email = binding.edtEmail.text.toString()
        val cno = binding.edtCno.text.toString()
        val pwd = binding.edtPwd.text.toString()
        val vno = binding.edtVeh.text.toString()

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmail.error = "Not a valid Email ID"
            return
        }

        if (cno.length != 10) {
            binding.edtCno.error = "Wrong Mobile Number"
            return
        }

        if (vno.isEmpty()) {
            binding.edtVeh.error = "Vehicle number is too short"
            return
        }

        if (pwd.length <= 5) {
            binding.edtPwd.error = "Password is too short"
            return
        }

        auth.createUserWithEmailAndPassword(email, pwd)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val user = User(fname, email, cno, vno,pwd)
                    database.reference.child("users").child(userId).setValue(user)
                        .addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                Toast.makeText(this, "Registration successful", Toast.LENGTH_LONG).show()
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, "Database error: ${task2.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}