package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MedicationReminder
import com.example.data.model.Prescription
import com.example.ui.components.QrCodeView
import com.example.ui.theme.*

@Composable
fun PatientMedicationsScreen(
    prescriptions: List<Prescription>,
    reminders: List<MedicationReminder>,
    onMarkReminderTaken: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhiteBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Medications & Adherence",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = LightBlueHeader
            )
            Text(
                text = "Digital prescriptions, dispensing status & daily reminders",
                fontSize = 12.sp,
                color = TextMuted
            )
        }

        // Daily Reminders Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Medication Schedule",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftEmeraldDark
                        )
                        val takenCount = reminders.count { it.isTakenToday }
                        Text(
                            text = "$takenCount of ${reminders.size} taken today",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (takenCount == reminders.size && reminders.isNotEmpty()) SoftEmeraldDark else TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (reminders.isEmpty()) {
                        Text("No active doses scheduled.", fontSize = 12.sp, color = TextMuted)
                    } else {
                        reminders.forEach { rem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (rem.isTakenToday) SoftEmeraldLight else WarmWhiteSubtle)
                                    .border(1.dp, if (rem.isTakenToday) SoftEmeraldBorder else SurfaceBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = rem.medicineName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (rem.isTakenToday) SoftEmeraldDark else TextDarkSlate
                                    )
                                    Text(
                                        text = "${rem.timeSlot} • ${rem.mealInstruction}",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (rem.isTakenToday) SoftEmeraldContainer else Color.White
                                    ) {
                                        Text(
                                            text = "🔥 ${rem.streakDays}d",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (rem.isTakenToday) SoftEmeraldDark else TextMuted,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (rem.isTakenToday) {
                                        // Once taken, locked state with NO option to unclick
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = SoftEmeraldContainer,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = "Dose Confirmed Taken",
                                                    tint = SoftEmeraldDark,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Taken",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SoftEmeraldDark
                                                )
                                            }
                                        }
                                    } else {
                                        // Not yet taken: Clickable button to mark dose taken
                                        Button(
                                            onClick = { onMarkReminderTaken(rem.id) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Mark Taken",
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Take", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Prescriptions with QR Dispense Tokens
        item {
            Text(
                text = "Prescriptions & Medicine Centre Tokens",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = LightBlueHeader
            )
        }

        items(prescriptions) { rx ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = rx.id,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightBlueHeader
                            )
                            Text(
                                text = "Prescribed by ${rx.doctorName} • ${rx.date}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (rx.isDispensed) SoftEmeraldLight else WarningContainer,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (rx.isDispensed) SoftEmeraldBorder else WarningAmber
                            )
                        ) {
                            Text(
                                text = if (rx.isDispensed) "DISPENSED" else "PENDING DISPENSE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (rx.isDispensed) SoftEmeraldDark else WarningAmber,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    rx.items.forEachIndexed { i, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${i + 1}. ${item.medicineName} (${item.dosage})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDarkSlate
                            )
                            Text(
                                text = "${item.frequency} | Qty: ${item.quantity}",
                                fontSize = 11.sp,
                                color = LightBlueHeader
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Verification Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(WarmWhiteSubtle)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (rx.isDispensed) "Dispensed at ${rx.dispensedByCentre ?: "Pharmacy"}"
                                else "Show token to Pharmacy for verification",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextDarkSlate
                            )
                            Text(
                                text = "Token: ${rx.qrPrescriptionToken}",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                        QrCodeView(dataToken = rx.qrPrescriptionToken, sizeDp = 44)
                    }
                }
            }
        }
    }
}
