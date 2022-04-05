import io.github.oxidefrp.oxide.core.Cell
import kotlinx.browser.document
import org.w3c.dom.svg.SVGElement

data class Point(
    val x: Double,
    val y: Double,
) {
    fun toSvgString() = "$x,$y"
}

abstract class SvgTransform {
    abstract fun buildTransformString(): String
}

abstract class SvgTransformFunction : SvgTransform() {
    final override fun buildTransformString(): String =
        buildTransformFunctionString()

    abstract fun buildTransformFunctionString(): String
}

data class SvgCombinedTransform(
    val transforms: List<SvgTransformFunction>,
) : SvgTransform() {
    override fun buildTransformString(): String =
        transforms.joinToString(separator = " ") {
            it.buildTransformFunctionString()
        }
}

data class SvgTranslate(
    val tx: Double,
    val ty: Double,
) : SvgTransformFunction() {
    override fun buildTransformFunctionString(): String = "translate($tx, $ty)"
}

data class SvgScale(
    val sx: Double = 1.0,
    val sy: Double = 1.0,
) : SvgTransformFunction() {
    override fun buildTransformFunctionString(): String = "scale($sx, $sy)"
}

abstract class SvgWidget {
    abstract fun buildElement(): SVGElement
}

private fun createSvgElement(qualifiedName: String): SVGElement =
    document.createElementNS("http://www.w3.org/2000/svg", qualifiedName).unsafeCast<SVGElement>()

data class SvgSvg(
    val width: Double,
    val height: Double,
    val children: List<SvgWidget>,
) : HtmlWidget() {
    override fun buildElement(): SVGElement =
        createSvgElement("svg").apply {
//            setAttribute("xmlns", "http://www.w3.org/2000/svg")
            setAttribute("viewBox", "0 0 ${width.toString()} ${height.toString()}")
            setAttribute("width", width.toString())
            setAttribute("height", height.toString())

            this@SvgSvg.children.forEach {
                appendChild(it.buildElement())
            }
        }
}

data class SvgGroup(
    val transform: SvgTransform? = null,
    val children: List<SvgWidget>,
) : SvgWidget() {
    override fun buildElement(): SVGElement =
        createSvgElement("g").apply {

            transform?.let {
                setAttribute("transform", it.buildTransformString())
            }

            this@SvgGroup.children.forEach {
                appendChild(it.buildElement())
            }
        }
}

data class SvgLine(
    val a: Point,
    val b: Point,
    val fill: String = "none",
    val stroke: String = "black",
) : SvgWidget() {
    override fun buildElement(): SVGElement =
        createSvgElement("line").apply {
            setAttribute("x1", a.x.toString())
            setAttribute("y1", a.y.toString())
            setAttribute("x2", b.x.toString())
            setAttribute("y2", b.y.toString())

            setAttribute("fill", fill)
            setAttribute("stroke", stroke)
        }
}

data class SvgPolyline(
    val points: List<Point>,
    val fill: String = "none",
    val stroke: String = "black",
    val strokeWidth: Double? = null,
) : SvgWidget() {
    override fun buildElement(): SVGElement =
        createSvgElement("polyline").apply {
            setAttribute(
                "points",
                points.joinToString(separator = " ") { it.toSvgString() },
            )

            setAttribute("fill", fill)
            setAttribute("stroke", stroke)

            strokeWidth?.let {
                setAttribute("stroke-width", it.toString())
            }
        }
}

data class SvgText(
    val p: Point,
    val text: String,
) : SvgWidget() {
    override fun buildElement(): SVGElement =
        createSvgElement("text").apply {
            setAttribute("x", p.x.toString())
            setAttribute("y", p.y.toString())
            setAttribute("text-anchor", "middle")
            setAttribute("dominant-baseline", "middle")

            setAttribute("fill", "black")
            setAttribute("stroke", "none")

            appendChild(document.createTextNode(text))
        }
}

data class SvgCircle(
    val c: Cell<Point>,
    val r: Double,
    val fill: String = "none",
    val stroke: String = "black",
) : SvgWidget() {
    override fun buildElement(): SVGElement =
        createSvgElement("circle").apply {
            c.reactExternallyIndefinitely {
                setAttribute("cx", it.x.toString())
                setAttribute("cy", it.y.toString())
            }

            setAttribute("r", r.toString())

            setAttribute("fill", fill)
            setAttribute("stroke", stroke)
        }
}
