package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FacilityCheckIn
import com.example.data.model.UserProfile
import com.example.ui.components.QrCodeView
import com.example.ui.theme.*

@Composable
fun DigitalOpdPassDialog(
    user: UserProfile,
    checkIn: FacilityCheckIn,
    onDismiss: () -> Unit,
    onCheckout: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("digital_opd_pass_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SoftEmeraldLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder)
                    ) {
                        Text(
                            text = "LIVE OPD QUEUE PASS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftEmeraldDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = checkIn.facilityName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightBlueHeader,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${checkIn.department} • ${checkIn.counterNumber}",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Token Number Hero Display
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SoftEmeraldLight,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SoftEmeraldAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TOKEN NUMBER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftEmeraldDark,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = checkIn.tokenNumber,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SoftEmeraldDark,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SoftEmeraldAccent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Queue Status: Active (~${checkIn.estimatedWaitMinutes} min wait)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDarkSlate
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // QR Code for facility verification
                QrCodeView(dataToken = checkIn.securityChecksum, sizeDp = 120)

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = checkIn.securityChecksum,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = SurfaceBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Patient Details
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PassInfoRow("Patient Name", user.name)
                    PassInfoRow("ABHA Health ID", checkIn.abhaShared)
                    PassInfoRow("Check-In Time", checkIn.timestamp)
                    PassInfoRow("Desk Counter", checkIn.counterNumber)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onCheckout()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Exit / Check-out", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Done", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PassInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 11.sp, color = TextMuted)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDarkSlate)
    }
}
