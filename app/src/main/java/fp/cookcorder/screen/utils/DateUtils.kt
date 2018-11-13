package fp.cookcorder.screen.utils

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit

fun getTimeFromEpoch(epoch: Long) =
        Instant.ofEpochMilli(epoch)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm"))

fun getDateTimeFromEpoch(epoch: Long) =
        Instant.ofEpochMilli(epoch)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd-MM-YYYY HH:mm"))


fun calculateTimeDifference(timeToCompare: Long): Triple<Long, Long, Long> {
    val time1 = Instant.ofEpochMilli(timeToCompare)
    val time2 = Instant.now()
    return Triple(ChronoUnit.HOURS.between(time2, time1),
            ChronoUnit.MINUTES.between(time2, time1) % 60,
            ChronoUnit.SECONDS.between(time2, time1) % 60)
}