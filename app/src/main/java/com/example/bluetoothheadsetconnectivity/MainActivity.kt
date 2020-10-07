package com.example.bluetoothheadsetconnectivity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothProfile.ServiceListener
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.lang.reflect.Method

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var isEnabled: Boolean = false
    private var REQUEST_ENABLE_BT = 0
    private var mPlayer: MediaPlayer? = null
    private var devices: MutableSet<BluetoothDevice>? = null
    private var device: BluetoothDevice? = null
    private var b: IBinder? = null
    private lateinit var a2dp: BluetoothA2dp  //class to connect to an A2dp device
    private lateinit var ia2dp: IBluetoothA2dp
    private lateinit var devicesAdapter: PairedDevicesAdapter

    private var mIsA2dpReady = false
    fun setIsA2dpReady(ready: Boolean) {
        mIsA2dpReady = ready
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableBluetooth()
        setOnClickListener()
    }

    private fun setOnClickListener() {
        btnShowPairedDevices.setOnClickListener(this)
        btnPlay.setOnClickListener(this)
        btnDisconnect.setOnClickListener(this)
    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    fun connectUsingBluetoothA2dp(
        deviceToConnect: BluetoothDevice?
    ) {
        try {
            val c2 = Class.forName("android.os.ServiceManager")
            val m2: Method = c2.getDeclaredMethod("getService", String::class.java)
            b = m2.invoke(c2.newInstance(), "bluetooth_a2dp") as IBinder?
            if (b == null) {
                // For Android 4.2 Above Devices
                device = deviceToConnect
                //establish a connection to the profile proxy object associated with the profile
                BluetoothAdapter.getDefaultAdapter().getProfileProxy(
                    this,
                    // listener notifies BluetoothProfile clients when they have been connected to or disconnected from the service
                    object : ServiceListener {
                        override fun onServiceDisconnected(profile: Int) {
                            setIsA2dpReady(false)
                        }

                        override fun onServiceConnected(
                            profile: Int,
                            proxy: BluetoothProfile
                        ) {
                            a2dp = proxy as BluetoothA2dp
                            try {
                                //establishing bluetooth connection with A2DP devices
                                a2dp.javaClass
                                    .getMethod("connect", BluetoothDevice::class.java)
                                    .invoke(a2dp, deviceToConnect)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            setIsA2dpReady(true)
                        }
                    }, BluetoothProfile.A2DP
                )
            } else {
                val c3 =
                    Class.forName("android.bluetooth.IBluetoothA2dp")
                val s2 = c3.declaredClasses
                val c = s2[0]
                val m: Method = c.getDeclaredMethod("asInterface", IBinder::class.java)
                m.isAccessible = true
                ia2dp = m.invoke(null, b) as IBluetoothA2dp
                ia2dp.connect(deviceToConnect)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    fun disConnectUsingBluetoothA2dp(
        deviceToConnect: BluetoothDevice?
    ) {
        try {
            // For Android 4.2 Above Devices
            if (b == null) {
                try {
                    //disconnecting bluetooth device
                    a2dp.javaClass.getMethod(
                        "disconnect",
                        BluetoothDevice::class.java
                    ).invoke(a2dp, deviceToConnect)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                ia2dp.disconnect(deviceToConnect)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        releaseMediaPlayer()
        disConnectUsingBluetoothA2dp(device)
        super.onDestroy()
    }

    override fun onPause() {
        releaseMediaPlayer()
        super.onPause()
    }

    private fun releaseMediaPlayer() {
        if (mPlayer != null) {
            mPlayer!!.release()
            mPlayer = null
        }
    }

    private fun playMusic() {
        //streaming music on the connected A2DP device
        mPlayer = MediaPlayer()
        try {
            mPlayer!!.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
            )
            mPlayer!!.setDataSource(
                this,
                Uri.parse("https://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3")
            )
            mPlayer!!.prepare()
            mPlayer!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnPlay -> {
                playMusic()
            }
            R.id.btnShowPairedDevices -> {
                //getting paired devices
                devices = BluetoothAdapter.getDefaultAdapter().bondedDevices
                setRecyclerview(
                    this.devices!!,
                    ::connectUsingBluetoothA2dp
                )
            }
            R.id.btnDisconnect -> {
                releaseMediaPlayer()
                disConnectUsingBluetoothA2dp(device)
            }
        }
    }

    private fun enableBluetooth() {
        //Checking if bluetooth is on or off
        if (BluetoothAdapter.getDefaultAdapter().isEnabled) {
            tbBluetooth.isChecked = true
            isEnabled = true
        }
        tbBluetooth.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!BluetoothAdapter.getDefaultAdapter().isEnabled) {
                    //turn bluetooth on
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                    isEnabled = true
                }
            } else {
                if (isEnabled) {
                    //turn bluetooth off
                    BluetoothAdapter.getDefaultAdapter().disable()
                    isEnabled = false
                }
            }
        }
    }

    private fun setRecyclerview(
        devices: MutableSet<BluetoothDevice>,
        connect: (deviceToConnect: BluetoothDevice?) -> Unit
    ) {
        rvPairedDevices.layoutManager = LinearLayoutManager(this)
        devicesAdapter = PairedDevicesAdapter(connect)
        devicesAdapter.addItems(devices)
        rvPairedDevices.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        rvPairedDevices.apply {
            adapter = devicesAdapter
        }
    }
}