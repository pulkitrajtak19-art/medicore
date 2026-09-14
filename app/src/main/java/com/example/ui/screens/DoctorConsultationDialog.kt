package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PrescriptionItem
import com.example.ui.theme.*
import java.util.UUID

@Composable
fun DoctorConsultationDialog(
    patientName: String,
    doctorName: String,
    onDismiss: () -> Unit,
    onSaveConsultation: (complaint: String, diagnosis: String, remarks: String, items: List<PrescriptionItem>) -> Unit
) {
    var complaint by remember { mutableStateOf("Throat irritation, low grade fever and body fatigue for 3 days.") }
    var diagnosis by remember { mutableStateOf("Acute Pharyngitis with mild allergic rhinitis") }
    var remarks by remember { mutableStateOf("Advised hydration, steam inhalation twice daily, and prescribed oral course.") }

    var items by remember {
        mutableStateOf(
            listOf(
                PrescriptionItem(
                    id = UUID.randomUUID().toString(),
                    medicineName = "Azithromycin 500mg",
                    dosage = "1 Tab",
                    frequency = "1 - 0 - 0",
                    timing = "After Breakfast",
                    durationDays = 3,
                    quantity = 3,
                    instructions = "Take for 3 days continuously"
                ),
                PrescriptionItem(
                    id = UUID.randomUUID().toString(),
                    medicineName = "Paracetamol 650mg",
                    dosage = "1 Tab",
                    frequency = "1 - 0 - 1 (SOS)",
                    timing = "After Meals",
                    durationDays = 5,
                    quantity = 10,
                    instructions = "Take in case of headache or body ache"
                )
            )
        )
    }

    var newMedName by remember { mutableStateOf("") }
    var newDose by remember { mutableStateOf("1 Tab") }
    var newFreq by remember { mutableStateOf("1 - 0 - 1") }
    var newTiming by remember { mutableStateOf("After Meals") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                            Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = LightBluePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "New Clinical Consultation",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightBlueHeader
                            )
                            Text(
                                text = "Patient: $patientName",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SurfaceBorder)

                OutlinedTextField(
                    value = complaint,
                    onValueChange = { complaint = it },
                    label = { Text("Chief Complaint / Symptoms") },
                    modifier = Modifier.fillMaxWidth().testTag("complaint_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LightBluePrimary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    label = { Text("Clinical Diagnosis / ICD Summary") },
                    modifier = Modifier.fillMaxWidth().testTag("diagnosis_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LightBluePrimary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Doctor Remarks & Lifestyle Advice") },
                    modifier = Modifier.fillMaxWidth().testTag("remarks_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LightBluePrimary)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "DIGITAL PRESCRIPTION ITEMS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftEmeraldDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Existing items
                items.forEachIndexed { index, item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = WarmWhiteSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${index + 1}. ${item.medicineName}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkSlate
                                )
                                Text(
                                    text = "Dosage: ${item.dosage} | Freq: ${item.frequency} | ${item.timing}",
                                    fontSize = 11.sp,
                                    color = LightBlueHeader
                                )
                            }
                            IconButton(onClick = { items = items.filter { it.id != item.id } }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = ErrorRose, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Add Medicine Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = newMedName,
                        onValueChange = { newMedName = it },
                        placeholder = { Text("Medicine name (e.g. Montelukast 10mg)", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoftEmeraldAccent)
                    )
                    IconButton(
                        onClick = {
                            if (newMedName.isNotBlank()) {
                                items = items + PrescriptionItem(
                                    id = UUID.randomUUID().toString(),
                                    medicineName = newMedName.trim(),
                                    dosage = newDose,
                                    frequency = newFreq,
                                    timing = newTiming,
                                    durationDays = 5,
                                    quantity = 10,
                                    instructions = "As directed by physician"
                                )
                                newMedName = ""
                            }
                        },
                        modifier = Modifier
                            .background(SoftEmeraldAccent, RoundedCornerShape(10.dp))
                            .size(44.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onSaveConsultation(complaint, diagnosis, remarks, items)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_consultation_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent)
                ) {
                    Text("Issue Prescription & Link to Timeline", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
