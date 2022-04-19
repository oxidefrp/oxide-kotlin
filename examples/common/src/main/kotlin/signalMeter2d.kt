import common.Point
import common.SvgCircle
import common.SvgGroup
import common.SvgLine
import common.SvgSvg
import common.SvgTranslate
import io.github.oxidefrp.oxide.core.EventStream
import io.github.oxidefrp.oxide.core.Signal

private const val meterWidth = 200.0

private const val meterMin = -meterWidth / 2
private const val meterMax = +meterWidth / 2

fun buildSignalMeter2d(
    signal: Signal<Point>,
    aRange: Double,
    ticks: EventStream<Unit>,
) = signal.discretize(ticks = ticks).map { discretizedSignal ->
    SvgSvg(
        width = meterWidth,
        height = meterWidth,
        children = listOf(
            SvgGroup(
                transform = SvgTranslate(
                    tx = meterWidth / 2,
                    ty = meterWidth / 2,
                ),
                children = listOf(
                    SvgLine(
                        p1 = Point(meterMin, 0.0),
                        p2 = Point(meterMax, 0.0),
                    ),
                    SvgLine(
                        p1 = Point(0.0, meterMin),
                        p2 = Point(0.0, meterMax),
                    ),
                    SvgCircle(
                        c = discretizedSignal.map { p ->
                            Point(
                                projectBetweenRanges(
                                    p.x,
                                    sourceRange = -aRange..aRange,
                                    targetRange = meterMin..meterMax,
                                ),
                                projectBetweenRanges(
                                    p.y,
                                    sourceRange = -aRange..aRange,
                                    targetRange = meterMin..meterMax,
                                ),
                            )
                        },
                        r = 8.0,
                        fill = "grey",
                        stroke = "black",
                    ),
                ),
            ),
        ),
    )
}

private fun projectBetweenRanges(
    a: Double,
    sourceRange: ClosedFloatingPointRange<Double>,
    targetRange: ClosedFloatingPointRange<Double>,
): Double {
    val aRelative = (a - sourceRange.start) / (sourceRange.endInclusive - sourceRange.start)
    return targetRange.start + aRelative * (targetRange.endInclusive - targetRange.start)
}
