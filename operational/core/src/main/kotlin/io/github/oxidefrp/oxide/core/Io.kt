package io.github.oxidefrp.oxide.core

abstract class Io<A> {
    companion object {
        fun <A> pure(a: A) = object : Io<A>() {
            override fun performExternally(): A = a
        }
    }

    abstract fun performExternally(): A

    fun <B> map(transform: (A) -> B): Io<B> =
        object : Io<B>() {
            override fun performExternally(): B =
                transform(this@Io.performExternally())
        }

    fun <B> performOf(transform: (A) -> Io<B>): Io<B> =
        object : Io<B>() {
            override fun performExternally(): B =
                transform(this@Io.performExternally()).performExternally()
        }
}

