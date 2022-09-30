import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Io
import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.Signal
import io.github.oxidefrp.core.impl.event_stream.ExternalSubscription
import io.github.oxidefrp.core.mapNested
import io.github.oxidefrp.core.samplePerformOf
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.css.CSSStyleDeclaration
import org.w3c.dom.events.Event

abstract class HtmlGenericWidget<W : HtmlWidgetInstance> {
    companion object {
        fun embed(parent: Element, widget: HtmlGenericWidget<*>) {
            parent.appendChild(
                widget.buildFinalElementExternally().sampleExternally().performExternally(),
            )
        }
    }

    abstract fun buildFinalWidgetExternally(): Signal<Io<HtmlFinalWidget<W>>>

//    fun <W2 : HtmlWidgetInstance> flatMap(transform: (W) -> HtmlGenericWidget<W>) {
//
//
//    }
}

fun <W : HtmlWidgetInstance> HtmlGenericWidget<W>.buildFinalInstanceExternally(): Signal<Io<W>> =
    buildFinalWidgetExternally().samplePerformOf {
        Signal.constant(it.buildInstanceExternally())
    }

fun <W : HtmlWidgetInstance> HtmlGenericWidget<W>.buildFinalElementExternally(): Signal<Io<Element>> =
    buildFinalInstanceExternally().mapNested { it.element }

abstract class HtmlBuildContext<A> {

    companion object {
        fun <A> build(buildContext: HtmlBuildContext<HtmlBuildContext<A>>): HtmlBuildContext<A> =
            object : HtmlBuildContext<A>() {
                override fun buildDirectly(): Signal<Io<A>> = buildContext.buildDirectly().samplePerformOf {
                    it.buildDirectly()
                }
            }

        fun <W : HtmlWidgetInstance> construct(widget: HtmlGenericWidget<W>): HtmlBuildContext<W> =
            object : HtmlBuildContext<W>() {
                override fun buildDirectly(): Signal<Io<W>> = widget.buildFinalInstanceExternally()
            }

        fun <A> sample(signal: Signal<A>): HtmlBuildContext<A> =
            object : HtmlBuildContext<A>() {
                override fun buildDirectly(): Signal<Io<A>> =
                    signal.map(Io.Companion::pure)
            }

        fun <A> pull(signal: Moment<A>): HtmlBuildContext<A> =
            TODO()
    }

    abstract fun buildDirectly(): Signal<Io<A>>

    fun <B> map(transform: (A) -> B): HtmlBuildContext<B> =
        object : HtmlBuildContext<B>() {
            override fun buildDirectly(): Signal<Io<B>> =
                this@HtmlBuildContext.buildDirectly().mapNested(transform)
        }

    fun <B> buildOf(transform: (A) -> HtmlBuildContext<B>): HtmlBuildContext<B> =
        build(map(transform))
}

typealias HtmlWidget = HtmlGenericWidget<HtmlWidgetInstance>

abstract class HtmlWidgetInstance {
    abstract val element: Element

    val onClick: EventStream<Unit>
        get() = TODO()
}

abstract class HtmlShadowWidget<W : HtmlWidgetInstance> : HtmlGenericWidget<W>() {
    abstract fun build(): HtmlBuildContext<HtmlGenericWidget<W>>

    override fun buildFinalWidgetExternally(): Signal<Io<HtmlFinalWidget<W>>> =
        build().buildDirectly().samplePerformOf {
            it.buildFinalWidgetExternally()
        }
}

abstract class HtmlFinalWidget<W : HtmlWidgetInstance> : HtmlGenericWidget<W>() {
    override fun buildFinalWidgetExternally(): Signal<Io<HtmlFinalWidget<W>>> =
        Signal.constant(Io.pure(this))

    abstract fun buildInstanceExternally(): Io<W>
}

private fun <E : HTMLElement> createHtmlElement(localName: String): E =
    document.createElement(localName).unsafeCast<E>()

data class TextStyle(
    val fontStyle: FontStyle = FontStyle.normal,
    val fontWeight: FontWeight = FontWeight.normal,
) {
    enum class FontStyle {
        normal,
        italic,
    }

    enum class FontWeight {
        normal,
        bold,
    }

    fun applyTo(decl: CSSStyleDeclaration) {
        decl.fontStyle = fontStyle.toString()
        decl.fontWeight = fontWeight.toString()
    }
}

