data class NodeVar<TNode : RawNode?>(
    val identity: Int,
) {
    companion object {
        fun <TNode : RawNode?> new(): Io<NodeVar<TNode>> =
            object : Io<NodeVar<TNode>>() {
                override fun enterDirectly(
                    oldState: RealWorld,
                ): Pair<RealWorld, NodeVar<TNode>> =
                    oldState.withNewVar()
            }
    }

    fun set(node: TNode?): Io<Unit> = object : Io<Unit>() {
        override fun enterDirectly(
            oldState: RealWorld,
        ): Pair<RealWorld, Unit> =
            oldState.withSetNode(
                identity = identity,
                node = node,
            ) to Unit
    }

    @Suppress("UNCHECKED_CAST")
    fun get(): Io<TNode?> = object : Io<TNode?>() {
        override fun enterDirectly(
            oldState: RealWorld,
        ): Pair<RealWorld, TNode?> =
            oldState to oldState.get(identity = identity) as TNode?
    }
}
