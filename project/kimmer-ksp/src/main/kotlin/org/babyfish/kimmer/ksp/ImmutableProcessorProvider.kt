package org.babyfish.kimmer.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class ImmutableProcessorProvider: SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {

        for (key in environment.options.keys) {
            if (key.startsWith(ENV_OPTION_PREFIX) && !ENV_OPTION_KEYS.contains(key)) {
                throw GeneratorException(
                    "ksp option '$key' starts with '$ENV_OPTION_PREFIX', " +
                        "but it is any one of these keys: $ENV_OPTION_KEYS"
                )
            }
        }

        val draft = environment.parseBoolean(ENV_OPTION_DRAFT) ?: true
        val table = environment.parseBoolean(ENV_OPTION_TABLE) ?: false
        val collectionJoinOnlyForSubQuery = environment.parseBoolean(ENV_OPTION_TABLE_COLLECTION_JOIN) ?: false
        if (!draft && !table) {
            throw GeneratorException(
                "Both 'immutable.draft' and 'immutable.table' of ksp options are false, " +
                    "this is not allowed because it makes this ksp module meaningless"
            )
        }
        return ImmutableProcessor(
            draft,
            table,
            collectionJoinOnlyForSubQuery,
            environment.codeGenerator,
            environment.logger
        )
    }

    private fun SymbolProcessorEnvironment.parseBoolean(optionName: String): Boolean? =
        options[optionName]?.let {
            when (it) {
                "true" -> true
                "false" -> false
                else -> throw GeneratorException("'${optionName}' of ksp options can only be true or false")
            }
        }
}


private const val ENV_OPTION_PREFIX = "kimmer."
private const val ENV_OPTION_DRAFT = "${ENV_OPTION_PREFIX}draft"
private const val ENV_OPTION_TABLE = "${ENV_OPTION_PREFIX}table"
private const val ENV_OPTION_TABLE_COLLECTION_JOIN = "${ENV_OPTION_PREFIX}table.collection-join-only-for-sub-query"
private val ENV_OPTION_KEYS = setOf(ENV_OPTION_DRAFT, ENV_OPTION_TABLE, ENV_OPTION_TABLE_COLLECTION_JOIN)