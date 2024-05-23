package com.example.smart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookingAdapter(private val bookings: List<Payment.Booking>) :
    RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.booking_layout, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.bind(booking)
    }

    override fun getItemCount(): Int = bookings.size

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtLocation: TextView = itemView.findViewById(R.id.txtLocation)
        private val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
        private val txtArrivalDate: TextView = itemView.findViewById(R.id.txtArrivalDate)
        private val txtArrivalTime: TextView = itemView.findViewById(R.id.txtArrivalTime)
        private val txtTransactionId: TextView = itemView.findViewById(R.id.txtTransactionId)

        fun bind(booking: Payment.Booking) {
            txtLocation.text = "Location: ${booking.location}"
            txtPrice.text = "Price: ${booking.price}"
            txtArrivalDate.text = "Arrival Date: ${booking.arrivalDate}"
            txtArrivalTime.text = "Arrival Time: ${booking.arrivalTime}"
            txtTransactionId.text = "Transaction ID: ${booking.transactionId}"
        }
    }
}
