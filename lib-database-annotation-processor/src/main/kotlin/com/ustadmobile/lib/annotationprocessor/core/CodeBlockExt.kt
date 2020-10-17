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

