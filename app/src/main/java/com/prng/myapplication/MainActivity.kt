package com.prng.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.prng.imagefilepicker.ConstantsCustomGallery
import com.prng.imagefilepicker.Image
import com.prng.imagefilepicker.ImageListActivity
import com.prng.imagefilepicker.filepicker.FilePickerActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    internal val PERMISSION_REQUEST_CODE = 111

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imagePickerBTN.setOnClickListener({
            if (Build.VERSION.SDK_INT >= 23) {
                // Marshmallow+
                if (!checkAccessFineLocationPermission() || !checkWriteExternalStoragePermission()) {
                    requestPermission()
                } else {
                    getSinglePicture()
                }
            } else {
                getSinglePicture()
            }
        })

        imageMultiplePickerBTN.setOnClickListener({
            if (Build.VERSION.SDK_INT >= 23) {
                // Marshmallow+
                if (!checkAccessFineLocationPermission() || !checkWriteExternalStoragePermission()) {
                    requestPermission()
                } else {
                    getMultiplePicture()
                }
            } else {
                getMultiplePicture()
            }
        })

        filePickerBTN.setOnClickListener({
            if (Build.VERSION.SDK_INT >= 23) {
                // Marshmallow+
                if (!checkAccessFineLocationPermission() || !checkWriteExternalStoragePermission()) {
                    requestPermission()
                } else {
                    getSingleFile()
                }
            } else {
                getSingleFile()
            }
        })

        filesMultiplePickerBTN.setOnClickListener({
            if (Build.VERSION.SDK_INT >= 23) {
                // Marshmallow+
                if (!checkAccessFineLocationPermission() || !checkWriteExternalStoragePermission()) {
                    requestPermission()
                } else {
                    getMultipleFile()
                }
            } else {
                getMultipleFile()
            }
        })
    }

    private fun getSinglePicture() {
        startActivityForResult(
            Intent(applicationContext, ImageListActivity::class.java)
                .putExtra(ConstantsCustomGallery.INTENT_EXTRA_LIMIT, 10)
                .putExtra(
                    ConstantsCustomGallery.INTENT_EXTRA_COUNTABLE,
                    ConstantsCustomGallery.INTENT_EXTRA_SINGLE
                ), ConstantsCustomGallery.IMAGE_PICKER_REQUEST_CODE
        )
    }

    private fun getMultiplePicture() {
        startActivityForResult(
            Intent(applicationContext, ImageListActivity::class.java)
                .putExtra(ConstantsCustomGallery.INTENT_EXTRA_LIMIT, 10)
                .putExtra(
                    ConstantsCustomGallery.INTENT_EXTRA_COUNTABLE,
                    ConstantsCustomGallery.INTENT_EXTRA_MULTIPLE
                ), ConstantsCustomGallery.IMAGE_PICKER_REQUEST_CODE
        )
    }

    private fun getSingleFile() {
        startActivityForResult(
            Intent(applicationContext, FilePickerActivity::class.java)
                .putExtra(
                    ConstantsCustomGallery.INTENT_EXTRA_SUFFIX,
                    arrayOf("xlsx", "xls", "doc", "dOcX", "ppt", ".pptx", "pdf")
                )

                .putExtra(ConstantsCustomGallery.INTENT_EXTRA_LIMIT, 10)
                .putExtra(
                    ConstantsCustomGallery.INTENT_EXTRA_COUNTABLE,
                    ConstantsCustomGallery.INTENT_EXTRA_SINGLE
                ), ConstantsCustomGallery.IMAGE_PICKER_REQUEST_CODE
        )
    }

    private fun getMultipleFile() {
        startActivityForResult(
            Intent(applicationContext, FilePickerActivity::class.java)
                .putExtra(
                    ConstantsCustomGallery.INTENT_EXTRA_SUFFIX,
                    arrayOf("xlsx", "xls", "doc", "dOcX", "ppt", ".pptx", "pdf")
                )
                .putExtra(ConstantsCustomGallery.INTENT_EXTRA_LIMIT, 10)
                .putExtra(
                    ConstantsCustomGallery.INTENT_EXTRA_COUNTABLE,
                    ConstantsCustomGallery.INTENT_EXTRA_MULTIPLE
                ), ConstantsCustomGallery.IMAGE_PICKER_REQUEST_CODE
        )
    }

    private fun checkAccessFineLocationPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        return if (result == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            false
        }
    }

    private fun checkWriteExternalStoragePermission(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return if (result == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            false
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ConstantsCustomGallery.IMAGE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            //The array list has the image paths of the selected images
//            val images =
//                data.getParcelableArrayListExtra(ConstantsCustomGallery.INTENT_EXTRA_IMAGES)

            var imagesDataArrayList =
                data.getParcelableArrayListExtra<Image>(ConstantsCustomGallery.INTENT_EXTRA_IMAGES)
        }
    }

}
