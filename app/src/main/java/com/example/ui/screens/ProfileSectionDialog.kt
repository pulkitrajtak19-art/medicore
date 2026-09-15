package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.UserProfile
import com.example.ui.theme.*

private val BLOOD_GROUPS = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
private val GENDERS = listOf("Female", "Male", "Other")

@Composable
fun ProfileSectionDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onSaveProfile: (
        name: String,
        email: String,
        phone: String,
        age: Int,
        gender: String,
        bloodGroup: String,
        heightCm: Double,
        weightKg: Double,
        city: String,
        emergencyContact: String,
        allergies: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    var phone by remember { mutableStateOf(user.phone) }
    var ageStr by remember { mutableStateOf(user.age.toString()) }
    var selectedGender by remember { mutableStateOf(user.gender) }
    var selectedBloodGroup by remember { mutableStateOf(user.bloodGroup) }
    var heightStr by remember { mutableStateOf(user.heightCm.toInt().toString()) }
    var weightStr by remember { mutableStateOf(user.weightKg.toInt().toString()) }
    var city by remember { mutableStateOf(user.city) }
    var emergencyContact by remember { mutableStateOf(user.emergencyContact) }
    var allergies by remember { mutableStateOf(user.allergies) }

    var hasSavedSuccessfully by remember { mutableStateOf(false) }

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
                    .widthIn(max = 600.dp)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .testTag("profile_section_dialog"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
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
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(LightBlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = LightBluePrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "User Profile & Health Details",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightBlueHeader
                                )
                                Text(
                                    text = "Manage your digital identity & vital information",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(LightBlueContainer.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextDarkSlate)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Identity & Role Badge
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = LightBlueContainer.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "ABHA HEALTH ID",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightBluePrimary,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = user.abhaId,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LightBlueHeader
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SoftEmeraldLight,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder)
                            ) {
                                Text(
                                    text = user.role.displayName.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftEmeraldDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "PERSONAL INFORMATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightBluePrimary,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Full Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Legal Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = LightBluePrimary) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Email and Phone
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = LightBluePrimary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_email_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = LightBluePrimary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_phone_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "BIOMETRIC & CLINICAL DETAILS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightBluePrimary,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Age & Blood Group
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = ageStr,
                            onValueChange = { ageStr = it.filter { ch -> ch.isDigit() }.take(3) },
                            label = { Text("Age (Yrs)") },
                            leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null, tint = LightBluePrimary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_age_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = heightStr,
                            onValueChange = { heightStr = it.filter { ch -> ch.isDigit() }.take(3) },
                            label = { Text("Height (cm)") },
                            leadingIcon = { Icon(Icons.Default.Height, contentDescription = null, tint = LightBluePrimary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_height_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = weightStr,
                            onValueChange = { weightStr = it.filter { ch -> ch.isDigit() }.take(3) },
                            label = { Text("Weight (kg)") },
                            leadingIcon = { Icon(Icons.Default.Scale, contentDescription = null, tint = LightBluePrimary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_weight_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Gender Selector
                    Text(text = "Gender:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDarkSlate)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GENDERS.forEach { g ->
                            val isSelected = selectedGender.equals(g, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) LightBluePrimary else LightBlueContainer.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) LightBluePrimary else SurfaceBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedGender = g }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = g,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else TextDarkSlate
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Blood Group Selector
                    Text(text = "Blood Group:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDarkSlate)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(BLOOD_GROUPS) { bg ->
                            val isSelected = selectedBloodGroup == bg
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) SoftEmeraldAccent else Color.White,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) SoftEmeraldAccent else SurfaceBorder
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedBloodGroup = bg }
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = bg,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else TextDarkSlate
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "LOCATION & EMERGENCY CONTACT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightBluePrimary,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City, State") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = LightBluePrimary) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_city_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = emergencyContact,
                        onValueChange = { emergencyContact = it },
                        label = { Text("Emergency Contact Phone") },
                        leadingIcon = { Icon(Icons.Default.ContactPhone, contentDescription = null, tint = LightBluePrimary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_emergency_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "KNOWN ALLERGIES & MEDICAL FLAGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightBluePrimary,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = allergies,
                        onValueChange = { allergies = it },
                        label = { Text("Known Allergies (e.g., Penicillin, Peanuts, Pollen)") },
                        leadingIcon = { Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFE11D48)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_allergies_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", color = TextMuted)
                        }

                        Button(
                            onClick = {
                                val parsedAge = ageStr.toIntOrNull() ?: user.age
                                val parsedHeight = heightStr.toDoubleOrNull() ?: user.heightCm
                                val parsedWeight = weightStr.toDoubleOrNull() ?: user.weightKg
                                onSaveProfile(
                                    name.trim().ifBlank { user.name },
                                    email.trim().ifBlank { user.email },
                                    phone.trim().ifBlank { user.phone },
                                    parsedAge,
                                    selectedGender,
                                    selectedBloodGroup,
                                    parsedHeight,
                                    parsedWeight,
                                    city.trim().ifBlank { user.city },
                                    emergencyContact.trim().ifBlank { user.emergencyContact },
                                    allergies.trim().ifBlank { "None known" }
                                )
                                hasSavedSuccessfully = true
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("save_profile_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
