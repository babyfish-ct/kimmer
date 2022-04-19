package org.babyfish.kimmer.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.sql.Association
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.impl.JACKSON_GENERIC_SOURCE_TYPE
import org.babyfish.kimmer.sql.impl.JACKSON_GENERIC_TARGET_TYPE
import org.babyfish.kimmer.sql.impl.SOURCE
import org.babyfish.kimmer.sql.impl.TARGET
import org.babyfish.kimmer.sql.produceAssociation
import java.lang.UnsupportedOperationException
import kotlin.reflect.KClass

class AssociationDeserializer : StdDeserializer<Association<*, *, *, *>>(Association::class.java) {

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(
        jp: JsonParser,
        ctx: DeserializationContext
    ): Association<*, *, *, *> {

        val node: JsonNode = jp.codec.readTree(jp)
        val (sourceType, source) = readReference(jp, ctx, node, SOURCE)
        val (targetType, target) = readReference(jp, ctx, node, TARGET)
        return produceAssociation(sourceType, targetType) {
            this.source = source
            this.target = target
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun readReference(
        jp: JsonParser,
        ctx: DeserializationContext,
        node: JsonNode,
        prop: String
    ): Pair<KClass<Entity<FakeId>>, Entity<FakeId>> {
        val genericTypeName = when (prop) {
            SOURCE -> node[JACKSON_GENERIC_SOURCE_TYPE].asText()
            TARGET -> node[JACKSON_GENERIC_TARGET_TYPE].asText()
            else -> error("Internal bug: Illegal argument of readReference")
        }
        val genericJavaType = try {
            Class.forName(genericTypeName)
        } catch (ex: ClassNotFoundException) {
            throw JsonMappingException(
                jp,
                "Cannot deserialize the association, cannot loaded class for generic $prop type '${genericTypeName}'"
            )
        }
        if (!Immutable::class.java.isAssignableFrom(genericJavaType) ||
            Immutable::class.java === genericJavaType) {
            throw JsonMappingException(
                jp,
                "Cannot deserialize the association, it's generic $prop type '${genericJavaType}' " +
                    "is not derived interface type of '${Immutable::class.java}'"
            )
        }
        if (Association::class.java.isAssignableFrom(genericJavaType) ||
            Draft::class.java.isAssignableFrom(genericJavaType)) {
            throw JsonMappingException(
                jp,
                "Cannot deserialize the association, it's generic $prop type '${genericJavaType}' cannot be association"
            )
        }
        return genericJavaType.kotlin as KClass<Entity<FakeId>> to
            ctx.readTreeAsValue(node[prop], genericJavaType) as Entity<FakeId>
    }

    private class FakeId: Comparable<FakeId> {
        override fun compareTo(other: FakeId): Int =
            throw UnsupportedOperationException()
    }
}