package com.cacaosd.droidmind.mind.layout.optimisation_strategy

interface NodeOptimisationStrategy<T> {
    fun optimise(node: T): T
}
