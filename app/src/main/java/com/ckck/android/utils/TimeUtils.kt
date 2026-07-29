package com.ckck.android.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object TimeUtils {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun formatDepartureTime(
        actualIso: String?,
        scheduledIso: String?,
        tz: String?
    ): AnnotatedString {
        if (actualIso == null) return buildAnnotatedString { append("-") }

        val actual = try {
            OffsetDateTime.parse(actualIso)
        } catch (_: Exception) {
            return buildAnnotatedString { append(actualIso) }
        }

        val displayZone = tz?.let {
            runCatching { ZoneId.of(it) }.getOrNull()
        } ?: ZoneId.systemDefault()

        val timeStr = actual.atZoneSameInstant(displayZone).format(timeFormatter)

        return buildAnnotatedString {
            append(timeStr)

            if (scheduledIso != null && actualIso != scheduledIso) {
                val scheduled = try {
                    OffsetDateTime.parse(scheduledIso)
                } catch (_: Exception) {
                    null
                }

                if (scheduled != null) {
                    val delayMinutes = ChronoUnit.MINUTES.between(scheduled, actual)
                    if (delayMinutes != 0L) {
                        val delayText =
                            if (delayMinutes > 0) "+$delayMinutes" else delayMinutes.toString()
                        val delayColor = when {
                            delayMinutes > 5 -> Color.Red
                            delayMinutes > 0 -> Color(0xFFFFA500) // Orange (1 to 5)
                            delayMinutes >= -5 -> Color.Blue // -1 to -5
                            else -> Color.Yellow // < -5
                        }
                        withStyle(
                            style = SpanStyle(
                                baselineShift = BaselineShift.Superscript,
                                color = delayColor
                            )
                        ) {
                            append(delayText)
                        }
                    }
                }
            }
        }
    }
}
