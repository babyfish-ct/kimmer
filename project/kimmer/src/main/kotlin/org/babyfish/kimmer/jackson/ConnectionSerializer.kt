package org.babyfish.kimmer.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.graphql.impl.EDGES
import org.babyfish.kimmer.graphql.impl.JACKSON_GENERIC_TYPE
import org.babyfish.kimmer.graphql.impl.PAGE_INFO
import org.babyfish.kimmer.graphql.impl.TOTAL_COUNT
import org.babyfish.kimmer.graphql.meta.ConnectionType
import org.babyfish.kimmer.meta.ImmutableType

class ConnectionSerializer : StdSerializer<Connection<*>>(Connection::class.java) {

    override fun serialize(
        value: Connection<*>,
        gen: JsonGenerator,
        provider: SerializerProvider
    ) {
        val connectionType = ImmutableType.fromInstance(value) as ConnectionType

        gen.writeStartObject()

        gen.writeFieldName(JACKSON_GENERIC_TYPE)
        gen.writeString(connectionType.edgeType.nodeType.kotlinType.qualifiedName)

        if (Immutable.isLoaded(value, Connection<*>::edges)) {
            val nodeSerializer = provider.findValueSerializer(
                connectionType.edgeType.nodeType.kotlinType.java
            )
            gen.writeFieldName(EDGES)
            gen.writeStartArray()
            for (edge in value.edges) {
                serializeEdge(nodeSerializer, edge, gen, provider)
            }
            gen.writeEndArray()
        }

        if (Immutable.isLoaded(value, Connection<*>::pageInfo)) {
            gen.writeFieldName(PAGE_INFO)
            provider
                .findValueSerializer(Connection.PageInfo::class.java)
                .serialize(value.pageInfo, gen, provider)
        }

        if (Immutable.isLoaded(value, Connection<*>::totalCount)) {
            gen.writeNumberField(TOTAL_COUNT, value.totalCount)
        }

        gen.writeEndObject()
    }

    private fun serializeEdge(
        nodeSerializer: JsonSerializer<Any>,
        value: Connection.Edge<*>,
        gen: JsonGenerator,
        provider: SerializerProvider
    ) {
        gen.writeStartObject()
        if (Immutable.isLoaded(value, Connection.Edge<*>::node)) {
            gen.writeFieldName("node")
            nodeSerializer.serialize(value.node, gen, provider)
        }
        if (Immutable.isLoaded(value, Connection.Edge<*>::cursor)) {
            gen.writeFieldName("cursor")
            gen.writeString(value.cursor)
        }
        gen.writeEndObject()
    }
}