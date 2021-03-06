package se.ansman.kotshi

import com.squareup.javapoet.*
import com.squareup.moshi.Types
import javax.lang.model.element.Element

data class AdapterKey(
        val type: TypeName,
        val jsonQualifiers: List<Element>
) {

    val isGeneric: Boolean
        get() = when (type) {
            is TypeVariableName -> true
            is WildcardTypeName -> false
            else -> false
        }

    fun asRuntimeType(typeVariableAccessor: (TypeVariableName) -> CodeBlock): CodeBlock =
            type.asRuntimeType(typeVariableAccessor)

    private fun TypeName.asRuntimeType(typeVariableAccessor: (TypeVariableName) -> CodeBlock): CodeBlock =
            when (this) {
                is ParameterizedTypeName ->
                    CodeBlock.builder()
                            .add("\$T.newParameterizedType(\$T.class", Types::class.java, rawType)
                            .apply {
                                for (typeArgument in typeArguments) {
                                    add(", ")
                                    add(typeArgument.asRuntimeType(typeVariableAccessor))
                                }
                            }
                            .add(")")
                            .build()
                is TypeVariableName -> typeVariableAccessor(this)
                else -> CodeBlock.of("\$T.class", this)
            }
}