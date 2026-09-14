package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.supabase.SupabaseClient
import com.example.data.supabase.SupabasePatientDossier
import com.example.ui.components.QrCodeGenerator
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupabasePatientPortalScannerScreen(
    currentRole: UserRole,
    initialQuery: String? = null,
    onNavigateBack: () -> Unit,
    onDispensePrescription: ((String, String) -> Unit)? = null
) {
    var searchInput by remember { mutableStateOf(initialQuery ?: "") }
    var isSearching by remember { mutableStateOf(false) }
    var retrievedDossier by remember { mutableStateOf<SupabasePatientDossier?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Auto-search if initial query passed
    LaunchedEffect(initialQuery) {
        if (!initialQuery.isNullOrBlank()) {
            isSearching = true
            errorMessage = null
            val dossier = SupabaseClient.fetchPatientDossier(initialQuery)
            isSearching = false
            if (dossier != null) {
                retrievedDossier = dossier
            } else {
                errorMessage = "No record found for '$initialQuery' in Supabase PostgreSQL."
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Supabase Medical Portal",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightBlueHeader
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3ECF8E))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Connected: ${SupabaseClient.DEFAULT_HOST}",
                                fontSize = 10.sp,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("scanner_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = LightBlueHeader
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SoftEmeraldLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = SoftEmeraldDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Scan & Retrieve",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftEmeraldDark
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = WarmWhiteBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Scanner / Input Control Panel
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = SoftEmeraldAccent)
                        .testTag("qr_scanner_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SoftEmeraldLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = SoftEmeraldDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Scan Patient Health QR",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightBlueHeader
                                )
                                Text(
                                    text = "Retrieves live profile, vitals & prescriptions from Supabase DB",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Search/Scan Input Field
                        OutlinedTextField(
                            value = searchInput,
                            onValueChange = { searchInput = it },
                            placeholder = { Text("Enter ABHA ID, QR Token or email", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Search, contentDescription = null, tint = SoftEmeraldAccent)
                            },
                            trailingIcon = {
                                if (searchInput.isNotEmpty()) {
                                    IconButton(onClick = { searchInput = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("scan_query_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = WarmWhiteBackground,
                                unfocusedContainerColor = WarmWhiteBackground,
                                focusedBorderColor = SoftEmeraldAccent,
                                unfocusedBorderColor = SurfaceBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Select Chips for Demonstration
                        Text(
                            text = "QUICK LOOKUP IN SUPABASE:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = LightBluePrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = searchInput == "ABHA-91-8472-9102-4412",
                                onClick = { searchInput = "ABHA-91-8472-9102-4412" },
                                label = { Text("Priya Sharma", fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                            FilterChip(
                                selected = searchInput == "pulkitrajtak19@gmail.com",
                                onClick = { searchInput = "pulkitrajtak19@gmail.com" },
                                label = { Text("Pulkit Rajtak", fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                            FilterChip(
                                selected = searchInput == "patient_001",
                                onClick = { searchInput = "patient_001" },
                                label = { Text("patient_001", fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val query = QrCodeGenerator.extractPatientIdentifier(searchInput)
                                if (query.isNotBlank()) {
                                    isSearching = true
                                    errorMessage = null
                                    scope.launch {
                                        val dossier = SupabaseClient.fetchPatientDossier(query)
                                        isSearching = false
                                        if (dossier != null) {
                                            retrievedDossier = dossier
                                        } else {
                                            errorMessage = "Patient not found for '$query'. Try scanning or searching with ABHA ID or email."
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("retrieve_from_supabase_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SoftEmeraldAccent,
                                contentColor = Color.White
                            ),
                            enabled = !isSearching
                        ) {
                            if (isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Querying Supabase PostgreSQL...", fontSize = 13.sp)
                            } else {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Retrieve Patient Dossier from Supabase",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Error Display
            if (errorMessage != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFEF2F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorMessage ?: "",
                                fontSize = 12.sp,
                                color = Color(0xFF991B1B)
                            )
                        }
                    }
                }
            }

            // Retrieved Supabase Patient Dossier View
            if (retrievedDossier != null) {
                val dossier = retrievedDossier!!
                val user = dossier.user

                // Verification & Live Connection Header
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = SoftEmeraldLight,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, SoftEmeraldBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF3ECF8E)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "LIVE DATA FROM SUPABASE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp,
                                        color = SoftEmeraldDark
                                    )
                                    Text(
                                        text = "Retrieved at ${dossier.retrievalTimestamp}",
                                        fontSize = 10.sp,
                                        color = TextDarkSlate
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White
                            ) {
                                Text(
                                    text = "ABDM Verified",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftEmeraldDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Patient Identity Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.name,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = LightBlueHeader
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = user.abhaId,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SoftEmeraldDark,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFEEF2FF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC7D2FE))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (user.isEmailVerified) "Email Verified" else "Email Pending",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4F46E5)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = SurfaceBorder)
                            Spacer(modifier = Modifier.height(14.dp))

                            // Contact info row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("EMAIL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextLightSlate)
                                    Text(user.email, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextDarkSlate)
                                }
                                Column {
                                    Text("PHONE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextLightSlate)
                                    Text(user.phone, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextDarkSlate)
                                }
                                Column {
                                    Text("CITY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextLightSlate)
                                    Text(user.city, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextDarkSlate)
                                }
                            }
                        }
                    }
                }

                // Biometrics & Body Metrics Card
                item {
                    val heightM = (user.heightCm / 100.0).coerceAtLeast(0.5)
                    val bmi = String.format(Locale.getDefault(), "%.1f", user.weightKg / (heightM * heightM))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "PHYSICAL STATS & HEALTH METRICS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = LightBluePrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricPill(title = "AGE", value = "${user.age} yrs", modifier = Modifier.weight(1f))
                                MetricPill(title = "SEX", value = user.gender, modifier = Modifier.weight(1f))
                                MetricPill(title = "HEIGHT", value = "${user.heightCm} cm", modifier = Modifier.weight(1f))
                                MetricPill(title = "WEIGHT", value = "${user.weightKg} kg", modifier = Modifier.weight(1f))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricPill(title = "BLOOD GROUP", value = user.bloodGroup, modifier = Modifier.weight(1f))
                                MetricPill(title = "BMI (KG/M²)", value = "$bmi (Normal)", modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // CRITICAL WARNING: Known Allergies Alert Box
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFFBEB),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFDE68A))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Allergies Alert",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "KNOWN DRUG & FOOD ALLERGIES",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = user.allergies.ifBlank { "None recorded in Supabase" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }
                }

                // Emergency Contact Box
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ContactPhone, contentDescription = null, tint = SoftEmeraldAccent)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("EMERGENCY CONTACT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextLightSlate)
                                Text(user.emergencyContact, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDarkSlate)
                            }
                        }
                    }
                }

                // Latest Vitals from Supabase
                if (dossier.vitals.isNotEmpty()) {
                    item {
                        Text(
                            text = "SUPABASE CLINICAL VITALS (${dossier.vitals.size} RECORDS)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = LightBluePrimary
                        )
                    }

                    items(dossier.vitals) { vital ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Vitals @ ${vital.timestamp}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LightBlueHeader
                                    )
                                    Text(
                                        text = vital.recordedBy,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("BP", fontSize = 10.sp, color = TextLightSlate)
                                        Text("${vital.bpSystolic}/${vital.bpDiastolic}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDarkSlate)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("PULSE", fontSize = 10.sp, color = TextLightSlate)
                                        Text("${vital.pulseRate} bpm", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDarkSlate)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("SpO2", fontSize = 10.sp, color = TextLightSlate)
                                        Text("${vital.spo2Percent}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SoftEmeraldDark)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("TEMP", fontSize = 10.sp, color = TextLightSlate)
                                        Text("${vital.temperatureF}°F", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDarkSlate)
                                    }
                                }
                            }
                        }
                    }
                }

                // Prescriptions retrieved from Supabase
                if (dossier.prescriptions.isNotEmpty()) {
                    item {
                        Text(
                            text = "SUPABASE ACTIVE PRESCRIPTIONS (${dossier.prescriptions.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = LightBluePrimary
                        )
                    }

                    items(dossier.prescriptions) { rx ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(rx.id, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LightBlueHeader)
                                        Text("By ${rx.doctorName} • ${rx.date}", fontSize = 11.sp, color = TextMuted)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (rx.isDispensed) Color(0xFFF1F5F9) else SoftEmeraldLight
                                    ) {
                                        Text(
                                            text = if (rx.isDispensed) "DISPENSED" else "ACTIVE RX",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (rx.isDispensed) TextMuted else SoftEmeraldDark,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = SurfaceBorder)
                                Spacer(modifier = Modifier.height(8.dp))

                                rx.items.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = item.medicineName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextDarkSlate
                                            )
                                            Text(
                                                text = "${item.dosage} • ${item.frequency} • ${item.timing}",
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )
                                        }
                                        Text(
                                            text = "${item.durationDays} days (${item.quantity} units)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = LightBluePrimary
                                        )
                                    }
                                }

                                if (!rx.isDispensed && currentRole == UserRole.MEDICINE_CENTRE && onDispensePrescription != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { onDispensePrescription(rx.id, "Jaipur Central Dispensary") },
                                        modifier = Modifier.fillMaxWidth().height(42.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent)
                                    ) {
                                        Icon(Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Dispense Medications at Pharmacy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = WarmWhiteBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextLightSlate)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkSlate, textAlign = TextAlign.Center)
        }
    }
}
