package org.babyfish.kimmer.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.babyfish.kimmer.graphql.Connection
import java.lang.UnsupportedOperationException

class ConnectionEdgeSerializer : StdSerializer<Connection.Edge<*>>(Connection.Edge::class.java) {

    override fun serialize(
        value: Connection.Edge<*>,
        gen: JsonGenerator,
        provider: SerializerProvider
    ) {
        throw UnsupportedOperationException(
            "'Connection.Edge' cannot be serialized directly, " +
                "please serialize its own Connection"
        )
    }
}