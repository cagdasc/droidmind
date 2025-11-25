package com.cacaosd.droidmind.mind.layout.optimisation_strategy

import com.cacaosd.droidmind.mind.layout.model.ios.WdaNode

/**
 * Strategy to collapse meaningless parent nodes in a WdaNode hierarchy.
 * A node is considered meaningless if it has no name, no label, is not visible,
 * and is not accessible. Such nodes with a single child are collapsed into their child.
 * Nodes that are meaningless and have no children are removed entirely.
 * ```xml
 * <XCUIElementTypeStaticText
 *         type="XCUIElementTypeStaticText"
 *         value="More Top Stories "
 *         name="More Top Stories "
 *         label="More Top Stories "
 *         enabled="true" visible="false"
 *         accessible="false" x="252"
 *         y="880" width="122" height="16"
 *         index="0" traits="StaticText"/>
 * ```
 */

class CollapseParentWdaNodeStrategy : NodeOptimisationStrategy<WdaNode> {
    override fun optimise(node: WdaNode): WdaNode {
        return node.cleanAndReindex() ?: error("Node cannot be optimised.")
    }

    private fun WdaNode.cleanAndReindex(): WdaNode? {
        // Recursively clean children
        val cleanedChildren = children.mapNotNull { it.cleanAndReindex() }

        // Collapse meaningless parent with a single child
        if (!isMeaningful() && cleanedChildren.size == 1) {
            return cleanedChildren.first()
        }

        // Remove node entirely if not meaningful and has no children
        if (!isMeaningful() && cleanedChildren.isEmpty()) {
            return null
        }

        // Reindex children
        val reIndexedChildren = cleanedChildren.mapIndexed { idx, child ->
            child.copy(index = idx)
        }

        // Return cleaned and re-indexed node
        return this.copy(children = reIndexedChildren)
    }

    private fun WdaNode.isMeaningful(): Boolean {
        return accessible && (name.isNotBlank() || label.isNotBlank())
    }
}
