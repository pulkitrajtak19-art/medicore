package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhiteBackground)
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
                            onClick = { onToggleConsent(con.id) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isActive) ErrorRose else SoftEmeraldDark
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isActive) ErrorRose else SoftEmeraldAccent
                            )
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
}
