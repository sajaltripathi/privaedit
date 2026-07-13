package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class DocumentRepository(
    private val context: Context,
    private val documentDao: DocumentDao
) {
    val allDocuments: Flow<List<LocalDocument>> = documentDao.getAllDocuments()
    val allCategories: Flow<List<Category>> = documentDao.getAllCategories()
    val allTags: Flow<List<Tag>> = documentDao.getAllTags()

    suspend fun getDocument(id: Int): LocalDocument? = withContext(Dispatchers.IO) {
        documentDao.getDocumentById(id)
    }

    suspend fun saveDocument(
        title: String,
        content: String,
        fileType: String,
        fileBytes: ByteArray,
        mimeType: String,
        width: Int = 0,
        height: Int = 0,
        categoryId: Int? = null,
        tags: String = "",
        password: String? = null
    ): Long = withContext(Dispatchers.IO) {
        // 1. Encrypt raw file bytes
        val encryptedBytes = CryptoUtils.encryptBytes(fileBytes, password)

        // 2. Save encrypted bytes to sandbox internal storage
        val fileName = "doc_${System.currentTimeMillis()}.${getFileExtension(mimeType)}"
        val file = File(context.filesDir, fileName)
        FileOutputStream(file).use { out ->
            out.write(encryptedBytes)
        }

        // 3. Encrypt metadata
        val titleEncrypted = CryptoUtils.encryptString(title, password)
        val contentEncrypted = CryptoUtils.encryptString(content, password)

        // 4. Save metadata to Room database
        val doc = LocalDocument(
            titleEncrypted = titleEncrypted,
            contentEncrypted = contentEncrypted,
            fileType = fileType,
            filePath = file.absolutePath,
            fileSize = fileBytes.size.toLong(),
            resolutionWidth = width,
            resolutionHeight = height,
            mimeType = mimeType,
            categoryId = categoryId,
            tags = tags,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        documentDao.insertDocument(doc)
    }

    suspend fun updateDocumentMetadata(
        id: Int,
        title: String,
        content: String,
        categoryId: Int?,
        tags: String,
        password: String? = null
    ) = withContext(Dispatchers.IO) {
        val existing = documentDao.getDocumentById(id) ?: return@withContext
        val titleEnc = CryptoUtils.encryptString(title, password)
        val contentEnc = CryptoUtils.encryptString(content, password)
        
        val updated = existing.copy(
            titleEncrypted = titleEnc,
            contentEncrypted = contentEnc,
            categoryId = categoryId,
            tags = tags,
            updatedAt = System.currentTimeMillis()
        )
        documentDao.insertDocument(updated)
    }

    suspend fun loadDecryptedBytes(filePath: String, password: String? = null): ByteArray = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) return@withContext ByteArray(0)
            val encryptedBytes = file.readBytes()
            CryptoUtils.decryptBytes(encryptedBytes, password)
        } catch (e: Exception) {
            e.printStackTrace()
            ByteArray(0)
        }
    }

    suspend fun deleteDocument(id: Int) = withContext(Dispatchers.IO) {
        val doc = documentDao.getDocumentById(id)
        if (doc != null) {
            try {
                val file = File(doc.filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            documentDao.deleteDocumentById(id)
        }
    }

    // Categories
    suspend fun addCategory(name: String, colorHex: String) = withContext(Dispatchers.IO) {
        documentDao.insertCategory(Category(name = name.trim(), colorHex = colorHex))
    }

    suspend fun deleteCategory(id: Int) = withContext(Dispatchers.IO) {
        documentDao.deleteCategoryById(id)
    }

    // Tags
    suspend fun addTag(name: String) = withContext(Dispatchers.IO) {
        documentDao.insertTag(Tag(name = name.trim()))
    }

    suspend fun deleteTag(id: Int) = withContext(Dispatchers.IO) {
        documentDao.deleteTagById(id)
    }

    private fun getFileExtension(mimeType: String): String {
        return when {
            mimeType.contains("png", true) -> "png"
            mimeType.contains("jpeg", true) || mimeType.contains("jpg", true) -> "jpg"
            mimeType.contains("webp", true) -> "webp"
            mimeType.contains("pdf", true) -> "pdf"
            else -> "txt"
        }
    }
}
