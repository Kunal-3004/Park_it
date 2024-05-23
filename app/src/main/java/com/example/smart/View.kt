import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smart.BookingAdapter
import com.example.smart.Payment
import com.example.smart.SwipeGesture
import com.example.smart.User1
import com.example.smart.databinding.ActivityViewBinding
import com.google.firebase.database.*

class View: AppCompatActivity(),SwipeGesture.SwipeListener{
    private lateinit var binding: ActivityViewBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var bookingAdapter: BookingAdapter
    private lateinit var uid: String
    private val bookings = mutableListOf<Payment.Booking>()

    private lateinit var swipeGestureDetector: SwipeGesture.SwipeGestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uid = intent.getStringExtra("uid") ?: ""
        recyclerView = binding.recyclerViewBookings
        recyclerView.layoutManager = LinearLayoutManager(this)
        bookingAdapter = BookingAdapter(bookings)
        recyclerView.adapter = bookingAdapter

        swipeGestureDetector= SwipeGesture.SwipeGestureDetector(this, this)
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
    }

    private fun loadBookings() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookings.clear()
                for (parkingSnapshot in snapshot.children) {
                    val bookingsSnapshot = parkingSnapshot.child("bookings")
                    for (bookingSnapshot in bookingsSnapshot.children) {
                        val booking = bookingSnapshot.getValue(Payment.Booking::class.java)
                        if (booking != null && booking.uid == uid) {
                            bookings.add(booking)
                        }
                    }
                }
                bookingAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@View, "Failed to load bookings", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
