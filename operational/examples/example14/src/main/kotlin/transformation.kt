import io.github.oxidefrp.oxide.core.Cell
import io.github.oxidefrp.oxide.core.EventStream

data class Output(
    val outputStream: EventStream<Int>,
)

fun transform(
    inputCell: Cell<EventStream<Int>>,
): Output =
    Output(
        outputStream = Cell.divert(inputCell),
    )
