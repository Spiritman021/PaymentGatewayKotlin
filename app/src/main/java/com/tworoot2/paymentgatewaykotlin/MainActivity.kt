package com.tworoot2.paymentgatewaykotlin

import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

// here implement the PaymentResultListener interface and Add all their required methods
class MainActivity : AppCompatActivity(), PaymentResultListener {

    lateinit var addMoney: Button
    lateinit var balanceEdit: EditText
    lateinit var walletBalance: TextView
    lateinit var paymentStatus: TextView
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar!!.hide()

        addMoney = findViewById(R.id.addMoney)
        walletBalance = findViewById(R.id.walletBalance)
        balanceEdit = findViewById(R.id.balanceEdit)
        paymentStatus = findViewById(R.id.paymentStatus)

        paymentStatus.visibility = View.GONE

        // this is for the saving the data of Wallet Balance in SharedPreferences
        val prefs = getSharedPreferences("walletBalance", MODE_PRIVATE)
        val name = prefs.getString("finalAmount", "0")

        walletBalance.text = name.toString()

        balanceEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    if (s.toString().toInt() < 5000) {
                        addMoney.visibility = View.VISIBLE
                        addMoney.text = "Proceed to add ₹$s"
                    } else {
                        balanceEdit.error = "Max balance should not exceed to 5000"
                    }
                    if (s.toString().toInt() == 0) {
                        addMoney.visibility = View.GONE
                    }
                } else {
                    addMoney.visibility = View.GONE
                    balanceEdit.hint = "Amount"
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })


        Checkout.preload(this@MainActivity)


        addMoney.setOnClickListener {
            val finalTotalAmount = balanceEdit.text.toString().toInt()
            startPayment(finalTotalAmount)
        }


    }

    // this method will initiate our payment process
    private fun startPayment(Amount: Int) {
        val checkout = Checkout()

        //here replace with your api key which you have copied earlier
        checkout.setKeyID("rzp_test_8wwGf6jYxSCaoH")

        try {
            val jsonObject = JSONObject()
            jsonObject.put("name", "twoRoot2")
            jsonObject.put("description", "twoRoot2")
            jsonObject.put(
                "image",
                "https://play-lh.googleusercontent.com/7897vqzpaq8crWunNxDBSXN03OrpHSusFdx1pZYy2xI-QD541gEzxRqviTALPiPU2ZI=w144-h144-n-rw"
            )
            jsonObject.put("theme.color", "#50B6F4")
            jsonObject.put("currency", "INR")
            jsonObject.put("amount", Amount * 100)
            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            jsonObject.put("retry", retryObj)
            checkout.open(this@MainActivity, jsonObject)
        } catch (e: Exception) {
            Toast.makeText(this@MainActivity, "Something Went Wrong", Toast.LENGTH_SHORT).show()
        }
    }

    // logic for payment success
    override fun onPaymentSuccess(s: String) {
        try {
            paymentStatus.visibility = View.VISIBLE
            Toast.makeText(this@MainActivity, "Payment Successful$s", Toast.LENGTH_LONG).show()
            paymentStatus.text = "Payment status : ₹${balanceEdit.text} Added successfully Payment ID = $s"
            walletBalance.text =
                (Integer.valueOf(walletBalance.text.toString()) + Integer.valueOf(balanceEdit.text.toString())).toString()
            sharedPreferences = getSharedPreferences("walletBalance", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("finalAmount", walletBalance.text.toString())
            editor.apply()
        } catch (e: Exception) {
            Toast.makeText(this@MainActivity, "eRROR : $e", Toast.LENGTH_SHORT).show()
        }
    }

    // logic for payment failure
    override fun onPaymentError(i: Int, s: String) {
        Toast.makeText(this@MainActivity, "Payment Unsuccessful$s", Toast.LENGTH_LONG).show()
        paymentStatus.text = "Payment status : ${balanceEdit.text}Failed to add Payment ID$s"
    }
}