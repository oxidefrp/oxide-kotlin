package io.github.oxidefrp.core.test_framework.validators

import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.test_framework.Validator
import io.github.oxidefrp.core.test_framework.findEvents
import io.github.oxidefrp.core.test_framework.shared.EventStreamIssue
import io.github.oxidefrp.core.test_framework.shared.EventStreamSpec
import io.github.oxidefrp.core.test_framework.shared.Tick

internal class EventStreamValidator<A>(
    private val streamSpec: EventStreamSpec<A>,
    private val stream: EventStream<A>,
    private val startTick: Tick,
) : Validator() {
    override fun validate() = EventStreamIssue.validate(
        streamSpec = streamSpec,
        events = findEvents(
            startTick = startTick,
            stream = stream,
        ),
    )
}
