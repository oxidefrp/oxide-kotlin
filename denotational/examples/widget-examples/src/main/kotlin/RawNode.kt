import io.github.oxidefrp.semantics.State

sealed interface RawNode {
    fun dump(): Io<List<String>>
}

fun RawNode.print(realWorld: RealWorld) {
    val lines = dump().enterDirectly(realWorld).second

    var level = 0
    lines.forEach { line ->
        println("  ".repeat(level) + line)
        ++level
    }
}

data class RawElement(
    val tagName: String,
    private val child: NodeVar<RawNode?>,
) : RawNode {
    companion object {
        fun new(tagName: String): Io<RawElement> =
            NodeVar.new<RawNode?>().map { child ->
                RawElement(
                    tagName = tagName,
                    child = child,
                )
            }
    }

    fun setChild(node: RawNode?): Io<Unit> =
        child.set(node = node)

    fun getChild(): Io<RawNode?> =
        child.get()

    override fun dump(): Io<List<String>> =
        getChild().enterOf { childNow ->
            (childNow?.dump() ?: Io.pure(emptyList())).map { tail ->
                listOf("Element $tagName") + tail
            }
        }
}

data class RawText(
    val content: String,
) : RawNode {
    companion object {
        fun new(content: String): Io<RawText> =
            State.pure(RawText(content = content))
    }

    override fun dump(): Io<List<String>> =
        Io.pure(listOf("Text: $content"))
}
