import io.github.oxidefrp.core.EventStream

data class Output(
    val outputStream: EventStream<Int>,
)

fun transform(
    inputStream: EventStream<Int>,
): Output =
    Output(
        outputStream = inputStream.filter(::isEven)
    )

private fun isEven(n: Int): Boolean =
    n % 2 == 0
