import common.buildConsecutiveIntStream
import io.github.oxidefrp.oxide.core.EventStream
import io.github.oxidefrp.oxide.core.Moment
import io.github.oxidefrp.oxide.core.Signal
import io.github.oxidefrp.oxide.core.hold
import io.github.oxidefrp.oxide.core.pullOf
import kotlinx.browser.document

fun main() {
    val ticks = animationFrameStream()

    val inputCell = buildConsecutiveIntStream(intervalS = 2.0).pullOf { eventStream ->
        eventStream.map { it % 8 }.hold(0)
    }.pullExternally()

    val output = transform(
        inputCell = inputCell,
    )

    val outputSignal = output.outputSignal

    val aMin = -1.0
    val aMax = 9.0

    val widget = Moment.map1(
        buildSignalMeter(
            signal = outputSignal.map { it.toDouble() },
            aMin = aMin,
            aMax = aMax,
            ticks = ticks,
        )
    ) { outputSignalMeter ->
        Row(
            gap = 16.0,
            children = listOf(
                buildCellMeter(inputCell),
                outputSignalMeter,
            ),
            padding = 4.0,
        )
    }.pullExternally()

    HtmlGenericWidget.embed(
        parent = document.body!!,
        widget = widget,
    )
}
