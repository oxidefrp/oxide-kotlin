package io.github.oxidefrp.core

data class RealWorld(
    val files: Map<String, String>,
) {
    companion object {
        val empty = RealWorld(
            files = emptyMap(),
        )
    }
}