data class Text(
    val style: TextStyle? = null,
    val text: Cell<String>,
) : HtmlFinalWidget<HtmlWidgetInstance>() {
    override fun buildInstanceExternally(): Io<HtmlWidgetInstance> =
        object : Io<HtmlWidgetInstance>() {
            override fun performExternally(): HtmlWidgetInstance = object : HtmlWidgetInstance() {
                override val element: Element = createHtmlElement<HTMLDivElement>("div").apply {
                    this@Text.style?.applyTo(style)

                    var node = document.createTextNode(text.value.sampleExternally())

                    appendChild(node)

                    text.reactExternallyIndefinitely {
                        removeChild(node)

                        val newNode = document.createTextNode(it)

                        node = newNode

                        this.appendChild(newNode)
                    }
                }
            }
        }
}

data class BorderStyle(
    val style: Style,
    val width: Double,
    val color: String,
) {
    enum class Style {
        none,
        solid,
    }

    fun applyTo(decl: CSSStyleDeclaration) {
        decl.borderStyle = style.toString()
        decl.borderWidth = "${width}px"
        decl.borderColor = color
    }
}

data class Column(
    val borderStyle: BorderStyle? = null,
    val children: List<HtmlGenericWidget<*>>,
) : HtmlFinalWidget<HtmlWidgetInstance>() {
    override fun buildInstanceExternally(): Io<HtmlWidgetInstance> =
        object : Io<HtmlWidgetInstance>() {
            override fun performExternally(): HtmlWidgetInstance = object : HtmlWidgetInstance() {
                override val element: Element = createHtmlElement<HTMLDivElement>("div").apply {
                    style.display = "flex"
                    style.flexDirection = "column"
                    style.alignItems = "stretch"

                    borderStyle?.applyTo(style)

                    this@Column.children.forEach {
                        // FIXME: Transactions
                        appendChild(it.buildFinalElementExternally().sampleExternally().performExternally())
                    }
                }
            }
        }
}

data class GrowableScrollView(
    val height: Double,
    val width: Double,
    val addChild: EventStream<HtmlGenericWidget<*>>,
) : HtmlFinalWidget<HtmlWidgetInstance>() {
    override fun buildInstanceExternally(): Io<HtmlWidgetInstance> =
        object : Io<HtmlWidgetInstance>() {
            override fun performExternally(): HtmlWidgetInstance = object : HtmlWidgetInstance() {
                override val element: Element = createHtmlElement<HTMLDivElement>("div").apply {
                    style.height = "${height}px"
                    style.width = "${width}px"

                    style.overflowX = "hidden"
                    style.overflowY = "scroll"

                    style.display = "flex"
                    style.flexDirection = "column"

                    addChild.subscribeExternallyIndefinitely {
                        // FIXME: Transactions
                        appendChild(it.buildFinalElementExternally().sampleExternally().performExternally())

                        scrollTop = (scrollHeight - clientHeight).toDouble()
                    }
                }
            }
        }
}

//data class ScrollView(
//    val height: Double,
//    val child: HtmlGenericWidget<*>,
//) : HtmlFinalWidget<HtmlWidgetInstance>() {
//    override fun buildInstanceExternally(): Element =
//        createHtmlElement<HTMLDivElement>("div").apply {
//            style.height = "${height}px"
//            style.overflowY = "scroll"
//
//            appendChild(child.buildFinalInstanceExternally())
//        }
//}

data class Row(
    val borderStyle: BorderStyle? = null,
    val padding: Double,
    val gap: Double,
    val children: List<HtmlGenericWidget<*>>,
) : HtmlFinalWidget<HtmlWidgetInstance>() {
    override fun buildInstanceExternally(): Io<HtmlWidgetInstance> =
        object : Io<HtmlWidgetInstance>() {
            override fun performExternally(): HtmlWidgetInstance = object : HtmlWidgetInstance() {
                override val element: Element = createHtmlElement<HTMLDivElement>("div").apply {
                    style.display = "flex"
                    style.flexDirection = "row"
                    style.alignItems = "center"
                    style.justifyContent = "center"
                    style.padding = "${padding}px"
                    style.setProperty("gap", "${gap}px")

                    borderStyle?.applyTo(style)

                    this@Row.children.forEach {
                        // FIXME: Transactions
                        appendChild(it.buildFinalElementExternally().sampleExternally().performExternally())
                    }
                }
            }
        }
}

class ButtonInstance(
    override val element: HTMLButtonElement,
) : HtmlWidgetInstance() {
    val onPressed: EventStream<Event> = EventStream.source { emit ->
        element.addEventListener("click", emit)

        object : ExternalSubscription {
            override fun cancel() {
                element.removeEventListener("click", emit)
            }
        }
    }
}

data class Button(
    val text: String,
) : HtmlFinalWidget<ButtonInstance>() {
    override fun buildInstanceExternally(): Io<ButtonInstance> =
        object : Io<ButtonInstance>() {
            override fun performExternally() = ButtonInstance(
                element = createHtmlElement<HTMLButtonElement>("button").apply {
                    appendChild(
                        document.createTextNode(text),
                    )
                },
            )
        }
}
