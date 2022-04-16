import io.github.oxidefrp.oxide.core.Cell
import io.github.oxidefrp.oxide.core.Signal

data class Output(
    val outputSignal: Signal<Int>,
)

fun transform(
    inputCell: Cell<Int>,
): Output =
    Output(
        outputSignal = inputCell.value,
    )
