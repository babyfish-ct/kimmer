package org.babyfish.kimmer.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.produce


class ImmutableDeserializer(type: Class<out Immutable>): StdDeserializer<Immutable>(type) {

    override fun deserialize(
        jp: JsonParser,
        ctx: DeserializationContext
    ): Immutable {

        val rawClass = handledType()
        val type = ImmutableType.fromAnyType(rawClass)
            ?: throw JsonMappingException(jp, "Cannot deserialize the object whose type is '${rawClass.name}'")

        val node: JsonNode = jp.codec.readTree(jp)

        return produce(type.kotlinType) {
            for (prop in type.props.values) {
                if (node.has(prop.name)) {
                    val value = ctx.readTreeAsValue<Any>(node[prop.name], prop.jacksonType)
                    Draft.set(this, prop, value)
                }
            }
        }
    }
}