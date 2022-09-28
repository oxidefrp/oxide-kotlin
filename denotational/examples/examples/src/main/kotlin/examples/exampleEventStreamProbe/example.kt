package examples.exampleEventStreamProbe

import common.CanvasProps
import common.Example
import common.FunctionWindow
import common.Plot
import common.PlotProps
import common.buildEventStreamPlot
import common.buildEventStreamPlotD
import common.buildSignalPlot
import common.now
import io.github.oxidefrp.semantics.Signal
import io.github.oxidefrp.semantics.Time

private val canvasProps = CanvasProps(
    width = 400.0,
    height = 300.0,
)

private val functionWindow = FunctionWindow(
    timeMax = 12.0,
    codomainMax = 4.0,
)

private fun buildExample(): Example = Signal.map1(
    transform(
        now = now,
    ),
) { output ->
    Example(
        name = "exampleEventStreamProbe",
        plots = listOf(
            Plot(
                name = "inputStream",
                graphic = buildEventStreamPlotD(
                    eventStream = output.inputStream,
                    props = PlotProps(
                        canvas = canvasProps,
                        window = functionWindow,
                    ),
                ),
            ),
            Plot(
                name = "inputSignal",
                graphic = buildSignalPlot(
                    signal = output.inputSignal,
                    props = PlotProps(
                        canvas = canvasProps,
                        window = functionWindow,
                    ),
                ),
            ),
            Plot(
                name = "probedStream",
                graphic = buildEventStreamPlotD(
                    eventStream = output.probedStream,
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
