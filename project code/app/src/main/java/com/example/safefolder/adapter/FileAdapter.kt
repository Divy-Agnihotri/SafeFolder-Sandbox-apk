package com.example.safefolder.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.safefolder.R
import java.io.File

class FileAdapter(private val files: List<File>) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    private val selectedFiles = mutableSetOf<File>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.fileName.text = file.name
        holder.fileCheckBox.isChecked = selectedFiles.contains(file)

        holder.fileCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedFiles.add(file) else selectedFiles.remove(file)
        }
    }

    override fun getItemCount() = files.size

    fun getSelectedFiles(): List<File> = selectedFiles.toList()

    class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileName: TextView = view.findViewById(R.id.fileName)
        val fileCheckBox: CheckBox = view.findViewById(R.id.fileCheckBox)
    }
}
