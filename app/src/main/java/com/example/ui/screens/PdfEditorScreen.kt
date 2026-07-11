package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.DocumentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfEditorScreen(
    viewModel: DocumentViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pdfName by remember { mutableStateOf("Compiled_Document") }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.addImagePageToPdf(context, it) }
    }

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
                            color = Color(0xFF6750A4)
                        )
                        Text(
                            text = "Offline PDF Studio",
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
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color(0xFF6750A4))
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.savePdfDocument(pdfName) },
                        enabled = viewModel.pdfPages.isNotEmpty(),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6750A4),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("save_pdf_button")
                    ) {
                        Icon(Icons.Default.Save, "Save", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save PDF", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFEF7FF)
                )
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFEF7FF))
        ) {
            // --- Left Panel: Pages Organizer Grid ---
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PDF Page Builder (${viewModel.pdfPages.size} pages)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1D1B20)
                    )
                    OutlinedButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF6750A4)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCAC4D0)),
                        modifier = Modifier.testTag("add_pdf_page_button")
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, "Add Image Page", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Page", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (viewModel.pdfPages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFF6750A4).copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No pages added yet", color = Color(0xFF1D1B20), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Add images to compile them into an encrypted PDF.",
                                fontSize = 11.sp,
                                color = Color(0xFF49454F),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(viewModel.pdfPages) { index, bitmap ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(0.7f) // A4 ratio
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Page ${index + 1}",
                                    modifier = Modifier.fillMaxSize().padding(6.dp)
                                )
                                // Page Index Badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(6.dp)
                                        .background(Color(0xFF6750A4), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Page ${index + 1}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }

                                // Delete page action button
                                IconButton(
                                    onClick = { viewModel.pdfPages.removeAt(index) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(24.dp)
                                        .background(Color(0xFFEF4444), RoundedCornerShape(6.dp)),
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                                ) {
                                    Icon(Icons.Default.Delete, "Delete Page", modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            // --- Right Panel: PDF Compilation & Compression Tools ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Compilation Settings", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1D1B20))

                OutlinedTextField(
                    value = pdfName,
                    onValueChange = { pdfName = it },
                    label = { Text("PDF Document Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF3EDF7),
                        unfocusedContainerColor = Color(0xFFF3EDF7),
                        focusedBorderColor = Color(0xFFCAC4D0),
                        unfocusedBorderColor = Color(0xFFCAC4D0)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("pdf_title_input")
                )

                // Sheet preset size
                var expandedPageSize by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedPageSize = true },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF6750A4)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCAC4D0)),
                        modifier = Modifier.fillMaxWidth().testTag("pdf_size_button")
                    ) {
                        Text("Page Size: ${viewModel.pdfPageSizeSelection}", fontSize = 12.sp)
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(expanded = expandedPageSize, onDismissRequest = { expandedPageSize = false }) {
                        DropdownMenuItem(
                            text = { Text("A4 Standard") },
                            onClick = {
                                viewModel.pdfPageSizeSelection = "A4"
                                expandedPageSize = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Letter Standard") },
                            onClick = {
                                viewModel.pdfPageSizeSelection = "Letter"
                                expandedPageSize = false
                            }
                        )
                    }
                }

                Divider(color = Color(0xFFCAC4D0).copy(alpha = 0.5f))

                // Compression slider
                Text("Offline Compression Settings", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1D1B20))
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Resolution Quality Scale", fontSize = 12.sp, color = Color(0xFF49454F))
                        Text("${viewModel.pdfCompressionQuality}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6750A4))
                    }
                    Slider(
                        value = viewModel.pdfCompressionQuality.toFloat(),
                        onValueChange = { viewModel.pdfCompressionQuality = it.toInt() },
                        valueRange = 20f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF6750A4),
                            activeTrackColor = Color(0xFF6750A4),
                            inactiveTrackColor = Color(0xFFCAC4D0)
                        ),
                        modifier = Modifier.testTag("pdf_compression_slider")
                    )
                    Text(
                        text = "Compress pixel matrices and scale down images locally to significantly shrink the compiled PDF size without losing legible structure.",
                        fontSize = 10.sp,
                        color = Color(0xFF49454F)
                    )
                }

                Divider(color = Color(0xFFCAC4D0).copy(alpha = 0.5f))

                // Security vault tip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFD0BCFF), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EnhancedEncryption,
                        contentDescription = "Encrypted compilation",
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Your PDF bytes will be fully encrypted on-device. No leaks, no telemetry, no clouds.",
                        fontSize = 10.sp,
                        color = Color(0xFF49454F)
                    )
                }
            }
        }
    }
}
