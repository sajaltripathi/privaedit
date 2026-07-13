package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class LocalDocument(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titleEncrypted: String,
    val contentEncrypted: String, // Stored formatted text or extracted text
    val fileType: String,         // "TEXT", "IMAGE", "PDF"
    val filePath: String,         // Path to local AES-encrypted file on disk
    val fileSize: Long,
    val resolutionWidth: Int = 0,
    val resolutionHeight: Int = 0,
    val mimeType: String,
    val categoryId: Int? = null,
    val tags: String = "",        // Comma-separated tag names, e.g. "Invoice,Tax"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val colorHex: String = "#4F46E5" // Modern default Indigo accent
)

@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)
