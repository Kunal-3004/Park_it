package com.example.smart


import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.smart.databinding.ActivityPaymentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar

class Payment : Activity() {

    private lateinit var binding: ActivityPaymentBinding

    private lateinit var locationName: String
    private lateinit var slotNo: String
    private lateinit var locationId: String
    private lateinit var price: String
    private lateinit var uid: String
    private lateinit var arrDate: String
    private lateinit var arrTime: String
    private lateinit var tid: String
    private var randomAttack: Int = 0
    private var mYear: Int = 0
    private var mMonth: Int = 0
    private var mDay: Int = 0

    companion object {
        const val DATE_DIALOG_ID = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_payment)

        val c = Calendar.getInstance()
        mYear = c.get(Calendar.YEAR)
        mMonth = c.get(Calendar.MONTH)
        mDay = c.get(Calendar.DAY_OF_MONTH)


        val intent = intent
        locationName = intent.extras?.getString("locationname") ?: ""
        price = intent.extras?.getString("price") ?: ""
        slotNo = intent.extras?.getString("slotno") ?: ""
        locationId = intent.extras?.getString("locationid") ?: ""
        uid = intent.extras?.getString("uid") ?: ""

        binding.txtpr.text = "Price:$price"
        binding.txt1.text = locationName
        binding.txt3.text = "User ID:$uid"

        for (j in 1..1) {
            randomAttack = (Math.random() * 20).toInt()
        }
        binding.edtid.setText(randomAttack.toString())

        binding.edtarr.setOnClickListener {
            val today = System.currentTimeMillis() - 1000
            val c = Calendar.getInstance()
            val mYear = c.get(Calendar.YEAR)
            val mMonth = c.get(Calendar.MONTH)
            val mDay = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                if (c.timeInMillis < today) {
                    Toast.makeText(this@Payment, "Invalid date, please try again", Toast.LENGTH_LONG).show()
                } else {
                    val s = monthOfYear + 1
                    val a = "$dayOfMonth/$s/$year"
                    binding.edtarr.setText(a)
                }
            }

            val d = DatePickerDialog(this@Payment, dpd, mYear, mMonth, mDay)
            d.show()
        }

        binding.edtarr1.setOnClickListener {
            val mCurrentTime = Calendar.getInstance()
            val hour = mCurrentTime.get(Calendar.HOUR_OF_DAY)
            val minute = mCurrentTime.get(Calendar.MINUTE)
            val mTimePicker = TimePickerDialog(this@Payment, { _, selectedHour, selectedMinute ->
                binding.edtarr1.setText("$selectedHour:$selectedMinute")
            }, hour, minute, true)
            mTimePicker.setTitle("Select Time")
            mTimePicker.show()
        }

        binding.btnpro.setOnClickListener {
            if (binding.edtid.text.isNotEmpty()) {
                CoroutineScope(Dispatchers.Main).launch {
                    pay()
                }
            }
        }

        binding.btncan.setOnClickListener {
            val intent = Intent(this@Payment, viewParking::class.java)
            intent.putExtra("locationname", locationName)
            intent.putExtra("locationid", locationId)
            intent.putExtra("slotno", slotNo)
            intent.putExtra("price", price)
            startActivity(intent)
        }
    }

    private suspend fun pay() {
        withContext(Dispatchers.IO) {
            var out = ""
            arrDate = binding.edtarr.text.toString()
            arrTime = binding.edtarr1.text.toString()
            tid = binding.edtid.text.toString()

            try {
                val url = URL("http://192.168.43.249/pay.php?price=$price&arrdate=$arrDate&arrtime=$arrTime&Tid=$tid&locationid=$locationId&slotno=$slotNo&locationname=$locationName&uid=$uid")
                val connection = url.openConnection() as HttpURLConnection
                val inputStream: InputStream = BufferedInputStream(connection.inputStream)

                var ch: Int
                while (inputStream.read().also { ch = it } != -1) {
                    if (ch == '\n'.toInt()) break
                    out += ch.toChar()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            withContext(Dispatchers.Main) {
                val alertDialog = AlertDialog.Builder(this@Payment).create()
                alertDialog.setTitle("Success")
                alertDialog.setMessage("Your request has been proceeded")
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "DONE") { dialog, _ ->
                    val intent = Intent(this@Payment, MapActivity::class.java)
                    startActivity(intent)
                    dialog.dismiss()
                }
                alertDialog.show()
                Toast.makeText(applicationContext, out, Toast.LENGTH_LONG).show()
            }
        }
    }
}
