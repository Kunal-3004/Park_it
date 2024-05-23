package com.example.smart

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.smart.databinding.ActivityDataBinding


class Data : AppCompatActivity() {

    private lateinit var binding: ActivityDataBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = intent.getStringExtra("email")
        val pwd=intent.getStringExtra("pwd")
        binding.tvEm.text = email
        binding.tvPw.text=pwd

        database = FirebaseDatabase.getInstance().reference.child("users")

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let {
            database.child(it).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        binding.tvNm.text = user.fname
                        binding.tvEm.text = user.email
                        binding.tvCn.text = user.cno
                        binding.tvN.text = user.vno
                        binding.tvPw.text = user.pwd
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
        binding.btnback.setOnClickListener {
            finish()
        }
    }
}
