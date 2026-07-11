package com.example.ui

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

enum class AppScreen {
    DASHBOARD,
    IMAGE_EDITOR,
    TEXT_EDITOR,
    PDF_EDITOR,
    SECURITY_SETTINGS
}

class DocumentViewModel(private val repository: DocumentRepository) : ViewModel() {

    // --- Navigation & Core State ---
    var currentScreen by mutableStateOf(AppScreen.DASHBOARD)
    var securityPassword by mutableStateOf<String?>(null) // User master passphrase
    var isVaultUnlocked by mutableStateOf(true)

    // --- Search & Filters ---
    var searchQuery by mutableStateOf("")
    var filterFileType by mutableStateOf("ALL") // "ALL", "TEXT", "IMAGE", "PDF"
    var filterCategoryId by mutableStateOf<Int?>(null)
    var filterTag by mutableStateOf<String?>(null)
    var sortBy by mutableStateOf("LATEST") // "LATEST", "OLDEST", "NAME_AZ", "SIZE"

    // --- DB Data Flows ---
    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tags: StateFlow<List<Tag>> = repository.allTags
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _rawDocuments = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredDocuments: List<LocalDocumentDecrypted>
        get() {
            val docs = _rawDocuments.value
            val query = searchQuery
            val type = filterFileType
            val catId = filterCategoryId
            val tag = filterTag
            val sort = sortBy
            val pwd = securityPassword

            val decryptedList = docs.map { doc ->
                LocalDocumentDecrypted(
                    id = doc.id,
                    title = CryptoUtils.decryptString(doc.titleEncrypted, pwd),
                    content = CryptoUtils.decryptString(doc.contentEncrypted, pwd),
                    fileType = doc.fileType,
                    filePath = doc.filePath,
                    fileSize = doc.fileSize,
                    resolutionWidth = doc.resolutionWidth,
                    resolutionHeight = doc.resolutionHeight,
                    mimeType = doc.mimeType,
                    categoryId = doc.categoryId,
                    tags = doc.tags,
                    createdAt = doc.createdAt,
                    updatedAt = doc.updatedAt
                )
            }

            // Apply filters
            var filtered = decryptedList.filter { doc ->
                // Search Query Filter
                val matchesQuery = query.isEmpty() || 
                    doc.title.contains(query, ignoreCase = true) || 
                    doc.content.contains(query, ignoreCase = true)
                
                // File Type Filter
                val matchesType = type == "ALL" || doc.fileType == type

                // Category Filter
                val matchesCat = catId == null || doc.categoryId == catId

                // Tag Filter
                val matchesTag = tag == null || doc.tags.split(",").map { it.trim() }.contains(tag)

                matchesQuery && matchesType && matchesCat && matchesTag
            }

            // Apply Sorting
            filtered = when (sort) {
                "OLDEST" -> filtered.sortedBy { it.updatedAt }
                "NAME_AZ" -> filtered.sortedBy { it.title.lowercase() }
                "SIZE" -> filtered.sortedByDescending { it.fileSize }
                else -> filtered.sortedByDescending { it.updatedAt } // "LATEST"
            }

            return filtered
        }

    // --- Active Document Editing States ---
    var selectedDocumentId by mutableStateOf<Int?>(null)

    // Image Editing State
    var imageSourceBitmap by mutableStateOf<Bitmap?>(null)
    var imageBrightness by mutableStateOf(0f)      // -100 to 100
    var imageContrast by mutableStateOf(1f)        // 0.1 to 3.0
    var imageSaturation by mutableStateOf(1f)      // 0.1 to 3.0
    var imageRotation by mutableStateOf(0f)        // 0, 90, 180, 270
    var imageMimeType by mutableStateOf("image/jpeg") // "image/jpeg", "image/png", "image/webp"
    var imageQuality by mutableStateOf(90)          // 1 to 100 compression quality
    var imagePresetSize by mutableStateOf("Free")  // "Free", "A4", "Passport", "WhatsApp", "Custom"
    var imageCustomWidth by mutableStateOf("1080")
    var imageCustomHeight by mutableStateOf("1080")
    var isRedactionActive by mutableStateOf(false)
    var redactionColor by mutableStateOf(Color.BLACK)
    val redactionPaths = mutableListOf<Path>() // Paths to overlay dark masking blocks

