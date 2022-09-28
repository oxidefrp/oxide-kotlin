import io.github.oxidefrp.oxide.core.Cell
import io.github.oxidefrp.oxide.core.EventStream
import io.github.oxidefrp.oxide.core.Signal

data class Output(
    val outputCell: Cell<String>,
)

fun transform(
    inputCell: Cell<Int>,
): Output =
    Output(
        outputCell = inputCell.map { "#${it * 2}" }
    )
