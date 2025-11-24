package com.cacaosd.droidmind.mind.layout.model

import com.cacaosd.droidmind.mind.layout.model.android.androidElement
import com.cacaosd.droidmind.mind.layout.model.ios.iosElementMap

internal val elementLookupTable: Map<String, ElementType> =
    (iosElementMap + androidElement)
        .flatMap { (type, classes) -> classes.map { it to type } }
        .toMap()
