package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.components.SegmentedControl
import com.example.ui.theme.*

@Composable
fun AuthScreen(
    onLoginSuccess: (emailOrPhone: String, role: UserRole, isPhone: Boolean) -> Unit,
    onSignUpSuccess: (name: String, emailOrPhone: String, role: UserRole, age: Int, gender: String, bloodGroup: String, isPhone: Boolean) -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }
    var authMethodIndex by remember { mutableIntStateOf(0) } // 0: Email, 1: Phone
    var selectedRole by remember { mutableStateOf(UserRole.PATIENT) }

    // Form inputs
    var nameInput by remember { mutableStateOf("Priya Sharma") }
    var emailInput by remember { mutableStateOf("priya.sharma@healthmail.in") }
    var phoneInput by remember { mutableStateOf("9829012345") }
    var passwordInput by remember { mutableStateOf("••••••••") }
    var ageInput by remember { mutableStateOf("28") }
    var genderInput by remember { mutableStateOf("Female") }
    var bloodGroupInput by remember { mutableStateOf("O+") }
    var otpInput by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhiteBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // App Brand Header
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x1A50C878)), // bg-[#50C878]/10
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(3.5.dp, SoftEmeraldAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.HealthAndSafety,
                        contentDescription = "Health ID Logo",
                        tint = SoftEmeraldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Health Identity Platform",
                fontSize = 26.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-0.5).sp,
                color = LightBlueHeader
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Simple. Sober. Secure. • ABDM Aligned",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SurfaceBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Sign In / Sign Up Switcher
                    SegmentedControl(
                        items = listOf("Sign In", "Sign Up"),
                        selectedIndex = if (isSignUp) 1 else 0,
                        onSelect = { isSignUp = (it == 1) },
                        modifier = Modifier.testTag("auth_mode_toggle")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email vs Phone Method Switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(WarmWhiteSubtle)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilterChip(
                            selected = authMethodIndex == 0,
                            onClick = { authMethodIndex = 0; isOtpSent = false },
                            label = { Text("Email Address", fontSize = 12.sp, fontWeight = if (authMethodIndex == 0) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.White,
                                selectedLabelColor = LightBlueHeader,
                                selectedLeadingIconColor = LightBlueHeader,
                                containerColor = Color.Transparent,
                                labelColor = TextMuted
                            ),
                            border = null,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(
                            selected = authMethodIndex == 1,
                            onClick = { authMethodIndex = 1; isOtpSent = false },
                            label = { Text("Phone (OTP)", fontSize = 12.sp, fontWeight = if (authMethodIndex == 1) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.White,
                                selectedLabelColor = LightBlueHeader,
                                selectedLeadingIconColor = LightBlueHeader,
                                containerColor = Color.Transparent,
                                labelColor = TextMuted
                            ),
                            border = null,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Role Picker (for Smart Portal Routing)
                    Text(
                        text = "SELECT PORTAL ROLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = LightBluePrimary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserRole.values().forEach { role ->
                            val isSelected = selectedRole == role
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) SoftEmeraldContainer else WarmWhiteSubtle,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) SoftEmeraldAccent else SurfaceBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedRole = role }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        when (role) {
                                            UserRole.PATIENT -> Icons.Default.Person
                                            UserRole.DOCTOR -> Icons.Default.MedicalServices
                                            UserRole.MEDICINE_CENTRE -> Icons.Default.LocalPharmacy
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) SoftEmeraldDark else LightBluePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = role.displayName.split(" ")[0],
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) SoftEmeraldDark else TextDarkSlate
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Sign Up Specific Fields
                    AnimatedVisibility(visible = isSignUp) {
                        Column {
                            Text(
                                text = "FULL NAME",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = LightBluePrimary,
                                modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                            )
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                placeholder = { Text("Priya Sharma", color = TextLightSlate) },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = SoftEmeraldAccent,
                                    unfocusedBorderColor = SurfaceBorder
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "AGE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = LightBluePrimary,
                                        modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                                    )
                                    OutlinedTextField(
                                        value = ageInput,
                                        onValueChange = { ageInput = it },
                                        placeholder = { Text("28") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(16.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedBorderColor = SoftEmeraldAccent,
                                            unfocusedBorderColor = SurfaceBorder
                                        )
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "BLOOD GROUP",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = LightBluePrimary,
                                        modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                                    )
                                    OutlinedTextField(
                                        value = bloodGroupInput,
                                        onValueChange = { bloodGroupInput = it },
                                        placeholder = { Text("O+") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedBorderColor = SoftEmeraldAccent,
                                            unfocusedBorderColor = SurfaceBorder
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Email or Phone Input
                    if (authMethodIndex == 0) {
                        Text(
                            text = "EMAIL ADDRESS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = LightBluePrimary,
                            modifier = Modifier.align(Alignment.Start).padding(start = 2.dp, bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            placeholder = { Text("name@healthmail.in", color = TextLightSlate) },
                            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = LightBluePrimary) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth().height(56.dp).testTag("email_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = SoftEmeraldAccent,
                                unfocusedBorderColor = SurfaceBorder
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "SECURITY KEY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = LightBluePrimary,
                            modifier = Modifier.align(Alignment.Start).padding(start = 2.dp, bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            placeholder = { Text("••••••••", color = TextLightSlate) },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = LightBluePrimary) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().height(56.dp).testTag("password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = SoftEmeraldAccent,
                                unfocusedBorderColor = SurfaceBorder
                            )
                        )
                    } else {
                        Text(
                            text = "PHONE NUMBER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = LightBluePrimary,
                            modifier = Modifier.align(Alignment.Start).padding(start = 2.dp, bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            leadingIcon = {
                                Text(
                                    "+91 ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightBlueHeader,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth().height(56.dp).testTag("phone_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = SoftEmeraldAccent,
                                unfocusedBorderColor = SurfaceBorder
                            )
                        )

                        if (isOtpSent) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "VERIFICATION OTP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = LightBluePrimary,
                                modifier = Modifier.align(Alignment.Start).padding(start = 2.dp, bottom = 4.dp)
                            )
                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = { otpInput = it },
                                placeholder = { Text("e.g. 4821") },
                                leadingIcon = { Icon(Icons.Outlined.Key, contentDescription = null, tint = SoftEmeraldAccent) },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("otp_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = SoftEmeraldAccent,
                                    unfocusedBorderColor = SurfaceBorder
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Submit Button with Soft Emerald styling (Clean Minimalism h-14 bg-[#50C878] rounded-2xl)
                    Button(
                        onClick = {
                            if (authMethodIndex == 1 && !isOtpSent) {
                                isOtpSent = true
                                otpInput = "4821"
                            } else {
                                val isPhone = (authMethodIndex == 1)
                                val identifier = if (isPhone) "+91 $phoneInput" else emailInput
                                if (isSignUp) {
                                    onSignUpSuccess(
                                        nameInput,
                                        identifier,
                                        selectedRole,
                                        ageInput.toIntOrNull() ?: 28,
                                        genderInput,
                                        bloodGroupInput,
                                        isPhone
                                    )
                                } else {
                                    onLoginSuccess(identifier, selectedRole, isPhone)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = SoftEmeraldAccent)
                            .testTag("submit_auth_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SoftEmeraldAccent,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (authMethodIndex == 1 && !isOtpSent) "Send OTP Verification"
                            else if (isSignUp) "Create ABHA Health Profile"
                            else "Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Clean Minimal Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceBorder)
                        Text(
                            text = if (isSignUp) "ALREADY REGISTERED?" else "NEW TO HEALTH ID?",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = LightBluePrimary.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceBorder)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Secondary outline toggle button
                    OutlinedButton(
                        onClick = { isSignUp = !isSignUp },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LightBluePrimary),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, SurfaceBorder)
                    ) {
                        Text(
                            text = if (isSignUp) "Sign In to Existing Profile" else "Create New Health Account",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Demo Fast-Login Strip
            Text(
                text = "EVALUATION PERSONAS (FAST DEMO)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = LightBluePrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onLoginSuccess("priya.sharma@healthmail.in", UserRole.PATIENT, false) },
                    modifier = Modifier.weight(1f).testTag("demo_patient_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LightBlueHeader),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Text("Patient", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onLoginSuccess("dr.rajesh@jaipurcare.org", UserRole.DOCTOR, false) },
                    modifier = Modifier.weight(1f).testTag("demo_doctor_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftEmeraldDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder)
                ) {
                    Text("Doctor", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onLoginSuccess("dispense@jaipurcentralpharma.in", UserRole.MEDICINE_CENTRE, false) },
                    modifier = Modifier.weight(1f).testTag("demo_pharmacy_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDarkSlate),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Text("Pharmacy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Designed for Android & iOS Compatibility",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextLightSlate
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
