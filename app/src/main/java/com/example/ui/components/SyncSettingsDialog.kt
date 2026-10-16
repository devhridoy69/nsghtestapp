package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.repository.SyncStatus
import com.example.ui.theme.HospitalNavy
import com.example.ui.theme.HospitalTeal
import com.example.ui.theme.MedicalRed

@Composable
fun SyncSettingsDialog(
    initialUrl: String,
    initialKey: String,
    initialTable: String,
    lastSyncTime: String,
    syncStatus: SyncStatus,
    onSaveAndSync: (url: String, key: String, table: String) -> Unit,
    onResetToDefaults: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var url by remember { mutableStateOf(initialUrl) }
    var key by remember { mutableStateOf(initialKey) }
    var table by remember { mutableStateOf(initialTable) }
    var showSqlGuide by remember { mutableStateOf(false) }

    val sampleSql = """
-- Supabase Table (Name and Price only):
create table if not exists diagnostic_tests (
  id bigint generated always as identity primary key,
  name text not null,
  price numeric not null
);

-- Enable public read access for your mobile app:
alter table diagnostic_tests enable row level security;
create policy "Allow public read" on diagnostic_tests
  for select using (true);
    """.trimIndent()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(24.dp))
                .testTag("sync_settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Cloud Sync",
                            tint = HospitalNavy,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Supabase Database",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Connect directly with your Supabase database. When data/Wi-Fi is on, changes in your Supabase table will automatically sync to your phone.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Status info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = HospitalTeal.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Last Synced:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = lastSyncTime,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HospitalNavy
                            )
                        }

                        when (syncStatus) {
                            is SyncStatus.Error -> {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "⚠️ ${syncStatus.message}",
                                    fontSize = 11.sp,
                                    color = MedicalRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            is SyncStatus.Success -> {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "✓ Successfully synced ${syncStatus.count} tests from Supabase",
                                    fontSize = 11.sp,
                                    color = Color(0xFF16A34A),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            is SyncStatus.Info -> {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = syncStatus.message,
                                    fontSize = 11.sp,
                                    color = HospitalTeal
                                )
                            }
                            else -> {}
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // URL Input
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Supabase Project URL") },
                    placeholder = { Text("https://xyzcompany.supabase.co") },
                    leadingIcon = {
                        Icon(Icons.Default.Link, contentDescription = null, tint = HospitalNavy)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("supabase_url_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // API Key Input
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("Supabase Anon API Key") },
                    placeholder = { Text("eyJhbGciOiJIUzI1NiIsInR5c...") },
                    leadingIcon = {
                        Icon(Icons.Default.Key, contentDescription = null, tint = HospitalNavy)
                    },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("supabase_key_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Table Name Input
                OutlinedTextField(
                    value = table,
                    onValueChange = { table = it },
                    label = { Text("Table Name") },
                    placeholder = { Text("diagnostic_tests (or tests)") },
                    leadingIcon = {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = HospitalNavy)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("supabase_table_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Expandable SQL Guide
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSqlGuide = !showSqlGuide }
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = HospitalTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Supabase SQL Setup & Columns",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HospitalTeal
                                )
                            }
                            Icon(
                                imageVector = if (showSqlGuide) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = HospitalTeal,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        AnimatedVisibility(visible = showSqlGuide) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                Text(
                                    text = "Supported column names (flexible):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "• Name: name or test_name\n• Price: price or test_price\n• Category: category or department\n• Code: code or test_code\n• Sample: sample_type or sample",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Supabase SQL", sampleSql))
                                            Toast.makeText(context, "SQL copied to clipboard!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy Table SQL", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Save & Sync Button
                Button(
                    onClick = {
                        onSaveAndSync(url, key, table)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_and_sync_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = HospitalNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Connect & Sync Database",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Reset to Default Hospital Catalog button
                OutlinedButton(
                    onClick = {
                        onResetToDefaults()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_catalog_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Restore Hospital Catalog Default",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
