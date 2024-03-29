package common

import intervalStream
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.Signal

fun buildConsecutiveIntStream(intervalS: Double): Moment<EventStream<Int>> {
    var nextNumber = 0

    val inputStream = intervalStream(timeout = (intervalS * 1000.0).toInt()).map {
        /// FIXME: This escapes the semantics
        // Replace this with accum when loops are implemented
        ++nextNumber
    }

    return Moment.pure(inputStream)
}
