package org.babyfish.kimmer.sql.mutation

import com.fasterxml.uuid.impl.UUIDUtil
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import org.babyfish.kimmer.sql.meta.ScalarProvider
import org.junit.Assume
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import java.util.*
import kotlin.reflect.KClass

fun assumeNativeDatabase() {
    Assume.assumeTrue(
        System.getenv("kimmer-sql-test-native-database") == "true"
    )
}

val MYSQL_DATA_SOURCE =
    SimpleDriverDataSource(
        com.mysql.cj.jdbc.Driver(),
        "jdbc:mysql://localhost:3306/kimmer",
        "root",
        "123456"
    )

val MYSQL_CONNECTION_FACTORY =
    ConnectionFactories.get(
        ConnectionFactoryOptions
            .builder()
            .apply {
                option(ConnectionFactoryOptions.DRIVER, "mysql")
                option(ConnectionFactoryOptions.HOST, "localhost")
                option(ConnectionFactoryOptions.PORT, 3306)
                option(ConnectionFactoryOptions.USER, "root")
                option(ConnectionFactoryOptions.PASSWORD, "123456")
                option(ConnectionFactoryOptions.DATABASE, "kimmer")
            }
            .build()
    )

val POSTGRES_DATA_SOURCE =
    SimpleDriverDataSource(
        org.postgresql.Driver(),
        "jdbc:postgresql://localhost:5432/db",
        "sa",
        "123456"
    )

val POSTGRES_CONNECTION_FACTORY =
    ConnectionFactories.get(
        ConnectionFactoryOptions
            .builder()
            .apply {
                option(ConnectionFactoryOptions.DRIVER, "postgres")
                option(ConnectionFactoryOptions.HOST, "localhost")
                option(ConnectionFactoryOptions.PORT, 5432)
                option(ConnectionFactoryOptions.USER, "sa")
                option(ConnectionFactoryOptions.PASSWORD, "123456")
                option(ConnectionFactoryOptions.DATABASE, "db")
            }
            .build()
    )

val MYSQL_UUID_PROVIDER = object: ScalarProvider<UUID, ByteArray> {

    override val scalarType: KClass<UUID>
        get() = UUID::class

    override val sqlType: KClass<ByteArray>
        get() = ByteArray::class

    override fun toScalar(sqlValue: ByteArray): UUID =
        UUIDUtil.uuid(sqlValue)

    override fun toSql(scalarValue: UUID): ByteArray =
        UUIDUtil.asByteArray(scalarValue)
}