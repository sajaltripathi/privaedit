package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Category
import com.example.data.LocalDocument
import com.example.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DocumentViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val docs = viewModel.filteredDocuments
    val categories by viewModel.categories.collectAsState()
    val tags by viewModel.tags.collectAsState()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }

    // File pickers
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importImageUri(context, it) }
    }

    val textPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importTextUri(context, it) }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // PDF compilation flow page adder
            viewModel.startNewPdf()
            viewModel.addImagePageToPdf(context, it)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {},
        floatingActionButton = {
            Box(contentAlignment = Alignment.BottomEnd) {
                if (showFabMenu) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 80.dp)
                    ) {
                        // Import Image option
                        ExtendedFloatingActionButton(
                            onClick = {
                                showFabMenu = false
                                imagePickerLauncher.launch("image/*")
                            },
                            icon = { Icon(Icons.Default.Photo, "Import Image") },
                            text = { Text("Import Image") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.testTag("import_image_fab")
                        )

                        // New Text Document option
                        ExtendedFloatingActionButton(
                            onClick = {
                                showFabMenu = false
                                viewModel.createNewTextDocument()
                            },
                            icon = { Icon(Icons.Default.NoteAdd, "Create Text") },
                            text = { Text("Create Document") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.testTag("create_text_fab")
                        )

                        // Import Text file option
                        ExtendedFloatingActionButton(
                            onClick = {
                                showFabMenu = false
                                textPickerLauncher.launch("text/*")
                            },
                            icon = { Icon(Icons.Default.FileOpen, "Import Text") },
                            text = { Text("Import TXT") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.testTag("import_txt_fab")
                        )

                        // Create PDF option
                        ExtendedFloatingActionButton(
                            onClick = {
                                showFabMenu = false
                                viewModel.startNewPdf()
                            },
                            icon = { Icon(Icons.Default.PictureAsPdf, "Create PDF") },
                            text = { Text("Compile PDF") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.testTag("create_pdf_fab")
                        )
                    }
                }

                FloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("main_action_fab")
                ) {
                    Icon(
                        imageVector = if (showFabMenu) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Actions Menu"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // --- Custom Editorial Aesthetic Header ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ENCRYPTED WORKSPACE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.5.sp,
                        color = Color(0xFF6750A4)
                    )
                    Text(
                        text = "PrivaEdit",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontWeight = FontWeight.Light,
                        fontSize = 36.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                // Security Vault circular badge
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEADDFF))
                        .clickable { viewModel.currentScreen = AppScreen.SECURITY_SETTINGS }
                        .testTag("security_settings_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (viewModel.securityPassword != null) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Security Vault Options",
                        tint = Color(0xFF21005D),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // --- Advanced Search Bar ---
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("search_field"),
                placeholder = { Text("Search encrypted files...", color = Color(0xFF49454F)) },
                leadingIcon = { Icon(Icons.Default.Search, "Search", tint = Color(0xFF49454F)) },
                trailingIcon = {
                    if (viewModel.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery = "" }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF3EDF7),
                    unfocusedContainerColor = Color(0xFFF3EDF7),
                    focusedBorderColor = Color(0xFFCAC4D0),
                    unfocusedBorderColor = Color(0xFFCAC4D0)
                ),
                singleLine = true,
                shape = RoundedCornerShape(28.dp)
            )

            // --- File Type Filter Row ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val types = listOf("ALL", "TEXT", "IMAGE", "PDF")
                types.forEach { type ->
                    val isSelected = viewModel.filterFileType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.filterFileType = type },
                        label = { Text(type, fontWeight = FontWeight.Medium) },
                        shape = RoundedCornerShape(24.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF6750A4),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFEADDFF),
                            labelColor = Color(0xFF21005D)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            selected = isSelected,
                            enabled = true,
                            borderColor = Color(0xFFCAC4D0),
                            selectedBorderColor = Color(0xFF6750A4),
                            borderWidth = 1.dp
                        )
                    )
                }
            }

            // --- Quick Processing Section ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "QUICK PROCESSING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF49454F),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Passport Size card (triggers image pick for import/crop)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { imagePickerLauncher.launch("image/*") }
                            .border(1.dp, Color(0xFFD0BCFF), RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3EDF7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AspectRatio,
                                    contentDescription = "Passport Size",
                                    tint = Color(0xFF6750A4)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Passport Size",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1D1B20)
                            )
                        }
                    }

                    // No-Loss Zip card (triggers file_open text pick / compiler)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.startNewPdf() }
                            .border(1.dp, Color(0xFFD0BCFF), RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3EDF7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Compress,
                                    contentDescription = "No-Loss Zip",
                                    tint = Color(0xFF6750A4)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No-Loss Zip",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1D1B20)
                            )
                        }
                    }
                }
            }

            // --- Categories & Tags Management Header ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Categories & Tags",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row {
                    TextButton(onClick = { showAddCategoryDialog = true }) {
                        Icon(Icons.Default.Add, "Add Category", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Category", fontSize = 12.sp)
                    }
                    TextButton(onClick = { showAddTagDialog = true }) {
                        Icon(Icons.Default.Add, "Add Tag", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tag", fontSize = 12.sp)
                    }
                }
            }

            // --- Horizontal Categories Row ---
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    val isSelected = viewModel.filterCategoryId == null
                    InputChip(
                        selected = isSelected,
                        onClick = { viewModel.filterCategoryId = null },
                        label = { Text("All Categories") },
                        leadingIcon = { Icon(Icons.Default.Folder, null, modifier = Modifier.size(16.dp)) }
                    )
                }
                items(categories) { cat ->
                    val isSelected = viewModel.filterCategoryId == cat.id
                    InputChip(
                        selected = isSelected,
                        onClick = { viewModel.filterCategoryId = cat.id },
                        label = { Text(cat.name) },
                        trailingIcon = {
                            IconButton(
                                onClick = { viewModel.deleteCategory(cat.id) },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(Icons.Default.Close, "Delete", modifier = Modifier.size(12.dp))
                            }
                        }
                    )
                }
            }

            // --- Horizontal Tags Row ---
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    val isSelected = viewModel.filterTag == null
                    InputChip(
                        selected = isSelected,
                        onClick = { viewModel.filterTag = null },
                        label = { Text("All Tags") },
                        leadingIcon = { Icon(Icons.Default.LocalOffer, null, modifier = Modifier.size(14.dp)) }
                    )
                }
                items(tags) { t ->
                    val isSelected = viewModel.filterTag == t.name
                    InputChip(
                        selected = isSelected,
                        onClick = { viewModel.filterTag = t.name },
                        label = { Text("#${t.name}") },
                        trailingIcon = {
                            IconButton(
                                onClick = { viewModel.deleteTag(t.id) },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(Icons.Default.Close, "Delete", modifier = Modifier.size(12.dp))
                            }
                        }
                    )
                }
            }

            // --- Sort & Stats Banner ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT LOCAL VAULT (${docs.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF49454F)
                )
                
                // Sorting Row selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sort: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val sorts = listOf("LATEST", "OLDEST", "NAME_AZ", "SIZE")
                    var expandedSort by remember { mutableStateOf(false) }
                    Box {
                        Text(
                            text = viewModel.sortBy,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { expandedSort = true }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        DropdownMenu(
                            expanded = expandedSort,
                            onDismissRequest = { expandedSort = false }
                        ) {
                            sorts.forEach { sortOption ->
                                DropdownMenuItem(
                                    text = { Text(sortOption) },
                                    onClick = {
                                        viewModel.sortBy = sortOption
                                        expandedSort = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // --- Documents List ---
            if (docs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = "No Documents",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No encrypted files found",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Import photos, text files, or compile a PDF offline.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 32.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(docs, key = { it.id }) { doc ->
                        DocumentItemCard(
                            doc = doc,
                            categories = categories,
                            onEdit = {
                                if (doc.fileType == "IMAGE") {
                                    viewModel.openImageDocument(context, doc)
                                } else if (doc.fileType == "TEXT") {
                                    viewModel.openTextDocument(doc)
                                }
                            },
                            onExtract = { viewModel.extractContentFromSelected(doc) },
                            onExport = { viewModel.exportDocumentAsFile(context, doc) },
                            onDelete = { viewModel.deleteDocument(doc.id) }
                        )
                    }
                }
            }
        }
    }

    // --- Add Category Dialog ---
    if (showAddCategoryDialog) {
        var categoryName by remember { mutableStateOf("") }
        var selectedColor by remember { mutableStateOf("#4F46E5") }
        val colors = listOf("#4F46E5", "#EF4444", "#10B981", "#F59E0B", "#8B5CF6", "#EC4899")

        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("New Custom Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = categoryName,
                        onValueChange = { categoryName = it },
                        label = { Text("Category Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("category_name_input")
                    )
                    Text("Select Color Theme:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colors.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .clickable { selectedColor = hex }
                                    .padding(4.dp)
                            ) {
                                if (selectedColor == hex) {
                                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (categoryName.isNotEmpty()) {
                            viewModel.addNewCategory(categoryName, selectedColor)
                            showAddCategoryDialog = false
                        }
                    },
                    modifier = Modifier.testTag("category_confirm_button")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) { Text("Cancel") }
            }
        )
    }

    // --- Add Tag Dialog ---
    if (showAddTagDialog) {
        var tagName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddTagDialog = false },
            title = { Text("New Custom Tag") },
            text = {
                OutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    label = { Text("Tag Name (e.g. Invoice)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("tag_name_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tagName.isNotEmpty()) {
                            viewModel.addNewTag(tagName)
                            showAddTagDialog = false
                        }
                    },
                    modifier = Modifier.testTag("tag_confirm_button")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTagDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun DocumentItemCard(
    doc: LocalDocumentDecrypted,
    categories: List<Category>,
    onEdit: () -> Unit,
    onExtract: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    val category = categories.find { it.id == doc.categoryId }
    val displaySize = if (doc.fileSize > 1024 * 1024) "${doc.fileSize / (1024 * 1024)} MB" else "${doc.fileSize / 1024} KB"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(16.dp))
            .testTag("document_card_${doc.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Main File row mimicking the design HTML:
            // <div class="flex items-center p-4 bg-white rounded-2xl border border-[#CAC4D0] shadow-sm">
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon container on the left: w-12 h-12 bg-[#F3EDF7] rounded-lg
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3EDF7)),
                    contentAlignment = Alignment.Center
                ) {
                    val fileIcon = when (doc.fileType) {
                        "PDF" -> Icons.Default.PictureAsPdf
                        "IMAGE" -> Icons.Default.Image
                        else -> Icons.Default.Description
                    }
                    Icon(
                        imageVector = fileIcon,
                        contentDescription = doc.fileType,
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Mid section: Title & subtitle info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doc.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1D1B20),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Modified ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(doc.updatedAt)} • $displaySize",
                        fontSize = 11.sp,
                        color = Color(0xFF49454F)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Verification Badge: verified_user icon in color-[#6750A4]
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "AES-256 E2EE Secured",
                    tint = Color(0xFF6750A4),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Categories & Tags Chips row (if any exist)
            if (category != null || doc.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (category != null) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Color(android.graphics.Color.parseColor(category.colorHex)).copy(alpha = 0.15f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = category.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(android.graphics.Color.parseColor(category.colorHex))
                            )
                        }
                    }

                    doc.tags.split(",").forEach { tag ->
                        if (tag.trim().isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Color(0xFFEADDFF),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(text = "#${tag.trim()}", fontSize = 10.sp, color = Color(0xFF21005D), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color(0xFFCAC4D0).copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(4.dp))

            // Operations bar below the card elements
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (doc.fileType != "PDF") {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, "Edit File", tint = Color(0xFF6750A4))
                        }
                    }
                    IconButton(onClick = onExtract) {
                        Icon(Icons.Default.AutoAwesome, "Extract content", tint = Color(0xFFF59E0B))
                    }
                    IconButton(onClick = onExport) {
                        Icon(Icons.Default.Download, "Export as File", tint = Color(0xFF49454F))
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete from Vault", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
