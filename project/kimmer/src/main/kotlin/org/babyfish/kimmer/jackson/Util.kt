package org.babyfish.kimmer.jackson

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.databind.type.SimpleType
import org.babyfish.kimmer.meta.ImmutableProp

internal val ImmutableProp.jacksonType: JavaType
    get() = if (isList || isScalarList) {
        CollectionType.construct(
            List::class.java,
            null,
            null,
            null,
            SimpleType.constructUnsafe(elementType.java)
        )
    } else {
        SimpleType.constructUnsafe(javaReturnType)
    }