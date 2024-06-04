package com.example.smart.DataClass

data class ParkingLocation(
    val uid: String,
    val location: String = "",
    val total_slots: Int = 0,
    val empty_slots: Int = 0,
    val longitude: Double = 0.0,
    val latitude: Double = 0.0,
    val price: Double = 0.0
)
{
    constructor() : this("","",0,0,0.0,0.0,0.0)
}