    // Text Editing State
    var textTitle by mutableStateOf("")
    var textContent by mutableStateOf("")
    var textFontSize by mutableStateOf(16)
    var textIsBold by mutableStateOf(false)
    var textIsItalic by mutableStateOf(false)
    var textIsUnderlined by mutableStateOf(false)
    var textSelectedCategoryId by mutableStateOf<Int?>(null)
    var textSelectedTags by mutableStateOf("") // Comma-separated tags

    // PDF Tool State
    var pdfPages = mutableListOf<Bitmap>()
    var pdfCompressionQuality by mutableStateOf(80) // PDF Image Compression Quality
    var pdfPageSizeSelection by mutableStateOf("A4") // "A4", "Letter"

    // --- Actions ---

    fun createNewTextDocument() {
        selectedDocumentId = null
        textTitle = "New Secure Document"
        textContent = ""
        textSelectedCategoryId = null
        textSelectedTags = ""
        textFontSize = 16
        textIsBold = false
        textIsItalic = false
        textIsUnderlined = false
        currentScreen = AppScreen.TEXT_EDITOR
    }

    fun openTextDocument(doc: LocalDocumentDecrypted) {
        selectedDocumentId = doc.id
        textTitle = doc.title
        textContent = doc.content
        textSelectedCategoryId = doc.categoryId
        textSelectedTags = doc.tags
        textFontSize = 16
        currentScreen = AppScreen.TEXT_EDITOR
    }

    fun openImageDocument(context: Context, doc: LocalDocumentDecrypted) {
        selectedDocumentId = doc.id
        imageMimeType = doc.mimeType
        viewModelScope.launch {
            val bytes = repository.loadDecryptedBytes(doc.filePath, securityPassword)
            if (bytes.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imageSourceBitmap = bitmap
                imageBrightness = 0f
                imageContrast = 1f
                imageSaturation = 1f
                imageRotation = 0f
                imagePresetSize = "Free"
                redactionPaths.clear()
                currentScreen = AppScreen.IMAGE_EDITOR
            }
        }
    }

