import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.hold

data class Output(
    val outputCell: Moment<Cell<Int>>,
)

fun transform(
    inputStream: EventStream<Int>,
): Output =
    Output(
        outputCell = inputStream.hold(-1),
    )
