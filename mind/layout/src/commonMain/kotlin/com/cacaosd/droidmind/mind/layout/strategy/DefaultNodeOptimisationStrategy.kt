package com.cacaosd.droidmind.mind.layout.strategy

import com.cacaosd.droidmind.mind.layout.model.Node

class DefaultNodeOptimisationStrategy : NodeOptimisationStrategy<Node> {
    override fun optimise(node: Node): Node = node
}
