package examples.exampleEventStreamMap

import common.CanvasProps
import common.Example
import common.FunctionWindow
import common.Plot
import common.PlotProps
import common.buildEventStreamPlot
import io.github.oxidefrp.semantics.Signal
import io.github.oxidefrp.semantics.Time

private val canvasProps = CanvasProps(
    width = 400.0,
    height = 300.0,
)

private val functionWindow = FunctionWindow(
    timeMax = 4.0,
    codomainMax = 7.0,
)

private fun buildExample(): Example = Signal.map1(
    transform(),
) { output ->
    Example(
        name = "exampleEventStreamMap",
        plots = listOf(
            Plot(
                name = "inputStream",
                graphic = buildEventStreamPlot(
                    eventStream = output.inputStream,
                    props = PlotProps(
                        canvas = canvasProps,
                        window = functionWindow,
                    ),
                ),
            ),
            Plot(
                name = "mappedStream",
                graphic = buildEventStreamPlot(
                    eventStream = output.mappedStream,
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
