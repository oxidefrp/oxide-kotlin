import io.github.oxidefrp.core.Signal

data class Ball(
    val name: String,
    val position: Signal<Double>,
)

data class Output(
    val givenBallPosition: Signal<Double>,
)

fun transform(
    givenBall: Signal<Ball>,
): Output =
    Output(
        givenBallPosition = givenBall.sampleOf { it.position },
    )
