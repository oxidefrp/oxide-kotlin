import io.github.oxidefrp.oxide.core.EventStream

data class Output(
    val outputStream: EventStream<String>,
)

fun transform(
    inputStream: EventStream<Int>,
): Output =
    Output(
        outputStream = inputStream.map { "#${it * 2}" }
    )
