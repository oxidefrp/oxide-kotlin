package io.github.oxidefrp.oxide.core.test_framework.validators

import io.github.oxidefrp.oxide.core.EventStream
import io.github.oxidefrp.oxide.core.impl.Transaction
import io.github.oxidefrp.oxide.core.test_framework.recorders.EventStreamRecorder
import io.github.oxidefrp.oxide.core.test_framework.TickProvider
import io.github.oxidefrp.oxide.core.test_framework.Validator
import io.github.oxidefrp.oxide.core.test_framework.TestVertex
import io.github.oxidefrp.oxide.core.test_framework.shared.EventStreamIssue
import io.github.oxidefrp.oxide.core.test_framework.shared.EventStreamSpec
import io.github.oxidefrp.oxide.core.test_framework.shared.Issue
import io.github.oxidefrp.oxide.core.test_framework.shared.Tick

internal class EventStreamValidator<A>(
    private val streamSpec: EventStreamSpec<A>,
    private val stream: EventStream<A>,
    startTick: Tick,
) : Validator(startTick = startTick) {
    override fun spawnDirectly(
        tickProvider: TickProvider,
        transaction: Transaction,
    ): TestVertex {
        val recorder = EventStreamRecorder.start(
            tickProvider = tickProvider,
            stream = stream,
            transaction = transaction,
        )

        return object : TestVertex() {
            override fun validateRecord(): Issue? {
                return EventStreamIssue.validate(
                    streamSpec = streamSpec,
                    events = recorder.getRecordedEvents(),
                )
            }
        }
    }
}
