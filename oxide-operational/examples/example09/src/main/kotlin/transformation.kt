import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Signal

data class Output(
    val outputCell: Cell<String>,
)

fun transform(
    inputCell: Cell<Int>,
): Output =
    Output(
        outputCell = inputCell.map { "#${it * 2}" }
    )
