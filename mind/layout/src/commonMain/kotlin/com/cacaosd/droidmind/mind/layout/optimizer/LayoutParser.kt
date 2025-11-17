package com.cacaosd.droidmind.mind.layout.optimizer

import com.cacaosd.droidmind.mind.layout.model.OptimisedHierarchy
import java.io.File

interface LayoutParser {
    fun parse(uiDumpFile: File): OptimisedHierarchy?
}
