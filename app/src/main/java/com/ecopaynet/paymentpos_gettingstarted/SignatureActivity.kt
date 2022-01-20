package com.ecopaynet.paymentpos_gettingstarted

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ecopaynet.module.paymentpos.SignatureView
import com.ecopaynet.module.paymentpos.TransactionRequestSignatureInformation
import kotlinx.serialization.json.Json

class SignatureActivity : AppCompatActivity() {
    private lateinit var signatureView: SignatureView
    private lateinit var transactionRequestSignatureInformation: TransactionRequestSignatureInformation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signature)

        signatureView = findViewById<View>(R.id.signatureView) as SignatureView

        transactionRequestSignatureInformation = Json.decodeFromString(
            TransactionRequestSignatureInformation.serializer(),
            intent.getStringExtra("TRANSACTION_INFORMATION")!!
        )

        val signatureContinueButton = findViewById<View>(R.id.signatureContinueButton) as Button
        signatureContinueButton.setOnClickListener {
            val intent = Intent()
            intent.putExtra("SIGNATURE_BITMAP", signatureView.getSignatureBitmap())
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}