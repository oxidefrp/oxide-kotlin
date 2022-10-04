import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.IoUtils
import io.github.oxidefrp.core.shared.Io
import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.RealWorld
import io.github.oxidefrp.core.Signal
import io.github.oxidefrp.core.executeExternally
import io.github.oxidefrp.core.impl.event_stream.ExternalSubscription
import io.github.oxidefrp.core.mapNested
import io.github.oxidefrp.core.shared.MomentIo
import io.github.oxidefrp.core.shared.enterOf
import io.github.oxidefrp.core.shared.map
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
                widget.buildFinalElementExternally().executeExternally()
            )
        }
    }

    abstract fun buildFinalWidgetExternally(): MomentIo<HtmlFinalWidget<W>>
}

fun <W : HtmlWidgetInstance> HtmlGenericWidget<W>.buildFinalInstanceExternally(): MomentIo<W> =
    buildFinalWidgetExternally().enterOf {
        it.buildInstanceExternally()
    }

fun <W : HtmlWidgetInstance> HtmlGenericWidget<W>.buildFinalElementExternally(): MomentIo<Element> =
    buildFinalInstanceExternally().map { it.element }

abstract class HtmlBuildContext<A> {
    companion object {
        fun <A> build(buildContext: HtmlBuildContext<HtmlBuildContext<A>>): HtmlBuildContext<A> =
            object : HtmlBuildContext<A>() {
                override fun buildDirectly(): MomentIo<A> = buildContext.buildDirectly().pullEnterOf {
                    it.buildDirectly()
                }
            }

        fun <W : HtmlWidgetInstance> construct(widget: HtmlGenericWidget<W>): HtmlBuildContext<W> =
            object : HtmlBuildContext<W>() {
                override fun buildDirectly(): MomentIo<W> = widget.buildFinalInstanceExternally()
            }

//        fun <A> sample(signal: Signal<A>): HtmlBuildContext<A> =
//            object : HtmlBuildContext<A>() {
//                override fun buildDirectly(): MomentIo<A> =
//                    signal.map(Io.pure)
//            }

        fun <A> pull(signal: Moment<A>): HtmlBuildContext<A> =
            TODO()
    }

    abstract fun buildDirectly(): MomentIo<A>

    fun <B> map(transform: (A) -> B): HtmlBuildContext<B> =
        object : HtmlBuildContext<B>() {
            override fun buildDirectly(): MomentIo<B> =
                this@HtmlBuildContext.buildDirectly().map(transform)
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

    override fun buildFinalWidgetExternally(): MomentIo<HtmlFinalWidget<W>> =
        build().buildDirectly().pullEnterOf {
            it.buildFinalWidgetExternally()
        }
}

abstract class HtmlFinalWidget<W : HtmlWidgetInstance> : HtmlGenericWidget<W>() {
    override fun buildFinalWidgetExternally(): MomentIo<HtmlFinalWidget<W>> =
        MomentIo.pure(this)

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
    override fun buildInstanceExternally(): Io<HtmlWidgetInstance> = IoUtils.wrap {
        object : HtmlWidgetInstance() {
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
    override fun buildInstanceExternally(): Io<HtmlWidgetInstance> = IoUtils.wrap {
        object : HtmlWidgetInstance() {
            override val element: Element = createHtmlElement<HTMLDivElement>("div").apply {
                style.display = "flex"
                style.flexDirection = "column"
                style.alignItems = "stretch"

                borderStyle?.applyTo(style)

                this@Column.children.forEach {
                    // FIXME: Transactions
                    appendChild(it.buildFinalElementExternally().executeExternally())
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
    override fun buildInstanceExternally(): Io<HtmlWidgetInstance> = IoUtils.wrap {
        object : HtmlWidgetInstance() {
            override val element: Element = createHtmlElement<HTMLDivElement>("div").apply {
                style.height = "${height}px"
                style.width = "${width}px"

                style.overflowX = "hidden"
                style.overflowY = "scroll"

                style.display = "flex"
                style.flexDirection = "column"

                addChild.subscribeExternallyIndefinitely {
                    // FIXME: Transactions
                    appendChild(it.buildFinalElementExternally().executeExternally())

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
    override fun buildInstanceExternally(): Io<HtmlWidgetInstance> = IoUtils.wrap {
        object : HtmlWidgetInstance() {
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
                    appendChild(it.buildFinalElementExternally().executeExternally())
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
    override fun buildInstanceExternally(): Io<ButtonInstance> = IoUtils.wrap {
        ButtonInstance(
            element = createHtmlElement<HTMLButtonElement>("button").apply {
                appendChild(
                    document.createTextNode(text),
                )
            },
        )
    }
}
