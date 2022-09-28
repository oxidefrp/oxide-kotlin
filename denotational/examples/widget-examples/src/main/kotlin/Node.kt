import io.github.oxidefrp.semantics.Cell

sealed interface Node {
    companion object {
        fun link(
            parent: RawElement,
            child: Cell<RawNode?>,
        ): IoStructure<Unit> = child.enterOf { childNow: RawNode? ->
            parent.setChild(childNow)
        }.map { }
    }

    fun build(): IoStructure<RawNode>
}

data class Element(
    val tagName: String,
    val child: Cell<Node?>,
) : Node {
    override fun build(): IoStructure<RawNode> =
        RawElement.new(tagName = tagName).constructOf { rawElement ->
            child.constructOf { it!!.build() }
                .constructOf { childRaw: Cell<RawNode> ->
                    Node.link(
                        parent = rawElement,
                        child = childRaw,
                    )
                }.map { rawElement }
        }
}

data class Text(
    val content: String,
) : Node {
    override fun build(): IoStructure<RawNode> =
        RawText.new(content = content).asStateStructure()
}

data class Document(
    val root: Element,
) {
    fun build(): IoStructure<RawNode> = root.build()
}
