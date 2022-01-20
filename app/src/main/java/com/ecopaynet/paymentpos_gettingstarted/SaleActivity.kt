package com.ecopaynet.paymentpos_gettingstarted

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ecopaynet.paymentpos_gettingstarted.R
import android.widget.EditText
import com.ecopaynet.paymentpos_gettingstarted.CurrencyTextWatcher
import android.view.WindowManager
import android.content.Intent
import android.content.res.Configuration
import android.view.View
import android.widget.Button
import com.ecopaynet.paymentpos_gettingstarted.PerformTransactionActivity

class SaleActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sale)

        val amountEditText = findViewById<View>(R.id.amountEditText) as EditText
        amountEditText.setRawInputType(Configuration.KEYBOARD_12KEY)
        amountEditText.addTextChangedListener(CurrencyTextWatcher(amountEditText))
        amountEditText.setSelection(4)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        val performTransactionButton = findViewById<View>(R.id.performTransactionButton) as Button
        performTransactionButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.performTransactionButton) {
            val intent = Intent(this, PerformTransactionActivity::class.java)
            intent.putExtra("TRANSACTION_TYPE", "SALE")

            val amountEditText = findViewById<View>(R.id.amountEditText) as EditText
            val amount = amountEditText.text.toString().replace(" EUR", "").replace(",", "")
            if (amount.toInt() > 0) {
                intent.putExtra("AMOUNT", amount)

                startActivity(intent)
                finish()
            } else {
                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setTitle("Incorrect amount")
                alertDialogBuilder.setMessage("Amount must be great than zero")
                alertDialogBuilder.setPositiveButton("OK", null)
                alertDialogBuilder.show()
            }
        }
    }
}