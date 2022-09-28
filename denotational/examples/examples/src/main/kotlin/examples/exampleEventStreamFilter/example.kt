package examples.exampleEventStreamFilter

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
    timeMax = 12.0,
    codomainMax = 8.0,
)

private fun buildExample(): Example = Signal.map1(
    transform(),
) { output ->
    Example(
        name = "exampleEventStreamFilter",
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
                name = "filteredStream",
                graphic = buildEventStreamPlot(
                    eventStream = output.filteredStream,
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
