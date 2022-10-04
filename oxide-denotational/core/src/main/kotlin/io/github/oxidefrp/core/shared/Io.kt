package io.github.oxidefrp.core.shared

import io.github.oxidefrp.core.RealWorld

typealias Io<A> = State<RealWorld, A>

typealias MomentIo<A> = MomentState<RealWorld, A>

typealias IoScheduler<A> = StateScheduler<RealWorld, A>

typealias IoStructure<A> = StateStructure<RealWorld, A>
