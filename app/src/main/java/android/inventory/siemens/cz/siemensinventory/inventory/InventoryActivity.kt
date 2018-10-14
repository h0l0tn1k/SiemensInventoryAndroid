package android.inventory.siemens.cz.siemensinventory.inventory

import android.app.Activity
import android.content.Intent
import android.inventory.siemens.cz.siemensinventory.R
import android.inventory.siemens.cz.siemensinventory.activities.ScanActivity
import android.inventory.siemens.cz.siemensinventory.api.InventoryRecordsServiceApi
import android.inventory.siemens.cz.siemensinventory.api.LoginUsersScdApi
import android.inventory.siemens.cz.siemensinventory.api.entity.Device
import android.inventory.siemens.cz.siemensinventory.api.entity.LoginUserScd
import android.inventory.siemens.cz.siemensinventory.device.DeviceActivity
import android.inventory.siemens.cz.siemensinventory.device.DeviceIntent
import android.inventory.siemens.cz.siemensinventory.device.DeviceServiceApi
import android.inventory.siemens.cz.siemensinventory.tools.SnackbarNotifier
import android.os.Bundle
import android.support.v7.app.AlertDialog.*
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.android.synthetic.main.activity_device_create.*
import kotlinx.android.synthetic.main.activity_inventory.*
import kotlinx.android.synthetic.main.dashboard_item.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InventoryActivity : AppCompatActivity(),
        SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private val SCAN_ACTIVITY_REQUEST_CODE = 0
    private val DEVICE_ACTIVITY_REQUEST_CODE = 1
    private val scanParameterName = "device_barcode_id"
    private var deviceApi: DeviceServiceApi? = null
    private var snackbarNotifier: SnackbarNotifier? = null
    private var inventoryApi: InventoryRecordsServiceApi? = null
    private var listAdapter: InventoryListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)

        snackbarNotifier = SnackbarNotifier(inventory_layout, this)
        inventoryApi = InventoryRecordsServiceApi.Factory.create(this)
        deviceApi = DeviceServiceApi.Factory.create(this)

        initListeners()
    }

    private fun initListeners() {
        inventory_search_box.setOnQueryTextListener(this)
        inventory_scanBtn.setOnClickListener { startScanActivity() }

        listAdapter = InventoryListAdapter(this, emptyList())
        inventory_devices_lv.adapter = listAdapter
        inventory_devices_lv.setOnItemClickListener { adapterView, _, position, _ ->
            startDeviceActivity(adapterView.getItemAtPosition(position) as Device)
        }
    }

    override fun onQueryTextSubmit(p0: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(query: String?): Boolean {
        val queryIsEmpty = query?.isEmpty() == true

        if (isSerialNumberValid(query)) {
            val queue = deviceApi?.getDevicesWithSerialNoLike(query.toString().trim())
            showProgressBar()
            queue?.enqueue(object : Callback<List<Device>> {
                override fun onResponse(call: Call<List<Device>>?, response: Response<List<Device>>?) {
                    updateData(response)
                }

                override fun onFailure(call: Call<List<Device>>?, t: Throwable?) {
                    this@InventoryActivity.onFailure()
                    hideProgressBar()
                }
            })
        } else {
            listAdapter?.updateList(emptyList())
        }
        return false
    }

    private fun startScanActivity() {
        val scanIntent = Intent(this, ScanActivity::class.java)
        scanIntent.putExtra("parameterName", scanParameterName)

        startActivityForResult(scanIntent, SCAN_ACTIVITY_REQUEST_CODE)
    }

    private fun startDeviceActivity(device: Device) {
        val deviceActivity = Intent(this, DeviceActivity::class.java)
        deviceActivity.putExtra("device", Gson().toJson(device))
        deviceActivity.putExtra("intent", DeviceIntent.INVENTORY.toString())
        startActivityForResult(deviceActivity, DEVICE_ACTIVITY_REQUEST_CODE)
        hideProgressBar()
    }

    private fun startCreateNewDeviceActivity(barcode: String) {
        val deviceActivity = Intent(this, DeviceActivity::class.java)
        deviceActivity.putExtra("barcode", barcode)
        deviceActivity.putExtra("intent", DeviceIntent.CREATE.toString())
        startActivityForResult(deviceActivity, DEVICE_ACTIVITY_REQUEST_CODE)
        hideProgressBar()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            SCAN_ACTIVITY_REQUEST_CODE -> handleScanActivityResult(resultCode, data)
            DEVICE_ACTIVITY_REQUEST_CODE -> handleDeviceActivityResult(resultCode, data)
        }
    }

    private fun handleDeviceActivityResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val deviceIntent = DeviceIntent.valueOf(data.getStringExtra("intent"))
            val device = Gson().fromJson(data.getStringExtra("result"), Device::class.java)
            if (deviceIntent == DeviceIntent.CREATE) {
                inventory_search_box.setQuery(device.serialNumber, true)
            } else if (deviceIntent == DeviceIntent.INVENTORY) {
                deviceApi?.updateDevice(device.id, device)?.enqueue(object : Callback<Device> {
                    override fun onResponse(call: Call<Device>?, response: Response<Device>?) {
                        if (response?.isSuccessful == true) {
                            loadData()
                        }
                    }
                    override fun onFailure(call: Call<Device>?, t: Throwable?) {
                        this@InventoryActivity.onFailure()
                    }
                })
            }
        }
    }

    private fun handleScanActivityResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            try {
                val barcode = data.getStringExtra("barcode")
                val device = Gson().fromJson(data.getStringExtra(scanParameterName), Device::class.java)
                if (device == null) {
                    showDeviceNotFound(barcode)
                } else {
                    startDeviceActivity(device)
                }
            } catch (ex: JsonSyntaxException) {
                Toast.makeText(this, "Device not found", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadData() {
        showProgressBar()
        deviceApi?.getDevices()?.enqueue(object : Callback<List<Device>> {
            override fun onResponse(call: Call<List<Device>>?, response: Response<List<Device>>?) {
                this@InventoryActivity.updateData(response)
            }

            override fun onFailure(call: Call<List<Device>>?, t: Throwable?) {
                this@InventoryActivity.onFailure()
            }
        })
    }

    private fun updateData(response: Response<List<Device>>?) {
        if (response?.isSuccessful == true) {
            val devices = response.body() as List<Device>
            listAdapter?.updateList(devices)
        }
        hideProgressBar()
    }

    private fun showProgressBar() {
        inventory_progress_bar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        inventory_progress_bar.visibility = View.GONE
    }

    private fun onFailure() {
        snackbarNotifier?.show(getString(R.string.error_cannot_connect_to_service))
        hideProgressBar()
    }

    override fun onClose(): Boolean {
        return false
    }

    private fun showDeviceNotFound(barcodeNum: String) {
        Builder(this)
                .setTitle(getString(R.string.device_doesnt_exist))
                .setMessage(getString(R.string.do_you_want_to_create_new_device, barcodeNum))
                .setIcon(resources.getDrawable(android.R.drawable.ic_dialog_alert, theme))
                .setNegativeButton(getString(R.string.no), null)
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    startCreateNewDeviceActivity(barcodeNum)
                }
                .show()
    }

    private fun isSerialNumberValid(query: String?): Boolean {
        return query?.isNotEmpty() == true
    }
}
