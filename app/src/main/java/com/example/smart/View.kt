package com.example.smart

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smart.databinding.ActivityViewBinding
import com.google.firebase.database.*

class View : AppCompatActivity(), SwipeGesture.SwipeListener {

    private lateinit var binding: ActivityViewBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var bookingAdapter: BookingAdapter
    private lateinit var uid: String
    private val bookings = mutableListOf<Payment.Booking>()
    private var databaseListener: ValueEventListener? = null

    private lateinit var swipeGestureDetector: SwipeGesture.SwipeGestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uid = intent.getStringExtra("uid") ?: ""
        Log.d("ViewActivity", "UID received from Intent: $uid")
        if (uid.isEmpty()) {
            Log.e("ViewActivity", "UID is null or empty")
            Toast.makeText(this, "Error: UID is not provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        recyclerView = binding.recyclerViewBookings
        recyclerView.layoutManager = LinearLayoutManager(this)
        bookingAdapter = BookingAdapter(bookings)
        recyclerView.adapter = bookingAdapter

        swipeGestureDetector = SwipeGesture.SwipeGestureDetector(this, this)
        swipeGestureDetector.setOnTouchListener(binding.viewSwipe)

        database = FirebaseDatabase.getInstance().reference.child("parking_locations")
        loadBookings()
    }

    override fun onSwipeRight() {
        val intent = Intent(this, User1::class.java)
        startActivity(intent)
        finish()
    }

    override fun onSwipeLeft() {
        // No action on swipe left
    }

    private fun loadBookings() {
        databaseListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookings.clear()
                for (parkingSnapshot in snapshot.children) {
                    val bookingsSnapshot = parkingSnapshot.child("bookings")
                    for (bookingSnapshot in bookingsSnapshot.children) {
                        val booking = bookingSnapshot.getValue(Payment.Booking::class.java)
                        if (booking != null && booking.uid == uid) {
                            bookings.add(booking)
                        } else {
                            Log.e("ViewActivity", "Booking is null or UID does not match")
                        }
                    }
                }
                bookingAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@View, "Failed to load bookings: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("ViewActivity", "Database error: ${error.message}")
            }
        }

        database.addValueEventListener(databaseListener as ValueEventListener)
    }

    override fun onPause() {
        super.onPause()
        // Remove Firebase database listener to prevent DeadObjectException
        databaseListener?.let {
            database.removeEventListener(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ensure listener is removed to avoid memory leaks
        databaseListener?.let {
            database.removeEventListener(it)
        }
    }
}
