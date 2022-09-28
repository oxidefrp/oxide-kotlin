package examples.exampleSignalSample

import common.CanvasProps
import common.Example
import common.FunctionWindow
import common.Plot
import common.PlotProps
import common.buildSignalPlot
import common.now
import kotlin.math.PI

private val canvasProps = CanvasProps(
    width = 400.0,
    height = 300.0,
)

private val functionWindow = FunctionWindow(
    timeMax = 4 * PI,
    codomainMax = 3.0,
)

private fun buildExample(): Example {
    val output = transform(
        now = now,
    )

    val innerSignal1Plot = buildSignalPlot(
        signal = output.innerSignal1,
        props = PlotProps(
            canvas = canvasProps,
            window = functionWindow,
        ),
    )

    val innerSignal2Plot = buildSignalPlot(
        signal = output.innerSignal2,
        props = PlotProps(
            canvas = canvasProps,
            window = functionWindow,
        ),
    )

    val sampledSignalPlot = buildSignalPlot(
        signal = output.sampledSignal,
        props = PlotProps(
            canvas = canvasProps,
            window = functionWindow,
        ),
    )

    return Example(
        name = "exampleSignalSample",
        plots = listOf(
            Plot(
                name = "innerSignal1",
                graphic = innerSignal1Plot,
            ),
            Plot(
                name = "innerSignal2",
                graphic = innerSignal2Plot,
            ),
            Plot(
                name = "sampledSignal",
                graphic = sampledSignalPlot,
            ),
        ),
    )
}

val example = buildExample()
