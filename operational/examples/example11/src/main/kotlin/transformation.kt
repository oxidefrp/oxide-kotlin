import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.ValueChange

data class Output(
    val outputStream: EventStream<ValueChange<Int>>,
)

fun transform(
    inputCell: Cell<Int>,
): Output =
    Output(
        outputStream = inputCell.changes,
    )
