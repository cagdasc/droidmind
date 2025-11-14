package com.cacaosd.droidmind.mind.layout.strategy

import com.cacaosd.droidmind.mind.layout.model.Node

class DefaultNodeOptimisationStrategy : NodeOptimisationStrategy {
    override fun optimise(node: Node): Node = node
}
