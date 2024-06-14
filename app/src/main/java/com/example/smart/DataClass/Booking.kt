package com.example.smart.DataClass

data class Booking(
    val uid: String = "",
    val upiId:String="",
    val location: String = "",
    val price: Double = 0.0,
    val arrivalDate: String = "",
    val arrivalTime: String = "",
    val endTime:String="",
    val transactionId: String = ""
)
