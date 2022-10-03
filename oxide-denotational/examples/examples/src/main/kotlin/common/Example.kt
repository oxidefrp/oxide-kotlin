package common

data class Plot(
    val name: String,
    val graphic: SvgSvg,
)

data class Example(
    val name: String,
    val plots: List<Plot>,
)