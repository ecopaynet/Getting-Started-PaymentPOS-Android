package com.ecopaynet.paymentpos_gettingstarted

import android.widget.EditText
import android.text.TextWatcher
import android.text.Editable
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class CurrencyTextWatcher(private val currency: EditText) : TextWatcher {
    private var current = ""
    private var index = 0
    private var deletingDecimalPoint = false
    override fun beforeTextChanged(p_s: CharSequence, p_start: Int, p_count: Int, p_after: Int) {
        index = if (p_after > 0) {
            p_s.length - p_start
        } else {
            p_s.length - p_start - 1
        }
        deletingDecimalPoint = p_count > 0 && p_s[p_start] == ','
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(p_s: Editable) {
        if (p_s.toString() != current) {
            currency.removeTextChangedListener(this)
            if (deletingDecimalPoint) {
                p_s.delete(p_s.length - index - 1, p_s.length - index)
            }
            // Currency char may be retrieved from  NumberFormat.getCurrencyInstance()
            var text = p_s.toString().replace("EUR", "").replace(",", "")
            text = text.replace("\\s".toRegex(), "")
            var value = 0.0
            if (text.isNotEmpty()) {
                value = text.toDouble()
            }
            // Currency instance may be retrieved from a static member.
            val decimalSeparator = DecimalFormatSymbols()
            decimalSeparator.decimalSeparator = ','
            val decimalFormat = DecimalFormat("0.00 EUR", decimalSeparator)
            val formattedValue = decimalFormat.format(value / 100)
            current = formattedValue
            currency.setText(formattedValue)
            if (index > formattedValue.length) {
                currency.setSelection(formattedValue.length)
            } else {
                currency.setSelection(formattedValue.length - index)
            }
            // include here anything you may want to do after the formatting is completed.
            currency.addTextChangedListener(this)
        }
    }
}