package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.DocumentViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditorScreen(
    viewModel: DocumentViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()
    
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var isContentMasked by remember { mutableStateOf(false) } // Visual masked vs unmasked state

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "WORKSPACE STUDIO",
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.2.sp,
                            color = ComposeColor(0xFF6750A4)
                        )
                        Text(
                            text = "Document Studio",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontWeight = FontWeight.Light,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = AppScreen.DASHBOARD }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = ComposeColor(0xFF6750A4))
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.saveTextDocument() },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ComposeColor(0xFF6750A4),
                            contentColor = ComposeColor.White
                        ),
                        modifier = Modifier.testTag("save_text_button")
                    ) {
                        Icon(Icons.Default.Save, "Save", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ComposeColor(0xFFFEF7FF)
                )
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ComposeColor(0xFFFEF7FF))
        ) {
            // --- Left Panel: Document Text Field & Format Bar ---
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title Field
                OutlinedTextField(
                    value = viewModel.textTitle,
                    onValueChange = { viewModel.textTitle = it },
                    placeholder = { Text("Document Title", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("text_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ComposeColor(0xFFF3EDF7),
                        unfocusedContainerColor = ComposeColor(0xFFF3EDF7),
                        focusedBorderColor = ComposeColor(0xFFCAC4D0),
                        unfocusedBorderColor = ComposeColor(0xFFCAC4D0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Formatting Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ComposeColor(0xFFF3EDF7), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Font Size up
                    IconButton(onClick = { viewModel.textFontSize = (viewModel.textFontSize + 2).coerceAtMost(32) }) {
                        Icon(Icons.Default.TextIncrease, "Increase font size")
                    }
                    IconButton(onClick = { viewModel.textFontSize = (viewModel.textFontSize - 2).coerceAtLeast(10) }) {
                        Icon(Icons.Default.TextDecrease, "Decrease font size")
                    }
                    // Bold Toggle
                    IconToggleButton(
                        checked = viewModel.textIsBold,
                        onCheckedChange = { viewModel.textIsBold = it }
                    ) {
                        Icon(Icons.Default.FormatBold, "Bold", tint = if (viewModel.textIsBold) ComposeColor(0xFF6750A4) else ComposeColor(0xFF49454F))
                    }
                    // Italic Toggle
                    IconToggleButton(
                        checked = viewModel.textIsItalic,
                        onCheckedChange = { viewModel.textIsItalic = it }
                    ) {
                        Icon(Icons.Default.FormatItalic, "Italic", tint = if (viewModel.textIsItalic) ComposeColor(0xFF6750A4) else ComposeColor(0xFF49454F))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Mask/Unmask toggler
                    IconButton(onClick = { isContentMasked = !isContentMasked }) {
                        Icon(
                            imageVector = if (isContentMasked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Mask text",
                            tint = if (isContentMasked) ComposeColor(0xFFEF4444) else ComposeColor(0xFF6750A4)
                        )
                    }
                    Text(
                        text = if (isContentMasked) "MASKED" else "UNMASKED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isContentMasked) ComposeColor(0xFFEF4444) else ComposeColor(0xFF6750A4),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                // Main Text Input
                val displayText = if (isContentMasked) {
                    viewModel.textContent.replace(Regex("[a-zA-Z0-9]"), "*")
                } else {
                    viewModel.textContent
                }

                OutlinedTextField(
                    value = displayText,
                    onValueChange = { if (!isContentMasked) viewModel.textContent = it },
                    placeholder = { Text("Start typing confidential text safely here...", color = ComposeColor(0xFF49454F)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("text_content_input"),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = viewModel.textFontSize.sp,
                        fontWeight = if (viewModel.textIsBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (viewModel.textIsItalic) FontStyle.Italic else FontStyle.Normal
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ComposeColor.White,
                        unfocusedContainerColor = ComposeColor.White,
                        focusedBorderColor = ComposeColor(0xFFCAC4D0),
                        unfocusedBorderColor = ComposeColor(0xFFCAC4D0)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isContentMasked
                )
            }

            // --- Right Panel: Categories, Tags & Export Tools ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Classification & Category Selection
                Text("Document Categorization", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ComposeColor(0xFF1D1B20))
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    val activeCat = categories.find { it.id == viewModel.textSelectedCategoryId }
                    OutlinedButton(
                        onClick = { showCategoryDropdown = true },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth().testTag("select_category_button")
                    ) {
                        Text(activeCat?.name ?: "No Category Assigned", fontSize = 12.sp)
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                viewModel.textSelectedCategoryId = null
                                showCategoryDropdown = false
                            }
                        )
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    viewModel.textSelectedCategoryId = cat.id
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                // 2. Custom Tags Assignment
                OutlinedTextField(
                    value = viewModel.textSelectedTags,
                    onValueChange = { viewModel.textSelectedTags = it },
                    label = { Text("Tags (comma-separated, e.g. Work, Private)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ComposeColor(0xFFF3EDF7),
                        unfocusedContainerColor = ComposeColor(0xFFF3EDF7),
                        focusedBorderColor = ComposeColor(0xFFCAC4D0),
                        unfocusedBorderColor = ComposeColor(0xFFCAC4D0)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("document_tags_input")
                )

                Divider(color = ComposeColor(0xFFCAC4D0).copy(alpha = 0.5f))

                // 3. Export Operations
                Text("Professional Exports (100% Offline)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ComposeColor(0xFF1D1B20))

                // Export as PDF
                Button(
                    onClick = {
                        val pdfDoc = PdfDocument()
                        val pageInfo = PdfDocument.PageInfo.Builder(612, 792, 1).create()
                        val page = pdfDoc.startPage(pageInfo)
                        val canvas = page.canvas
                        
                        val paint = Paint().apply {
                            color = Color.BLACK
                            textSize = viewModel.textFontSize.toFloat()
                            isAntiAlias = true
                        }
                        
                        canvas.drawText("DOCUMENT TITLE: ${viewModel.textTitle}", 50f, 60f, Paint().apply {
                            color = Color.BLACK
                            textSize = 20f
                            isFakeBoldText = true
                        })
                        
                        var currentY = 120f
                        val lines = viewModel.textContent.split("\n")
                        lines.forEach { line ->
                            canvas.drawText(line, 50f, currentY, paint)
                            currentY += viewModel.textFontSize + 8f
                        }
                        
                        pdfDoc.finishPage(page)
                        
                        val outDir = File(context.getExternalFilesDir(null), "Exports")
                        if (!outDir.exists()) outDir.mkdirs()
                        val outFile = File(outDir, "${viewModel.textTitle.replace(" ", "_")}.pdf")
                        FileOutputStream(outFile).use { out ->
                            pdfDoc.writeTo(out)
                        }
                        pdfDoc.close()
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ComposeColor(0xFF6750A4),
                        contentColor = ComposeColor.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("export_text_pdf_button")
                ) {
                    Icon(Icons.Default.PictureAsPdf, "PDF", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export as PDF File", fontSize = 12.sp)
                }

                // Export as TXT
                OutlinedButton(
                    onClick = {
                        val outDir = File(context.getExternalFilesDir(null), "Exports")
                        if (!outDir.exists()) outDir.mkdirs()
                        val outFile = File(outDir, "${viewModel.textTitle.replace(" ", "_")}.txt")
                        FileOutputStream(outFile).use { out ->
                            out.write(viewModel.textContent.toByteArray(Charsets.UTF_8))
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ComposeColor(0xFF6750A4)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ComposeColor(0xFFCAC4D0)),
                    modifier = Modifier.fillMaxWidth().testTag("export_text_txt_button")
                ) {
                    Icon(Icons.Default.TextSnippet, "TXT", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export as Plain TXT", fontSize = 12.sp)
                }

                // Export as Image
                OutlinedButton(
                    onClick = {
                        val bitmap = Bitmap.createBitmap(800, 1200, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(Color.WHITE)
                        
                        val paint = Paint().apply {
                            color = Color.BLACK
                            textSize = viewModel.textFontSize.toFloat()
                            isAntiAlias = true
                        }
                        
                        canvas.drawText(viewModel.textTitle, 60f, 80f, Paint().apply {
                            color = Color.BLACK
                            textSize = 24f
                            isFakeBoldText = true
                        })
                        
                        var currentY = 150f
                        val lines = viewModel.textContent.split("\n")
                        lines.forEach { line ->
                            canvas.drawText(line, 60f, currentY, paint)
                            currentY += viewModel.textFontSize + 8f
                        }

                        val outDir = File(context.getExternalFilesDir(null), "Exports")
                        if (!outDir.exists()) outDir.mkdirs()
                        val outFile = File(outDir, "${viewModel.textTitle.replace(" ", "_")}.png")
                        FileOutputStream(outFile).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ComposeColor(0xFF6750A4)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ComposeColor(0xFFCAC4D0)),
                    modifier = Modifier.fillMaxWidth().testTag("export_text_png_button")
                ) {
                    Icon(Icons.Default.Image, "PNG", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export as PNG Image", fontSize = 12.sp)
                }
            }
        }
    }
}
