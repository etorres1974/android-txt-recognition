package br.com.mlkit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import br.com.mlkit.databinding.ActivityMainBinding
import com.squareup.picasso.Picasso
import pl.aprilapps.easyphotopicker.*
import java.io.File
import java.io.IOException
import java.util.ArrayList


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var easyImage: EasyImage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        getRuntimePermissions()
        setupEasyImage()
        setupUi()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        handleActivityResult(easyImage, requestCode, resultCode, data, ::onPhotosReturned)
    }

    private fun getRuntimePermissions() {
        val allNeededPermissions = ArrayList<String>()
        for (permission in getRequiredPermissions()) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    allNeededPermissions.add(permission)
                }
            }
        }

        if (allNeededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this, allNeededPermissions.toTypedArray(), 1
            )
        }
    }

    private fun getRequiredPermissions(): Array<String?> {
        return try {
            val info = this.packageManager
                .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            if (ps != null && ps.isNotEmpty()) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: Exception) {
            arrayOfNulls(0)
        }
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, permission)
            == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("PERMISSION", "Permission granted: $permission")
            return true
        }
        Log.d("PERMISSION", "Permission NOT granted: $permission")
        return false
    }

    fun setupEasyImage() {
        easyImage = EasyImage.Builder(baseContext)
            .setChooserTitle("Pick Media")
            .setChooserType(ChooserType.CAMERA_AND_GALLERY)
            .setCopyImagesToPublicGalleryFolder(true)
            .setFolderName("Easy Image Sample")
            .build()
    }



    fun setupUi() {
        binding.button.setOnClickListener{ easyImage.openGallery(this) }
    }

    fun onPhotosReturned(imageFiles: Array<MediaFile>) {
        var file = imageFiles.first().file
        Picasso.get().load(file).into(binding.imageView)
    }

    fun handleActivityResult(
        easyImage: EasyImage,
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        successCallback: (Array<MediaFile>) -> Unit
    ) {
        easyImage.handleActivityResult(
            requestCode,
            resultCode,
            data,
            this,
            object : DefaultCallback() {
                override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                    successCallback(imageFiles)
                }

                override fun onImagePickerError(error: Throwable, source: MediaSource) {
                    //Some error handling
                    error.printStackTrace()
                }

                override fun onCanceled(source: MediaSource) {
                    //Not necessary to remove any files manually anymore
                }
            })
    }


}