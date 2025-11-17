package com.cacaosd.droidmind.mind.layout.strategy

import com.cacaosd.droidmind.mind.layout.model.WdaNode

class CollapseParentWdaNodeStrategy : NodeOptimisationStrategy<WdaNode> {
    override fun optimise(node: WdaNode): WdaNode {
        return node.cleanAndReindex() ?: error("Node cannot be optimised.")
    }

    private fun WdaNode.cleanAndReindex(): WdaNode? {
        // Recursively clean children
        val cleanedChildren = children.mapNotNull { it.cleanAndReindex() }

        // Determine if this node is meaningful
        val isMeaningful = name.isNotBlank()
                || label.isNotBlank()
                || visible
                || accessible

        // Collapse meaningless parent with a single child
        if (!isMeaningful && cleanedChildren.size == 1) {
            return cleanedChildren.first()
        }

        // Remove node entirely if not meaningful and has no children
        if (!isMeaningful && cleanedChildren.isEmpty()) {
            return null
        }

        // Reindex children
        val reIndexedChildren = cleanedChildren.mapIndexed { idx, child ->
            child.copy(index = idx)
        }

        // Return cleaned and re-indexed node
        return this.copy(children = reIndexedChildren)
    }
}
