package com.ecopaynet.paymentpos_gettingstarted

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.ecopaynet.module.paymentpos.PaymentPOS
import com.ecopaynet.module.paymentpos.TransactionResult
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class TransactionCompleteActivity : AppCompatActivity() {
    private lateinit var transactionResultImageView: ImageView
    private lateinit var transactionResult: TransactionResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_result)

        transactionResult = Json.decodeFromString(
            TransactionResult.serializer(),
            intent.getStringExtra("TRANSACTION_RESULT")!!
        )

        transactionResultImageView =
            findViewById<View>(R.id.transactionResultImageView) as ImageView

        val closeButton = findViewById<View>(R.id.closeButton) as Button
        closeButton.setOnClickListener { finish() }

        fillTransactionResult()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            saveTicketsPDF()
        } else {
            saveTicketsImage()
        }
    }

    private fun fillTransactionResult() {
        try {
            val tickets = PaymentPOS.generateTransactionTicketsBMP(
                transactionResult, null
            )
            val commerceTicketWithBorder = addBorder(tickets!![0], Color.BLACK, 2)
            transactionResultImageView.setImageBitmap(commerceTicketWithBorder)
        } catch (ex: Exception) {
            println(ex.message)
        }
    }

    private fun addBorder(bmp: Bitmap, borderColor: Int, borderSize: Int): Bitmap {
        val bmpWithBorder = Bitmap.createBitmap(
            bmp.width + borderSize * 2,
            bmp.height + borderSize * 2,
            bmp.config
        )
        val canvas = Canvas(bmpWithBorder)
        canvas.drawColor(borderColor)
        canvas.drawBitmap(bmp, borderSize.toFloat(), borderSize.toFloat(), null)
        return bmpWithBorder
    }

    @TargetApi(19)
    private fun saveTicketsPDF() {
        try {
            val tickets = PaymentPOS.generateTransactionTicketsPDF(
                transactionResult, null
            )
            val ticketsFolder = File(getExternalFilesDir(null), "/PaymentPOS/Tickets")
            if (!ticketsFolder.exists()) ticketsFolder.mkdirs()
            val fileDateTime = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
            for (i in tickets!!.indices) {
                val fileName =
                    getExternalFilesDir(null).toString() + "/PaymentPOS/Tickets/" + fileDateTime + (if (i == 0) "" else "_CC") + ".pdf"
                tickets[i].writeTo(FileOutputStream(fileName, false))
            }
        } catch (ex: Exception) {
            println(ex.message)
        }
    }

    private fun saveTicketsImage() {
        try {
            val tickets = PaymentPOS.generateTransactionTicketsBMP(
                transactionResult, null
            )
            val ticketsFolder = File(getExternalFilesDir(null), "/PaymentPOS/Tickets")
            if (!ticketsFolder.exists()) ticketsFolder.mkdirs()
            val fileDateTime = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
            for (i in tickets!!.indices) {
                val fileName = getExternalFilesDir(null)
                    .toString() + "/PaymentPOS/Tickets/" + fileDateTime + (if (i == 0) "" else "_CC") + ".png"
                val out: OutputStream = FileOutputStream(fileName, false)
                tickets[i].compress(Bitmap.CompressFormat.PNG, 100, out)
                out.close()
            }
        } catch (ex: Exception) {
        }
    }
}