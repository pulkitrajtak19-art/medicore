package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.HealthVitals
import com.example.data.model.Prescription
import com.example.data.model.UserProfile
import com.example.data.supabase.SupabaseClient
import com.example.data.supabase.SupabaseConnectionStatus
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SupabaseSyncDialog(
    user: UserProfile,
    vitals: List<HealthVitals>,
    prescriptions: List<Prescription>,
    onDismiss: () -> Unit,
    onOpenScanner: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val syncStatus by SupabaseClient.status.collectAsState()
    val syncLogs by SupabaseClient.syncLogs.collectAsState()

    var customPassword by remember { mutableStateOf(syncStatus.password) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x80000000))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .testTag("supabase_sync_dialog"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3ECF8E).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Supabase Database",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightBlueHeader
                                )
                                Text(
                                    text = "PostgreSQL Cloud Connection",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Status banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (syncStatus.isConnected) Color(0xFFF0FDF4) else Color(0xFFFFFBEB),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (syncStatus.isConnected) Color(0xFF86EFAC) else Color(0xFFFDE68A)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (syncStatus.isConnected) Icons.Default.CheckCircle else Icons.Default.SyncProblem,
                                contentDescription = null,
                                tint = if (syncStatus.isConnected) SoftEmeraldDark else WarningAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (syncStatus.isConnected) "Supabase Connected & Synchronized" else "Connecting to Supabase...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (syncStatus.isConnected) SoftEmeraldDark else WarningAmber
                                )
                                Text(
                                    text = syncStatus.statusMessage,
                                    fontSize = 11.sp,
                                    color = TextDarkSlate
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Connection Parameters Card
                    Text(
                        text = "CONNECTION PARAMETERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = LightBluePrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            ConnectionParamRow(label = "Host", value = syncStatus.host)
                            ConnectionParamRow(label = "Port", value = "${syncStatus.port}")
                            ConnectionParamRow(label = "Database", value = syncStatus.database)
                            ConnectionParamRow(label = "User", value = syncStatus.user)
                            ConnectionParamRow(label = "REST API", value = syncStatus.url)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Connection URI snippet
                    Text(
                        text = "POSTGRESQL URI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = LightBluePrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val connectionUri = "postgresql://postgres:${if (customPassword.isNotBlank()) customPassword else "[PASSWORD]"}@${syncStatus.host}:${syncStatus.port}/${syncStatus.database}"

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = connectionUri,
                                fontSize = 10.sp,
                                color = Color(0xFF38BDF8),
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Supabase Connection URI", connectionUri))
                                    Toast.makeText(context, "Connection URI copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy URI",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons: Test Connection & Sync Records
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val ok = SupabaseClient.testConnection()
                                    Toast.makeText(
                                        context,
                                        if (ok) "Supabase connection verified!" else "Connection check completed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LightBluePrimary),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, SurfaceBorder)
                        ) {
                            if (syncStatus.isTesting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Ping", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val ok = SupabaseClient.syncUserData(user, vitals, prescriptions)
                                    Toast.makeText(
                                        context,
                                        if (ok) "All records synced to Supabase!" else "Records queued locally for sync",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.weight(1.3f).height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent)
                        ) {
                            if (syncStatus.isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync to Cloud", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (onOpenScanner != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onDismiss()
                                onOpenScanner()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("open_supabase_scanner_from_dialog_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3ECF8E),
                                contentColor = Color(0xFF0F172A)
                            )
                        ) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Launch Supabase Patient Scanner & Dossier",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Real-Time Supabase Event Stream
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SoftEmeraldAccent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "REAL-TIME SUPABASE LOGS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = LightBluePrimary
                            )
                        }
                        Text(
                            text = "${syncStatus.syncedRecordsCount} Records",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftEmeraldDark
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        syncLogs.take(5).forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = SoftEmeraldAccent.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = log.targetTable,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SoftEmeraldDark,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = log.timestamp,
                                            fontSize = 9.sp,
                                            color = TextMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = log.summary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextDarkSlate,
                                        maxLines = 1
                                    )
                                }
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SoftEmeraldAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionParamRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = TextMuted)
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = LightBlueHeader,
            fontFamily = FontFamily.Monospace
        )
    }
}
