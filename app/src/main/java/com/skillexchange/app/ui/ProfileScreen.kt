package com.skillexchange.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.skillexchange.app.data.AREAS
import com.skillexchange.app.data.Config
import com.skillexchange.app.data.UploadedFile

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    vm: AppViewModel,
    onNavigateHome: () -> Unit,
    onNavigateToMySkills: () -> Unit
) {
    val p = vm.profile
    val context = LocalContext.current
    var showUrlPhotoDialog by remember { mutableStateOf(false) }
    var showUrlFileDialog by remember { mutableStateOf(false) }
    var urlPhotoInput by remember { mutableStateOf("") }
    var urlFileInput by remember { mutableStateOf("") }
    var urlFileNameInput by remember { mutableStateOf("") }

    // Activity Result Launchers for picking files and photos
    val profilePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}
            vm.updateProfile(p.copy(profilePhoto = it.toString()))
        }
    }

    val galleryPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}
            vm.updateProfile(p.copy(photos = p.photos + it.toString()))
        }
    }

    val documentFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}
            val name = getFileName(context, it) ?: "Uploaded_Document"
            val mimeType = context.contentResolver.getType(it) ?: "file"
            val file = UploadedFile(name = name, uri = it.toString(), mimeType = mimeType)
            vm.updateProfile(p.copy(uploadedFiles = p.uploadedFiles + file))
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SwapprHeaderLogo(onNavigateHome = onNavigateHome)

                TextButton(onClick = { vm.logout() }) {
                    Text("Sign Out", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Profile & Portfolio",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF8FAFC)
                )
                Text(
                    text = "Manage your information, bio, credentials, and work samples",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // Quick Navigation Button to My Offered Skills
        MarketplaceCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToMySkills() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF065F46)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = Color(0xFF34D399)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Manage Offered Skills",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = "${vm.myServices.size} skill(s) listed for swap",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        // 1. Profile Picture, Name, Bio & Basic Info Card
        MarketplaceCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Profile Avatar with Camera Edit Badge
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF065F46))
                        .border(1.dp, Color(0xFF34D399), CircleShape)
                        .clickable { profilePhotoPicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (p.profilePhoto.isNotBlank()) {
                        AsyncImage(
                            model = p.profilePhoto,
                            contentDescription = "Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Photo",
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    // Camera Icon Overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF34D399)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Profile Picture",
                            tint = Color(0xFF064E3B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { profilePhotoPicker.launch("image/*") },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Change Photo")
                    }
                    if (p.profilePhoto.isNotBlank()) {
                        TextButton(
                            onClick = { vm.updateProfile(p.copy(profilePhoto = "")) }
                        ) {
                            Text("Remove", color = Color(0xFFEF4444))
                        }
                    }
                }

                OutlinedTextField(
                    value = p.name,
                    onValueChange = { vm.updateProfile(p.copy(name = it)) },
                    singleLine = true,
                    label = { Text("Your Name") },
                    colors = marketplaceTextFieldColors(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                )

                // Bio Text Field
                OutlinedTextField(
                    value = p.bio,
                    onValueChange = { vm.updateProfile(p.copy(bio = it)) },
                    minLines = 3,
                    maxLines = 5,
                    label = { Text("About Me / Bio") },
                    placeholder = { Text("Share your skills, background, or experience...") },
                    colors = marketplaceTextFieldColors(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                )

                Dropdown(
                    label = "Neighbourhood",
                    options = AREAS.map { it.name },
                    selected = p.area,
                    onSelect = { vm.updateProfile(p.copy(area = it)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 2. Uploaded Files & Documents Card
        MarketplaceCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Uploaded Files & Documents", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFF8FAFC))
                        Text("Certificates, resumes, or sample work files", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { documentFilePicker.launch("*/*") },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399), contentColor = Color(0xFF064E3B))
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Upload File", color = Color(0xFF064E3B))
                    }
                    OutlinedButton(
                        onClick = { showUrlFileDialog = true },
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Link")
                    }
                }

                if (p.uploadedFiles.isEmpty()) {
                    Text(
                        "No files uploaded yet. Upload document files or add links to showcase your credentials!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        p.uploadedFiles.forEach { file ->
                            MarketplaceCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = marketplaceCardColors(containerColor = Color(0xFF0F172A))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (file.mimeType.startsWith("image")) Icons.Default.Image else Icons.Default.Description,
                                            contentDescription = null,
                                            tint = Color(0xFF34D399),
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(file.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, color = Color(0xFFF8FAFC))
                                            if (file.mimeType.isNotBlank()) {
                                                Text(file.mimeType, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                                            }
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            val updated = p.uploadedFiles - file
                                            vm.updateProfile(p.copy(uploadedFiles = updated))
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete File",
                                            tint = Color(0xFFEF4444)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Showcase Photos & Gallery Card
        MarketplaceCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("My Work Photos & Gallery", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFF8FAFC))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { galleryPhotoPicker.launch("image/*") },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399), contentColor = Color(0xFF064E3B))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Upload Photo", color = Color(0xFF064E3B))
                    }
                    OutlinedButton(
                        onClick = { showUrlPhotoDialog = true },
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add URL")
                    }
                }

                if (p.photos.isEmpty()) {
                    Text(
                        "No work photos added yet. Add photos showcasing your skills or completed projects!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(p.photos) { photoUrl ->
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Showcase Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                                IconButton(
                                    onClick = {
                                        val updated = p.photos - photoUrl
                                        vm.updateProfile(p.copy(photos = updated))
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(24.dp)
                                        .background(Color(0xFF0F172A).copy(alpha = 0.85f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete photo", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Photo via URL Dialog
        if (showUrlPhotoDialog) {
            MarketplaceCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add Photo via URL", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFF8FAFC))
                    OutlinedTextField(
                        value = urlPhotoInput,
                        onValueChange = { urlPhotoInput = it },
                        singleLine = true,
                        placeholder = { Text("Enter image web URL or URI") },
                        colors = marketplaceTextFieldColors(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showUrlPhotoDialog = false }) {
                            Text("Cancel", color = Color(0xFF94A3B8))
                        }
                        Button(
                            onClick = {
                                if (urlPhotoInput.isNotBlank()) {
                                    val updated = p.photos + urlPhotoInput.trim()
                                    vm.updateProfile(p.copy(photos = updated))
                                    urlPhotoInput = ""
                                    showUrlPhotoDialog = false
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399), contentColor = Color(0xFF064E3B))
                        ) {
                            Text("Add Photo", color = Color(0xFF064E3B))
                        }
                    }
                }
            }
        }

        // Add File via URL Link Dialog
        if (showUrlFileDialog) {
            MarketplaceCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add Document or File Link", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFF8FAFC))
                    OutlinedTextField(
                        value = urlFileNameInput,
                        onValueChange = { urlFileNameInput = it },
                        singleLine = true,
                        label = { Text("File / Document Name") },
                        placeholder = { Text("e.g. My Resume / Portfolio PDF") },
                        colors = marketplaceTextFieldColors(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = urlFileInput,
                        onValueChange = { urlFileInput = it },
                        singleLine = true,
                        label = { Text("File URL or Link") },
                        placeholder = { Text("https://...") },
                        colors = marketplaceTextFieldColors(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showUrlFileDialog = false }) {
                            Text("Cancel", color = Color(0xFF94A3B8))
                        }
                        Button(
                            onClick = {
                                if (urlFileInput.isNotBlank()) {
                                    val name = if (urlFileNameInput.isNotBlank()) urlFileNameInput.trim() else "Linked Document"
                                    val file = UploadedFile(name = name, uri = urlFileInput.trim(), mimeType = "link")
                                    vm.updateProfile(p.copy(uploadedFiles = p.uploadedFiles + file))
                                    urlFileInput = ""
                                    urlFileNameInput = ""
                                    showUrlFileDialog = false
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399), contentColor = Color(0xFF064E3B))
                        ) {
                            Text("Add File Link", color = Color(0xFF064E3B))
                        }
                    }
                }
            }
        }

        // 4. Skills & Interests Section
        MarketplaceCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("I am looking for", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFF8FAFC))
                Text("Select categories you wish to learn or receive skills in:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    vm.estimator.taxonomy.keys.forEach { cat ->
                        FilterChip(
                            selected = cat in p.wants,
                            onClick = { vm.updateProfile(p.copy(wants = if (cat in p.wants) p.wants - cat else p.wants + cat)) },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF34D399),
                                selectedLabelColor = Color(0xFF064E3B),
                                containerColor = Color(0xFF0F172A),
                                labelColor = Color(0xFFF8FAFC)
                            ),
                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = cat in p.wants, borderColor = Color(0xFF334155))
                        )
                    }
                }
            }
        }

        // 5. Value Engine Card
        MarketplaceCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Value Engine Benchmark", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFF8FAFC))
                Text(vm.estimator.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                Text(
                    "Values are calculated in EUR from historical benchmark data and shown as KSh at ${Config.KES_PER_EUR.toInt()} per €.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

private fun getFileName(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex)
                    }
                }
            }
        } catch (_: Exception) {}
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}
