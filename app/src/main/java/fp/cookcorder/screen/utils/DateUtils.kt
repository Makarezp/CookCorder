package fp.cookcorder.screen.utils

import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit

fun getTimeFromEpoch(epoch: Long) =
        epoch.getTimeInstant().format(DateTimeFormatter.ofPattern("hh:mm a"))

fun getDateTimeFromEpoch(epoch: Long) =
        epoch.getTimeInstant().format(DateTimeFormatter.ofPattern("dd MMM hh:mm a"))

fun Long.getTimeInstant() = Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())


fun calculateTimeDifference(timeToCompare: Long): Triple<Long, Long, Long> {
    val time1 = Instant.ofEpochMilli(timeToCompare)
    val time2 = Instant.now()
    return Triple(ChronoUnit.HOURS.between(time2, time1),
            ChronoUnit.MINUTES.between(time2, time1) % 60,
            ChronoUnit.SECONDS.between(time2, time1) % 60)
}

fun LocalDate.isToday() = LocalDateTime.now().dayOfYear == this.dayOfYear
fun LocalDateTime.isToday() = LocalDateTime.now().dayOfYear == this.dayOfYear
fun ZonedDateTime.isToday() = LocalDateTime.now().dayOfYear == this.dayOfYear
