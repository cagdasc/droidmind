package com.cacaosd.droidmind.mind.layout.optimisation_strategy

import com.cacaosd.droidmind.mind.layout.model.android.UiAutomatorNode

/**
 * Optimisation strategy that collapses parent nodes with a single child
 * if the parent node is considered meaningless. A node is considered meaningless
 * if it has no text, no resource ID, no content description, and is not interactive
 * (i.e., not clickable, long-clickable, checkable, or focusable). Such nodes with a single child
 * are collapsed into their child. Nodes that are meaningless and have no children are removed entirely.
 *
 * ```xml
 * <node index="0" text="" resource-id="android:id/content" class="android.widget.FrameLayout"
 *       package="com.google.android.youtube" content-desc="" checkable="false" checked="false"
 *       clickable="false" enabled="true" focusable="false" focused="false" scrollable="false"
 *       long-clickable="false" password="false" selected="false" bounds="[0,0][1080,2424]"
 *       drawing-order="2" hint="">
 * ```
 */
class CollapseParentNodeStrategy : NodeOptimisationStrategy<UiAutomatorNode> {
    override fun optimise(uiAutomatorNode: UiAutomatorNode): UiAutomatorNode {
        return uiAutomatorNode.cleanAndReindex() ?: error("Node cannot be optimised.")
    }

    private fun UiAutomatorNode.cleanAndReindex(): UiAutomatorNode? {
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
            child.copy(index = idx.toString())
        }

        // Return cleaned and re-indexed node
        return this.copy(children = reIndexedChildren)
    }

    private fun UiAutomatorNode.isMeaningful(): Boolean {
        return text.isNotBlank()
                || resourceId.isNotBlank()
                || contentDesc.isNotBlank()
                || clickable
                || longClickable
                || checkable
                || focusable
    }
}
