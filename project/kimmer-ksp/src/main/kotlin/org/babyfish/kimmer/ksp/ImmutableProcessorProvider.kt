package org.babyfish.kimmer.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class ImmutableProcessorProvider: SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val draft = environment.parseBoolean("immutable.draft") ?: true
        val table = environment.parseBoolean("immutable.table") ?: false
        if (!draft && table) {
            throw GeneratorException(
                "Both 'immutable.draft' and 'immutable.table' of ksp options are false, " +
                    "this is not allowed because it makes this ksp module meaningless"
            )
        }
        return ImmutableProcessor(
            draft,
            table,
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