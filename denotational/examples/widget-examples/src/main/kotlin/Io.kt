import io.github.oxidefrp.core.State
import io.github.oxidefrp.core.shared.StateScheduler
import io.github.oxidefrp.core.StateStructure

data class RealWorld(
    val nodes: Map<Int, RawNode?>,
) {
    fun <TNode : RawNode?> withNewVar(): Pair<RealWorld, NodeVar<TNode>> {
        val identity = (nodes.keys.maxOrNull() ?: 0) + 1
        return RealWorld(
            nodes = nodes + (identity to null),
        ) to NodeVar(identity = identity)
    }

    fun withSetNode(identity: Int, node: RawNode?): RealWorld = RealWorld(
        nodes = nodes + (identity to node),
    )

    fun get(identity: Int): RawNode? = nodes[identity]

    companion object {
        fun empty(): RealWorld =
            RealWorld(nodes = emptyMap())
    }
}

typealias Io<A> = State<RealWorld, A>

typealias IoScheduler<A> = StateScheduler<RealWorld, A>

typealias IoStructure<A> = StateStructure<RealWorld, A>
