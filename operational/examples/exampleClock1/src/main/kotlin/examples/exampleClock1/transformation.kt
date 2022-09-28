package examples.exampleClock1

import common.Point
import common.SvgCircle
import common.SvgGroup
import common.SvgLine
import common.SvgSvg
import common.SvgTranslate
import common.Transform
import io.github.oxidefrp.oxide.core.Cell
import io.github.oxidefrp.oxide.core.Signal
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.PI
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private const val clockRadius = 100.0
private const val padding = 16.0

private const val width = clockRadius * 2 + padding
private const val height = width

private const val hourHandLength = 40.0
private const val minuteHandLength = 75.0
private const val secondHandLength = 80.0
private const val secondHandNegativeLength = 15.0

data class ClockProgress(
    val hourHandProgress: Double,
    val minuteHandProgress: Double,
    val secondHandProgress: Double,
) {
    companion object {
        fun of(
            timeZone: TimeZone,
            instant: Instant,
        ): ClockProgress {
            val date = instant.toLocalDateTime(timeZone).date
            val midnight = date.atTime(hour = 0, minute = 0).toInstant(timeZone)

            val dayDuration = instant - midnight
            val hourDuration = dayDuration - dayDuration.inWholeHours.hours
            val minuteDuration = hourDuration - hourDuration.inWholeMinutes.minutes

            val dayDurationReminder = when {
                dayDuration > 12.hours -> dayDuration - 12.hours
                else -> dayDuration
            }

            return ClockProgress(
                hourHandProgress = dayDurationReminder / 12.hours,
                minuteHandProgress = hourDuration / 1.hours,
                secondHandProgress = minuteDuration / 1.minutes,
            )
        }
    }
}

fun buildMarker(
    angleRad: Double,
    bold: Boolean,
): SvgLine = SvgLine(
    p1 = Point(0.0, clockRadius - 8.0),
    p2 = Point(0.0, clockRadius),
    transform = Signal.constant(
        Transform.rotateOfAngle(angleRad),
    ),
    strokeWidth = if (bold) 4.0 else 1.0,
)

fun buildClock(
    timeZone: TimeZone,
    now: Signal<Instant>,
): SvgSvg {
    val clockProgress = now.map {
        ClockProgress.of(
            timeZone = timeZone,
            instant = it,
        )
    }

    val markerCount = 60

    val markers = (0 until markerCount).map {
        buildMarker(
            angleRad = it.toDouble() / markerCount * (2 * PI),
            bold = it % 5 == 0,
        )
    }

    val shield = SvgGroup(
        children = listOf(
            SvgCircle(
                c = Cell.constant(Point(0.0, 0.0)),
                r = clockRadius,
                fill = "white",
                stroke = "black",
                strokeWidth = 8.0,
            ),
        ) + markers
    )

    val hourHand = SvgLine(
        p1 = Point(0.0, 0.0),
        p2 = Point(0.0, -hourHandLength),
        transform = clockProgress.map {
            Transform.rotateOfAngle(it.hourHandProgress * (2 * PI))
        },
        fill = "none",
        stroke = "black",
        strokeWidth = 4.0,
    )

    val minuteHand = SvgLine(
        p1 = Point(0.0, 0.0),
        p2 = Point(0.0, -minuteHandLength),
        transform = clockProgress.map {
            Transform.rotateOfAngle(it.minuteHandProgress * (2 * PI))
        },
        fill = "none",
        stroke = "black",
        strokeWidth = 4.0,
    )

    val secondHand = SvgLine(
        p1 = Point(0.0, secondHandNegativeLength),
        p2 = Point(0.0, -secondHandLength),
        transform = clockProgress.map {
            Transform.rotateOfAngle(it.secondHandProgress * (2 * PI))
        },
        fill = "none",
        stroke = "black",
        strokeWidth = 2.0,
    )

    return SvgSvg(
        width = width,
        height = height,
        children = listOf(
            SvgGroup(
                transform = SvgTranslate(
                    tx = width / 2,
                    ty = height / 2,
                ),
                children = listOf(
                    shield,
                    hourHand,
                    minuteHand,
                    secondHand,
                ),
            ),
        ),
    )
}
