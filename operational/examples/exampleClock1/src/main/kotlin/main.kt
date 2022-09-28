import examples.exampleClock1.buildClock
import io.github.oxidefrp.oxide.core.Signal
import io.github.oxidefrp.oxide.core.hold
import kotlinx.browser.document
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

val timeZone: TimeZone = TimeZone.currentSystemDefault()

val now: Signal<Instant> = Signal.source {
    Clock.System.now()
}

class MainWidget : HtmlShadowWidget<HtmlWidgetInstance>() {
    override fun build(): HtmlBuildContext<HtmlWidget> =
        HtmlBuildContext.construct(
            widget = Button(text = "Foo"),
        ).buildOf { button ->
            HtmlBuildContext.pull(
                signal = button.onPressed.map { 1 }.hold(-1),
            ).map { intCell ->
                Column(
                    children = listOf(
                        Text(
                            text = intCell.map { "#$it" },
                        )
                    ),
                )
            }
        }
}

/*

class MyWidget {
    override fun build() = do html.build {
        val button1 = ~html.construct(Button(...))

        val button2 = ~html.construct(Button(...))

        val state = ~html.sample(button.onPressed.hold(-1))

        return Column(
            children = listOf(
                button1,
                button2,
                state,
            )
        )
    }
}


 */

fun main() {
    val widget = Row(
        gap = 4.0,
        padding = 4.0,
        children = listOf(
            buildClock(
                timeZone = timeZone,
                now = now,
            ),
        ),
    )

    document.body!!.appendChild(
        widget.buildFinalElementExternally().sampleExternally().performExternally(),
    )
}
