package com.alexanderglueck.urlpusher.ui.home

import android.text.format.DateUtils
import java.time.Instant
import java.time.OffsetDateTime

internal fun formatRelativeTime(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    val millis = parseIsoToMillis(iso) ?: return null
    return DateUtils.getRelativeTimeSpanString(
        millis,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE,
    ).toString()
}

private fun parseIsoToMillis(iso: String): Long? {
    runCatching { return Instant.parse(iso).toEpochMilli() }
    runCatching { return OffsetDateTime.parse(iso).toInstant().toEpochMilli() }
    return null
}
