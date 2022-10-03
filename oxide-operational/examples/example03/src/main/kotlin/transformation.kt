import io.github.oxidefrp.core.Signal

data class Output(
    val signal: Signal<Double>,
)

fun transform(
    signal: Signal<Double>,
): Output =
    Output(
        signal = signal.map { it * 2 },
    )