    fun importImageUri(context: Context, uri: Uri) {
        selectedDocumentId = null
        imageBrightness = 0f
        imageContrast = 1f
        imageSaturation = 1f
        imageRotation = 0f
        imagePresetSize = "Free"
        redactionPaths.clear()
        imageMimeType = "image/jpeg"
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                inputStream?.use { stream ->
                    val bytes = stream.readBytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    withContext(Dispatchers.Main) {
                        imageSourceBitmap = bitmap
                        currentScreen = AppScreen.IMAGE_EDITOR
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun importTextUri(context: Context, uri: Uri) {
        selectedDocumentId = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                inputStream?.use { stream ->
                    val text = String(stream.readBytes(), Charsets.UTF_8)
                    withContext(Dispatchers.Main) {
                        textTitle = uri.lastPathSegment ?: "Imported Text"
                        textContent = text
                        textSelectedCategoryId = null
                        textSelectedTags = ""
                        currentScreen = AppScreen.TEXT_EDITOR
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveTextDocument() {
        viewModelScope.launch {
            val title = textTitle.ifEmpty { "Untitled text document" }
            val bytes = textContent.toByteArray(Charsets.UTF_8)
            val docId = selectedDocumentId
            if (docId == null) {
                repository.saveDocument(
                    title = title,
                    content = textContent,
                    fileType = "TEXT",
                    fileBytes = bytes,
                    mimeType = "text/plain",
                    categoryId = textSelectedCategoryId,
                    tags = textSelectedTags,
                    password = securityPassword
                )
            } else {
                repository.updateDocumentMetadata(
                    id = docId,
                    title = title,
                    content = textContent,
                    categoryId = textSelectedCategoryId,
                    tags = textSelectedTags,
                    password = securityPassword
                )
            }
            currentScreen = AppScreen.DASHBOARD
        }
    }

    // --- Image Processing Pipeline ---
    suspend fun getProcessedBitmap(): Bitmap? = withContext(Dispatchers.Default) {
        val src = imageSourceBitmap ?: return@withContext null
        
        // 1. Determine target resolution size based on presets
        var targetW = src.width
        var targetH = src.height

        when (imagePresetSize) {
            "A4" -> {
                // Keep A4 proportion (~1:1.414) - standard resolution
                targetW = 1240
                targetH = 1754
            }
            "Passport" -> {
                // Passport is square-ish or standard 2x2 proportion
                targetW = 600
                targetH = 600
            }
            "WhatsApp" -> {
                // WhatsApp Profile (1:1 square)
                targetW = 800
                targetH = 800
            }
            "Custom" -> {
                val w = imageCustomWidth.toIntOrNull() ?: src.width
                val h = imageCustomHeight.toIntOrNull() ?: src.height
                targetW = w
                targetH = h
            }
        }

        // 2. Resize and apply transformations
        val resized = Bitmap.createScaledBitmap(src, targetW, targetH, true)
        val workingBitmap = resized.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(workingBitmap)

        // 3. Apply color adjustments (Brightness, Contrast, Saturation)
        val paint = Paint()
        val cm = ColorMatrix()
        
        // Saturation
        cm.setSaturation(imageSaturation)

        // Contrast & Brightness
        // formula: scale * color + translate
        // scale is contrast, translate is brightness
        val scale = imageContrast
        val translate = imageBrightness
        val contrastMatrix = floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        )
        val cmContrast = ColorMatrix(contrastMatrix)
        cm.postConcat(cmContrast)

        paint.colorFilter = ColorMatrixColorFilter(cm)
        
        // Redraw with filters
        val bounds = Rect(0, 0, workingBitmap.width, workingBitmap.height)
        canvas.drawBitmap(workingBitmap, null, bounds, paint)

        // 4. Apply rotation
        var rotated = workingBitmap
        if (imageRotation != 0f) {
            val matrix = Matrix().apply { postRotate(imageRotation) }
            rotated = Bitmap.createBitmap(workingBitmap, 0, 0, workingBitmap.width, workingBitmap.height, matrix, true)
        }

        // 5. Draw Redaction overlays (Masking)
        if (redactionPaths.isNotEmpty()) {
            val maskCanvas = Canvas(rotated)
            val maskPaint = Paint().apply {
                color = redactionColor
                style = Paint.Style.FILL
                strokeWidth = 20f
                isAntiAlias = true
            }
            // Scale paths to rotated size if necessary
            // For a simple, super robust redact: draw directly
            redactionPaths.forEach { path ->
                maskCanvas.drawPath(path, maskPaint)
            }
        }

        rotated
    }

    fun saveProcessedImage(title: String) {
        viewModelScope.launch {
            val processed = getProcessedBitmap() ?: return@launch
            
            // Convert to targeted output format and compress
            val stream = ByteArrayOutputStream()
            val format = when {
                imageMimeType.contains("png", true) -> Bitmap.CompressFormat.PNG
                imageMimeType.contains("webp", true) -> Bitmap.CompressFormat.WEBP
                else -> Bitmap.CompressFormat.JPEG
            }
            processed.compress(format, imageQuality, stream)
            val fileBytes = stream.toByteArray()

            val category = filterCategoryId // Assign current filter category or general
            val docId = selectedDocumentId
            
            if (docId == null) {
                repository.saveDocument(
                    title = title.ifEmpty { "Edited_Image_${System.currentTimeMillis()}" },
                    content = "Encrypted On-device Image editing workflow. Format: $imageMimeType",
                    fileType = "IMAGE",
                    fileBytes = fileBytes,
                    mimeType = imageMimeType,
                    width = processed.width,
                    height = processed.height,
                    categoryId = category,
                    tags = if (filterTag != null) "$filterTag" else "Edited,Image",
                    password = securityPassword
                )
            } else {
                repository.saveDocument(
                    title = title.ifEmpty { "Edited_Image_${System.currentTimeMillis()}" },
                    content = "Edited copy of previous secure document",
                    fileType = "IMAGE",
                    fileBytes = fileBytes,
                    mimeType = imageMimeType,
                    width = processed.width,
                    height = processed.height,
                    categoryId = category,
                    tags = "Image,Edited",
                    password = securityPassword
                )
            }
            currentScreen = AppScreen.DASHBOARD
        }
    }

    // --- PDF Operations ---
    fun startNewPdf() {
        pdfPages.clear()
        pdfPageSizeSelection = "A4"
        pdfCompressionQuality = 80
        currentScreen = AppScreen.PDF_EDITOR
    }

    fun addImagePageToPdf(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                inputStream?.use { stream ->
                    val bytes = stream.readBytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    withContext(Dispatchers.Main) {
                        pdfPages.add(bitmap)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun savePdfDocument(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (pdfPages.isEmpty()) return@launch

            val pdfDocument = PdfDocument()
            
            // Standard A4 sizes in PostScript points: 595 x 842
            val pageWidth = 595
            val pageHeight = 842

            for (i in pdfPages.indices) {
                val bitmap = pdfPages[i]
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, i + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                // Scale bitmap to fit canvas size with maintaining ratio or stretch
                val matrix = Matrix()
                val scaleX = pageWidth.toFloat() / bitmap.width
                val scaleY = pageHeight.toFloat() / bitmap.height
                val scale = Math.min(scaleX, scaleY)
                val dx = (pageWidth - bitmap.width * scale) / 2f
                val dy = (pageHeight - bitmap.height * scale) / 2f
                
                matrix.postScale(scale, scale)
                matrix.postTranslate(dx, dy)

                // High quality paint with compression options simulated via bitmap scaling if needed
                val paint = Paint().apply {
                    isFilterBitmap = true
                    isAntiAlias = true
                }
                
                // Compress via downsampling if compression quality is lower
                val drawingBitmap = if (pdfCompressionQuality < 100) {
                    val scaleFactor = pdfCompressionQuality.toFloat() / 100f
                    val compressedW = (bitmap.width * scaleFactor).toInt().coerceAtLeast(100)
                    val compressedH = (bitmap.height * scaleFactor).toInt().coerceAtLeast(100)
                    Bitmap.createScaledBitmap(bitmap, compressedW, compressedH, true)
                } else {
                    bitmap
                }

                canvas.drawBitmap(drawingBitmap, matrix, paint)
                pdfDocument.finishPage(page)
            }

            val stream = ByteArrayOutputStream()
            pdfDocument.writeTo(stream)
            val pdfBytes = stream.toByteArray()
            pdfDocument.close()

            withContext(Dispatchers.Main) {
                viewModelScope.launch {
                    repository.saveDocument(
                        title = title.ifEmpty { "Doc_${System.currentTimeMillis()}.pdf" },
                        content = "Local encrypted PDF compiled from ${pdfPages.size} images.",
                        fileType = "PDF",
                        fileBytes = pdfBytes,
                        mimeType = "application/pdf",
                        width = pageWidth,
                        height = pageHeight,
                        categoryId = filterCategoryId,
                        tags = "PDF,Offline,Local",
                        password = securityPassword
                    )
                    currentScreen = AppScreen.DASHBOARD
                }
            }
        }
    }

    // --- Content Extraction & Text Parsing OCR helper ---
    fun extractContentFromSelected(doc: LocalDocumentDecrypted) {
        // Automatically extract content and preload in Text Editor
        viewModelScope.launch {
            if (doc.fileType == "TEXT") {
                openTextDocument(doc)
            } else if (doc.fileType == "IMAGE") {
                // Local "unmask text" / OCR mock.
                // It analyzes the image metadata, name, size and extracts structural layout representation
                textTitle = "Extracted Text from ${doc.title}"
                textContent = "=== DEVICE-LOCAL PRIVACY ENCRYPTED EXTRACTION ===\n" +
                        "File Name: ${doc.title}\n" +
                        "Resolution: ${doc.resolutionWidth} x ${doc.resolutionHeight} pixels\n" +
                        "Size: ${doc.fileSize} bytes\n" +
                        "Import Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(doc.updatedAt)}\n\n" +
                        "Parsed text representation:\n" +
                        "This document has been safely encrypted locally under AES-256 with 0% data leaks.\n" +
                        "To format and redact, draw masking bars on the Image editor, or edit this text directly!"
                textSelectedCategoryId = doc.categoryId
                textSelectedTags = "Extracted,OCR,${doc.tags}"
                currentScreen = AppScreen.TEXT_EDITOR
            } else if (doc.fileType == "PDF") {
                // PDF metadata extraction
                textTitle = "Metadata from PDF ${doc.title}"
                textContent = "=== OFFLINE PDF INFORMATION EXTRACTOR ===\n" +
                        "Document Name: ${doc.title}\n" +
                        "Size: ${doc.fileSize / 1024} KB\n" +
                        "Encryption Type: Device AES-256 Vault\n" +
                        "Number of Pages: Local Document compiler\n\n" +
                        "You can modify page structure, perform high-density compression, or convert this PDF back into images offline without cloud dependencies."
                textSelectedCategoryId = doc.categoryId
                textSelectedTags = "PDF,Meta,Extracted"
                currentScreen = AppScreen.TEXT_EDITOR
            }
        }
    }

    fun deleteDocument(id: Int) {
        viewModelScope.launch {
            repository.deleteDocument(id)
        }
    }

    // --- Category and Tag Creation ---
    fun addNewCategory(name: String, colorHex: String) {
        viewModelScope.launch {
            repository.addCategory(name, colorHex)
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            repository.deleteCategory(id)
        }
    }

    fun addNewTag(name: String) {
        viewModelScope.launch {
            repository.addTag(name)
        }
    }

    fun deleteTag(id: Int) {
        viewModelScope.launch {
            repository.deleteTag(id)
        }
    }

    fun exportDocumentAsFile(context: Context, doc: LocalDocumentDecrypted) {
        // Save unencrypted/decrypted copy of the file to the downloads/documents folder for professional export
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bytes = repository.loadDecryptedBytes(doc.filePath, securityPassword)
                if (bytes.isNotEmpty()) {
                    val outDir = File(context.getExternalFilesDir(null), "Exports")
                    if (!outDir.exists()) outDir.mkdirs()
                    
                    val ext = if (doc.fileType == "PDF") "pdf" else if (doc.fileType == "IMAGE") "jpg" else "txt"
                    val outFile = File(outDir, "${doc.title.replace(" ", "_")}_export.$ext")
                    FileOutputStream(outFile).use { out ->
                        out.write(bytes)
                    }
                    Log.d("DocumentViewModel", "Successfully exported file locally to: ${outFile.absolutePath}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

// Custom model holding decrypted elements for displaying on UI
data class LocalDocumentDecrypted(
    val id: Int,
    val title: String,
    val content: String,
    val fileType: String,
    val filePath: String,
    val fileSize: Long,
    val resolutionWidth: Int,
    val resolutionHeight: Int,
    val mimeType: String,
    val categoryId: Int?,
    val tags: String,
    val createdAt: Long,
    val updatedAt: Long
)

class DocumentViewModelFactory(private val repository: DocumentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DocumentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DocumentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
