package com.ecopaynet.paymentpos_gettingstarted

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.ecopaynet.module.paymentpos.*

class MainActivity : AppCompatActivity(), Events.Initialization, Events.Log {
    private lateinit var selectDeviceButton: Button
    private lateinit var selectedDeviceInfoTextView: TextView
    private lateinit var initializeButton: Button
    private lateinit var saleButton: Button
    private lateinit var refundButton: Button
    private lateinit var terminateButton: Button
    private lateinit var deviceInformationTextView: TextView

    private lateinit var selectedDevice: Device

    private lateinit var progressDialog: ProgressDialog

    private lateinit var sharedPreferences: SharedPreferences
    private val sharedPreferencesKey = "com.ecopaynet.paymentpos_gettingstarted_preferences"

    companion object {
        const val DEVICE_SELECTION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Check file write permissions
        checkStoragePermissionGranted()

        sharedPreferences = getSharedPreferences(sharedPreferencesKey, MODE_PRIVATE)

        if (savedInstanceState == null) {
            PaymentPOS.addLogEventHandler(this)
        }

        val libraryVersionTextView = findViewById<View>(R.id.libraryVersionTextView) as TextView
        libraryVersionTextView.text = "PaymentPOS Library v" + PaymentPOS.getLibraryVersion()

        selectedDeviceInfoTextView = findViewById<View>(R.id.selectedDeviceInfoTextView) as TextView

        deviceInformationTextView = findViewById<View>(R.id.deviceInformationTextView) as TextView

        selectDeviceButton = findViewById<View>(R.id.selectDeviceButton) as Button
        selectDeviceButton.setOnClickListener {
            val intent = Intent(this@MainActivity, DeviceSelectionActivity::class.java)
            startActivityForResult(intent, DEVICE_SELECTION_REQUEST_CODE)
        }

        initializeButton = findViewById<View>(R.id.initializeButton) as Button
        initializeButton.setOnClickListener {
            progressDialog =
                ProgressDialog.show(this@MainActivity, "Initializing Device", "Please wait...")
            PaymentPOS.setEnvironment(Environment.TEST)
            PaymentPOS.initialize(selectedDevice, this@MainActivity)
        }

        saleButton = findViewById<View>(R.id.saleButton) as Button
        saleButton.setOnClickListener {
            val intent = Intent(this@MainActivity, SaleActivity::class.java)
            startActivity(intent)
        }

        refundButton = findViewById<View>(R.id.refundButton) as Button
        refundButton.setOnClickListener {
            val intent = Intent(this@MainActivity, RefundActivity::class.java)
            startActivity(intent)
        }

        terminateButton = findViewById<View>(R.id.terminateButton) as Button
        terminateButton.setOnClickListener {
            PaymentPOS.terminate()
            setPhase2Buttons()
            deviceInformationTextView.text = ""
        }

        setPhase1Buttons()
        loadSavedDevice()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (PaymentPOS.getLibraryStatus() == LibraryStatus.READY) {
            setPhase3Buttons()
            setDeviceInformation()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            DEVICE_SELECTION_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    saveSelectedDevice(data)
                    loadSavedDevice()
                }
            }
        }
    }

    private fun saveSelectedDevice(intentData: Intent?) {
        val editor = sharedPreferences.edit()

        val deviceType = intentData!!.getStringExtra("DEVICE_TYPE")
        editor.putString("DEVICE_TYPE", deviceType)

        if (deviceType == "BLUETOOTH") {
            editor.putString("DEVICE_NAME", intentData.getStringExtra("DEVICE_NAME"))
            editor.putString("DEVICE_ADDRESS", intentData.getStringExtra("DEVICE_ADDRESS"))
        } else if (deviceType == "TCPIP") {
            editor.putString("DEVICE_NAME", intentData.getStringExtra("DEVICE_NAME"))
            editor.putString("DEVICE_IP_ADDRESS", intentData.getStringExtra("DEVICE_IP_ADDRESS"))
            editor.putInt("DEVICE_PORT", intentData.getIntExtra("DEVICE_PORT", 0))
        }

        editor.apply()
    }

    private fun loadSavedDevice() {
        val deviceType = sharedPreferences.getString("DEVICE_TYPE", "BLUETOOTH")
        when (deviceType) {
            "BLUETOOTH" -> {
                val deviceName = sharedPreferences.getString("DEVICE_NAME", "")
                val deviceAddress = sharedPreferences.getString("DEVICE_ADDRESS", "")
                if (deviceName!!.isNotEmpty() && deviceAddress!!.isNotEmpty()) {
                    selectedDevice = DeviceBluetooth(deviceName, deviceAddress)
                    selectedDeviceInfoTextView.text = "Device: ${selectedDevice.name}"
                    setPhase2Buttons()
                }
            }
            "SERIAL" -> {
                selectedDevice = DeviceSerial()
                selectedDeviceInfoTextView.text = "Device: Serial Port"
                setPhase2Buttons()
            }
            "TCPIP" -> {
                val deviceName = sharedPreferences.getString("DEVICE_NAME", "")
                val deviceIpAddress = sharedPreferences.getString("DEVICE_IP_ADDRESS", "")
                val devicePort = sharedPreferences.getInt("DEVICE_PORT", 0)
                if (deviceName!!.isNotEmpty() && deviceIpAddress!!.isNotEmpty() && devicePort > 0) {
                    selectedDevice = DeviceTcpip(deviceName, deviceIpAddress, devicePort)
                    selectedDeviceInfoTextView.text = "Device: ${selectedDevice.name}"
                    setPhase2Buttons()
                }
            }
        }
    }

    private fun setPhase1Buttons() {
        selectDeviceButton.isEnabled = true
        initializeButton.isEnabled = false
        saleButton.isEnabled = false
        refundButton.isEnabled = false
        terminateButton.isEnabled = false
    }

    private fun setPhase2Buttons() {
        selectDeviceButton.isEnabled = true
        initializeButton.isEnabled = true
        saleButton.isEnabled = false
        refundButton.isEnabled = false
        terminateButton.isEnabled = false
    }

    private fun setPhase3Buttons() {
        selectDeviceButton.isEnabled = false
        initializeButton.isEnabled = false
        saleButton.isEnabled = true
        refundButton.isEnabled = true
        terminateButton.isEnabled = true
    }

    private fun setDeviceInformation() {
        val (_, environment, commerceName, commerceAddress, commerceNumber, commerceCurrency) = PaymentPOS.getInformation()
        var deviceInformation = ""
        deviceInformation += "Environment: $environment"
        deviceInformation += "Commerce name: $commerceName"
        deviceInformation += "Commerce address: $commerceAddress"
        deviceInformation += "Commerce number: $commerceNumber"
        deviceInformation += "Currency: ${commerceCurrency!!.alpha}"
        deviceInformationTextView.text = deviceInformation
    }

    override fun onInitializationComplete() {
        runOnUiThread {
            progressDialog.dismiss()
            setPhase3Buttons()
            setDeviceInformation()
        }
    }

    override fun onInitializationError(error: Error) {
        runOnUiThread {
            progressDialog.dismiss()
            val alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
            alertDialogBuilder.setTitle("Initialization error:")
            alertDialogBuilder.setMessage(error.code + " - " + error.message)
            alertDialogBuilder.setPositiveButton("OK", null)
            alertDialogBuilder.show()
            deviceInformationTextView.text = ""
        }
    }

    override fun onNewMessageLogged(level: LogLevel, message: String) {
        Log.println(Log.DEBUG, "GettingStarted", message)
    }

    //permission is automatically granted on sdk<23 upon installation
    private fun checkStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                //Permission granted
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
            }
        } else {
            //permission is automatically granted on sdk<23 upon installation
        }
    }
}