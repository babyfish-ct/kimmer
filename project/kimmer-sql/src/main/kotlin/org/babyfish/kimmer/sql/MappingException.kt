package org.babyfish.kimmer.sql

import java.lang.RuntimeException

class MappingException(message: String, cause: Throwable? = null): RuntimeException(message, cause)