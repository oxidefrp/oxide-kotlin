import io.github.oxidefrp.oxide.core.Cell

data class Output(
    val outputCell: Cell<Int>,
)

fun transform(
    inputCell: Cell<Cell<Int>>,
): Output =
    Output(
        outputCell = Cell.switch(inputCell),
    )
