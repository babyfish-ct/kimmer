package org.babyfish.kimmer.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.Association
import org.babyfish.kimmer.sql.AssociationId
import org.babyfish.kimmer.sql.impl.*
import org.babyfish.kimmer.sql.impl.JACKSON_GENERIC_SOURCE_TYPE
import org.babyfish.kimmer.sql.impl.JACKSON_GENERIC_TARGET_TYPE
import org.babyfish.kimmer.sql.impl.SOURCE
import org.babyfish.kimmer.sql.impl.TARGET
import org.babyfish.kimmer.sql.meta.AssociationType

class AssociationSerializer : StdSerializer<Association<*, *, *, *>>(Association::class.java) {

    override fun serialize(
        value: Association<*, *, *, *>,
        gen: JsonGenerator,
        provider: SerializerProvider
    ) {
        val associationType = ImmutableType.fromInstance(value) as AssociationType

        gen.writeStartObject()

        gen.writeFieldName(JACKSON_GENERIC_SOURCE_TYPE)
        gen.writeString(associationType.sourceType.kotlinType.qualifiedName)

        gen.writeFieldName(JACKSON_GENERIC_TARGET_TYPE)
        gen.writeString(associationType.targetType.kotlinType.qualifiedName)

        if (Immutable.isLoaded(value, Association<*, *, *, *>::source)) {
            val sourceSerializer = provider.findValueSerializer(
                associationType.sourceType.kotlinType.java
            )
            gen.writeFieldName(SOURCE)
            sourceSerializer.serialize(value.source, gen, provider)
        }

        if (Immutable.isLoaded(value, Association<*, *, *, *>::target)) {
            val targetSerializer = provider.findValueSerializer(
                associationType.targetType.kotlinType.java
            )
            gen.writeFieldName(TARGET)
            targetSerializer.serialize(value.target, gen, provider)
        }

        gen.writeEndObject()
    }
}