import io.github.oxidefrp.oxide.core.Cell
import io.github.oxidefrp.oxide.core.EventStream
import io.github.oxidefrp.oxide.core.ValueChange

data class Output(
    val outputStream: EventStream<ValueChange<Int>>,
)

fun transform(
    inputCell: Cell<Int>,
): Output =
    Output(
        outputStream = inputCell.changes,
    )
