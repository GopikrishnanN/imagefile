package com.prng.imagefilepicker

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.Process
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_image_item_list.*
import kotlinx.android.synthetic.main.activity_image_item_list.imagePickerRV
import kotlinx.android.synthetic.main.activity_image_list.*
import java.io.File
import java.util.HashSet

class ImageItemListActivity : AppCompatActivity() {

    private var thread: Thread? = null
    private val projection = arrayOf(
        MediaStore.Images.Media.BUCKET_ID,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Images.Media.DATA
    )
    lateinit var albums: ArrayList<Image>

    lateinit var images: ArrayList<Image>
    lateinit var observer: ContentObserver
    private var album: String = ""
    private var countSelected: Int = 0

    lateinit var aAdapter: ImagePickerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_item_list)

        val intent = intent
        if (intent == null) {
            finish()
        }
        album = intent.getStringExtra(ConstantsCustomGallery.INTENT_EXTRA_ALBUM)
        images = ArrayList()
        imagePickerRV.setHasFixedSize(false)
        imagePickerRV.layoutManager =
            GridLayoutManager(applicationContext, 3, GridLayoutManager.VERTICAL, false)
        loadImages()
    }

    private fun loadImages() {

        var file: File?
        val selectedImages = HashSet<Long>()
        if (images != null) {
            var image: Image
            var i = 0
            val l = images.size
            while (i < l) {
                image = images[i]
                file = File(image.path)
                if (file.exists() && image.isSelected) {
                    selectedImages.add(image.id)
                }
                i++
            }
        }

        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " =?",
            arrayOf(album),
            MediaStore.Images.Media.DATE_ADDED
        )
        if (cursor == null) {
//                sendMessage(ConstantsCustomGallery.ERROR)
            return
        }

        /*
        In case this runnable is executed to onChange calling loadImages,
        using countSelected variable can result in a race condition. To avoid that,
        tempCountSelected keeps track of number of selected images. On handling
        FETCH_COMPLETED message, countSelected is assigned value of tempCountSelected.
         */
        var tempCountSelected = 0
        val temp = ArrayList<Image>(cursor.count)
        if (cursor.moveToLast()) {
            do {
                if (Thread.interrupted()) {
                    return
                }

                val id = cursor.getLong(cursor.getColumnIndex(projection[0]))
                val name = cursor.getString(cursor.getColumnIndex(projection[1]))
                val path = cursor.getString(cursor.getColumnIndex(projection[2]))
                val isSelected = selectedImages.contains(id)
                if (isSelected) {
                    tempCountSelected++
                }

                file = null
                try {
                    file = File(path)
                } catch (e: Exception) {
                    Log.d("Exception : ", e.toString())
                }

                if (file!!.exists()) {
                    temp.add(Image(id, name, path, isSelected))
                }

            } while (cursor.moveToPrevious())
        }
        cursor.close()

        if (images == null) {
            images = ArrayList()
        }
        images.clear()
        images.addAll(temp)

        aAdapter = ImagePickerAdapter(
            applicationContext,
            images,
            object : ImagePickerAdapter.ChooseSelectImageFolder {
                override fun ChoosedImage(imageName: String) {

                }

            })
        imagePickerRV.adapter = aAdapter

    }

    private fun startThread(runnable: Runnable) {
        stopThread()
        thread = Thread(runnable)
        thread!!.start()
    }

    private fun stopThread() {
        if (thread == null || !thread!!.isAlive()) {
            return
        }

        thread!!.interrupt()
        try {
            thread!!.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    class ImagePickerAdapter(
        val aContext: Context,
        val aData: ArrayList<Image>,
        val aChooseSelectImageFolder: ChooseSelectImageFolder
    ) :
        RecyclerView.Adapter<ImagePickerAdapter.MyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.mip_get_image_layout,
                    parent,
                    false
                )
            )
        }

        interface ChooseSelectImageFolder {
            fun ChoosedImage(imageName: String)
        }

        override fun getItemCount(): Int {
            return aData.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val uri = Uri.fromFile(File(aData.get(position).path))
            Glide.with(aContext).load(uri)
                .placeholder(R.drawable.sample_image)
                .override(200, 200)
                .centerCrop()
                .into(holder.selectImageView)

            if (aData[position].isSelected) {
                holder.selectImageView.setAlpha(0.5f)
            } else {
                holder.selectImageView.setAlpha(1.0f)
            }

            holder.selectImageView.setOnClickListener({
                if (!aData[position].isSelected) {
                    aData[position].isSelected = true
                } else {
                    aData[position].isSelected = false
                }
                notifyDataSetChanged()
//                aChooseSelectImageFolder.ChoosedImage(aData.get(position).name)
            })
        }

        class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val selectImageView: ImageView
            val selectedTickIV: ImageView

            init {
                selectImageView = view.findViewById(R.id.selectImageView)
                selectedTickIV = view.findViewById(R.id.selectedTickIV)
            }
        }
    }
}
