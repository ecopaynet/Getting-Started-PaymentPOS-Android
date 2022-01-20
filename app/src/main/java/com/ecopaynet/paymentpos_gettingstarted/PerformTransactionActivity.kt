package com.ecopaynet.paymentpos_gettingstarted

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ecopaynet.module.paymentpos.*
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PerformTransactionActivity : AppCompatActivity(), Events.Transaction {
    private lateinit var deviceMessagesTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences

    private val sharedPreferencesKey = "com.ecopaynet.paymentpos_gettingstarted_preferences"
    private var startingOrientation = Configuration.ORIENTATION_UNDEFINED

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_perform_transaction)

            sharedPreferences = getSharedPreferences(sharedPreferencesKey, MODE_PRIVATE)

            deviceMessagesTextView = findViewById<View>(R.id.deviceMessagesTextView) as TextView

            if (savedInstanceState == null) {
                startingOrientation = this.resources.configuration.orientation

                val intent = intent
                when (intent.getStringExtra("TRANSACTION_TYPE")) {
                    "SALE" -> {
                        val amount = intent.getStringExtra("AMOUNT")!!.toLong()
                        if (PaymentPOS.sale(amount, this)) {
                            //ok
                        } else {
                            val alertDialogBuilder = AlertDialog.Builder(this)
                            alertDialogBuilder.setTitle("Unable to perform sale")
                            alertDialogBuilder.setMessage("Incorrect parameters")
                            alertDialogBuilder.setPositiveButton("OK", null)
                            alertDialogBuilder.show()
                            finish()
                        }
                    }
                    "REFUND" -> {
                        val amount = intent.getStringExtra("AMOUNT")!!.toLong()
                        val authorizationCode = intent.getStringExtra("AUTHORIZATION_CODE")
                        val operationNumber = intent.getStringExtra("OPERATION_NUMBER")
                        val saleDate = LocalDate.parse(
                            intent.getStringExtra("SALE_DATE"),
                            DateTimeFormatter.ofPattern("ddMMyyyy")
                        ).atStartOfDay().toKotlinLocalDateTime().date

                        if (PaymentPOS.refund(
                                amount,
                                operationNumber!!,
                                authorizationCode!!,
                                saleDate,
                                this
                            )
                        ) {
                            //ok
                        } else {
                            val alertDialogBuilder = AlertDialog.Builder(this)
                            alertDialogBuilder.setTitle("Unable to perform refund")
                            alertDialogBuilder.setMessage("Incorrect parameters")
                            alertDialogBuilder.setPositiveButton("OK", null)
                            alertDialogBuilder.show()
                            finish()
                        }
                    }
                    else -> {
                        finish()
                    }
                }
            } else {
                startingOrientation = savedInstanceState.getInt(
                    "STARTING_ORIENTATION",
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR
                )
                if (startingOrientation != this.resources.configuration.orientation) {
                    requestedOrientation = when (startingOrientation) {
                        Configuration.ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        Configuration.ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        else -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
                    }
                }
            }
        } catch (ex: Exception) {
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("STARTING_ORIENTATION", startingOrientation)
    }

    override fun onTransactionRequestSignature(transactionRequestSignatureInformation: TransactionRequestSignatureInformation) {
        val intent = Intent(this@PerformTransactionActivity, SignatureActivity::class.java)
        intent.putExtra(
            "TRANSACTION_INFORMATION", Json.encodeToString(
                TransactionRequestSignatureInformation.serializer(),
                transactionRequestSignatureInformation
            )
        )
        startActivityForResult(intent, REQUEST_SIGNATURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SIGNATURE -> {
                if (resultCode == RESULT_OK) {
                    val signatureBitmap = data!!.getByteArrayExtra("SIGNATURE_BITMAP")
                    PaymentPOS.returnTransactionRequestedSignature(signatureBitmap)
                } else {
                    PaymentPOS.returnTransactionRequestedSignature(null)
                }
            }
        }
    }

    override fun onTransactionComplete(result: TransactionResult) {
        val intent = Intent(this, TransactionCompleteActivity::class.java)
        intent.putExtra(
            "TRANSACTION_RESULT",
            Json.encodeToString(TransactionResult.serializer(), result)
        )
        startActivity(intent)
        finish()
    }

    override fun onTransactionError(error: Error) {
        val intent = Intent(this, TransactionErrorActivity::class.java)
        intent.putExtra("TRANSACTION_ERROR", Json.encodeToString(Error.serializer(), error))
        startActivity(intent)
        finish()
    }

    override fun onTransactionDisplayMessage(message: String) {
        runOnUiThread { deviceMessagesTextView.text = message }
    }

    override fun onTransactionDisplayDCCMessage(message: String) {
        runOnUiThread { // setup the alert builder
            val builder = AlertDialog.Builder(this@PerformTransactionActivity)
            builder.setMessage(message)

            // add the buttons
            builder.setPositiveButton("OK", null)

            // create and show the alert dialog
            val dialog = builder.create()
            dialog.show()
        }
    }

    companion object {
        const val REQUEST_SIGNATURE = 1
    }
}