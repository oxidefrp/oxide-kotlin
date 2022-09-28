package examples.exampleSignal

import common.CanvasProps
import common.Example
import common.FunctionWindow
import common.Plot
import common.PlotProps
import common.buildSignalPlot
import common.now
import kotlin.math.PI

private fun buildExample(): Example {
    val output = transform(
        now = now,
    )

    val signal = output.signal

    val signalPlot = buildSignalPlot(
        signal = signal,
        props = PlotProps(
            canvas = CanvasProps(
                width = 400.0,
                height = 300.0,
            ),
            window = FunctionWindow(
                timeMax = 2 * PI,
                codomainMax = 3.0,
            ),
        ),
    )

    return Example(
        name = "exampleSignal",
        plots = listOf(
            Plot(
                name = "signal",
                graphic = signalPlot,
            ),
        ),
    )
}

val example = buildExample()
