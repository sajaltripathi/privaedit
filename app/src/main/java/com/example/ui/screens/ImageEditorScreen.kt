package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.DocumentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageEditorScreen(
    viewModel: DocumentViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var saveName by remember { mutableStateOf("Edited_Image") }

    // Redraw processed preview when sliders change
    LaunchedEffect(
        viewModel.imageSourceBitmap,
        viewModel.imageBrightness,
        viewModel.imageContrast,
        viewModel.imageSaturation,
        viewModel.imageRotation,
        viewModel.imagePresetSize,
        viewModel.imageCustomWidth,
        viewModel.imageCustomHeight,
        viewModel.redactionPaths.size
    ) {
        coroutineScope.launch {
            processedBitmap = viewModel.getProcessedBitmap()
        }
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
                            color = ComposeColor(0xFF6750A4)
                        )
                        Text(
                            text = "Offline Image Studio",
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
                        onClick = { viewModel.saveProcessedImage(saveName) },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ComposeColor(0xFF6750A4),
                            contentColor = ComposeColor.White
                        ),
                        modifier = Modifier.testTag("save_image_button")
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
            // --- Left Panel: Interactive Image Preview with Mask Redaction drawing ---
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ComposeColor.White)
                    .border(1.dp, ComposeColor(0xFFCAC4D0), RoundedCornerShape(16.dp))
                    .pointerInput(viewModel.isRedactionActive) {
                        if (viewModel.isRedactionActive) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val newPath = Path().apply { moveTo(offset.x, offset.y) }
                                    viewModel.redactionPaths.add(newPath)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val currentPath = viewModel.redactionPaths.lastOrNull()
                                    currentPath?.lineTo(change.position.x, change.position.y)
                                    // Trigger recomposition/redraw
                                    viewModel.redactionPaths[viewModel.redactionPaths.size - 1] = currentPath ?: Path()
                                }
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (processedBitmap != null) {
                    Image(
                        bitmap = processedBitmap!!.asImageBitmap(),
                        contentDescription = "Image Preview",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                } else {
                    CircularProgressIndicator(color = ComposeColor(0xFF6750A4))
                }

                // Redaction mode indicator
                if (viewModel.isRedactionActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(12.dp)
                            .background(ComposeColor(0xFFEF4444), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Redaction Drawing Mode Active: Click and drag to draw masks",
                            color = ComposeColor.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // --- Right Panel: Rich Controls ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Save Title
                OutlinedTextField(
                    value = saveName,
                    onValueChange = { saveName = it },
                    label = { Text("File Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ComposeColor(0xFFF3EDF7),
                        unfocusedContainerColor = ComposeColor(0xFFF3EDF7),
                        focusedBorderColor = ComposeColor(0xFFCAC4D0),
                        unfocusedBorderColor = ComposeColor(0xFFCAC4D0)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("image_name_input")
                )

                // 1. Redaction Masking & Unmasking Trigger
                Card(
                    colors = CardDefaults.cardColors(containerColor = ComposeColor.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ComposeColor(0xFFCAC4D0), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Confidentiality Mask", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ComposeColor(0xFF1D1B20))
                            Switch(
                                checked = viewModel.isRedactionActive,
                                onCheckedChange = { viewModel.isRedactionActive = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ComposeColor.White,
                                    checkedTrackColor = ComposeColor(0xFF6750A4),
                                    uncheckedThumbColor = ComposeColor(0xFF49454F),
                                    uncheckedTrackColor = ComposeColor(0xFFCAC4D0).copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.testTag("redact_switch")
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Draw secure black blocks over highly sensitive data like credit card numbers or passport IDs before exporting.",
                            fontSize = 11.sp,
                            color = ComposeColor(0xFF49454F)
                        )
                        if (viewModel.redactionPaths.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.redactionPaths.clear() },
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ComposeColor(0xFFEF4444), contentColor = ComposeColor.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.DeleteSweep, "Clear Redactions", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Unmask / Clear Redaction blocks", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // 2. Preset Aspect Ratios & Resizing
                Text("Preset Dimensions & Resizing", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ComposeColor(0xFF1D1B20))
                val sizes = listOf("Free", "A4", "Passport", "WhatsApp", "Custom")
                var sizeExpanded by remember { mutableStateOf(false) }
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { sizeExpanded = true },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ComposeColor(0xFF6750A4)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ComposeColor(0xFFCAC4D0)),
                        modifier = Modifier.fillMaxWidth().testTag("size_dropdown_button")
                    ) {
                        Text("Size Preset: ${viewModel.imagePresetSize}", fontSize = 12.sp)
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(
                        expanded = sizeExpanded,
                        onDismissRequest = { sizeExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.4f)
                    ) {
                        sizes.forEach { size ->
                            DropdownMenuItem(
                                text = { Text(size) },
                                onClick = {
                                    viewModel.imagePresetSize = size
                                    sizeExpanded = false
                                }
                            )
                        }
                    }
                }

                if (viewModel.imagePresetSize == "Custom") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = viewModel.imageCustomWidth,
                            onValueChange = { viewModel.imageCustomWidth = it },
                            label = { Text("Width (px)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ComposeColor(0xFFF3EDF7),
                                unfocusedContainerColor = ComposeColor(0xFFF3EDF7),
                                focusedBorderColor = ComposeColor(0xFFCAC4D0),
                                unfocusedBorderColor = ComposeColor(0xFFCAC4D0)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("custom_width_input")
                        )
                        OutlinedTextField(
                            value = viewModel.imageCustomHeight,
                            onValueChange = { viewModel.imageCustomHeight = it },
                            label = { Text("Height (px)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ComposeColor(0xFFF3EDF7),
                                unfocusedContainerColor = ComposeColor(0xFFF3EDF7),
                                focusedBorderColor = ComposeColor(0xFFCAC4D0),
                                unfocusedBorderColor = ComposeColor(0xFFCAC4D0)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("custom_height_input")
                        )
                    }
                }

                // 3. Sliders for Tuning
                Text("Color & Light Enhancers", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ComposeColor(0xFF1D1B20))
                
                // Brightness Slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Brightness", fontSize = 12.sp, color = ComposeColor(0xFF49454F))
                        Text("${viewModel.imageBrightness.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ComposeColor(0xFF6750A4))
                    }
                    Slider(
                        value = viewModel.imageBrightness,
                        onValueChange = { viewModel.imageBrightness = it },
                        valueRange = -100f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = ComposeColor(0xFF6750A4),
                            activeTrackColor = ComposeColor(0xFF6750A4),
                            inactiveTrackColor = ComposeColor(0xFFCAC4D0)
                        ),
                        modifier = Modifier.testTag("brightness_slider")
                    )
                }

                // Contrast Slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Contrast", fontSize = 12.sp, color = ComposeColor(0xFF49454F))
                        Text(String.format("%.1fx", viewModel.imageContrast), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ComposeColor(0xFF6750A4))
                    }
                    Slider(
                        value = viewModel.imageContrast,
                        onValueChange = { viewModel.imageContrast = it },
                        valueRange = 0.1f..3.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = ComposeColor(0xFF6750A4),
                            activeTrackColor = ComposeColor(0xFF6750A4),
                            inactiveTrackColor = ComposeColor(0xFFCAC4D0)
                        ),
                        modifier = Modifier.testTag("contrast_slider")
                    )
                }

                // Saturation Slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Saturation", fontSize = 12.sp, color = ComposeColor(0xFF49454F))
                        Text(String.format("%.1fx", viewModel.imageSaturation), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ComposeColor(0xFF6750A4))
                    }
                    Slider(
                        value = viewModel.imageSaturation,
                        onValueChange = { viewModel.imageSaturation = it },
                        valueRange = 0.1f..3.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = ComposeColor(0xFF6750A4),
                            activeTrackColor = ComposeColor(0xFF6750A4),
                            inactiveTrackColor = ComposeColor(0xFFCAC4D0)
                        ),
                        modifier = Modifier.testTag("saturation_slider")
                    )
                }

                // 4. Rotation Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rotation", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ComposeColor(0xFF1D1B20))
                    IconButton(
                        onClick = {
                            viewModel.imageRotation = (viewModel.imageRotation + 90f) % 360f
                        },
                        modifier = Modifier.testTag("rotate_button")
                    ) {
                        Icon(Icons.Default.RotateRight, "Rotate 90 deg", tint = ComposeColor(0xFF6750A4))
                    }
                }

                Divider(color = ComposeColor(0xFFCAC4D0).copy(alpha = 0.5f))

                // 5. Target Format Selector
                Text("Format Conversion", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ComposeColor(0xFF1D1B20))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val formats = listOf("image/jpeg" to "JPEG", "image/png" to "PNG", "image/webp" to "WEBP")
                    formats.forEach { (mime, label) ->
                        val isSelected = viewModel.imageMimeType == mime
                        OutlinedButton(
                            onClick = { viewModel.imageMimeType = mime },
                            shape = RoundedCornerShape(24.dp),
                            colors = if (isSelected) ButtonDefaults.outlinedButtonColors(
                                containerColor = ComposeColor(0xFFD0BCFF),
                                contentColor = ComposeColor(0xFF6750A4)
                            ) else ButtonDefaults.outlinedButtonColors(
                                contentColor = ComposeColor(0xFF49454F)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, 
                                if (isSelected) ComposeColor(0xFF6750A4) else ComposeColor(0xFFCAC4D0)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label, fontSize = 12.sp)
                        }
                    }
                }

                // 6. Quality / Compression Slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Compression / Quality Setting", fontSize = 12.sp, color = ComposeColor(0xFF49454F))
                        Text("${viewModel.imageQuality}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ComposeColor(0xFF6750A4))
                    }
                    Slider(
                        value = viewModel.imageQuality.toFloat(),
                        onValueChange = { viewModel.imageQuality = it.toInt() },
                        valueRange = 10f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = ComposeColor(0xFF6750A4),
                            activeTrackColor = ComposeColor(0xFF6750A4),
                            inactiveTrackColor = ComposeColor(0xFFCAC4D0)
                        ),
                        modifier = Modifier.testTag("compression_slider")
                    )
                    Text(
                        text = "Reduce file size up to 10x offline without losing visual structure.",
                        fontSize = 10.sp,
                        color = ComposeColor(0xFF49454F)
                    )
                }

                // Export out of sandbox
                Button(
                    onClick = {
                        coroutineScope.launch {
                            processedBitmap?.let { bitmap ->
                                val docDecrypted = com.example.ui.LocalDocumentDecrypted(
                                    id = 0,
                                    title = saveName,
                                    content = "",
                                    fileType = "IMAGE",
                                    filePath = "",
                                    fileSize = 0,
                                    resolutionWidth = bitmap.width,
                                    resolutionHeight = bitmap.height,
                                    mimeType = viewModel.imageMimeType,
                                    categoryId = null,
                                    tags = "",
                                    createdAt = 0,
                                    updatedAt = 0
                                )
                                // Save mock file temp and trigger export
                                val outDir = java.io.File(context.getExternalFilesDir(null), "Exports")
                                if (!outDir.exists()) outDir.mkdirs()
                                val ext = if (viewModel.imageMimeType.contains("png")) "png" else "jpg"
                                val outFile = java.io.File(outDir, "${saveName}_export.$ext")
                                java.io.FileOutputStream(outFile).use { out ->
                                    val stream = java.io.ByteArrayOutputStream()
                                    val format = if (viewModel.imageMimeType.contains("png")) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                                    bitmap.compress(format, viewModel.imageQuality, stream)
                                    out.write(stream.toByteArray())
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ComposeColor(0xFF6750A4),
                        contentColor = ComposeColor.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("export_image_to_disk")
                ) {
                    Icon(Icons.Default.Download, "Export", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Unencrypted to Downloads", fontSize = 12.sp)
                }
            }
        }
    }
}
