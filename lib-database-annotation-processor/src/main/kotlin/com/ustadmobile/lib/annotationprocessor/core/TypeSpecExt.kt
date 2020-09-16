package com.ustadmobile.lib.annotationprocessor.core

import androidx.room.Database
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.util.*
import javax.lang.model.element.ExecutableElement
import com.ustadmobile.door.RepositoryConnectivityListener
import com.ustadmobile.door.TableChangeListener
import javax.lang.model.element.TypeElement

/**
 * Add a method or property that overrides the given accessor. The ExecutableElement could be a
 * getter method - in which case we need to add a Kotlin property with a getter method. Otherwise we
 * add an overriding function
 */
fun TypeSpec.Builder.addAccessorOverride(methodName: String, returnType: TypeName, codeBlock: CodeBlock) {
    if(methodName.startsWith("get")) {
        val propName = methodName.substring(3, 4).toLowerCase(Locale.ROOT) + methodName.substring(4)
        val getterFunSpec = FunSpec.getterBuilder().addCode(codeBlock)
        addProperty(PropertySpec.builder(propName, returnType,
                KModifier.OVERRIDE).getter(getterFunSpec.build()).build())
    }else {
        addFunction(FunSpec.builder(methodName)
                .addModifiers(KModifier.OVERRIDE)
                .returns(returnType)
                .addCode(codeBlock)
                .build())
    }
}

fun TypeSpec.Builder.addAccessorOverride(executableElement: ExecutableElement, codeBlock: CodeBlock)  =
        addAccessorOverride(executableElement.simpleName.toString(), executableElement.returnType.asTypeName(), codeBlock)

/**
 * Implement the DoorDatabaseRepository methods for add/remove mirror etc. by delegating to a
 * RepositoryHelper.
 */
internal fun TypeSpec.Builder.addRepositoryHelperDelegateCalls(delegatePropName: String,
    clientSyncMgrVarName: String? = null): TypeSpec.Builder {
    addProperty(PropertySpec.builder("connectivityStatus", INT)
            .addModifiers(KModifier.OVERRIDE)
            .mutable(true)
            .getter(FunSpec.getterBuilder()
                    .addCode("return $delegatePropName.connectivityStatus\n")
                    .build())
            .setter(FunSpec.setterBuilder()
                    .addParameter("newValue", INT)
                    .addCode("$delegatePropName.connectivityStatus = newValue\n")
                    .apply { takeIf { clientSyncMgrVarName != null }
                            ?.addCode("$clientSyncMgrVarName?.connectivityStatus = newValue\n") }
                    .build())
            .build())
    addFunction(FunSpec.builder("addMirror")
            .returns(INT)
            .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
            .addParameter("mirrorEndpoint", String::class)
            .addParameter("initialPriority", INT)
            .addCode("return $delegatePropName.addMirror(mirrorEndpoint, initialPriority)\n")
            .build())
    addFunction(FunSpec.builder("removeMirror")
            .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
            .addParameter("mirrorId", INT)
            .addCode("$delegatePropName.removeMirror(mirrorId)\n")
            .build())
    addFunction(FunSpec.builder("updateMirrorPriorities")
            .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
            .addParameter("newPriorities", Map::class.asClassName().parameterizedBy(INT, INT))
            .addCode("$delegatePropName.updateMirrorPriorities(newPriorities)\n")
            .build())
    addFunction(FunSpec.builder("activeMirrors")
            .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
            .addCode("return $delegatePropName.activeMirrors()\n")
            .build())
    addFunction(FunSpec.builder("addWeakConnectivityListener")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("listener", RepositoryConnectivityListener::class)
            .addCode("$delegatePropName.addWeakConnectivityListener(listener)\n")
            .build())
    addFunction(FunSpec.builder("removeWeakConnectivityListener")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("listener", RepositoryConnectivityListener::class)
            .addCode("$delegatePropName.removeWeakConnectivityListener(listener)\n")
            .build())
    addFunction(FunSpec.builder("addTableChangeListener")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("listener", TableChangeListener::class)
            .addCode("$delegatePropName.addTableChangeListener(listener)\n")
            .build())
    addFunction(FunSpec.builder("removeTableChangeListener")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("listener", TableChangeListener::class)
            .addCode("$delegatePropName.removeTableChangeListener(listener)\n")
            .build())
    addFunction(FunSpec.builder("handleTableChanged")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("tableName", String::class)
            .addCode("$delegatePropName.handleTableChanged(tableName)\n")
            .build())



    return this
}

/**
 * Add a property
 */
fun TypeSpec.Builder.addDbVersionProperty(dbTypeElement: TypeElement): TypeSpec.Builder {
    addProperty(PropertySpec.builder("dbVersion", INT)
            .addModifiers(KModifier.OVERRIDE)
            .getter(FunSpec.getterBuilder()
                    .addCode("return ${dbTypeElement.getAnnotation(Database::class.java).version}")
                    .build())
            .build())

    return this
}
