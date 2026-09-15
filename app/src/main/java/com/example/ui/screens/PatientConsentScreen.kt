package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AuditLog
import com.example.data.model.ConsentRequest
import com.example.data.model.ConsentStatus
import com.example.ui.theme.*

@Composable
fun PatientConsentScreen(
    consents: List<ConsentRequest>,
    auditLogs: List<AuditLog>,
    onToggleConsent: (String) -> Unit
) {
    var pendingRevokeConsent by remember { mutableStateOf<ConsentRequest?>(null) }
    var hasConfirmedDeclaration by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhiteBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Consent & Privacy Architecture",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightBlueHeader
                )
                Text(
                    text = "ABDM-aligned patient-controlled data sharing & immutable audit log",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            item {
                Text(
                    text = "Active Clinical Consent Grants",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightBlueHeader
                )
            }

            items(consents) { con ->
                val isActive = con.status == ConsentStatus.ACTIVE
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
                                    text = con.requesterName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkSlate
                                )
                                Text(
                                    text = "${con.requesterRole} • ${con.facilityName}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isActive) SoftEmeraldLight else ErrorContainer,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isActive) SoftEmeraldBorder else ErrorRose
                                )
                            ) {
                                Text(
                                    text = if (isActive) "ACTIVE (24h)" else "REVOKED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) SoftEmeraldDark else ErrorRose,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Authorized Data Categories:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkSlate
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            con.categories.forEach { cat ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = LightBlueContainer
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 10.sp,
                                        color = LightBlueHeader,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Granted: ${con.grantedAt}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            OutlinedButton(
                                onClick = {
                                    if (isActive) {
                                        pendingRevokeConsent = con
                                        hasConfirmedDeclaration = false
                                    } else {
                                        onToggleConsent(con.id)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (isActive) ErrorRose else SoftEmeraldDark
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isActive) ErrorRose else SoftEmeraldAccent
                                ),
                                modifier = Modifier.testTag(if (isActive) "revoke_access_btn_${con.id}" else "grant_access_btn_${con.id}")
                            ) {
                                Text(
                                    text = if (isActive) "Revoke Access" else "Re-Grant Consent",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

        // Access Audit Log Section
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Immutable Data Access Audit Log",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = LightBlueHeader
            )
        }

        items(auditLogs) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = WarmWhiteSubtle),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = log.action,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftEmeraldDark
                            )
                            Text(
                                text = " • ${log.actorName} (${log.actorRole})",
                                fontSize = 11.sp,
                                color = TextDarkSlate
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = log.details, fontSize = 11.sp, color = TextMuted)
                    }
                    Text(text = log.timestamp, fontSize = 10.sp, color = TextLightSlate)
                }
            }
        }
    }

    // Revoke Access Declaration Dialog with Mandatory Checkbox
    pendingRevokeConsent?.let { consent ->
        Dialog(
            onDismissRequest = {
                pendingRevokeConsent = null
                hasConfirmedDeclaration = false
            },
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
                        .widthIn(max = 520.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .testTag("revoke_declaration_dialog"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(ErrorContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.SecurityUpdateWarning,
                                    contentDescription = null,
                                    tint = ErrorRose,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Revoke Data Access",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightBlueHeader
                                )
                                Text(
                                    text = "ABDM Patient Privacy Declaration",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF1F2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Healthcare Provider:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFBE123C)
                                )
                                Text(
                                    text = "${consent.requesterName} (${consent.requesterRole})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkSlate
                                )
                                Text(
                                    text = "Facility: ${consent.facilityName}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Legal & Clinical Declaration Notice:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkSlate
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "By submitting this revocation, this doctor and facility will immediately lose all authorization to view your electronic health records, past prescriptions, biometric vitals, and diagnostic summaries. Any ongoing prescription dispensing or clinical review will terminate.",
                            fontSize = 12.sp,
                            color = TextDarkSlate,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Required Checkbox
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = LightBlueContainer.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (hasConfirmedDeclaration) SoftEmeraldAccent else SurfaceBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { hasConfirmedDeclaration = !hasConfirmedDeclaration }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = hasConfirmedDeclaration,
                                    onCheckedChange = { hasConfirmedDeclaration = it },
                                    modifier = Modifier.testTag("revoke_declaration_checkbox"),
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = SoftEmeraldAccent,
                                        uncheckedColor = TextMuted
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "I declare and confirm that I want to immediately revoke all data access permissions for this healthcare provider.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDarkSlate,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    pendingRevokeConsent = null
                                    hasConfirmedDeclaration = false
                                },
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel", color = TextMuted)
                            }

                            Button(
                                onClick = {
                                    onToggleConsent(consent.id)
                                    pendingRevokeConsent = null
                                    hasConfirmedDeclaration = false
                                },
                                enabled = hasConfirmedDeclaration,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("submit_revoke_access_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ErrorRose,
                                    disabledContainerColor = ErrorRose.copy(alpha = 0.4f)
                                )
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Submit Revoke", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
}
