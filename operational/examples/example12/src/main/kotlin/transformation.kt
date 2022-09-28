import io.github.oxidefrp.core.Cell

data class Output(
    val outputCell: Cell<String>,
)

fun transform(
    inputCell1: Cell<Int>,
    inputCell2: Cell<Int>,
): Output =
    Output(
        outputCell = Cell.map2(inputCell1, inputCell2) { a, b ->
            "$a $ $b"
        },
    )
