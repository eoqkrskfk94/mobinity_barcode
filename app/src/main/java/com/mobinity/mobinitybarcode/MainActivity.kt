package com.mobinity.mobinitybarcode

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import java.util.*

class MainActivity : AppCompatActivity() {
    private var beepManager: BeepManager? = null
    private var lastText: String? = null
    private lateinit var context: Context
    private var CAMERA_REQUEST_CODE = 2
    private val PERMISSION_MULTI_CODE = 100
    private lateinit var barcodeScanner: DecoratedBarcodeView
    private val DELAY = 2000 // 1 second


    private val callback: BarcodeCallback = object : BarcodeCallback {
        private var lastTimestamp: Long = 0

        override fun barcodeResult(result: BarcodeResult) {
            if (result.text != null) {
                if (System.currentTimeMillis() - lastTimestamp < DELAY) {
                    // Too soon after the last barcode - ignore.
                    return
                }

                Toast.makeText(context, result.text, Toast.LENGTH_SHORT).show()
                lastText = result.text
                lastTimestamp = System.currentTimeMillis()
                beepManager!!.playBeepSoundAndVibrate()
            }
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        context = this

        checkAndRequestPermissions()

        //scanBtn()


        val formats: Collection<BarcodeFormat> =
                listOf(BarcodeFormat.CODABAR, BarcodeFormat.CODE_128)

        barcodeScanner = findViewById<DecoratedBarcodeView>(R.id.barcode_scanner)

        barcodeScanner.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
        barcodeScanner.initializeFromIntent(intent)
        //barcode_scanner.decodeContinuous(callback)
        beepManager = BeepManager(this)
        beepManager!!.isVibrateEnabled = true
        barcodeScanner.decodeContinuous(callback)
    }


    private fun scanBtn(){
//        val button = findViewById<Button>(R.id.btn_scan)
//
//        button.setOnClickListener {
//            barcodeScanner = findViewById<DecoratedBarcodeView>(R.id.barcode_scanner)
//            barcodeScanner!!.decodeSingle(callback)
//        }
    }

    /**
     * 권한을 확인하고 권한이 부여되어 있지 않다면 권한을 요청한다.
     * @return 필요한 권한이 모두 부여되었다면 true, 그렇지 않다면 false
     */
    private fun checkAndRequestPermissions(): Boolean {
        val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CAMERA
        )
        val listPermissionsNeeded: MutableList<String> = ArrayList()


        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    !== PackageManager.PERMISSION_GRANTED
            ) {
                listPermissionsNeeded.add(permission)
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            showPermissionInfoDialog(listPermissionsNeeded)
            return false
        }



        return true
    }

    private fun showPermissionInfoDialog(listPermissionsNeeded: List<String>) {
        val dialog = AlertDialog.Builder(this).create()
        val layoutInflater = LayoutInflater.from(this)
        val promptView = layoutInflater.inflate(R.layout.dialog_permission, null)
        val alertDialogButton: TextView = promptView.findViewById(R.id.alertdialog_button)

        alertDialogButton.setOnClickListener {
            dialog.cancel()

            //앱 위에 그리기 권한 받기 overlay
//        if (!Settings.canDrawOverlays(this)) {              // 체크
//            val intent = Intent(
//                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                Uri.parse("package:$packageName")
//            )
//            startActivityForResult(
//                intent,
//                ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE
//            )
//        }

            ActivityCompat.requestPermissions(
                    this,
                    listPermissionsNeeded.toTypedArray(),
                    PERMISSION_MULTI_CODE
            )
        }
        dialog.setView(promptView)
        dialog.show()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
    }

    override fun onResume() {
        super.onResume()
        barcodeScanner!!.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeScanner!!.pause()
    }

    fun pause(view: View?) {
        barcodeScanner!!.pause()
    }

    fun resume(view: View?) {
        barcodeScanner!!.resume()
    }

    fun triggerScan(view: View?) {
        //barcode_scanner!!.decodeSingle(callback)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return barcodeScanner!!.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}