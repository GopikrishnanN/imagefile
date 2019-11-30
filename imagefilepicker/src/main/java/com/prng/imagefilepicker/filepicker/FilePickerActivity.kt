package com.prng.imagefilepicker.filepicker

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.prng.imagefilepicker.*
import com.prng.imagefilepicker.callback.FilterResultCallback
import com.prng.imagefilepicker.entity.Directory
import com.prng.imagefilepicker.entity.NormalFile
import kotlinx.android.synthetic.main.activity_file_picker.*
import kotlinx.android.synthetic.main.header_title_layout.*

class FilePickerActivity : AppCompatActivity() {

    private lateinit var mAdapter: FilePickerAdapter
    private lateinit var mSelectedList: ArrayList<NormalFile>
    private var mSuffix: Array<String>? =
        arrayOf("xlsx", "xls", "doc", "dOcX", "ppt", ".pptx", "pdf")

    companion object {
        var fileSelectCountable: String? = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_picker)
        initGetIntent()
        initialization()
        initOnClick()
    }

    private fun initGetIntent() {
        if (intent.hasExtra(ConstantsCustomGallery.INTENT_EXTRA_SUFFIX))
            mSuffix = intent.getStringArrayExtra(ConstantsCustomGallery.INTENT_EXTRA_SUFFIX)
        if (intent.hasExtra(ConstantsCustomGallery.INTENT_EXTRA_LIMIT))
            ConstantsCustomGallery.limit =
                intent.getIntExtra(ConstantsCustomGallery.INTENT_EXTRA_LIMIT, 10)
        if (intent.hasExtra(ConstantsCustomGallery.INTENT_EXTRA_COUNTABLE))
            fileSelectCountable =
                intent.getStringExtra(ConstantsCustomGallery.INTENT_EXTRA_COUNTABLE)
    }

    private fun initialization() {
        titleTV.setText(getString(R.string.select_the_files_txt))
        doneTV.setText(getString(R.string.file_save_done_txt))
        doneTV.visibility = View.GONE
        mSelectedList = ArrayList<NormalFile>()
        fileListRV.layoutManager =
            GridLayoutManager(applicationContext, 3, GridLayoutManager.VERTICAL, false)
        fileListRV.setHasFixedSize(true)
        loadData()
    }

    private fun initOnClick() {
        backIconIV.setOnClickListener({
            if (doneTV.visibility == View.VISIBLE) {
                deselectAll()
            } else {
                super.onBackPressed()
                finish()
            }
        })

        doneTV.setOnClickListener({
            val intent = Intent()
            intent.putParcelableArrayListExtra(
                ConstantsCustomGallery.INTENT_EXTRA_IMAGES,
                getSelected()
            )
            setResult(RESULT_OK, intent)
            finish()
        })
    }

    private fun getSelected(): ArrayList<NormalFile> {
        val selectedImages = ArrayList<NormalFile>()
        var i = 0
        val l = mSelectedList.size
        while (i < l) {
            if (mSelectedList[i].isSelected) {
                selectedImages.add(mSelectedList[i])
            }
            i++
        }
        return selectedImages
    }

    private fun loadData() {
        FileFilter.getFiles(this, object : FilterResultCallback<NormalFile> {
            override fun onResult(directories: List<Directory<NormalFile>>) {
                // Refresh folder list
//                if (isNeedFolderList) {
//                    val list = ArrayList<Directory>()
//                    val all = Directory()
//                    all.setName(resources.getString(R.string.vw_all))
//                    list.add(all)
//                    list.addAll(directories)
//                    mFolderHelper.fillData(list)
//                }
                refreshData(directories)
            }
        }, mSuffix)
    }

    class FilePickerAdapter(
        val mContext: Context,
        val aData: ArrayList<NormalFile>,
        val aChooseSelectFiles: ChooseSelectFiles
    ) :
        RecyclerView.Adapter<FilePickerAdapter.MyViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.file_list_layout,
                    parent,
                    false
                )
            )
        }

        interface ChooseSelectFiles {
            fun ChoosedFile(countSelected: Int)
            fun SingleSelectFile()
        }

        override fun getItemCount(): Int {
            return aData.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val extension = aData[position].path.substring(aData[position].path.lastIndexOf("."))
            val id = mContext.getResources()
                .getIdentifier(
                    "prng_file_" + extension.replace(".", ""),
                    "drawable",
                    mContext.getPackageName()
                )
            Glide.with(mContext).load(id).placeholder(R.drawable.sample_image)
                .into(holder.fileImageIV)

            if (aData[position].isSelected) {
                if (extension.contains(".doc")
                    || extension.contains(".docx")
                    || extension.contains(".xls")
                    || extension.contains(".xlsx")
                ) {
                    holder.fileImageIV.setColorFilter(
                        Color.parseColor("#43b0f0"),
                        PorterDuff.Mode.SRC_ATOP
                    )
                } else {
                    holder.fileImageIV.setColorFilter(
                        Color.parseColor("#f04343"),
                        PorterDuff.Mode.SRC_ATOP
                    )
                }
            } else {
                holder.fileImageIV.setColorFilter(
                    Color.BLACK,
                    PorterDuff.Mode.SRC_ATOP
                )
            }

            holder.selectFilesRL.setOnClickListener({
                if (!aData[position].isSelected) {
                    aData[position].isSelected = true
                } else {
                    aData[position].isSelected = false
                }

                if (ConstantsCustomGallery.INTENT_EXTRA_SINGLE.equals(fileSelectCountable)) {
                    aChooseSelectFiles.SingleSelectFile()
                } else {
                    toggleSelection(position, aData)
                }
                notifyDataSetChanged()
            })

            holder.documentNameTV.setText(aData[position].name)
        }

        class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var selectFilesRL: RelativeLayout
            var fileImageIV: ImageView
            var documentNameTV: TextView

            init {
                selectFilesRL = view.findViewById(R.id.selectFilesRL)
                fileImageIV = view.findViewById(R.id.fileImageIV)
                documentNameTV = view.findViewById(R.id.documentNameTV)
            }
        }

        fun toggleSelection(position: Int, aData: ArrayList<NormalFile>) {
            if (aData.get(position).isSelected && ImageItemListActivity.countSelected == ConstantsCustomGallery.limit) {
                aData.get(position).isSelected = !aData.get(position).isSelected
                Toast.makeText(
                    mContext,
                    String.format(
                        mContext.getString(R.string.limit_exceeded),
                        ConstantsCustomGallery.limit
                    ),
                    Toast.LENGTH_SHORT
                )
                    .show()
                return
            }

            if (aData.get(position).isSelected) {
                ImageItemListActivity.countSelected++
            } else {
                ImageItemListActivity.countSelected--
            }
            aChooseSelectFiles.ChoosedFile(ImageItemListActivity.countSelected)
            notifyDataSetChanged()
        }
    }

    private fun refreshData(directories: List<Directory<NormalFile>>) {
        val list = ArrayList<NormalFile>()
        for (directory in directories) {
            list.addAll(directory.files)
        }
        for (file in mSelectedList) {
            val index = list.indexOf(file)
            if (index != -1) {
                list[index].isSelected = true
            }
        }
        if (list.size == 0) {
            Toast.makeText(applicationContext, "Documents not available", Toast.LENGTH_SHORT)
                .show();
            finish()
            return
        }

        mSelectedList = list
        mAdapter = FilePickerAdapter(
            applicationContext,
            mSelectedList,
            object : FilePickerAdapter.ChooseSelectFiles {
                override fun ChoosedFile(countSelected: Int) {
                    if (countSelected == 0) {
                        doneTV.visibility = View.GONE
                    } else {
                        doneTV.visibility = View.VISIBLE
                    }
                    if (ConstantsCustomGallery.INTENT_EXTRA_SINGLE.equals(ImageListActivity.imageSelectCountable)) {
                        doneTV.visibility = View.GONE
                    }
                }

                override fun SingleSelectFile() {
                    val intent = Intent()
                    intent.putParcelableArrayListExtra(
                        ConstantsCustomGallery.INTENT_EXTRA_IMAGES,
                        getSelected()
                    )
                    setResult(RESULT_OK, intent)
                    finish()
                }

            })
        fileListRV.adapter = mAdapter
    }

    private fun deselectAll() {
        doneTV.setVisibility(View.GONE)
        var i = 0
        val l = mSelectedList.size
        while (i < l) {
            mSelectedList[i].isSelected = false
            i++
        }
        ImageItemListActivity.countSelected = 0
        mAdapter.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        if (doneTV.visibility == View.VISIBLE) {
            deselectAll()
        } else {
            super.onBackPressed()
            finish()
        }
    }

}
