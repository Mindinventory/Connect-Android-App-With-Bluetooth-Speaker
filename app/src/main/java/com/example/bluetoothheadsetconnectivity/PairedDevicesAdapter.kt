package com.example.bluetoothheadsetconnectivity

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.row_paired_device_item.view.*

class PairedDevicesAdapter(private val connect: (deviceToConnect: BluetoothDevice?) -> Unit) : RecyclerView.Adapter<PairedDevicesAdapter.DeviceViewHolder>() {

    private var devicesList: ArrayList<BluetoothDevice> = ArrayList()

    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_paired_device_item, parent, false)
        return DeviceViewHolder(view)
    }

    override fun getItemCount(): Int {
        return devicesList.size
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devicesList[position]
        holder.itemView.tvDeviceName.text = device.name
        holder.itemView.tvDeviceAddress.text = device.address
        holder.itemView.setOnClickListener {
            connect(device)
        }
    }

    fun addItems(list: MutableSet<BluetoothDevice>) {
        devicesList.clear()
        devicesList.addAll(list)
        notifyDataSetChanged()
    }
}