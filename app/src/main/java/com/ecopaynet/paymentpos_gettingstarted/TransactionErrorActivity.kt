package com.ecopaynet.paymentpos_gettingstarted

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ecopaynet.module.paymentpos.Error
import kotlinx.serialization.json.Json

class TransactionErrorActivity : AppCompatActivity() {
    private lateinit var transactionErrorTextView: TextView
    private lateinit var transactionError: Error

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_error)

        transactionError = Json.decodeFromString(
            Error.serializer(),
            intent.getStringExtra("TRANSACTION_ERROR")!!
        )

        transactionErrorTextView = findViewById<View>(R.id.transactionResultTextView) as TextView

        val closeButton = findViewById<View>(R.id.closeButton) as Button
        closeButton.setOnClickListener { finish() }

        fillTransactionResult()
    }

    private fun fillTransactionResult() {
        var message = ""
        message += "${transactionError.code}\r\n"
        message += "${transactionError.message}\r\n"
        transactionErrorTextView.text = message
    }
}