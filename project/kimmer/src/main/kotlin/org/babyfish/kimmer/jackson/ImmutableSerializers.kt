package org.babyfish.kimmer.jackson

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.ser.Serializers
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableType

class ImmutableSerializers: Serializers.Base() {

    override fun findSerializer(
        config: SerializationConfig,
        type: JavaType,
        beanDesc: BeanDescription
    ): JsonSerializer<*>? =
        type
            .rawClass
            .let { ImmutableType.fromAnyType(it) }
            ?.let {
                ImmutableSerializer(it)
            }
}