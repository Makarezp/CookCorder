package fp.cookcorder.screen.utils

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

fun getTimeFromEpoch(epoch: Long) =
        Instant.ofEpochMilli(epoch)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm"))

fun getDateTimeFromEpoch(epoch: Long) =
        Instant.ofEpochMilli(epoch)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd MMM HH:mm"))