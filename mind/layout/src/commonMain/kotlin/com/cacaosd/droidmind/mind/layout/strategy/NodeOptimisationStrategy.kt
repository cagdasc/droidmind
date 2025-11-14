package com.cacaosd.droidmind.mind.layout.strategy

import com.cacaosd.droidmind.mind.layout.model.Node

interface NodeOptimisationStrategy {
    fun optimise(node: Node): Node
}
