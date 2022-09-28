package examples.exampleEventStreamSample

import common.CanvasProps
import common.Example
import common.FunctionWindow
import common.Plot
import common.PlotProps
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
    codomainMax = 6.0,
)

private fun buildExample(): Example = Signal.map1(
    transform(
        now = now,
    ),
) { output ->
    Example(
        name = "exampleEventStreamSample",
        plots = listOf(
            Plot(
                name = "inputSignal1",
                graphic = buildSignalPlot(
                    signal = output.inputSignal1,
                    props = PlotProps(
                        canvas = canvasProps,
                        window = functionWindow,
                    ),
                ),
            ),
            Plot(
                name = "inputSignal2",
                graphic = buildSignalPlot(
                    signal = output.inputSignal2,
                    props = PlotProps(
                        canvas = canvasProps,
                        window = functionWindow,
                    ),
                ),
            ),
            Plot(
                name = "inputSignal3",
                graphic = buildSignalPlot(
                    signal = output.inputSignal3,
                    props = PlotProps(
                        canvas = canvasProps,
                        window = functionWindow,
                    ),
                ),
            ),
            Plot(
                name = "sampleStream",
                graphic = buildEventStreamPlotD(
                    eventStream = output.sampleStream,
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
