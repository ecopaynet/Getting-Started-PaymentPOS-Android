package com.ecopaynet.paymentpos_gettingstarted

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.ecopaynet.module.paymentpos.*
import java.util.*

class DeviceSelectionActivity : AppCompatActivity(), OnItemClickListener {
    private lateinit var progressDialog: ProgressDialog
    private lateinit var availableDevicesList: MutableList<Device>
    private lateinit var devicesListView: ListView
    private lateinit var listAdapter: ArrayAdapter<String?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_selection)

        devicesListView = findViewById<View>(R.id.availableDevicesListView) as ListView
        devicesListView.onItemClickListener = this

        progressDialog = ProgressDialog.show(this, "Loading Devices", "Please wait...")

        loadDevices()
        showDevices()
    }

    private fun loadDevices() {
        availableDevicesList = ArrayList()

        //Force add tcpip device
        availableDevicesList.add(DeviceTcpip("Local Device", "127.0.0.1", 5556))

        //Force add serial device
        availableDevicesList.add(DeviceSerial())

        //Load bluetooth devices
        availableDevicesList.addAll(PaymentPOS.getBluetoothPairedDevices())
    }

    private fun showDevices() {
        val deviceNamesList = ArrayList<String?>()
        for (index in availableDevicesList.indices) {
            val device = availableDevicesList[index]
            val deviceName = when (device.type) {
                DeviceType.BLUETOOTH -> {
                    "BT: " + device.name
                }
                DeviceType.SERIAL -> {
                    "Serial Port Device"
                }
                DeviceType.TCPIP -> {
                    device.name
                }
            }
            deviceNamesList.add(deviceName)
        }
        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceNamesList)
        devicesListView.adapter = listAdapter
        progressDialog.hide()
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (parent.id == R.id.availableDevicesListView) {
            val selectedDevice = availableDevicesList[id.toInt()]

            val intent = Intent()
            intent.putExtra("DEVICE_TYPE", selectedDevice.type.toString())

            when (selectedDevice.type) {
                DeviceType.BLUETOOTH -> {
                    intent.putExtra("DEVICE_NAME", selectedDevice.name)
                    intent.putExtra("DEVICE_ADDRESS", (selectedDevice as DeviceBluetooth).address)
                }
                DeviceType.SERIAL -> {
                }
                DeviceType.TCPIP -> {
                    intent.putExtra("DEVICE_NAME", selectedDevice.name)
                    intent.putExtra("DEVICE_IP_ADDRESS", (selectedDevice as DeviceTcpip).ipAddress)
                    intent.putExtra("DEVICE_PORT", selectedDevice.port)
                }
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}