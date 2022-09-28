import common.Point
import common.SvgCircle
import common.SvgGroup
import common.SvgLine
import common.SvgSvg
import common.SvgTranslate
import io.github.oxidefrp.oxide.core.EventStream
import io.github.oxidefrp.oxide.core.Signal

private const val meterWidth = 40.0
private const val meterHeight = 200.0

private const val meterMin = -meterHeight / 2
private const val meterMax = +meterHeight / 2

fun buildSignalMeter(
    signal: Signal<Double>,
    aMin: Double,
    aMax: Double,
    ticks: EventStream<Unit>,
) = signal.discretize(ticks = ticks).map { discretizedSignal ->
    SvgSvg(
        width = meterWidth,
        height = meterHeight,
        children = listOf(
            SvgGroup(
                transform = SvgTranslate(
                    tx = meterWidth / 2,
                    ty = meterHeight / 2,
                ),
                children = listOf(
                    SvgLine(
                        p1 = Point(0.0, meterMin),
                        p2 = Point(0.0, meterMax),
                    ),
                    SvgCircle(
                        c = discretizedSignal.map { a ->
                            Point(
                                0.0,
                                projectBetweenRanges(
                                    a,
                                    sourceRange = aMin..aMax,
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
