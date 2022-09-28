package common

import io.github.oxidefrp.semantics.ClosedTimeEndpoint
import io.github.oxidefrp.semantics.EventStream
import io.github.oxidefrp.semantics.Time

class EventStreamTimeFunction(
    private val eventStream: EventStream<Double>,
) : TimeFunction {
    override fun buildCurve(
        props: PlotProps,
    ): List<SvgElement> = eventStream.occurrences
        .getOccurrencesUntil(
            ClosedTimeEndpoint(Time(t = props.window.timeMax))
        )
        .map { eventOccurrence ->
            val t = eventOccurrence.time.t
            val a = eventOccurrence.event

            SvgCircle(
                c = Point(props.mapT(t), -props.mapA(a)),
                r = 4.0,
                fill = "blue",
                stroke = "lightblue",
                strokeWidth = 1.0,
            )
        }
        .toList()
}

fun buildEventStreamPlot(
    eventStream: EventStream<Int>,
    props: PlotProps,
): SvgSvg = buildEventStreamPlotD(
    eventStream = eventStream.map { it.toDouble() },
    props = props,
)

fun buildEventStreamPlotD(
    eventStream: EventStream<Double>,
    props: PlotProps,
): SvgSvg = buildFunctionPlot(
    timeFunction = EventStreamTimeFunction(
        eventStream = eventStream,
    ),
    props = props,
)
