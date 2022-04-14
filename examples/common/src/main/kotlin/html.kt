import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

abstract class HtmlWidget {
    abstract fun buildElement(): Element
}

private fun <E : HTMLElement> createHtmlElement(localName: String): E =
    document.createElement(localName).unsafeCast<E>()

data class Column(
    val children: List<HtmlWidget>,
) : HtmlWidget() {
    override fun buildElement(): Element =
        createHtmlElement<HTMLDivElement>("div").apply {
            style.display = "flex"
            style.flexDirection = "column"
            style.alignItems = "center"

            this@Column.children.forEach { appendChild(it.buildElement()) }
        }
}

data class Row(
    val children: List<HtmlWidget>,
    val gap: Double,
) : HtmlWidget() {
    override fun buildElement(): Element =
        createHtmlElement<HTMLDivElement>("div").apply {
            style.display = "flex"
            style.flexDirection = "row"
            style.alignItems = "center"
            style.justifyContent = "center"
            style.setProperty("gap", "${gap}px")

            this@Row.children.forEach { appendChild(it.buildElement()) }
        }
}
