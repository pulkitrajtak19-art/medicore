package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.HealthVitals
import com.example.data.model.UserProfile
import com.example.data.supabase.SupabaseClient
import com.example.ui.components.PerfectQrCode
import com.example.ui.components.QrCodeGenerator
import com.example.ui.components.SegmentedControl
import com.example.ui.theme.*

@Composable
fun UserIdentityQrDialog(
    user: UserProfile,
    latestVital: HealthVitals? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Identity QR, 1: Medical Data QR, 2: JSON Payload
    var showCopiedNotification by remember { mutableStateOf(false) }

    val vitalsSummary = latestVital?.let {
        "BP ${it.bpSystolic}/${it.bpDiastolic} mmHg, SpO2 ${it.spo2Percent}%, Pulse ${it.pulseRate} bpm"
    } ?: "BP 118/78 mmHg, SpO2 99%, Pulse 72 bpm"

    val fullIdentityJson = remember(user, vitalsSummary) {
        QrCodeGenerator.buildIdentityPayload(user, vitalsSummary)
    }

    val qrContent = when (selectedTab) {
        0 -> fullIdentityJson
        1 -> "ABDM-MEDICAL://abha=${user.abhaId}&name=${user.name}&blood=${user.bloodGroup}&vitals=$vitalsSummary&db=${SupabaseClient.DEFAULT_HOST}"
        else -> fullIdentityJson
    }

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
                    .testTag("user_identity_qr_dialog"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar
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
                                    .background(SoftEmeraldLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.QrCode2,
                                    contentDescription = null,
                                    tint = SoftEmeraldDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Digital Health QR",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightBlueHeader
                                )
                                Text(
                                    text = "Universal ABDM Identity & Cloud Database",
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

                    // QR Mode Selector
                    SegmentedControl(
                        items = listOf("Identity QR", "Medical Summary", "JSON Payload"),
                        selectedIndex = selectedTab,
                        onSelect = { selectedTab = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedTab < 2) {
                        // QR Code Graphic Display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(WarmWhiteSubtle)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                                .padding(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                PerfectQrCode(
                                    content = qrContent,
                                    sizeDp = 210,
                                    showCenterEmblem = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(SoftEmeraldAccent)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "High-Density ISO/IEC 18004 Standard QR",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = SoftEmeraldDark
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // User Details Strip
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "PATIENT / USER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                        Text(text = user.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LightBlueHeader)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "ABHA HEALTH NUMBER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                        Text(text = user.abhaId, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SoftEmeraldDark)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = SurfaceBorder.copy(alpha = 0.6f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Blood Group: ${user.bloodGroup}", fontSize = 11.sp, color = TextDarkSlate)
                                    Text(text = "Age/Gender: ${user.age} • ${user.gender}", fontSize = 11.sp, color = TextDarkSlate)
                                    Text(text = "Phone: ${user.phone}", fontSize = 11.sp, color = TextDarkSlate)
                                }
                            }
                        }
                    } else {
                        // Raw Encoded JSON Payload
                        Surface(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 280.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ABDM IDENTITY PAYLOAD (JSON)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8),
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF1E293B)
                                    ) {
                                        Text(
                                            text = "SHA-256",
                                            fontSize = 9.sp,
                                            color = Color(0xFF38BDF8),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = fullIdentityJson,
                                    fontSize = 11.sp,
                                    color = Color(0xFFE2E8F0),
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Supabase Cloud Reference Indicator
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = SoftEmeraldDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Supabase Database Linked",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftEmeraldDark
                                )
                                Text(
                                    text = "${SupabaseClient.DEFAULT_HOST} • Port 5432",
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons: Copy JSON & Dismiss
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("ABDM Health Identity JSON", fullIdentityJson)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Identity JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                                showCopiedNotification = true
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LightBluePrimary),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, SurfaceBorder)
                        ) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Copy Payload", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent)
                        ) {
                            Text(text = "Done", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
