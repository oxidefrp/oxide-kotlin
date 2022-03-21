import io.github.oxidefrp.oxide.Cell
import io.github.oxidefrp.oxide.EventEmitter
import io.github.oxidefrp.oxide.hold
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

@OptIn(DelicateCoroutinesApi::class, ExperimentalTime::class)
fun main() {
    GlobalScope.launch {
        val emitter = EventEmitter<Int>()

        var result: Cell<Int>? = emitter.hold(0)

        emitter.emitExternally(1)

        println("Sample: ${result!!.value.sampleExternally()}")

        delay(100.0.milliseconds)

        emitter.emitExternally(2)

        println("Sample: ${result!!.value.sampleExternally()}")

        delay(100.0.milliseconds)

        result = null

        println("RC: ${emitter.vertex.referenceCount}")

        while (true) {
            delay(100.0.milliseconds)

            emitter.emitExternally(-1)

            println("RC: ${emitter.vertex.referenceCount}")
        }
    }
}
