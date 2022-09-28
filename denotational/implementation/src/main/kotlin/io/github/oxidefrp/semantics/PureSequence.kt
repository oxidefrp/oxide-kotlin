package io.github.oxidefrp.semantics

sealed class PureSequence<out A> {
    companion object {
        fun <T> cons(
            head: T,
            tail: () -> PureSequence<T>,
        ): NonEmptyPureSequence<T> = object : NonEmptyPureSequence<T>(
            head = head,
        ) {
            override fun buildTail(): PureSequence<T> = tail()
        }

        fun <E> empty(): EmptyPureSequence<E> = EmptyPureSequence()

        fun <A> ofSingle(element: A): PureSequence<A> =
            object : NonEmptyPureSequence<A>(head = element) {
                override fun buildTail() = empty<A>()
            }

        fun <T> of(
            vararg elements: T,
        ): PureSequence<T> = elements.asPureSequence()

        fun <T> generate(
            seed: T,
            nextFunction: (T) -> T,
        ): PureSequence<T> = cons(
            head = seed,
        ) {
            generate(
                seed = nextFunction(seed),
                nextFunction = nextFunction,
            )
        }
    }

    abstract fun <B> map(transform: (A) -> B): PureSequence<B>

    abstract fun <B : Any> mapNotNull(transform: (A) -> B?): PureSequence<B>

    abstract fun first(): A

    abstract fun lastOrNull(): A?

    abstract fun singleOrNull(predicate: (A) -> Boolean): A?

    abstract fun concatWith(other: PureSequence<@UnsafeVariance A>): PureSequence<A>

    abstract fun take(n: Int): PureSequence<A>

    abstract fun takeWhile(predicate: (A) -> Boolean): PureSequence<A>

    abstract fun dropWhile(predicate: (A) -> Boolean): PureSequence<A>

    abstract fun <S, R> runningStatefulFold(
        initialState: S,
        operation: (acc: S, A) -> Pair<S, R>,
    ): PureSequence<R>

    abstract fun <R> zipWithPreviousOrNull(
        transform: (prev: A?, element: A) -> R,
    ): PureSequence<R>

    internal abstract fun <R> zipWithPreviousOrNullRecursive(
        prev: @UnsafeVariance A?,
        transform: (prev: A?, element: A) -> R,
    ): PureSequence<R>

    abstract fun toList(): List<A>
}

