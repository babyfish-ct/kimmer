package org.babyfish.kimmer.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.module.kotlin.contains
import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.graphql.impl.*
import org.babyfish.kimmer.graphql.impl.CURSOR
import org.babyfish.kimmer.graphql.impl.EDGES
import org.babyfish.kimmer.graphql.impl.JACKSON_GENERIC_TYPE
import org.babyfish.kimmer.graphql.impl.NODE
import org.babyfish.kimmer.graphql.impl.TOTAL_COUNT
import org.babyfish.kimmer.graphql.produceConnection
import org.babyfish.kimmer.graphql.produceEdgeDraft
import kotlin.reflect.KClass

class ConnectionDeserializer : StdDeserializer<Connection<*>>(Connection::class.java) {

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(
        jp: JsonParser,
        ctx: DeserializationContext
    ): Connection<*> {

        val node: JsonNode = jp.codec.readTree(jp)
        val genericTypeName = node[JACKSON_GENERIC_TYPE].asText()
        val genericJavaType = try {
            Class.forName(genericTypeName)
        } catch (ex: ClassNotFoundException) {
            throw JsonMappingException(
                jp,
                "Cannot deserialize the connection, cannot loaded class for generic type '${genericTypeName}'"
            )
        }
        if (!Immutable::class.java.isAssignableFrom(genericJavaType) ||
            Immutable::class.java === genericJavaType) {
            throw JsonMappingException(
                jp,
                "Cannot deserialize the connection, it's generic type '${genericTypeName}' " +
                    "is not derived interface type of '${Immutable::class.java}'"
            )
        }
        if (Connection::class.java.isAssignableFrom(genericJavaType) ||
            Draft::class.java.isAssignableFrom(genericJavaType)) {
            throw JsonMappingException(
                jp,
                "Cannot deserialize the connection, it's generic type '${genericTypeName}' cannot be connection/draft"
            )
        }
        val nodeType = genericJavaType.kotlin as KClass<Immutable>
        return produceConnection(nodeType) {
            if (node.contains(EDGES)) {
                edges = node[EDGES].asIterable().map {
                    produceEdgeDraft(nodeType) {
                        if (it.contains(NODE)) {
                            this.node = ctx.readTreeAsValue(it[NODE], genericJavaType) as Immutable
                        }
                        if (it.contains(CURSOR)) {
                            cursor = it[CURSOR].asText()
                        }
                    }
                }
            }
            if (node.contains(PAGE_INFO)) {
                pageInfo = ctx.readTreeAsValue(node[PAGE_INFO], Connection.PageInfo::class.java)
            }
            if (node.contains(TOTAL_COUNT)) {
                totalCount = node[TOTAL_COUNT].asInt()
            }
        }
    }
}