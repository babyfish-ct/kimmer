package org.babyfish.kimmer.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableType


class ImmutableSerializer(
    private val immutableType: ImmutableType
) : StdSerializer<Immutable>(immutableType.kotlinType.java as Class<Immutable>) {

    override fun serialize(
        value: Immutable,
        gen: JsonGenerator,
        provider: SerializerProvider
    ) {
        gen.writeStartObject()
        serializeFields(value, gen, provider)
        gen.writeEndObject()
    }

    override fun serializeWithType(
        value: Immutable,
        gen: JsonGenerator,
        serializers: SerializerProvider,
        typeSer: TypeSerializer
    ) {
        typeSer.writeTypePrefix(gen, typeSer.typeId(value, JsonToken.START_OBJECT))
        serializeFields(value, gen, serializers)
        gen.writeEndObject()
    }

    private fun serializeFields(
        value: Immutable,
        gen: JsonGenerator,
        provider: SerializerProvider
    ) {
        for (prop in immutableType.props.values) {
            if (Immutable.isLoaded(value, prop)) {
                val value = Immutable.get(value, prop)
                if ((prop.isAssociation || prop.isScalarList) && value !== null) {
                    gen.writeFieldName(prop.name)
                    val typeSer = if (!prop.isList && !prop.isScalarList && ImmutableType.fromAnyObject(value) !== immutableType) {
                        provider.findTypeSerializer(prop.jacksonType)
                    } else {
                        null
                    }
                    if (typeSer !== null) {
                        provider.findValueSerializer(value::class.java).serializeWithType(value, gen, provider, typeSer)
                    } else {
                        provider.findValueSerializer(prop.jacksonType).serialize(value, gen, provider)
                    }
                } else {
                    provider.defaultSerializeField(prop.name, value, gen)
                }
            }
        }
    }
}
