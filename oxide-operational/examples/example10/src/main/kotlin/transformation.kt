import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.Signal

data class Output(
    val outputSignal: Signal<Int>,
)

fun transform(
    inputCell: Cell<Int>,
): Output =
    Output(
        outputSignal = inputCell.value,
    )
