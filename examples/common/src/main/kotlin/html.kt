import io.github.oxidefrp.oxide.core.Cell
import io.github.oxidefrp.oxide.core.EventStream
import io.github.oxidefrp.oxide.core.Signal
import io.github.oxidefrp.oxide.core.hold
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.css.CSSStyleDeclaration

class BuildContext

abstract class HtmlWidget {
    abstract fun buildFinalWidgetExternally(): HtmlFinalWidget

    fun buildFinalElementExternally() =
        buildFinalWidgetExternally().buildElementExternally()
}

abstract class HtmlShadowWidget : HtmlWidget() {
    abstract fun build(): Signal<HtmlWidget>

    override fun buildFinalWidgetExternally(): HtmlFinalWidget =
        build().sampleExternally().buildFinalWidgetExternally()
}

abstract class HtmlFinalWidget : HtmlWidget() {
    override fun buildFinalWidgetExternally(): HtmlFinalWidget = this

    abstract fun buildElementExternally(): Element
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
) : HtmlFinalWidget() {
    override fun buildElementExternally(): Element =
        createHtmlElement<HTMLDivElement>("div").apply {
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
    val children: List<HtmlWidget>,
) : HtmlFinalWidget() {
    override fun buildElementExternally(): Element =
        createHtmlElement<HTMLDivElement>("div").apply {
            style.display = "flex"
            style.flexDirection = "column"
            style.alignItems = "stretch"

            borderStyle?.applyTo(style)

            this@Column.children.forEach {
                appendChild(it.buildFinalElementExternally())
            }
        }
}

data class GrowableScrollView(
    val height: Double,
    val width: Double,
    val addChild: EventStream<HtmlWidget>,
) : HtmlFinalWidget() {
    override fun buildElementExternally(): Element =
        createHtmlElement<HTMLDivElement>("div").apply {
            style.height = "${height}px"
            style.width = "${width}px"

            style.overflowX = "hidden"
            style.overflowY = "scroll"

            style.display = "flex"
            style.flexDirection = "column"

            addChild.subscribeIndefinitely {
                appendChild(it.buildFinalElementExternally())

                scrollTop = (scrollHeight - clientHeight).toDouble()
            }
        }
}

data class ScrollView(
    val height: Double,
    val child: HtmlWidget,
) : HtmlFinalWidget() {
    override fun buildElementExternally(): Element =
        createHtmlElement<HTMLDivElement>("div").apply {
            style.height = "${height}px"
            style.overflowY = "scroll"

            appendChild(child.buildFinalElementExternally())
        }
}

data class Row(
    val borderStyle: BorderStyle? = null,
    val padding: Double,
    val gap: Double,
    val children: List<HtmlWidget>,
) : HtmlFinalWidget() {
    override fun buildElementExternally(): Element =
        createHtmlElement<HTMLDivElement>("div").apply {
            style.display = "flex"
            style.flexDirection = "row"
            style.alignItems = "center"
            style.justifyContent = "center"
            style.padding = "${padding}px"
            style.setProperty("gap", "${gap}px")

            borderStyle?.applyTo(style)

            this@Row.children.forEach {
                appendChild(it.buildFinalElementExternally())
            }
        }
}
