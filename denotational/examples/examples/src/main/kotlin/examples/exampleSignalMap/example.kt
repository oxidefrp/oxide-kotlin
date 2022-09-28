package examples.exampleSignalMap

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
    codomainMax = 4.0,
)

private fun buildExample(): Example {
    val output = transform(
        now = now,
    )

    val inputSignalPlot = buildSignalPlot(
        signal = output.inputSignal,
        props = PlotProps(
            canvas = canvasProps,
            window = functionWindow,
        ),
    )

    val mappedSignalPlot = buildSignalPlot(
        signal = output.mappedSignal,
        props = PlotProps(
            canvas = canvasProps,
            window = functionWindow,
        ),
    )

    return Example(
        name = "exampleSignalMap",
        plots = listOf(
            Plot(
                name = "inputSignal",
                graphic = inputSignalPlot,
            ),
            Plot(
                name = "mappedSignal",
                graphic = mappedSignalPlot,
            ),
        ),
    )
}

val example = buildExample()
