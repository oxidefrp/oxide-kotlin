package common

import io.github.oxidefrp.semantics.Signal
import io.github.oxidefrp.semantics.Time
import kotlin.math.floor

private const val polylinePointCount = 1000

class SignalTimeFunction(
    private val signal: Signal<Double>,
) : TimeFunction {
    override fun buildCurve(
        props: PlotProps,
    ): List<SvgElement> = listOf(
        SvgPolyline(
            points = (0..polylinePointCount).map { i ->
                val t = props.window.getTRelative(i.toDouble() / polylinePointCount)
                val a = signal.at(Time(t = t))

                Point(props.mapT(t), -props.mapA(a))
            },
            stroke = "red",
            strokeWidth = 2.0,
        ),
    )
}

fun buildSignalPlot(
    signal: Signal<Double>,
    props: PlotProps,
): SvgSvg = buildFunctionPlot(
    timeFunction = SignalTimeFunction(
        signal = signal,
    ),
    props = props,
)
