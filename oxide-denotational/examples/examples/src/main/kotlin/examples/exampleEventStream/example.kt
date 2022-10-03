package examples.exampleEventStream

import common.CanvasProps
import common.Example
import common.FunctionWindow
import common.Plot
import common.PlotProps
import common.buildEventStreamPlot
import io.github.oxidefrp.core.Signal
import io.github.oxidefrp.core.Time

private val canvasProps = CanvasProps(
    width = 400.0,
    height = 300.0,
)

private val functionWindow = FunctionWindow(
    timeMax = 4.0,
    codomainMax = 4.0,
)

private fun buildExample(): Example = Signal.map1(
    transform(),
) { output ->
    Example(
        name = "exampleEventStream",
        plots = listOf(
            Plot(
                name = "eventStream",
                graphic = buildEventStreamPlot(
                    eventStream = output.eventStream,
                    props = PlotProps(
                        canvas = canvasProps,
                        window = functionWindow,
                    ),
                ),
            ),
        ),
    )
}.at(Time(t = 0.0))

val example = buildExample()
