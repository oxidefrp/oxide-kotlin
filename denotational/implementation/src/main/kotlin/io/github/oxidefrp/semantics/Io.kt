package io.github.oxidefrp.semantics

data class RealWorld(
    val files: Map<String, String>,
) {
    companion object {
        val empty = RealWorld(
            files = emptyMap(),
        )
    }
}

typealias Io<A> = State<RealWorld, A>

fun readFile(fileName: String): Io<String?> =
    State.read<RealWorld>().map { it.files[fileName] }

fun writeFile(fileName: String, content: String): Io<Unit> =
    State.read<RealWorld>().enterOf { oldWorld ->
        State.write(
            oldWorld.copy(
                files = oldWorld.files + (fileName to content),
            ),
        )
    }
