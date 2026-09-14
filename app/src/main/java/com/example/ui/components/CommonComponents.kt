package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.*

@Composable
fun QrCodeView(
    dataToken: String,
    modifier: Modifier = Modifier,
    sizeDp: Int = 140
) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellCount = 13
            val cellSize = size.width / cellCount
            val seed = dataToken.hashCode()

            for (r in 0 until cellCount) {
                for (c in 0 until cellCount) {
                    val isCornerTopLeft = (r < 4 && c < 4)
                    val isCornerTopRight = (r < 4 && c >= cellCount - 4)
                    val isCornerBottomLeft = (r >= cellCount - 4 && c < 4)

                    val isFinderPattern = (isCornerTopLeft && (r == 0 || r == 3 || c == 0 || c == 3 || (r in 1..2 && c in 1..2))) ||
                            (isCornerTopRight && (r == 0 || r == 3 || c == cellCount - 4 || c == cellCount - 1 || (r in 1..2 && c in cellCount - 3 until cellCount - 1))) ||
                            (isCornerBottomLeft && (r == cellCount - 4 || r == cellCount - 1 || c == 0 || c == 3 || (r in cellCount - 3 until cellCount - 1 && c in 1..2)))

                    val pseudoRandom = ((seed * (r + 1) * 31 + c * 17) % 7) == 0 || ((r + c) % 3 == 0)

                    if (isFinderPattern || (!isCornerTopLeft && !isCornerTopRight && !isCornerBottomLeft && pseudoRandom)) {
                        drawRect(
                            color = if (isFinderPattern) LightBlueHeader else Color(0xFF1E293B),
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize * 0.92f, cellSize * 0.92f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HealthIdCard(
    user: UserProfile,
    onShowFullQr: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightBlueContainer)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(SoftEmeraldAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NATIONAL DIGITAL HEALTH ID",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightBlueHeader,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SoftEmeraldLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder)
                    ) {
                        Text(
                            text = "ABDM VERIFIED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftEmeraldDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Body
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkSlate
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.abhaId,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LightBluePrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InfoBadge(label = "Age", value = "${user.age} Yrs")
                        InfoBadge(label = "Gender", value = user.gender)
                        InfoBadge(label = "Blood", value = user.bloodGroup)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Phone: ${user.phone}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onShowFullQr() }
                ) {
                    QrCodeView(dataToken = user.qrToken, sizeDp = 78)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap to Expand",
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun InfoBadge(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = WarmWhiteSubtle,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
    ) {
        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)) {
            Text(text = "$label: ", fontSize = 10.sp, color = TextMuted)
            Text(text = value, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextDarkSlate)
        }
    }
}

@Composable
fun SegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(WarmWhiteSubtle)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEachIndexed { index, title ->
            val isSelected = index == selectedIndex
            val bgColor by animateColorAsState(if (isSelected) Color.White else Color.Transparent)
            val textColor by animateColorAsState(if (isSelected) LightBlueHeader else TextMuted)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )
            }
        }
    }
}
