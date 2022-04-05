import io.github.oxidefrp.oxide.core.Signal
import kotlin.js.Date
import kotlin.math.sin

fun main() {
    val inputSignal = Signal.source {
        sin(Date.now())
    }

    val output = transform(
        signal = inputSignal,
    )

    val outputSignal = output.signal

    println("Hello, world! (${outputSignal.sampleExternally()})")
}