abstract class NonEmptyPureSequence<A>(
    val head: A,
) : PureSequence<A>() {
    abstract fun buildTail(): PureSequence<A>

    val tail: PureSequence<A> by lazy { buildTail() }

    override fun <B> map(transform: (A) -> B): NonEmptyPureSequence<B> = cons(
        head = transform(head),
    ) {
        this@NonEmptyPureSequence.tail.map(transform)
    }

    override fun <B : Any> mapNotNull(transform: (A) -> B?): PureSequence<B> {
        val newHead = transform(head)

        return when {
            newHead != null -> cons(
                head = newHead,
            ) {
                this@NonEmptyPureSequence.tail.mapNotNull(transform)
            }

            else -> tail.mapNotNull(transform)
        }
    }

    override fun first(): A = head

    override fun lastOrNull(): A? = tail.lastOrNull() ?: head

    override fun singleOrNull(predicate: (A) -> Boolean): A? =
        head.takeIf(predicate) ?: tail.singleOrNull(predicate)

    override fun concatWith(other: PureSequence<A>): PureSequence<A> = cons(
        head = this@NonEmptyPureSequence.head,
    ) {
        this@NonEmptyPureSequence.tail.concatWith(other)
    }

    override fun take(n: Int): PureSequence<A> = when {
        n <= 1 -> {
            assert(n == 1)
            ofSingle(head)
        }

        else -> cons(
            head = this@NonEmptyPureSequence.head,
        ) {
            this@NonEmptyPureSequence.tail.take(n - 1)
        }
    }

    override fun takeWhile(predicate: (A) -> Boolean): PureSequence<A> = when {
        predicate(head) -> cons(
            head = this@NonEmptyPureSequence.head,
        ) {
            this@NonEmptyPureSequence.tail.takeWhile(predicate)
        }

        else -> empty()
    }

    override fun dropWhile(predicate: (A) -> Boolean): PureSequence<A> = when {
        predicate(head) -> tail.dropWhile(predicate)

        else -> this
    }

    override fun <S, R> runningStatefulFold(
        initialState: S,
        operation: (acc: S, A) -> Pair<S, R>,
    ): PureSequence<R> {
        val (newState, newHead) = operation(initialState, head)

        return cons(
            head = newHead,
        ) {
            this@NonEmptyPureSequence.tail.runningStatefulFold(
                initialState = newState,
                operation = operation,
            )
        }
    }

    override fun <R> zipWithPreviousOrNull(
        transform: (prev: A?, element: A) -> R,
    ): PureSequence<R> = cons(
        head = transform(null, head),
    ) {
        this@NonEmptyPureSequence.tail.zipWithPreviousOrNullRecursive(
            prev = this@NonEmptyPureSequence.head,
            transform = transform,
        )
    }

    override fun <R> zipWithPreviousOrNullRecursive(
        prev: A?,
        transform: (prev: A?, element: A) -> R,
    ): PureSequence<R> = cons(
        head = transform(prev, head),
    ) {
        this@NonEmptyPureSequence.tail.zipWithPreviousOrNullRecursive(
            prev = this@NonEmptyPureSequence.head,
            transform = transform,
        )
    }

    override fun toList(): List<A> =
        listOf(head) + tail.toList()
}

class EmptyPureSequence<A> : PureSequence<A>() {
    override fun <B> map(transform: (A) -> B): EmptyPureSequence<B> = empty()

    override fun <B : Any> mapNotNull(transform: (A) -> B?): EmptyPureSequence<B> = empty()

    override fun first(): A = throw UnsupportedOperationException()

    override fun lastOrNull(): A? = null

    override fun singleOrNull(predicate: (A) -> Boolean): A? = null

    override fun concatWith(other: PureSequence<A>): PureSequence<A> = other

    override fun take(n: Int): EmptyPureSequence<A> = empty()

    override fun takeWhile(predicate: (A) -> Boolean): EmptyPureSequence<A> = empty()

    override fun dropWhile(predicate: (A) -> Boolean): EmptyPureSequence<A> = empty()

    override fun <S, R> runningStatefulFold(
        initialState: S,
        operation: (acc: S, A) -> Pair<S, R>,
    ): EmptyPureSequence<R> = empty()

    override fun <R> zipWithPreviousOrNull(
        transform: (prev: A?, element: A) -> R,
    ): EmptyPureSequence<R> = empty()

    override fun <R> zipWithPreviousOrNullRecursive(
        prev: A?,
        transform: (prev: A?, element: A) -> R,
    ): EmptyPureSequence<R> = empty()

    override fun toList(): List<A> = emptyList()

    override fun equals(other: Any?): Boolean = javaClass == other?.javaClass

    override fun hashCode(): Int = javaClass.hashCode()

    override fun toString(): String = "EmptyPureSequence()"
}

fun <A> PureSequence<A>.asNonEmpty(): NonEmptyPureSequence<A>? = this as? NonEmptyPureSequence

operator fun <A> PureSequence<A>.plus(other: PureSequence<A>): PureSequence<A> = this.concatWith(other)

fun <T> Array<T>.asPureSequence(): PureSequence<T> = toList().asPureSequence()

fun <T> List<T>.asPureSequence(): PureSequence<T> = when {
    this.isNotEmpty() -> PureSequence.cons(
        head = this@asPureSequence.first(),
    ) {
        this@asPureSequence.drop(1).asPureSequence()
    }

    else -> PureSequence.empty()
}
