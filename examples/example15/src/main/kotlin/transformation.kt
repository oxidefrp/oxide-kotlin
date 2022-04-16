import io.github.oxidefrp.oxide.core.Cell
import io.github.oxidefrp.oxide.core.EventStream
import io.github.oxidefrp.oxide.core.Signal
import io.github.oxidefrp.oxide.core.hold

data class Output(
    val outputCell: Signal<Cell<Int>>,
)

fun transform(
    inputStream: EventStream<Int>,
): Output =
    Output(
        outputCell = inputStream.hold(-1),
    )
