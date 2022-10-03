package common

import org.w3c.dom.DOMMatrixReadOnly
import org.w3c.dom.svg.SVGSVGElement
import kotlin.math.cos
import kotlin.math.sin

data class Vector(
    val x: Double,
    val y: Double,
)

data class Transform(
    val a: Double,
    val b: Double,
    val c: Double,
    val d: Double,
    val e: Double,
    val f: Double,
) {
    companion object {
        val identity: Transform = Transform(
            a = 1.0,
            c = 0.0,
            e = 0.0,
            b = 0.0,
            d = 1.0,
            f = 0.0,
        )

        fun translate(t: Vector): Transform =
            Transform(
                a = 1.0,
                c = 0.0,
                e = t.x,
                b = 0.0,
                d = 1.0,
                f = t.y,
            )

        fun scale(s: Double): Transform =
            Transform(
                a = s,
                c = 0.0,
                e = 0.0,
                b = 0.0,
                d = s,
                f = 0.0,
            )

        fun rotateOfAngle(angleRad: Double): Transform {
            val cosAngle = cos(angleRad)
            val sinAngle = sin(angleRad)

            return rotate(
                cosAngle = cosAngle,
                sinAngle = sinAngle,
            )
        }

        private fun rotate(
            cosAngle: Double,
            sinAngle: Double,
        ): Transform = Transform(
            a = cosAngle,
            c = -sinAngle,
            e = 0.0,
            b = sinAngle,
            d = cosAngle,
            f = 0.0,
        )
    }

    val inversed: Transform by lazy { this.calculateInversed() }

    private fun calculateInversed(): Transform {
        val de = a * d - b * c

        return Transform(
            a = d / de,
            b = b / -de,
            c = c / -de,
            d = a / de,
            e = (d * e - c * f) / -de,
            f = (b * e - a * f) / de
        )
    }

    operator fun times(that: Transform): Transform {
        val m1 = this
        val m2 = that

        return Transform(
            a = m1.a * m2.a + m1.c * m2.b,
            c = m1.a * m2.c + m1.c * m2.d,
            e = m1.a * m2.e + m1.c * m2.f + m1.e,
            b = m1.b * m2.a + m1.d * m2.b,
            d = m1.b * m2.c + m1.d * m2.d,
            f = m1.b * m2.e + m1.d * m2.f + m1.f,
        )
    }

    fun transform(v: Vector): Vector =
        Vector(
            x = a * v.x + c * v.y + e,
            y = b * v.x + d * v.y + f,
        )

    // The returned object is actually an instance of SVGMatrix
    fun toSVGMatrix(svg: SVGSVGElement): DOMMatrixReadOnly =
        svg.createSVGMatrix().apply {
            a = this@Transform.a
            b = this@Transform.b
            c = this@Transform.c
            d = this@Transform.d
            e = this@Transform.e
            f = this@Transform.f
        }
}
