import common.Point
import io.github.oxidefrp.oxide.core.Moment
import io.github.oxidefrp.oxide.core.Signal

data class Output(
    val point: Signal<Point>,
)

fun transform(
    xSignal: Signal<Double>,
    ySignal: Signal<Double>,
): Output =
    Output(
        point = Signal.map2(
            xSignal,
            ySignal
        ) { x, y ->
            Point(x, y)
        },
    )
