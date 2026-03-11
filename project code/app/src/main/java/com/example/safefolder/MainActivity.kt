package com.example.safefolder

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.safefolder.adapter.FileAdapter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    // Request codes for file and folder picker results
    private val PICK_FILE_REQUEST_CODE = 1
    private val EXPORT_FOLDER_REQUEST_CODE = 2

    // UI elements
    private lateinit var fileRecyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter
    private lateinit var sharedPreferences: SharedPreferences

    // List of files imported into the app
    private val importedFiles = mutableListOf<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize buttons and RecyclerView
        val importButton: Button = findViewById(R.id.importButton)
        val exportButton: Button = findViewById(R.id.exportButton)
        fileRecyclerView = findViewById(R.id.fileRecyclerView)

        // Initialize shared preferences to store file info persistently
        sharedPreferences = getSharedPreferences("SafeFolderPrefs", MODE_PRIVATE)

        // Set up RecyclerView with adapter
        fileAdapter = FileAdapter(importedFiles)
        fileRecyclerView.layoutManager = LinearLayoutManager(this)
        fileRecyclerView.adapter = fileAdapter

        // Button click listeners
        importButton.setOnClickListener { openFilePicker() }
        exportButton.setOnClickListener { openExportFolderPicker() }

        // Restore saved file list on startup
        loadImportedFiles()
    }

    // Opens file picker for importing files
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*" // allow all file types
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // allow multiple selections
        }
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
    }

    // Opens folder picker for exporting selected files
    private fun openExportFolderPicker() {
        val selectedFiles = fileAdapter.getSelectedFiles()
        if (selectedFiles.isEmpty()) {
            Toast.makeText(this, "No files selected for export", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, EXPORT_FOLDER_REQUEST_CODE)
    }

    // Handles results from file/folder picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                // Import files
                PICK_FILE_REQUEST_CODE -> handleFileImport(data)

                // Export files
                EXPORT_FOLDER_REQUEST_CODE -> {
                    val uri: Uri? = data?.data
                    if (uri != null) {
                        // Persist folder permission
                        contentResolver.takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                        exportFilesToSelectedLocation(uri)
                    }
                }
            }
        }
    }

    // Handles selected files from picker
    private fun handleFileImport(data: Intent?) {
        // Multiple files selected
        data?.clipData?.let { clipData ->
            for (i in 0 until clipData.itemCount) {
                val uri = clipData.getItemAt(i).uri
                importFile(uri)
            }
        }
        // Single file selected
            ?: data?.data?.let { uri ->
                importFile(uri)
            }

        fileAdapter.notifyDataSetChanged()
        saveImportedFiles() // Save imported files to shared preferences
    }

    // Imports a file from external storage into app's internal storage
    private fun importFile(uri: Uri) {
        val fileName = getFileName(uri) ?: return
        val destinationFile = File(filesDir, fileName)
        try {
            // Copy content from selected file into internal storage
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            importedFiles.add(destinationFile)
            fileAdapter.notifyDataSetChanged()
            Toast.makeText(this, "Imported: $fileName", Toast.LENGTH_SHORT).show()
            saveImportedFiles() // Save updated list
        } catch (e: Exception) {
            Log.e("SafeFolder", "Import error", e)
        }
    }

    // Exports selected files to chosen folder and deletes them from internal storage
    private fun exportFilesToSelectedLocation(folderUri: Uri) {
        val selectedFiles = fileAdapter.getSelectedFiles()
        if (selectedFiles.isEmpty()) {
            Toast.makeText(this, "No files selected for export", Toast.LENGTH_SHORT).show()
            return
        }

        // Get folder document URI for export
        val folderDocumentUri = DocumentsContract.buildDocumentUriUsingTree(
            folderUri, DocumentsContract.getTreeDocumentId(folderUri)
        )

        val successfullyExportedFiles = mutableListOf<File>()

        // Copy each selected file to chosen folder
        for (file in selectedFiles) {
            val newFileUri = createFileInFolder(folderDocumentUri, file.name)
            if (newFileUri != null) {
                try {
                    contentResolver.openOutputStream(newFileUri)?.use { outputStream ->
                        FileInputStream(file).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    successfullyExportedFiles.add(file) // Mark for deletion
                } catch (e: Exception) {
                    Log.e("SafeFolder", "Export error", e)
                }
            } else {
                Log.e("SafeFolder", "Failed to create file in folder")
            }
        }

        // Delete exported files from app storage
        for (file in successfullyExportedFiles) {
            if (file.delete()) {
                importedFiles.remove(file)
            } else {
                Log.e("SafeFolder", "Failed to delete ${file.name} after export")
            }
        }

        fileAdapter.notifyDataSetChanged()
        saveImportedFiles() // Update stored list

        Toast.makeText(this, "Files exported and deleted successfully", Toast.LENGTH_SHORT).show()
    }

    // Creates new file in selected export folder
    private fun createFileInFolder(folderUri: Uri, fileName: String): Uri? {
        return try {
            DocumentsContract.createDocument(
                contentResolver,
                folderUri,
                "application/octet-stream", // generic MIME type
                fileName
            )
        } catch (e: Exception) {
            Log.e("SafeFolder", "Error creating file in folder", e)
            null
        }
    }

    // Retrieves display name (file name) from URI
    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName
    }

    // Save imported file names to shared preferences for persistence
    private fun saveImportedFiles() {
        val fileNames = importedFiles.map { it.name }.toSet()
        sharedPreferences.edit().putStringSet("importedFiles", fileNames).apply()
    }

    // Load previously imported files from shared preferences
    private fun loadImportedFiles() {
        val fileNames = sharedPreferences.getStringSet("importedFiles", emptySet()) ?: return
        importedFiles.clear()
        for (fileName in fileNames) {
            val file = File(filesDir, fileName)
            if (file.exists()) {
                importedFiles.add(file)
            }
        }
        fileAdapter.notifyDataSetChanged()
    }
}
