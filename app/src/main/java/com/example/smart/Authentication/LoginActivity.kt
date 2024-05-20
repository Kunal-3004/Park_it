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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : Activity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth= Firebase.auth

        binding.showPasswordToggleButton.setOnCheckedChangeListener{ buttonView,isChecked->
            if(isChecked){
                binding.passwordEditText.inputType= InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }
            else{
                binding.passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

        binding.btnLogin.setOnClickListener {
            val email=binding.editTextEmail.text.toString()
            val password=binding.passwordEditText.text.toString()
            if(checkAllField()){
                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
                    if(it.isSuccessful){
                        Toast.makeText(this,"Successfully Login", Toast.LENGTH_SHORT).show()
                        val intent= Intent(this, User1::class.java)
                        intent.putExtra("email", binding.editTextEmail.text.toString())
                        startActivity(intent)
                        finish()
                    }
                    else{
                        Log.e("error",it.exception.toString())
                    }
                }
            }
        }
        binding.forgotPass.setOnClickListener{
            val intent=Intent(this, ForgotPassword::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun checkAllField(): Boolean{
        val email=binding.editTextEmail.text.toString()
        if(email==""){
            binding.editTextEmail.error="This is a required field"
            return false
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.editTextEmail.error="Check email format"
            return false
        }
        if(binding.passwordEditText.text.toString()==""){
            binding.passwordEditText.error="This is a required field"
            return false
        }
        if(binding.passwordEditText.length() <= 6) {
            binding.passwordEditText.error = "Password should atleast 8 characters long"
            return false
        }
        return true
    }
}
