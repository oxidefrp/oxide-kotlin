import io.github.oxidefrp.core.Moment
import kotlinx.browser.document
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val ball1PeriodMs = 6000
private const val ball2PeriodMs = 8000
private const val givenBallPeriodMs = 8000

fun main() {
    val now = performanceNow()

    val ball1Position = now.map { t ->
        sin((2 * PI * t) / ball1PeriodMs)
    }

    val ball1 = Ball(
        name = "ball1",
        position = ball1Position,
    )

    val ball2Position = now.map { t ->
        cos((2 * PI * t) / ball2PeriodMs)
    }

    val ball2 = Ball(
        name = "ball2",
        position = ball2Position,
    )

    val givenBall = now.map { t ->
        val tp = t % givenBallPeriodMs
        if (tp < givenBallPeriodMs / 2) ball1 else ball2
    }

    val output = transform(
        givenBall = givenBall,
    )

    val givenBallPosition = output.givenBallPosition

    val ticks = animationFrameStream()

    val aMin = -1.125
    val aMax = 1.125

    val widget = Moment.map4(
        buildSignalMeter(
            signal = ball1Position,
            aMin = aMin,
            aMax = aMax,
            ticks = ticks,
        ),
        buildSignalMeter(
            signal = ball2Position,
            aMin = aMin,
            aMax = aMax,
            ticks = ticks,
        ),
        buildSignalText(
            text = givenBall.map { it.name },
            ticks = ticks,
        ),
        buildSignalMeter(
            signal = givenBallPosition,
            aMin = aMin,
            aMax = aMax,
            ticks = ticks,
        ),
    ) { ball1Meter, ball2Meter, givenBallName, givenBallMeter ->
        Row(
            gap = 16.0,
            children = listOf(
                ball1Meter,
                ball2Meter,
                givenBallName,
                givenBallMeter,
            ),
            padding = 4.0,
        )
    }.pullExternally()

    HtmlGenericWidget.embed(
        parent = document.body!!,
        widget = widget,
    )
}
