package com.nullvalidateannotation

//import org.yanex.takenoko.*
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class NullValidatorClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class NullValidatorField

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("com.nullvalidateannotation.NullValidatorClass", "com.nullvalidateannotation.NullValidatorField")
@SupportedOptions(NullValidateProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
//@AutoService(Processor::class)
class NullValidateProcessor: AbstractProcessor() {

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(NullValidatorClass::class.java)
        if (annotatedElements.isEmpty()) return false

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: run {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Can't find the target directory for generated Kotlin files.")
            return false
        }

        for (element in annotatedElements) {
            element.toTypeElementOrNull()?.let {
                val filePath = it.simpleName.toString().plus("_nullvalidate_generated")
                val fileSpec = FileSpec.builder("", filePath)
                    .addImport(it.asClassName().packageName, it.simpleName.toString())
                    .addFunction(generateValidateFunction(it.asType().asTypeName(), it.enclosedElements))
                    .build()

                fileSpec.writeTo(File(kaptKotlinGeneratedDir))
            }
        }

        return true
    }

    private fun generateValidateFunction(typeName: TypeName, enclosedElements: MutableList<out Element>): FunSpec {
        val builder = FunSpec.builder("validate").returns(Boolean::class)

        builder.receiver(typeName)
        builder.addStatement("val result = ")

        builder.addStatement(
            if (enclosedElements.isEmpty()) {
                "true"
            } else {
                getFieldsToValidate(enclosedElements)
                    .map { "${it.simpleName} != null" }
                    .joinToString("\n&& ")
            }
        )

//        enclosedElements.filter {
//            it.kind == ElementKind.FIELD && it.getAnnotation(NullValidatorField::class.java) != null
//        }.map {
//            "${it.simpleName} != null"
//        }.forEachIndexed { index, element ->
//            if (index != 0) {
//                builder.addStatement("&&")
//            }
//            builder.addStatement("${element.simpleName} != null")
//        }

        builder.addStatement("return result")

        return builder.build()
    }

    private fun getFieldsToValidate(enclosedElements: List<Element>): List<Element> {
        val allFieldsWithAnnotation = getAllFieldsWithNullValidatorFieldAnnotation(enclosedElements)
        return if (allFieldsWithAnnotation.isNotEmpty()) {
            allFieldsWithAnnotation
        } else {
            getAllFields(enclosedElements)
        }
    }

    private fun getAllFields(enclosedElements: List<Element>): List<Element> {
        return enclosedElements.filter {
            it.kind == ElementKind.FIELD
        }
    }

    private fun getAllFieldsWithNullValidatorFieldAnnotation(enclosedElements: List<Element>): List<Element> {
        return enclosedElements.filter {
            it.kind == ElementKind.FIELD && it.getAnnotation(NullValidatorField::class.java) != null
        }
    }

    fun Element.toTypeElementOrNull(): TypeElement? {
        if (this !is TypeElement) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Invalid element type, class expected", this)
            return null
        }

        return this
    }
}