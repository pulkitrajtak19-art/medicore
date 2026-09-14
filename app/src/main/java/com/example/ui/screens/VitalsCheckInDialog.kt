package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun VitalsCheckInDialog(
    onDismiss: () -> Unit,
    onSaveVitals: (systolic: Int, diastolic: Int, pulse: Int, temp: Double, weight: Double, spo2: Int) -> Unit
) {
    var sysInput by remember { mutableStateOf("118") }
    var diaInput by remember { mutableStateOf("78") }
    var pulseInput by remember { mutableStateOf("72") }
    var tempInput by remember { mutableStateOf("98.4") }
    var weightInput by remember { mutableStateOf("59.5") }
    var spo2Input by remember { mutableStateOf("99") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = SoftEmeraldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Capture Vitals (Early Check-In)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightBlueHeader
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Text(
                    text = "Record baseline physiological vitals to link with current hospital visit.",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                // BP Row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sysInput,
                        onValueChange = { sysInput = it },
                        label = { Text("Systolic BP") },
                        suffix = { Text("mmHg", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("systolic_bp_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoftEmeraldAccent)
                    )
                    OutlinedTextField(
                        value = diaInput,
                        onValueChange = { diaInput = it },
                        label = { Text("Diastolic BP") },
                        suffix = { Text("mmHg", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("diastolic_bp_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoftEmeraldAccent)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Pulse & SpO2
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pulseInput,
                        onValueChange = { pulseInput = it },
                        label = { Text("Pulse Rate") },
                        suffix = { Text("bpm", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("pulse_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoftEmeraldAccent)
                    )
                    OutlinedTextField(
                        value = spo2Input,
                        onValueChange = { spo2Input = it },
                        label = { Text("SpO2 Level") },
                        suffix = { Text("%", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("spo2_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoftEmeraldAccent)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Temperature & Weight
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tempInput,
                        onValueChange = { tempInput = it },
                        label = { Text("Temperature") },
                        suffix = { Text("°F", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoftEmeraldAccent)
                    )
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Weight") },
                        suffix = { Text("kg", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoftEmeraldAccent)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val sys = sysInput.toIntOrNull() ?: 120
                        val dia = diaInput.toIntOrNull() ?: 80
                        val pulse = pulseInput.toIntOrNull() ?: 72
                        val temp = tempInput.toDoubleOrNull() ?: 98.6
                        val wt = weightInput.toDoubleOrNull() ?: 60.0
                        val sp = spo2Input.toIntOrNull() ?: 98
                        onSaveVitals(sys, dia, pulse, temp, wt, sp)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_vitals_button")
                ) {
                    Text("Save & Update Health ID Record", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
