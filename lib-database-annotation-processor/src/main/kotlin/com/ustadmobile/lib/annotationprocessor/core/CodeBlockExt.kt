package com.ustadmobile.lib.annotationprocessor.core

import com.squareup.kotlinpoet.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

/**
 * Generate a delegation style function call, e.g.
 * varName.callMethod(param1, param2, param3)
 *
 * @param varName the variable name for the object that has the desired function
 * @param funSpec the function spec that we are generating a delegated call for
 */
fun CodeBlock.Builder.addDelegateFunctionCall(varName: String, funSpec: FunSpec) : CodeBlock.Builder {
    return add("$varName.${funSpec.name}(")
            .add(funSpec.parameters.filter { !isContinuationParam(it.type)}.joinToString { it.name })
            .add(")")
}

/**
 * Add a section to this CodeBlock that will declare a variable for the clientId and get it
 * from the header.
 *
 * e.g.
 * val clientIdVarName = call.request.header("X-nid")?.toInt() ?: 0
 *
 * @param varName the varname to create in the CodeBlock
 * @param serverType SERVER_TYPE_KTOR or SERVER_TYPE_NANOHTTPD
 *
 */
fun CodeBlock.Builder.addGetClientIdHeader(varName: String, serverType: Int) : CodeBlock.Builder {
    takeIf { serverType == DbProcessorKtorServer.SERVER_TYPE_KTOR }
            ?.add("val $varName = %M.request.%M(%S)?.toInt() ?: 0\n",
                    DbProcessorKtorServer.CALL_MEMBER,
                    MemberName("io.ktor.request","header"),
                    "x-nid")
    takeIf { serverType == DbProcessorKtorServer.SERVER_TYPE_NANOHTTPD }
            ?.add("val $varName = _session.headers.get(%S)?.toInt() ?: 0\n",
                "x-nid")

    return this
}

fun CodeBlock.Builder.beginIfNotNullOrEmptyControlFlow(varName: String, isList: Boolean) : CodeBlock.Builder{
    if(isList) {
        beginControlFlow("if(!$varName.isEmpty())")
    }else {
        beginControlFlow("if($varName != null)")
    }

    return this
}

/**
 * Generate insert statements that will insert the TableSyncStatus entities required for each syncable
 * entity on the database in place.
 *
 * e.g.
 * _stmt.executeUpdate("INSERT INTO TableSyncstatus(tsTableId, tsLastChanged, tsLastSynced) VALUES (42, ${systemTimeInMillis(), 0)")")
 * _stmt.executeUpdate("INSERT INTO TableSyncstatus(tsTableId, tsLastChanged, tsLastSynced) VALUES (43, ${systemTimeInMillis(), 0)")")
 * ...
 *
 * @param dbType TypeElement of the database itself
 * @param execSqlFunName the name of the function that must be called to execute SQL
 * @param processingEnv the processing environment
 *
 * @return the same CodeBlock.Builder
 */
fun CodeBlock.Builder.addInsertTableSyncStatuses(dbType: TypeElement,
                                               execSqlFunName: String = "_stmt.executeUpdate",
                                               processingEnv: ProcessingEnvironment) : CodeBlock.Builder{

    syncableEntityTypesOnDb(dbType, processingEnv).forEach {
        val syncableEntityInfo = SyncableEntityInfo(it.asClassName(), processingEnv)
        add("$execSqlFunName(\"INSERT·INTO·TableSyncStatus(tsTableId,·tsLastChanged,·tsLastSynced)·" +
                "VALUES(${syncableEntityInfo.tableId},·\${%M()},·0)\")\n",
            MemberName("com.ustadmobile.door.util", "systemTimeInMillis"))
    }

    return this
}

/**
 * When generating code for a parameter we often want to add some statements that would run directly
 * on a given variable if it is a singular type, or use a forEach loop if it is a list or array.
 *
 * e.g.
 *
 * singular.changeSeqNum = 0
 *
 * or
 *
 * list.forEach {
 *     it.changeSeqNum = 0
 * }
 *
 * @param param the ParameterSpec that gives the type and the variable name
 * @param codeBlocks codeBlocks that should be run against each component of the parameter if it is
 * a list or array, or directly against the parameter if it is singular. Each will be automatically
 * prefixed with the parameter name for singular components, or "it" for lists and arrays
 * @return this
 */
fun CodeBlock.Builder.addRunCodeBlocksOnParamComponents(param: ParameterSpec, vararg codeBlocks: CodeBlock) : CodeBlock.Builder {
    if(param.type.isListOrArray()) {
        beginControlFlow("${param.name}.forEach")
        codeBlocks.forEach {
            add("it.")
            add(it)
        }
        endControlFlow()
    }else {
        codeBlocks.forEach {
            add("${param.name}.")
            add(it)
        }
    }

    return this
}