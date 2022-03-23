package org.babyfish.kimmer.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName

fun KSType.asClassName(): ClassName =
    (declaration as KSClassDeclaration).asClassName()

fun KSClassDeclaration.asClassName(simpleNameMapper: ((String) -> String)? = null): ClassName {
    val simpleName = simpleName.asString()
    if (simpleNameMapper === null) {
        return ClassName(packageName.asString(), simpleName)
    }
    return ClassName(packageName.asString(), simpleNameMapper(simpleName))
}

fun KSClassDeclaration.asClassNameByArray(simpleNameMapper: (String) -> Array<String>): ClassName {
    val simpleName = simpleName.asString()
    return ClassName(packageName.asString(), *simpleNameMapper(simpleName))
}

val KSClassDeclaration.isImmutableAbstract: Boolean
    get() = this.annotations.any {
        it.annotationType.resolve().declaration.qualifiedName?.asString() ==
            "$KIMMER_PACKAGE.Abstract"
    }