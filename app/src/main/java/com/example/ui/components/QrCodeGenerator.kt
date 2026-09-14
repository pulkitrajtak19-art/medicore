package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.UserProfile
import com.example.ui.theme.LightBlueHeader
import com.example.ui.theme.SoftEmeraldAccent
import com.example.ui.theme.SurfaceBorder
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.json.JSONObject

object QrCodeGenerator {

    /**
     * Generates a structured JSON identity payload for the user,
     * storing their ABDM Identity, health metadata, emergency contacts,
     * and Supabase database connection reference.
     */
    fun buildIdentityPayload(user: UserProfile, vitalsSummary: String? = null): String {
        return JSONObject().apply {
            put("format", "ABDM-HEALTH-ID")
            put("version", "1.0")
            put("abhaId", user.abhaId)
            put("name", user.name)
            put("phone", user.phone)
            put("email", user.email)
            put("role", user.role.name)
            put("bloodGroup", user.bloodGroup)
            put("age", user.age)
            put("gender", user.gender)
            put("heightCm", user.heightCm)
            put("weightKg", user.weightKg)
            put("allergies", user.allergies)
            put("isEmailVerified", user.isEmailVerified)
            put("emergencyContact", user.emergencyContact)
            put("city", user.city)
            put("supabaseHost", "db.zeriogmzhbsqilcaewuc.supabase.co")
            put("supabaseDatabase", "postgres")
            put("supabasePort", 5432)
            put("supabaseUrl", "https://zeriogmzhbsqilcaewuc.supabase.co")
            if (!vitalsSummary.isNullOrBlank()) {
                put("latestVitals", vitalsSummary)
            }
            put("qrToken", user.qrToken)
            put("checksum", "SHA256:MEDICORE-" + Integer.toHexString((user.id + user.abhaId).hashCode()))
        }.toString()
    }

    /**
     * Extracts a patient identifier (ABHA ID, QR Token, or User ID) from any scanned QR text.
     */
    fun extractPatientIdentifier(scannedText: String): String {
        val trimmed = scannedText.trim()
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            try {
                val json = JSONObject(trimmed)
                val abha = json.optString("abhaId")
                if (abha.isNotBlank()) return abha
                val token = json.optString("qrToken")
                if (token.isNotBlank()) return token
                val id = json.optString("id")
                if (id.isNotBlank()) return id
            } catch (_: Exception) {}
        }
        if (trimmed.contains("abha=")) {
            val parts = trimmed.split("&")
            for (p in parts) {
                if (p.contains("abha=")) {
                    return p.substringAfter("abha=")
                }
            }
        }
        return trimmed
    }

    /**
     * Generates a real ZXing BitMatrix for any text content.
     */
    fun encodeQrMatrix(
        content: String,
        dimension: Int = 200,
        errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.M
    ): BitMatrix? {
        return try {
            val hints = HashMap<EncodeHintType, Any>()
            hints[EncodeHintType.ERROR_CORRECTION] = errorCorrectionLevel
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.MARGIN] = 1
            QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, dimension, dimension, hints)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Converts a ZXing BitMatrix into a standard Android Bitmap.
     */
    fun matrixToBitmap(matrix: BitMatrix): Bitmap {
        val width = matrix.width
        val height = matrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (matrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE)
            }
        }
        return bitmap
    }
}

/**
 * High-performance, pixel-crisp Jetpack Compose QR Code Component
 * rendered directly from a real ZXing BitMatrix with an optional center brand badge.
 */
@Composable
fun PerfectQrCode(
    content: String,
    modifier: Modifier = Modifier,
    sizeDp: Int = 180,
    darkColor: Color = Color(0xFF0F172A),
    lightColor: Color = Color.White,
    showCenterEmblem: Boolean = true
) {
    val bitMatrix = remember(content) {
        QrCodeGenerator.encodeQrMatrix(content, dimension = 120)
    }

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(lightColor)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        if (bitMatrix != null) {
            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height

            Canvas(modifier = Modifier.fillMaxSize()) {
                val moduleWidth = size.width / matrixWidth
                val moduleHeight = size.height / matrixHeight

                for (y in 0 until matrixHeight) {
                    for (x in 0 until matrixWidth) {
                        if (bitMatrix.get(x, y)) {
                            // Leave a small clearing in the center for the emblem if requested
                            val isCenterArea = showCenterEmblem &&
                                (x in (matrixWidth / 2 - 2)..(matrixWidth / 2 + 2)) &&
                                (y in (matrixHeight / 2 - 2)..(matrixHeight / 2 + 2))

                            if (!isCenterArea) {
                                drawRect(
                                    color = darkColor,
                                    topLeft = Offset(x * moduleWidth, y * moduleHeight),
                                    size = Size(moduleWidth * 1.02f, moduleHeight * 1.02f)
                                )
                            }
                        }
                    }
                }
            }

            if (showCenterEmblem) {
                Box(
                    modifier = Modifier
                        .size((sizeDp * 0.22f).dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, SoftEmeraldAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.HealthAndSafety,
                        contentDescription = "ABDM Verified",
                        tint = SoftEmeraldAccent,
                        modifier = Modifier.size((sizeDp * 0.14f).dp)
                    )
                }
            }
        }
    }
}
