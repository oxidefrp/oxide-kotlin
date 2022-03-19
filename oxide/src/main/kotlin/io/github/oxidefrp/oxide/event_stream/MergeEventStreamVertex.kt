package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.Option
import io.github.oxidefrp.oxide.orElse

class MergeEventStreamVertex<A>(
    private val source1: EventStreamVertex<A>,
    private val source2: EventStreamVertex<A>,
    private val combine: (A, A) -> A,
) : ObservingEventStreamVertex<A>() {
    override fun observe(): Subscription {
        val subscription1 = source1.registerDependent(this)
        val subscription2 = source2.registerDependent(this)
        return subscription1 + subscription2
    }

    override fun computeCurrentOccurrence(): Option<A> =
        Option
            .map2(
                source1.currentOccurrence,
                source2.currentOccurrence,
                combine,
            )
            .orElse { source1.currentOccurrence }
            .orElse { source2.currentOccurrence }
}
