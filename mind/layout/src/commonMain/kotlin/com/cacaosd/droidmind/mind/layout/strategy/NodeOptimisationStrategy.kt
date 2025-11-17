package com.cacaosd.droidmind.mind.layout.strategy

interface NodeOptimisationStrategy<T> {
    fun optimise(node: T): T
}
