package com.prng.imagefilepicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_image_list.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.header_title_layout.*


class ImageListActivity : AppCompatActivity() {

    private val projection = arrayOf(
        MediaStore.Images.Media.BUCKET_ID,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Images.Media.DATA
    )

    private var albums: ArrayList<Album>? = null
    private var imageSelectCount = 0

    companion object {
        var imageSelectCountable: String? = ""
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_list)
        initGetIntent()
        initialization()
    }

    private fun initGetIntent() {
        if (intent.hasExtra(ConstantsCustomGallery.INTENT_EXTRA_COUNTABLE))
            imageSelectCountable =
                intent.extras!!.getString(ConstantsCustomGallery.INTENT_EXTRA_COUNTABLE)
        if (intent.hasExtra(ConstantsCustomGallery.INTENT_EXTRA_LIMIT))
            imageSelectCount = intent.extras!!.getInt(ConstantsCustomGallery.INTENT_EXTRA_LIMIT)
    }

    private fun initialization() {
        imagePickerRV.setHasFixedSize(false)
        imagePickerRV.layoutManager =
            GridLayoutManager(applicationContext, 2, GridLayoutManager.VERTICAL, false);
        getFilePaths()
        titleTV.setText(getString(R.string.select_image_album_txt))

        backIconIV.setOnClickListener({
            finish()
        })
    }

    fun getFilePaths() {
        val cursor = applicationContext.contentResolver
            .query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Images.Media.DATE_MODIFIED
            )
        if (cursor == null) {
//            sendMessage(ConstantsCustomGallery.ERROR)
            Toast.makeText(applicationContext, "ERROR", Toast.LENGTH_SHORT).show();
            return
        }

        val temp = ArrayList<Album>(cursor.count)
        val albumSet = HashSet<Long>()
        var file: File
        if (cursor.moveToLast()) {
            do {
                if (Thread.interrupted()) {
                    return
                }

                val albumId = cursor.getLong(cursor.getColumnIndex(projection[0]))
                val album = cursor.getString(cursor.getColumnIndex(projection[1]))
                val image = cursor.getString(cursor.getColumnIndex(projection[2]))

                if (!albumSet.contains(albumId)) {
                    /*
                        It may happen that some image file paths are still present in cache,
                        though image file does not exist. These last as long as media
                        scanner is not run again. To avoid get such image file paths, check
                        if image file exists.
                         */
                    file = File(image)
                    if (file.exists()) {
//                        val myBitmap = BitmapFactory.decodeFile(image)
                        temp.add(Album(album, image))

                        /*if (!album.equals("Hiding particular folder")) {
                                temp.add(new Album(album, image));
                            }*/
                        albumSet.add(albumId)
                    }
                }

            } while (cursor.moveToPrevious())
        }
        cursor.close()

        if (albums == null) {
            albums = ArrayList<Album>()
        }
        albums!!.clear()
        // adding taking photo from camera option!
        /*albums.add(new Album(getString(R.string.capture_photo),
                    "https://image.freepik.com/free-vector/flat-white-camera_23-2147490625.jpg"));*/
        albums!!.addAll(temp)

        for (i in albums!!.indices) {
            Log.e("albums", "albums-- " + albums!!.get(i).cover)
        }
        imagePickerRV.adapter =
            ImagePicker(applicationContext, albums!!, object : ImagePicker.ChooseImageFolder {
                override fun ChoosedImage(imageName: String) {
                    val intent = Intent(applicationContext, ImageItemListActivity::class.java)
                    intent.putExtra("album", imageName)
                    intent.putExtra(ConstantsCustomGallery.INTENT_EXTRA_LIMIT, imageSelectCount)
                    startActivityForResult(intent, ConstantsCustomGallery.IMAGE_PICKER_REQUEST_CODE)
                }
            })
    }

    class ImagePicker(
        val aContext: Context,
        val aData: ArrayList<Album>,
        val aChooseImageFolder: ChooseImageFolder
    ) :
        RecyclerView.Adapter<ImagePicker.MyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.mip_layout_single,
                    parent,
                    false
                )
            )
        }

        interface ChooseImageFolder {
            fun ChoosedImage(imageName: String)
        }

        override fun getItemCount(): Int {
            return aData.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.folderTitleTV.text = aData.get(position).name
            if (aData.get(position).name.equals("Take Photo")) {
                Glide.with(aContext).load(aData.get(position).cover)
                    .override(200, 200)
                    .centerCrop()
                    .into(holder.imagePickerIV)
            } else {
                val uri = Uri.fromFile(File(aData.get(position).cover))
                Glide.with(aContext).load(uri)
                    .placeholder(R.drawable.sample_image)
                    .override(200, 200)
                    .centerCrop()
                    .into(holder.imagePickerIV)
            }

            holder.imagePickerIV.setOnClickListener({
                aChooseImageFolder.ChoosedImage(aData.get(position).name)
            })
        }

        class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imagePickerIV: ImageView
            val folderTitleTV: TextView

            init {
                imagePickerIV = view.findViewById(R.id.imagePickerIV)
                folderTitleTV = view.findViewById(R.id.folderTitleTV)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ConstantsCustomGallery.IMAGE_PICKER_REQUEST_CODE
            && resultCode == RESULT_OK
            && data != null
        ) {
            setResult(RESULT_OK, data)
            finish()
        }
    }
}
