package com.example.smart.Authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.smart.User1
import com.example.smart.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : Activity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.showPasswordToggleButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                binding.passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.passwordEditText.text.toString()
            if (checkAllField()) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            val uid = user.uid
                            Toast.makeText(this, "Successfully Logged In", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, User1::class.java)
                            intent.putExtra("uid", uid)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "User is null", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("LoginActivity", "Login failed: ${it.exception}")
                        Toast.makeText(this, "Login failed: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.forgotPass.setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
            finish()
        }

        binding.Register.setOnClickListener {
            val intent = Intent(this, Registration::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun checkAllField(): Boolean {
        val email = binding.editTextEmail.text.toString()
        if (email.isEmpty()) {
            binding.editTextEmail.error = "This is a required field"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.error = "Check email format"
            return false
        }
        val password = binding.passwordEditText.text.toString()
        if (password.isEmpty()) {
            binding.passwordEditText.error = "This is a required field"
            return false
        }
        if (password.length <= 6) {
            binding.passwordEditText.error = "Password should be at least 8 characters long"
            return false
        }
        return true
    }
}
