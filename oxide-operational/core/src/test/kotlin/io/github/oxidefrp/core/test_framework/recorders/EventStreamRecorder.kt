package io.github.oxidefrp.core.test_framework.recorders

import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.Vertex
import io.github.oxidefrp.core.test_framework.TickProvider
import io.github.oxidefrp.core.test_framework.shared.EventOccurrenceDesc

internal class EventStreamRecorder<out A> private constructor(
    tickProvider: TickProvider,
    stream: EventStream<A>,
    transaction: Transaction,
    initialRecordedEvents: List<EventOccurrenceDesc<A>>,
) {
    companion object {
        fun <A> start(
            tickProvider: TickProvider,
            stream: EventStream<A>,
            transaction: Transaction,
        ): EventStreamRecorder<A> {
            val initialRecordedEvent = stream.vertex.pullCurrentOccurrence(transaction)
                .map {
                    EventOccurrenceDesc(
                        tick = tickProvider.currentTick,
                        event = it,
                    )
                }

            return EventStreamRecorder(
                tickProvider = tickProvider,
                stream = stream,
                transaction = transaction,
                initialRecordedEvents = initialRecordedEvent.toList(),
            )
        }
    }

    init {
        val streamVertex = stream.vertex

        streamVertex.registerDependent(
            transaction = transaction,
            dependent = object : Vertex() {
                override fun getDependents() = emptyList<Vertex>()

                override fun process(transaction: Transaction) {
                    streamVertex.pullCurrentOccurrence(transaction).ifSome { event ->
                        recordedEvents.add(
                            EventOccurrenceDesc(
                                tick = tickProvider.currentTick,
                                event = event,
                            ),
                        )
                    }
                }
            }
        )
    }

    private val recordedEvents = initialRecordedEvents.toMutableList()

    fun getRecordedEvents(): List<EventOccurrenceDesc<A>> =
        recordedEvents.toList()
}
