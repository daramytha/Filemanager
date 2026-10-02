package com.example.pcfilemanager

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HardDrive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkStoragePermission()
        setContent {
            MaterialTheme {
                PcFileManagerScreen()
            }
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }
}

@Composable
fun PcFileManagerScreen() {
    val rootPath = Environment.getExternalStorageDirectory().absolutePath
    var currentPath by remember { mutableStateOf(rootPath) }

    Row(modifier = Modifier.fillMaxSize()) {
        // 1. SIDEBAR KIRI (Quick Access)
        Sidebar(
            onNavigate = { path -> currentPath = path },
            rootPath = rootPath,
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
                .background(Color(0xFF252526))
        )

        Divider(modifier = Modifier.fillMaxHeight().width(1.dp), color = Color.Gray)

        // 2. AREA UTAMA (Tampilan PC)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFF1E1E1E))
        ) {
            AddressBar(
                currentPath = currentPath,
                onBack = {
                    val parent = File(currentPath).parent
                    if (parent != null && currentPath != rootPath) {
                        currentPath = parent
                    }
                }
            )

            TableHeader()

            FileListTable(
                currentPath = currentPath,
                onFolderClick = { folderPath -> currentPath = folderPath }
            )
        }
    }
}

@Composable
fun Sidebar(onNavigate: (String) -> Unit, rootPath: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(8.dp)) {
        Text(
            text = "Quick Access",
            color = Color.LightGray,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )

        SidebarItem(icon = Icons.Default.HardDrive, label = "Internal Storage") {
            onNavigate(rootPath)
        }
        SidebarItem(icon = Icons.Default.Star, label = "Downloads") {
            onNavigate("$rootPath/Download")
        }
        SidebarItem(icon = Icons.Default.Star, label = "Documents") {
            onNavigate("$rootPath/Documents")
        }
        SidebarItem(icon = Icons.Default.Star, label = "Pictures") {
            onNavigate("$rootPath/Pictures")
        }
    }
}

@Composable
fun SidebarItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = Color.White, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun AddressBar(currentPath: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2D2D2D))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            color = Color(0xFF3C3C3C),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = currentPath,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF252526))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text("Nama", color = Color.Gray, modifier = Modifier.weight(2f), style = MaterialTheme.typography.labelSmall)
        Text("Tanggal Modifikasi", color = Color.Gray, modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.labelSmall)
        Text("Tipe", color = Color.Gray, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
        Text("Ukuran", color = Color.Gray, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun FileListTable(currentPath: String, onFolderClick: (String) -> Unit) {
    val directory = File(currentPath)
    val files = directory.listFiles()?.toList()?.sortedWith(
        compareBy({ !it.isDirectory }, { it.name.lowercase() })
    ) ?: emptyList()

    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(files) { file ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (file.isDirectory) {
                            onFolderClick(file.absolutePath)
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nama File
                Row(modifier = Modifier.weight(2f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                        contentDescription = null,
                        tint = if (file.isDirectory) Color(0xFFE5C07B) else Color(0xFF61AFEF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(file.name, color = Color.White, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }

                // Modifikasi
                Text(
                    text = dateFormat.format(Date(file.lastModified())),
                    color = Color.LightGray,
                    modifier = Modifier.weight(1.5f),
                    style = MaterialTheme.typography.bodySmall
                )

                // Tipe
                Text(
                    text = if (file.isDirectory) "File folder" else file.extension.uppercase() + " File",
                    color = Color.LightGray,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall
                )

                // Ukuran
                Text(
                    text = if (file.isDirectory) "" else "${file.length() / 1024} KB",
                    color = Color.LightGray,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Divider(color = Color(0xFF2D2D2D), thickness = 0.5.dp)
        }
    }
}
