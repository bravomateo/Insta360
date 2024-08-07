package com.example.insta360app.activity


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast

import com.example.insta360app.R
import com.example.insta360app.util.CameraBindNetworkManager
import com.example.insta360app.util.NetworkManager

import com.arashivision.sdkcamera.camera.InstaCameraManager

import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission

class MainActivity : BaseObserveCameraActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle(R.string.main_toolbar_title)


        checkStoragePermission()

        if (InstaCameraManager.getInstance().cameraConnectedType != InstaCameraManager.CONNECT_TYPE_NONE) {
            onCameraStatusChanged(true)
        }

        // Button Connect WIFI
        findViewById<View>(R.id.btn_connect_by_wifi).setOnClickListener {
            CameraBindNetworkManager.getInstance().bindNetwork { _ ->
                InstaCameraManager.getInstance().openCamera(InstaCameraManager.CONNECT_TYPE_WIFI)
            }
        }

        // Button Disconnect
        findViewById<View>(R.id.btn_close_camera).setOnClickListener {
            CameraBindNetworkManager.getInstance().unbindNetwork()
            InstaCameraManager.getInstance().closeCamera()
        }


        // Button Capture
        findViewById<View>(R.id.btn_capture).setOnClickListener { _ ->
            startActivity(Intent(this@MainActivity, CaptureActivity::class.java)
            )
        }

    }


    private fun checkStoragePermission() {
        AndPermission.with(this)
            .runtime()
            .permission(Permission.READ_EXTERNAL_STORAGE, Permission.ACCESS_FINE_LOCATION)
            .onDenied { permissions ->
                if (AndPermission.hasAlwaysDeniedPermission(this, permissions)) {
                    AndPermission.with(this)
                        .runtime()
                        .setting()
                        .start(1000)
                } else {
                    finish()
                }
            }
            .start()
    }

    override fun onCameraStatusChanged(enabled: Boolean) {
        super.onCameraStatusChanged(enabled)
        //findViewById<Button>(R.id.btn_capture).isEnabled = enabled
        if (enabled) {
            Toast.makeText(this, R.string.main_toast_camera_connected, Toast.LENGTH_SHORT).show()
        } else {
            CameraBindNetworkManager.getInstance().unbindNetwork()
            NetworkManager.getInstance().clearBindProcess()
            Toast.makeText(this, R.string.main_toast_camera_disconnected, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCameraConnectError(errorCode: Int) {
        super.onCameraConnectError(errorCode)
        CameraBindNetworkManager.getInstance().unbindNetwork()
        Toast.makeText(
            this,
            resources.getString(R.string.main_toast_camera_connect_error, errorCode),
            Toast.LENGTH_SHORT
        ).show()
    }


    override fun onCameraSDCardStateChanged(enabled: Boolean) {
        super.onCameraSDCardStateChanged(enabled)
        if (enabled) {
            Toast.makeText(this, R.string.main_toast_sd_enabled, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, R.string.main_toast_sd_disabled, Toast.LENGTH_SHORT).show()
        }
    }


}