package com.example.ui.screens

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Prescription
import com.example.data.model.UserProfile
import com.example.ui.components.QrCodeView
import com.example.ui.theme.*

@Composable
fun MedicineCentreScreen(
    user: UserProfile,
    prescriptions: List<Prescription>,
    onDispensePrescription: (prescriptionId: String, pharmacyName: String) -> Boolean
) {
    var searchToken by remember { mutableStateOf("") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhiteBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Medicine Centre Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(LightBlueContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocalPharmacy,
                        contentDescription = null,
                        tint = LightBlueHeader,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = user.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LightBlueHeader)
                    Text(text = "HFR Dispensing Unit • ${user.city}", fontSize = 12.sp, color = TextDarkSlate)
                    Text(text = "ABDM QR Verification Desk", fontSize = 11.sp, color = TextMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scanner / Quick Dispensing Search & Queue
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Scan / Verify Prescription Token",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightBlueHeader
                )
                Text(
                    text = "Scan patient QR or enter token to locate and dispense medication batch",
                    fontSize = 11.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchToken,
                    onValueChange = { searchToken = it },
                    label = { Text("Prescription Token / Patient Name") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = LightBluePrimary)
                    },
                    trailingIcon = {
                        if (searchToken.isNotEmpty()) {
                            IconButton(onClick = { searchToken = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("pharmacy_token_search_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LightBluePrimary,
                        unfocusedBorderColor = SurfaceBorder
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val filteredPrescriptions = if (searchToken.isBlank()) {
            prescriptions
        } else {
            prescriptions.filter {
                it.qrPrescriptionToken.contains(searchToken, ignoreCase = true) ||
                        it.patientName.contains(searchToken, ignoreCase = true) ||
                        it.id.contains(searchToken, ignoreCase = true)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Prescription Dispensing Queue",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightBlueHeader
                )
                Text(
                    text = "${filteredPrescriptions.size} record(s) available",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredPrescriptions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No prescriptions matching token \"$searchToken\"",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        } else {
            filteredPrescriptions.forEach { rx ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .shadow(2.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (rx.isDispensed) SoftEmeraldBorder else SurfaceBorder
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Rx #${rx.id}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightBlueHeader
                            )
                            Text(
                                text = "Patient: ${rx.patientName} • Dr. ${rx.doctorName}",
                                fontSize = 12.sp,
                                color = TextDarkSlate
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
                                text = if (rx.isDispensed) "DISPENSED" else "READY TO DISPENSE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (rx.isDispensed) SoftEmeraldDark else WarningAmber,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "PRESCRIBED MEDICINES:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )

                    rx.items.forEachIndexed { i, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(WarmWhiteSubtle)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${i + 1}. ${item.medicineName}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkSlate
                                )
                                Text(
                                    text = "Dosage: ${item.dosage} | Frequency: ${item.frequency} (${item.timing})",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                            Text(
                                text = "Qty: ${item.quantity}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftEmeraldDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!rx.isDispensed) {
                        Button(
                            onClick = {
                                val ok = onDispensePrescription(rx.id, user.name)
                                if (ok) {
                                    snackbarMessage = "Medicines dispensed and adherence reminders scheduled for ${rx.patientName}!"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("dispense_rx_${rx.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verify Token & Dispense Medicines", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SoftEmeraldLight)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SoftEmeraldDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dispensed on ${rx.dispensedDate ?: "Today"} by ${rx.dispensedByCentre ?: user.name}",
                                fontSize = 11.sp,
                                color = SoftEmeraldDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

        Spacer(modifier = Modifier.height(16.dp))

        if (snackbarMessage != null) {
            Snackbar(
                modifier = Modifier.padding(vertical = 8.dp),
                containerColor = SoftEmeraldDark,
                contentColor = Color.White
            ) {
                Text(snackbarMessage!!)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
