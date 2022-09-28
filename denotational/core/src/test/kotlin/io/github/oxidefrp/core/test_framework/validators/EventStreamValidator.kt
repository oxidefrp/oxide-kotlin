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
    /**
     * [startTick] could be used to more explicitly reject events that happened
     * before the validation start time, but such events will still be rejected
     * as non-matching the specification.
     */
    @Suppress("UNUSED_PARAMETER") startTick: Tick,
) : Validator() {
    override fun validate() = EventStreamIssue.validate(
        streamSpec = streamSpec,
        events = findEvents(
            stream = stream,
        ),
    )
}
