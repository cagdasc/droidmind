package com.cacaosd.droidmind.mind.layout.optimisation_strategy

import com.cacaosd.droidmind.mind.layout.model.android.UiAutomatorNode

class DefaultNodeOptimisationStrategy : NodeOptimisationStrategy<UiAutomatorNode> {
    override fun optimise(uiAutomatorNode: UiAutomatorNode): UiAutomatorNode = uiAutomatorNode
}
