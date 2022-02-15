package com.ecopaynet.paymentpos_gettingstarted

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
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
            var ticketWithBorder: Bitmap? = null
            val commerceTicket = PaymentPOS.generateCommerceTransactionTicketBMP(
                transactionResult, null
            )
            if(commerceTicket != null) {
                ticketWithBorder = addBorder(commerceTicket, Color.BLACK, 2)
            } else {
                val cardholderTicket = PaymentPOS.generateCardholderTransactionTicketBMP(
                    transactionResult, null
                )
                if(cardholderTicket != null) {
                    ticketWithBorder = addBorder(cardholderTicket, Color.BLACK, 2)
                }
            }
            if(ticketWithBorder != null) {
                transactionResultImageView.setImageBitmap(ticketWithBorder)
            }
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
            val ticketsFolder = File(getExternalFilesDir(null), "/PaymentPOS/Tickets")
            if (!ticketsFolder.exists()) ticketsFolder.mkdirs()
            val fileDateTime = SimpleDateFormat("yyyyMMddHHmmss").format(Date())

            val commerceTicket = PaymentPOS.generateCommerceTransactionTicketPDF(transactionResult, null)
            if(commerceTicket != null) {
                val commerceTicketFileName =
                    getExternalFilesDir(null).toString() + "/PaymentPOS/Tickets/" + fileDateTime + ".pdf"
                commerceTicket.writeTo(FileOutputStream(commerceTicketFileName, false))
            }

            val cardholderTicket = PaymentPOS.generateCardholderTransactionTicketPDF(transactionResult, null)
            if(cardholderTicket != null) {
                val cardholderTicketFileName =
                    getExternalFilesDir(null).toString() + "/PaymentPOS/Tickets/" + fileDateTime + "_CC.pdf"
                cardholderTicket.writeTo(FileOutputStream(cardholderTicketFileName, false))
            }
        } catch (ex: Exception) {
            println(ex.message)
        }
    }

    private fun saveTicketsImage() {
        try {
            val ticketsFolder = File(getExternalFilesDir(null), "/PaymentPOS/Tickets")
            if (!ticketsFolder.exists()) ticketsFolder.mkdirs()
            val fileDateTime = SimpleDateFormat("yyyyMMddHHmmss").format(Date())

            val commerceTicket = PaymentPOS.generateCommerceTransactionTicketBMP(transactionResult, null)
            if(commerceTicket != null) {
                val commerceTicketFileName =
                    getExternalFilesDir(null).toString() + "/PaymentPOS/Tickets/" + fileDateTime + ".png"
                saveTicketImage(commerceTicket, commerceTicketFileName)
            }

            val cardholderTicket = PaymentPOS.generateCommerceTransactionTicketBMP(transactionResult, null)
            if(cardholderTicket != null) {
                val cardholderTicketFileName =
                    getExternalFilesDir(null).toString() + "/PaymentPOS/Tickets/" + fileDateTime + "_CC.png"
                saveTicketImage(cardholderTicket, cardholderTicketFileName)
            }
        } catch (ex: Exception) {
        }
    }

    private fun saveTicketImage(ticketBitmap: Bitmap, filename: String) {
        val out: OutputStream = FileOutputStream(filename, false)
        ticketBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.close()
    }
}