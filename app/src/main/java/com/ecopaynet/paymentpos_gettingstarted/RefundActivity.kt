package com.ecopaynet.paymentpos_gettingstarted

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import com.ecopaynet.paymentpos_gettingstarted.PerformTransactionActivity
import java.text.SimpleDateFormat
import java.util.*

class RefundActivity : Activity(), View.OnClickListener {
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var dateFormatter: SimpleDateFormat
    private lateinit var saleDateEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refund)

        val amountEditText = findViewById<View>(R.id.amountEditText) as EditText
        amountEditText.setRawInputType(Configuration.KEYBOARD_12KEY)
        amountEditText.addTextChangedListener(CurrencyTextWatcher(amountEditText))
        amountEditText.setSelection(4)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        val performTransactionButton = findViewById<View>(R.id.performTransactionButton) as Button
        performTransactionButton.setOnClickListener(this)

        saleDateEditText = findViewById<View>(R.id.saleDateEditText) as EditText
        saleDateEditText.setOnClickListener { datePickerDialog.show() }

        val newCalendar = Calendar.getInstance()
        dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                val newDate = Calendar.getInstance()
                newDate[year, monthOfYear] = dayOfMonth
                saleDateEditText.setText(dateFormatter.format(newDate.time))
            },
            newCalendar[Calendar.YEAR],
            newCalendar[Calendar.MONTH],
            newCalendar[Calendar.DAY_OF_MONTH]
        )
    }

    override fun onClick(v: View) {
        if (v.id == R.id.performTransactionButton) {
            val intent = Intent(this, PerformTransactionActivity::class.java)
            intent.putExtra("TRANSACTION_TYPE", "REFUND")

            val amountEditView = findViewById<View>(R.id.amountEditText) as EditText
            val amount = amountEditView.text.toString().replace(" EUR", "").replace(",", "")
            if (amount.toInt() > 0) {
                intent.putExtra("AMOUNT", amount)

                val authorizationCodeEditText =findViewById<View>(R.id.authorizationCodeEditText) as EditText
                intent.putExtra("AUTHORIZATION_CODE", authorizationCodeEditText.text.toString())

                val operationNumberEditText = findViewById<View>(R.id.operationNumberEditText) as EditText
                intent.putExtra("OPERATION_NUMBER", operationNumberEditText.text.toString())

                val saleDateEditText = findViewById<View>(R.id.saleDateEditText) as EditText
                intent.putExtra("SALE_DATE", saleDateEditText.text.toString().replace("/", ""))

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