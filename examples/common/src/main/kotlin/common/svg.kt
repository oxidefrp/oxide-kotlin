package common

import HtmlFinalWidget
import HtmlWidgetInstance
import animationFrameStream
import io.github.oxidefrp.oxide.core.Cell
import io.github.oxidefrp.oxide.core.EventStream
import io.github.oxidefrp.oxide.core.Io
import io.github.oxidefrp.oxide.core.Signal
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.svg.SVGCircleElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGGraphicsElement
import org.w3c.dom.svg.SVGLineElement
import org.w3c.dom.svg.SVGPolylineElement
import org.w3c.dom.svg.SVGSVGElement
import org.w3c.dom.svg.SVGTextElement

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
    abstract fun buildElement(
        svg: SVGSVGElement,
        ticks: EventStream<Unit>,
    ): SVGElement
}

private inline fun <reified E : SVGElement> createSvgElement(qualifiedName: String): E =
    document.createElementNS("http://www.w3.org/2000/svg", qualifiedName).unsafeCast<E>()

data class SvgSvg(
    val width: Double,
    val height: Double,
    val children: List<SvgWidget>,
) : HtmlFinalWidget<HtmlWidgetInstance>() {
    override fun buildInstanceExternally() = object : Io<HtmlWidgetInstance>() {
        override fun performExternally() = object : HtmlWidgetInstance() {
            override val element: Element = createSvgElement<SVGSVGElement>("svg").apply {
                setAttribute("xmlns", "http://www.w3.org/2000/svg")
                setAttribute("viewBox", "0 0 ${this@SvgSvg.width} ${this@SvgSvg.height}")
                setAttribute("width", this@SvgSvg.width.toString())
                setAttribute("height", this@SvgSvg.height.toString())

                val ticks = animationFrameStream()

                this@SvgSvg.children.forEach {
                    appendChild(
                        it.buildElement(
                            svg = this,
                            ticks = ticks,
                        ),
                    )
                }
            }
        }
    }
}

data class SvgGroup(
    val transform: SvgTransform? = null,
    val children: List<SvgWidget>,
) : SvgWidget() {
    override fun buildElement(
        svg: SVGSVGElement,
        ticks: EventStream<Unit>,
    ): SVGElement =
        createSvgElement<SVGGElement>("g").apply {
            this@SvgGroup.transform?.let {
                setAttribute("transform", it.buildTransformString())
            }

            this@SvgGroup.children.forEach {
                appendChild(
                    it.buildElement(
                        svg = svg,
                        ticks = ticks,
                    ),
                )
            }
        }
}

private fun linkSvgTransform(
    svg: SVGSVGElement,
    ticks: EventStream<Unit>,
    element: SVGGraphicsElement,
    transform: Signal<Transform>,
) {
    val transformDiscretized = transform.discretize(ticks = ticks).pullExternally()

    val initialTransform = transformDiscretized.value.sampleExternally()

    val svgTransform =
        svg.createSVGTransformFromMatrix(initialTransform.toSVGMatrix(svg))

    element.transform.baseVal.initialize(svgTransform)

    transformDiscretized.newValues.subscribeIndefinitely { transformNow ->
        svgTransform.setMatrix(
            matrix = transformNow.toSVGMatrix(svg),
        )
    }
}

data class SvgLine(
    val p1: Point,
    val p2: Point,
    val transform: Signal<Transform>? = null,
    val fill: String = "none",
    val stroke: String = "black",
    val strokeWidth: Double? = null,
) : SvgWidget() {
    override fun buildElement(
        svg: SVGSVGElement,
        ticks: EventStream<Unit>,
    ): SVGElement =
        createSvgElement<SVGLineElement>("line").apply {
            x1.baseVal.value = p1.x.toFloat()
            y1.baseVal.value = p1.y.toFloat()

            x2.baseVal.value = p2.x.toFloat()
            y2.baseVal.value = p2.y.toFloat()

            setAttribute("fill", fill)
            setAttribute("stroke", stroke)

            this@SvgLine.transform?.let {
                linkSvgTransform(
                    svg = svg,
                    ticks = ticks,
                    element = this,
                    transform = it,
                )
            }

            strokeWidth?.let {
                setAttribute("stroke-width", it.toString())
            }

        }
}

data class SvgPolyline(
    val points: List<Point>,
    val fill: String = "none",
    val stroke: String = "black",
    val strokeWidth: Double? = null,
) : SvgWidget() {
    override fun buildElement(
        svg: SVGSVGElement,
        ticks: EventStream<Unit>,
    ) = createSvgElement<SVGPolylineElement>("polyline").apply {
        setAttribute(
            "points",
            this@SvgPolyline.points.joinToString(separator = " ") { it.toSvgString() },
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
    override fun buildElement(
        svg: SVGSVGElement,
        ticks: EventStream<Unit>,
    ) = createSvgElement<SVGTextElement>("text").apply {
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
    val strokeWidth: Double? = null,
) : SvgWidget() {
    override fun buildElement(
        svg: SVGSVGElement,
        ticks: EventStream<Unit>,
    ) = createSvgElement<SVGCircleElement>("circle").apply {
        c.reactExternallyIndefinitely {
            cx.baseVal.value = it.x.toFloat()
            cy.baseVal.value = it.y.toFloat()
        }

        r.baseVal.value = this@SvgCircle.r.toFloat()

        setAttribute("fill", fill)
        setAttribute("stroke", stroke)

        strokeWidth?.let {
            setAttribute("stroke-width", it.toString())
        }
    }
}
