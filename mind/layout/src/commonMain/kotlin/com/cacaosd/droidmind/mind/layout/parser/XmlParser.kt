package com.cacaosd.droidmind.mind.layout.parser

import nl.adaptivity.xmlutil.serialization.XML

val xmlParser = XML {
    // Configure XML parsing
    defaultPolicy {
        // Ignore unknown attributes and elements
        ignoreUnknownChildren()
    }
    autoPolymorphic = true
}
