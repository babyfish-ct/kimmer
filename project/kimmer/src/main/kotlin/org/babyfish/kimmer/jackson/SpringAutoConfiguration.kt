package org.babyfish.kimmer.jackson

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnBean(ImmutableModule::class)
open class SpringAutoConfiguration {

    @Bean
    open fun immutableModule() = ImmutableModule()
}