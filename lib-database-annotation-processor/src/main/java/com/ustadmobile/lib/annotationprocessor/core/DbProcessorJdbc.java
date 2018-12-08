package com.ustadmobile.lib.annotationprocessor.core;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmCallbackUtil;
import com.ustadmobile.lib.database.annotation.UmClearAll;
import com.ustadmobile.lib.database.annotation.UmDbContext;
import com.ustadmobile.lib.database.annotation.UmDelete;
import com.ustadmobile.lib.database.annotation.UmEmbedded;
import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmOnConflictStrategy;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmQueryFindByPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmSyncFindAllChanges;
import com.ustadmobile.lib.database.annotation.UmSyncFindLocalChanges;
import com.ustadmobile.lib.database.annotation.UmSyncFindUpdateable;
import com.ustadmobile.lib.database.annotation.UmSyncIncoming;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncOutgoing;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.database.jdbc.DbChangeListener;
import com.ustadmobile.lib.database.jdbc.JdbcDatabaseUtils;
import com.ustadmobile.lib.database.jdbc.PreparedStatementArrayProxy;
import com.ustadmobile.lib.database.jdbc.UmJdbcDatabase;
import com.ustadmobile.lib.database.jdbc.UmLiveDataJdbc;
import com.ustadmobile.lib.db.UmDbWithExecutor;
import com.ustadmobile.lib.db.sync.UmRepositoryDb;
import com.ustadmobile.lib.db.sync.UmRepositoryUtils;
import com.ustadmobile.lib.db.sync.entities.SyncStatus;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.io.IOException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.tools.Diagnostic;

import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorCore.OPT_JDBC_OUTPUT;
import static com.ustadmobile.lib.database.jdbc.JdbcDatabaseUtils.PRODUCT_NAME_POSTGRES;
import static com.ustadmobile.lib.database.jdbc.JdbcDatabaseUtils.PRODUCT_NAME_SQLITE;
import static com.ustadmobile.lib.database.jdbc.JdbcDatabaseUtils.SUPPORTED_DB_PRODUCT_NAMES;

/**
 * Generates a JDBC based implementation of database classes annotated with @UmDatabase and their
 * associated DAOs.
 */
public class DbProcessorJdbc extends AbstractDbProcessor {

    private static String SUFFIX_JDBC_DBMANAGER = "_Jdbc";

    private static final String SUFFIX_JDBC_DAO = "_JdbcDaoImpl";

    private static final char SQL_IDENTIFIER_CHAR = ' ';

    private File dbTmpFile;

    //Map of fully qualified database class name to a connection that has that database
    private Map<String, DataSource> nameToDataSourceMap = new HashMap<>();

    public DbProcessorJdbc() {
        setOutputDirOpt(OPT_JDBC_OUTPUT);
    }

    public void processDbClass(TypeElement dbType, String destination) throws IOException {
        String jdbcDbClassName = dbType.getSimpleName() + SUFFIX_JDBC_DBMANAGER;
        PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(dbType);
        ParameterizedTypeName dbChangeListenersMapType = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(DbChangeListener.class),
                ClassName.get(JdbcDatabaseUtils.DbChangeListenerRequest.class));

        ClassName initialContextClassName = ClassName.get(InitialContext.class);
        TypeSpec.Builder jdbcDbTypeSpec = TypeSpec.classBuilder(jdbcDbClassName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(dbType))
                .addSuperinterface(ClassName.get(UmJdbcDatabase.class))
                .addSuperinterface(UmDbWithExecutor.class)
                .addJavadoc("Generated code - DO NOT EDIT!\n")
                .addField(ClassName.get(Object.class), "_context", Modifier.PRIVATE)
                .addField(ClassName.get(DataSource.class), "_dataSource", Modifier.PRIVATE)
                .addField(ClassName.get(ExecutorService.class), "_executor", Modifier.PRIVATE)
                .addField(dbChangeListenersMapType, "_dbChangeListeners", Modifier.PRIVATE)
                .addField(TypeName.BOOLEAN, "_arraySupported", Modifier.PRIVATE)
                .addField(ParameterizedTypeName.get(List.class, UmRepositoryDb.class), "_repositories",
                        Modifier.PRIVATE)
                .addField(String.class, "_jdbcProductName", Modifier.PRIVATE)
                .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(TypeName.get(Object.class), "context")
                    .addParameter(TypeName.get(String.class), "dbName")
                    .addCode(CodeBlock.builder().add("\tthis._context = context;\n")
                        .add("this._dbChangeListeners = new $T<>();\n", HashMap.class)
                        .add("this._repositories = new $T<>();\n", ArrayList.class)
                        .beginControlFlow("try ")
                            .add("$T iContext = new $T();\n", initialContextClassName, initialContextClassName)
                            .add("_executor = $T.newCachedThreadPool();\n", Executors.class)
                            .add("this._dataSource = (DataSource)iContext.lookup(\"java:/comp/env/jdbc/\"+dbName);\n")
                            .add("this._arraySupported = $T.isArraySupported(this._dataSource);\n",
                                    JdbcDatabaseUtils.class)
                            .add("$T.setIsMasterFromJndi(this, dbName, iContext);\n",
                                    JdbcDatabaseUtils.class)
                            .add("createAllTables();\n")
                        .endControlFlow()
                        .beginControlFlow("catch($T e)",
                                ClassName.get(NamingException.class))
                            .add("throw new RuntimeException(e);\n")
                        .endControlFlow()
                        .build())
                .build())
                .addMethod(MethodSpec.methodBuilder("getExecutor")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.get(ExecutorService.class))
                        .addCode("return this._executor;\n").build())
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Runnable.class, "runnable")
                        .addCode("this._executor.execute(runnable);\n")
                        .build())
                .addMethod(MethodSpec.methodBuilder("getConnection")
                        .addException(ClassName.get(SQLException.class))
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.get(Connection.class))
                        .addCode("return this._dataSource.getConnection();\n")
                        .build())
                .addMethod(MethodSpec.methodBuilder("handleTablesChanged")
                        .addParameter(ArrayTypeName.of(String.class), "tablesChanged")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .varargs()
                        .addCode("$T.handleTablesChanged(_dbChangeListeners, tablesChanged);\n",
                                JdbcDatabaseUtils.class)
                        .build())

                .addMethod(MethodSpec.methodBuilder("addDbChangeListener")
                        .addParameter(ClassName.get(JdbcDatabaseUtils.DbChangeListenerRequest.class),
                                "listenerRequest")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addCode(CodeBlock.builder()
                                .add("$T.addDbChangeListener(listenerRequest, _dbChangeListeners);\n",
                                        JdbcDatabaseUtils.class)
                                .build())
                        .build())
                .addMethod(MethodSpec.methodBuilder("removeDbChangeListener")
                        .addParameter(ClassName.get(JdbcDatabaseUtils.DbChangeListenerRequest.class),
                            "listenerRequest")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addCode(CodeBlock.builder()
                            .add("$T.removeDbChangeListener(listenerRequest, _dbChangeListeners);\n",
                                    JdbcDatabaseUtils.class)
                            .build())
                        .build())
                .addMethod(MethodSpec.methodBuilder("isArraySupported")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.BOOLEAN)
                        .addCode("return this._arraySupported;\n")
                        .build())
                .addMethod(MethodSpec.methodBuilder("getJdbcProductName")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(String.class)
                        .addCode(CodeBlock.builder()
                                .beginControlFlow("if(_jdbcProductName == null)")
                                    .add("try(").indent()
                                        .add("$T _connection = getConnection();\n", Connection.class)
                                    .unindent().beginControlFlow(")")
                                        .add("$T _metaData = _connection.getMetaData();\n",
                                            DatabaseMetaData.class)
                                        .add("_jdbcProductName = _metaData.getDatabaseProductName();\n")
                                    .nextControlFlow("catch ($T _sqlE)", SQLException.class)
                                        .add("_sqlE.printStackTrace();\n")
                                    .endControlFlow()
                                .endControlFlow()
                                .add("return _jdbcProductName;\n").build()).build());

        jdbcDbTypeSpec.addMethod(generateCreateTablesMethod(dbType));
        jdbcDbTypeSpec.addMethod(generateCreateSeqNumTriggersMethod(dbType));

        for(Element subElement : dbType.getEnclosedElements()) {
            if (subElement.getKind() != ElementKind.METHOD)
                continue;

            ExecutableElement dbMethod = (ExecutableElement)subElement;
            if(dbMethod.getModifiers().contains(Modifier.STATIC))
                continue;

            if(!dbMethod.getModifiers().contains(Modifier.ABSTRACT))
                continue;


            MethodSpec.Builder overrideSpec =  MethodSpec.overriding(dbMethod);
            TypeElement returnTypeElement = (TypeElement)processingEnv.getTypeUtils().asElement(
                    dbMethod.getReturnType());

            if(dbMethod.getAnnotation(UmDbContext.class) != null) {
                overrideSpec.addCode("return _context;\n");
            }else if(dbMethod.getAnnotation(UmClearAll.class) != null) {
                addClearAllTablesCodeToMethod(dbType, overrideSpec, SQL_IDENTIFIER_CHAR);
            }else if(dbMethod.getAnnotation(UmRepository.class) != null) {
                addGetRepositoryMethod(dbType, dbMethod, overrideSpec, "_repositories");
            }else if(dbMethod.getAnnotation(UmSyncOutgoing.class) != null) {
                overrideSpec = generateDbSyncOutgoingMethod(dbType, dbMethod);
            }else {
                String daoFieldName = "_" + returnTypeElement.getSimpleName();
                jdbcDbTypeSpec.addField(TypeName.get(dbMethod.getReturnType()), daoFieldName, Modifier.PRIVATE);
                ClassName daoImplClassName = ClassName.get(
                        processingEnv.getElementUtils().getPackageOf(returnTypeElement).getQualifiedName().toString(),
                        returnTypeElement.getSimpleName() + SUFFIX_JDBC_DAO);

                overrideSpec.beginControlFlow("if($L == null)", daoFieldName)
                        .addCode("$L = new $T(this);\n", daoFieldName, daoImplClassName)
                    .endControlFlow()
                    .addCode("return $L;\n", daoFieldName);
            }


            jdbcDbTypeSpec.addMethod(overrideSpec.build());

        }




        JavaFile databaseJavaFile = JavaFile.builder(packageElement.getQualifiedName().toString(), jdbcDbTypeSpec.build())
                .indent("    ").build();
        writeJavaFileToDestination(databaseJavaFile, destination);

        //now create an in temporary file implementation of this database, this will be used when generating the DAOs
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dbTmpFile = File.createTempFile(dbType.getQualifiedName().toString(), ".db");

        messager.printMessage(Diagnostic.Kind.NOTE,
                "DbProcessorJdbc: creating temporary database in: " +
                        dbTmpFile.getAbsolutePath());

        dataSource.setUrl("jdbc:sqlite:" + dbTmpFile.getAbsolutePath());
        String createSql = null;
        try(
            Connection connection = dataSource.getConnection();
            Statement stmt = connection.createStatement();
        ) {
            nameToDataSourceMap.put(dbType.getQualifiedName().toString(), dataSource);
            for(TypeElement entityType : findEntityTypes(dbType)){
                createSql = makeCreateTableStatement(entityType, SQL_IDENTIFIER_CHAR,
                        PRODUCT_NAME_SQLITE);
                stmt.execute(createSql);
            }
        }catch(SQLException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Error attempting to create database for "
                + dbType.getQualifiedName().toString() + ": executing SQL \"" + createSql + "\": " +
                e.getMessage());
        }


    }

    /**
     * Generate a createAllTables method that will run the SQL required to generate all tables for
     * all entities on the given database type.
     *
     * @param dbType TypeElement representing the class annotated with @UmDatabase
     * @return MethodSpec with a generated implementation to create all tables for this database
     */
    protected MethodSpec generateCreateTablesMethod(TypeElement dbType) {
        MethodSpec.Builder createMethod = MethodSpec.methodBuilder("createAllTables");
        CodeBlock.Builder createCb = CodeBlock.builder();
        createCb.add("try (\n").indent()
                .add("$T _connection = getConnection();\n", Connection.class)
                .add("$T _stmt = _connection.createStatement();\n", ClassName.get(Statement.class))
            .unindent().beginControlFlow(")")
                .add("$T _metaData = _connection.getMetaData();\n", DatabaseMetaData.class)
                .add("$T _existingTableNames = $T.getTableNames(_connection);\n",
                        ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)),
                        ClassName.get(JdbcDatabaseUtils.class));

        for(String sqlProductName : SUPPORTED_DB_PRODUCT_NAMES) {
            createCb.beginControlFlow("if($S.equals(_metaData.getDatabaseProductName()))",
                    sqlProductName);
            List<TypeElement> entityTypes = findEntityTypes(dbType);

            //Make sure that the SyncStatus is teh first table
            TypeElement syncStatusTypeEl = processingEnv.getElementUtils().getTypeElement(
                    SyncStatus.class.getName());
            if(entityTypes.contains(syncStatusTypeEl)) {
                entityTypes.remove(syncStatusTypeEl);
                entityTypes.add(0, syncStatusTypeEl);
            }

            for(TypeElement entityTypeElement : entityTypes) {
                addCreateTableStatements(createCb, "_stmt", entityTypeElement,
                        SQL_IDENTIFIER_CHAR, sqlProductName);
            }
            createCb.endControlFlow();
        }



        createCb.endControlFlow() //end try/catch control flow
                .beginControlFlow("catch($T e)\n", SQLException.class)
                .add("throw new $T(e);\n", RuntimeException.class)
                .endControlFlow();



        createMethod.addCode(createCb.build());
        return createMethod.build();
    }

    /**
     * Generate a method to create sequence number triggers on tables. The generated method takes a
     * single class object as an argument. This is designed so it can one day be used for migration
     * purposes etc.
     *
     * @param dbType The TypeElement representing the database class
     * @return MethodSpec with a generated implementation
     */
    protected MethodSpec generateCreateSeqNumTriggersMethod(TypeElement dbType) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("createSeqNumTriggers")
                .addModifiers(Modifier.PROTECTED)
                .addParameter(Class.class, "_entityClass");
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        codeBlock.add("try (").indent()
                .add("$T _connection = getConnection();\n", Connection.class)
                .add("$T _stmt = _connection.createStatement();\n", Statement.class)
            .unindent().beginControlFlow(")");

        for(String sqlProductName : SUPPORTED_DB_PRODUCT_NAMES) {
            codeBlock.beginControlFlow("if($S.equals(getJdbcProductName()))",
                    sqlProductName);
            addCreateTriggersForEntitiesToCodeBlock(sqlProductName,
                    "_stmt.executeUpdate", dbType, codeBlock);
            codeBlock.endControlFlow();
        }

        codeBlock.nextControlFlow("catch($T _sqlE)", SQLException.class)
                .add("_sqlE.printStackTrace();\n")
                .endControlFlow();

        methodBuilder.addCode(codeBlock.build());
        return methodBuilder.build();
    }

    protected void addClearAllTablesCodeToMethod(TypeElement dbType, MethodSpec.Builder builder,
                                                 char identifierQuoteChar) {
        String identifierQuoteStr = StringEscapeUtils.escapeJava(String.valueOf(identifierQuoteChar));
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        codeBlock.add("try(\n").indent()
                .add("$T connection = getConnection();\n", Connection.class)
                .add("$T stmt = connection.createStatement();\n", Statement.class)
                .unindent().beginControlFlow(") ");
        TypeElement syncStatusTypeEL = processingEnv.getElementUtils().getTypeElement(
                SyncStatus.class.getName());


        for(TypeElement entityType : findEntityTypes(dbType)) {
            if(!entityType.equals(syncStatusTypeEL))
                codeBlock.add("stmt.executeUpdate(\"DELETE FROM $1L$2L$1L\");\n",
                    identifierQuoteStr, entityType.getSimpleName().toString());
            else
                codeBlock.add("stmt.executeUpdate(\"UPDATE SyncStatus SET masterChangeSeqNum = 1, " +
                        "localChangeSeqNum = 1, " +
                        "syncedToMasterChangeNum = 0, " +
                        "syncedToLocalChangeSeqNum = 0\");\n");
        }

        codeBlock.nextControlFlow("catch($T e)", SQLException.class)
                .add("e.printStackTrace();\n")
                .endControlFlow();

        builder.addCode(codeBlock.build());
    }





    /**
     * Generates code that will execute CREATE TABLE and CREATE INDEX as required for the given
     * entity.
     *
     * @param codeBlock CodeBlock.Builder that this code will be added to
     * @param stmtVariableName Name of the SQL Statement object variable in the CodeBlock
     * @param entitySpec The TypeElement representing the entity for which the statements are being generated
     * @param quoteChar The quote char used to contain SQL table names e.g. '`' for MySQL and Sqlite
     * @param sqlProductName Name of the SQL database for which we are generating create code (e.g.
     *                       "PostgreSQL", "SQLite")
     *
     * @return SQL for table creation only, to be used within the annotation processor itself
     */
    protected void addCreateTableStatements(CodeBlock.Builder codeBlock, String stmtVariableName,
                                                 TypeElement entitySpec, char quoteChar,
                                                 String sqlProductName) {

        Map<String, List<String>> indexes = new HashMap<>();
        for(VariableElement fieldVariable : DbProcessorUtils.getEntityFieldElements(entitySpec,
                processingEnv)) {
            if(fieldVariable.getAnnotation(UmIndexField.class) != null) {
                indexes.put("index_" + entitySpec.getSimpleName() + '_' + fieldVariable.getSimpleName(),
                        Collections.singletonList(fieldVariable.getSimpleName().toString()));
            }

        }

        codeBlock.beginControlFlow("if(!$T.listContainsStringIgnoreCase(_existingTableNames, $S))",
                JdbcDatabaseUtils.class,
                entitySpec.getSimpleName().toString())
                .add("$L.executeUpdate($S);\n", stmtVariableName,
                        makeCreateTableStatement(entitySpec, quoteChar, sqlProductName));

        if(DbProcessorUtils.entityHasChangeSequenceNumbers(entitySpec, processingEnv)) {
            //we need to add a trigger to handle change sequence numbers
            codeBlock.add("createSeqNumTriggers($T.class);\n", entitySpec);
        }


        for(Map.Entry<String, List<String>> index : indexes.entrySet()) {
            Map<String, String> formatArgs = new HashMap<>();
            formatArgs.put("quot", StringEscapeUtils.escapeJava(String.valueOf(quoteChar)));
            formatArgs.put("index_name", index.getKey());
            formatArgs.put("table_name", entitySpec.getSimpleName().toString());
            formatArgs.put("stmt", stmtVariableName);
            boolean indexFieldCommaNeeded = false;
            StringBuffer indexFieldBuffer = new StringBuffer();
            for(String fieldName : index.getValue()) {
                if(indexFieldCommaNeeded)
                    indexFieldBuffer.append(',');

                indexFieldCommaNeeded = true;
                indexFieldBuffer.append(quoteChar).append(fieldName).append(quoteChar).append(' ');
            }

            formatArgs.put("index_fields", indexFieldBuffer.toString());
            codeBlock.addNamed("$stmt:L.executeUpdate(\"CREATE INDEX $quot:L$index_name:L$quot:L ON $quot:L$table_name:L$quot:L ($index_fields:L)\");\n",
                    formatArgs);
        }

        codeBlock.endControlFlow();
    }

    /**
     * Generate a create table statement for the given entity class
     *
     * @param entitySpec TypeElement representing the entity class to generate a create table statement for
     * @param quoteChar quoteChar used to enclose database identifiers
     *
     * @return Create table SQL as a String
     */
    private String makeCreateTableStatement(TypeElement entitySpec, char quoteChar, String sqlProductName) {
        boolean fieldVariablesStarted = false;

        StringBuffer sbuf = new StringBuffer()
                .append("CREATE TABLE IF NOT EXISTS ").append(quoteChar)
                .append(entitySpec.getSimpleName()).append(quoteChar)
                .append(" (");




        for(VariableElement fieldVariable : DbProcessorUtils.getEntityFieldElements(entitySpec,
                processingEnv)) {
            if (fieldVariablesStarted)
                sbuf.append(", ");

            UmPrimaryKey primaryKeyAnnotation = fieldVariable.getAnnotation(UmPrimaryKey.class);

            sbuf.append(quoteChar).append(fieldVariable.getSimpleName().toString())
                    .append(quoteChar).append(' ');
            if(primaryKeyAnnotation != null && primaryKeyAnnotation.autoIncrement()){
                if(PRODUCT_NAME_POSTGRES.equals(sqlProductName)) {
                    sbuf.append("SERIAL");
                }else {
                    sbuf.append("INTEGER");
                }
            }else {
                sbuf.append(makeSqlTypeDeclaration(fieldVariable));
            }

            if(primaryKeyAnnotation!= null) {
                sbuf.append(" PRIMARY KEY ");
                if(primaryKeyAnnotation.autoIncrement() && !PRODUCT_NAME_POSTGRES.equals(sqlProductName))
                    sbuf.append(" AUTOINCREMENT ");
                sbuf.append(" NOT NULL ");
            }

            fieldVariablesStarted = true;
        }

        sbuf.append(')');


        return sbuf.toString();
    }

    protected String makeSqlTypeDeclaration(VariableElement field) {
        String result = makeSqlTypeDeclaration(field.asType());
        //didn't recognize that.
        if(result == null) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Could not determine SQL data type for field: " + field.getEnclosingElement() +
                            "." + field.getSimpleName().toString());
        }

        return result;
    }

    protected String makeSqlTypeDeclaration(TypeMirror fieldType) {
        switch(fieldType.getKind()) {
            case BOOLEAN:
                return "BOOL";

            case INT:
                return "INTEGER";

            case LONG:
                return "BIGINT";

            case FLOAT:
                return "FLOAT";

            case DOUBLE:
                return "DOUBLE";

            case DECLARED:
                Element typeEl = processingEnv.getTypeUtils().asElement(fieldType);
                if(processingEnv.getElementUtils().getTypeElement("java.lang.String")
                        .equals(typeEl)) {
                    return "TEXT";
                }else if(processingEnv.getElementUtils().getTypeElement("java.lang.Integer")
                    .equals(typeEl)) {
                    return "INTEGER";
                }else if(processingEnv.getElementUtils().getTypeElement("java.lang.Long")
                    .equals(typeEl)) {
                    return "BIGINT";
                }

                break;
        }

        return null;
    }


    @Override
    public void processDbDao(TypeElement daoType, TypeElement dbType, String destination) throws IOException {
        String daoClassName = daoType.getSimpleName() + SUFFIX_JDBC_DAO;
        TypeSpec.Builder jdbcDaoClassSpec = TypeSpec.classBuilder(daoClassName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(daoType))
                .addField(ClassName.get(UmJdbcDatabase.class), "_db", Modifier.PRIVATE)
                .addJavadoc(" GENERATED CODE - DO NOT EDIT! \n")
                .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get(UmJdbcDatabase.class), "_db")
                    .addCode("this._db = _db;\n").build());

        List<ExecutableElement> methodsToImplement = findMethodsToImplement(daoType);
        for(ExecutableElement daoMethod : methodsToImplement) {
            if(daoMethod.getAnnotation(UmInsert.class) != null) {
                addInsertMethod(daoMethod, daoType, jdbcDaoClassSpec, SQL_IDENTIFIER_CHAR);
            }else if(daoMethod.getAnnotation(UmQuery.class) != null){
                addQueryMethod(daoMethod, daoType, dbType, jdbcDaoClassSpec, SQL_IDENTIFIER_CHAR,
                        daoMethod.getAnnotation(UmQuery.class).value());
            }else if(daoMethod.getAnnotation(UmQueryFindByPrimaryKey.class) != null) {
                addQueryMethod(daoMethod, daoType, dbType, jdbcDaoClassSpec, SQL_IDENTIFIER_CHAR,
                        generateFindByPrimaryKeySql(daoType, daoMethod, processingEnv, SQL_IDENTIFIER_CHAR));
            }else if(daoMethod.getAnnotation(UmUpdate.class) != null) {
                addUpdateMethod(daoMethod, daoType, dbType, jdbcDaoClassSpec, SQL_IDENTIFIER_CHAR);
            }else if(daoMethod.getAnnotation(UmDelete.class) != null) {
                addDeleteMethod(daoMethod, jdbcDaoClassSpec, SQL_IDENTIFIER_CHAR);
            }else if(daoMethod.getAnnotation(UmSyncIncoming.class) != null) {
                addSyncHandleIncomingMethod(daoMethod, daoType, jdbcDaoClassSpec, "_db");
            }else if(daoMethod.getAnnotation(UmSyncOutgoing.class) != null) {
                addSyncOutgoing(daoMethod, daoType, jdbcDaoClassSpec, "_db");
            }else if(daoMethod.getAnnotation(UmSyncFindLocalChanges.class) != null) {
                addQueryMethod(daoMethod, daoType, dbType, jdbcDaoClassSpec, SQL_IDENTIFIER_CHAR,
                        generateFindLocalChangesSql(daoType, daoMethod, processingEnv));
            }else if(daoMethod.getAnnotation(UmSyncFindAllChanges.class) != null) {
                addQueryMethod(daoMethod, daoType, dbType, jdbcDaoClassSpec, SQL_IDENTIFIER_CHAR,
                        generateSyncFindAllChanges(daoType, daoMethod, processingEnv));
            }else if(daoMethod.getAnnotation(UmSyncFindUpdateable.class) != null) {
                addQueryMethod(daoMethod, daoType, dbType, jdbcDaoClassSpec, SQL_IDENTIFIER_CHAR,
                        generateSyncFindUpdatable(daoType, daoMethod, processingEnv));
            }
        }


        JavaFile javaFile = JavaFile.builder(processingEnv.getElementUtils().getPackageOf(daoType).toString(),
                jdbcDaoClassSpec.build()).build();
        writeJavaFileToDestination(javaFile, destination);
    }


    public void addInsertMethod(ExecutableElement daoMethod, TypeElement daoType,
                                TypeSpec.Builder daoBuilder,
                                char identifierQuote) {
        MethodSpec.Builder methodBuilder = overrideAndResolve(daoMethod, daoType, processingEnv);
        VariableElement insertedElement = daoMethod.getParameters().get(0);
        boolean isList = false;
        boolean isArray = false;
        TypeMirror resultType;
        DaoMethodInfo methodInfo = new DaoMethodInfo(daoMethod, daoType, processingEnv);
        boolean replaceEnabled =
                daoMethod.getAnnotation(UmInsert.class).onConflict() == UmOnConflictStrategy.REPLACE;

        TypeElement umCallbackTypeElement = processingEnv.getElementUtils().getTypeElement(
                UmCallback.class.getName());
        List<Element> variableTypeElements = getMethodParametersAsElements(daoMethod);
        int asyncParamIndex = variableTypeElements.indexOf(umCallbackTypeElement);
        boolean asyncMethod = asyncParamIndex != -1;

        if(asyncMethod) {
            resultType = ((DeclaredType)daoMethod.getParameters().get(asyncParamIndex)
                    .asType()).getTypeArguments().get(0);
        }else {
            resultType = daoMethod.getReturnType();
        }

        TypeMirror insertParameter = daoMethod.getParameters().get(0).asType();

        if(processingEnv.getElementUtils().getTypeElement(List.class.getName()).equals(
                processingEnv.getTypeUtils().asElement(insertParameter))) {
            isList = true;
            DeclaredType declaredType = (DeclaredType)daoMethod.getParameters().get(0).asType();
            insertParameter = declaredType.getTypeArguments().get(0);
        }else if(insertParameter.getKind().equals(TypeKind.ARRAY)) {
            isArray = true;
            insertParameter = ((ArrayType)insertParameter).getComponentType();
        }

        if(insertParameter.getKind().equals(TypeKind.TYPEVAR)) {
            //should actually provide a list, not a map. It's done by index
            insertParameter = daoType.asType().accept(
                    new TypeVariableResolutionVisitor((TypeVariable)insertParameter), new ArrayList<>());
        }



        TypeElement entityTypeElement = (TypeElement)processingEnv.getTypeUtils().asElement(
                methodInfo.resolveEntityParameterComponentType());

        if(entityTypeElement.getAnnotation(UmEntity.class) == null) {
            messager.printMessage(Diagnostic.Kind.ERROR, daoMethod.getEnclosingElement().getSimpleName() +
                    "." + daoMethod.getSimpleName() +
                    "@UmInsert first parameter must be an entity, array of entities, or list of entities");
            return;
        }


        String preparedStmtVarName = "_stmt";
        String autoIncPreparedStmtVarName = "_stmtAutoInc";

        String identifierQuoteStr = StringEscapeUtils.escapeJava(String.valueOf(identifierQuote));
        CodeBlock.Builder codeBlock = CodeBlock.builder();


        if(asyncMethod) {
            codeBlock.beginControlFlow("_db.getExecutor().execute(() ->");
        }

        if(!isVoid(resultType)) {
            codeBlock.add("$T _result = $L;\n", resultType, defaultValue(resultType));
        }

        List<VariableElement> entityFields = DbProcessorUtils.getEntityFieldElements(
                entityTypeElement, processingEnv, true);

        String postgresReplaceSuffxVarName = null;
        if(replaceEnabled){
            String postgresReplaceSuffx = " ON CONFLICT UPDATE SET ";
            boolean commaRequired = false;
            for(VariableElement field : entityFields) {
                if(field.getAnnotation(UmPrimaryKey.class) != null)
                    continue;

                if(commaRequired)
                    postgresReplaceSuffx += ",";

                postgresReplaceSuffx += field.getSimpleName() + " = excluded." + field.getSimpleName();
                commaRequired = true;
            }

            postgresReplaceSuffxVarName = "_query_PostgresReplaceSuffix";
            codeBlock.add("$T $L = $S;\n", String.class, postgresReplaceSuffxVarName,
                    postgresReplaceSuffx);
        }



        codeBlock.add(generateInsertSqlStatementCodeBlock(entityTypeElement,
                preparedStmtVarName + "_querySql",
                false,  identifierQuoteStr, replaceEnabled,
                postgresReplaceSuffxVarName));
        boolean hasAutoIncrementKey = DbProcessorUtils.entityHasAutoIncrementPrimaryKey(
                entityTypeElement, processingEnv);
        if(hasAutoIncrementKey)
            codeBlock.add(generateInsertSqlStatementCodeBlock(entityTypeElement,
                    autoIncPreparedStmtVarName + "_querySql",true,
                    identifierQuoteStr, replaceEnabled, postgresReplaceSuffxVarName));

        codeBlock.add("try (\n").indent()
                    .add("$T _connection = _db.getConnection();\n", Connection.class)
                    .add("$1T $2L = _connection.prepareStatement($2L_querySql);\n", PreparedStatement.class,
                            preparedStmtVarName);
        if(hasAutoIncrementKey) {
            codeBlock.add("$1T $2L = _connection.prepareStatement($2L_querySql",
                    PreparedStatement.class, autoIncPreparedStmtVarName);
            if(!isVoid(resultType))
                codeBlock.add(", $T.RETURN_GENERATED_KEYS", Statement.class);
            codeBlock.add(");\n");
        }



        codeBlock.unindent().beginControlFlow(")");


        if(isList || isArray) {
            codeBlock.beginControlFlow("for($T _element : $L)", entityTypeElement,
                    daoMethod.getParameters().get(0).getSimpleName().toString());
        }

        String preparedStmtToUseVarName = hasAutoIncrementKey ? "_stmtToUse" : preparedStmtVarName;

        String elVariableName = (isList|| isArray) ?
                "_element" : insertedElement.getSimpleName().toString();
        if(hasAutoIncrementKey) {
            codeBlock.add("$T _stmtToUse = $L.get$L() == $L ? $L : $L;\n",
                    PreparedStatement.class,
                    elVariableName,
                    DbProcessorUtils.capitalize(findPrimaryKey(entityTypeElement).getSimpleName()),
                    defaultValue(findPrimaryKey(entityTypeElement).asType()),
                    autoIncPreparedStmtVarName, preparedStmtVarName);
        }

        for(int i = 0; i < entityFields.size(); i++) {
            boolean isAutoIncrementField = hasAutoIncrementKey
                    && entityFields.get(i).getAnnotation(UmPrimaryKey.class) != null;
            if(isAutoIncrementField) {
                codeBlock.beginControlFlow("if($L != $L)", preparedStmtToUseVarName,
                        autoIncPreparedStmtVarName);
            }
            addSetPreparedStatementValueToCodeBlock(entityTypeElement, preparedStmtToUseVarName,
                    elVariableName,
                    i + 1, entityFields.get(i), codeBlock, daoMethod);
            if(isAutoIncrementField) {
                codeBlock.endControlFlow();
            }
        }

        if(isList || isArray) {
            codeBlock.add("$L.addBatch();\n", preparedStmtToUseVarName)
                .endControlFlow()
                .add("$L.executeBatch();\n", preparedStmtVarName);
            if(hasAutoIncrementKey)
                codeBlock.add("$L.executeBatch();\n", autoIncPreparedStmtVarName);

        }else {
            codeBlock.add("$L.execute();\n", preparedStmtToUseVarName);
        }

        /*
         * Handle getting generated primary keys (if any)
         */
        if(!isVoid(resultType) && hasAutoIncrementKey) {
            codeBlock.add("try (\n").indent()
                    .add("$T generatedKeys = $L.getGeneratedKeys();\n", ResultSet.class,
                            autoIncPreparedStmtVarName)
                .unindent().beginControlFlow(")");

            boolean resultIsList = DbProcessorUtils.isList(resultType, processingEnv);
            boolean resultIsArray = resultType.getKind().equals(TypeKind.ARRAY);
            if(resultIsList || resultIsArray) {
                TypeMirror primaryKeyType = DbProcessorUtils.getArrayOrListComponentType(resultType,
                        processingEnv);
                String arrayListVarName = resultIsList ? "_result" : "_resultList";

                ParameterizedTypeName listTypeName =ParameterizedTypeName.get(
                        ClassName.get(ArrayList.class),
                        ClassName.get(DbProcessorUtils.boxIfPrimitive(primaryKeyType, processingEnv)));

                if(resultIsArray)
                    codeBlock.add("$T ", listTypeName);

                codeBlock.add("$L = new $T();\n", arrayListVarName, listTypeName);

                codeBlock.beginControlFlow("while(generatedKeys.next())")
                        .add("$L.add(generatedKeys.get$L(1));\n", arrayListVarName,
                                getPreparedStatementSetterGetterTypeName(primaryKeyType))
                        .endControlFlow();

                if(resultIsArray) {
                    codeBlock.add("_result = _resultList.toArray(new $T[_resultList.size()]);\n",
                            primaryKeyType);
                }
            }else {
                codeBlock.beginControlFlow("if(generatedKeys.next())")
                        .add("_result = generatedKeys.get$L(1);\n",
                                getPreparedStatementSetterGetterTypeName(resultType))
                        .endControlFlow();
            }

            codeBlock.nextControlFlow("catch($T pkE)", SQLException.class)
                    .add("pkE.printStackTrace();\n")
                    .endControlFlow();
        }

        codeBlock.add("_db.handleTablesChanged($S);\n", entityTypeElement.getSimpleName().toString());


        codeBlock.nextControlFlow("catch($T e)", SQLException.class)
                .add("e.printStackTrace();\n").endControlFlow();

        if(!isVoid(resultType) && !asyncMethod) {
            codeBlock.add("return _result;\n");
        }

        if(asyncMethod) {
            codeBlock.add("$T.onSuccessIfNotNull($L, $L);\n", UmCallbackUtil.class,
                    daoMethod.getParameters().get(asyncParamIndex).getSimpleName().toString(),
                    isVoid(resultType) ? "null" : "_result");

            codeBlock.endControlFlow(")");
        }

        methodBuilder.addCode(codeBlock.build());
        daoBuilder.addMethod(methodBuilder.build());
    }

    private CodeBlock generateInsertSqlStatementCodeBlock(TypeElement entityTypeElement,
                                                          String querySqlVarName,
                                                          boolean isAutoIncrementInsert,
                                                          String identifierQuoteStr,
                                                          boolean replaceEnabled,
                                                          String postgresReplaceSuffixVarName) {

        CodeBlock.Builder codeBlock = CodeBlock.builder()
                .add("$1T $2L = \"INSERT INTO $3L$4L$3L (",
                    String.class, querySqlVarName, identifierQuoteStr,
                        entityTypeElement.getSimpleName());

        List<VariableElement> entityFields = DbProcessorUtils.getEntityFieldElements(
                entityTypeElement, processingEnv, true);
        boolean commaRequired = false;
        int numFields = 0;

        for(VariableElement fieldElement : entityFields) {
            if(isAutoIncrementInsert
                    && fieldElement.getAnnotation(UmPrimaryKey.class) != null
                    && fieldElement.getAnnotation(UmPrimaryKey.class).autoIncrement())
                continue;

            if(commaRequired)
                codeBlock.add(", ");

            codeBlock.add(identifierQuoteStr).add(fieldElement.getSimpleName().toString())
                    .add(identifierQuoteStr);
            commaRequired = true;
            numFields++;
        }
        codeBlock.add(") VALUES (");
        for(int i = 0; i < numFields; i++) {
            codeBlock.add("?");
            if(i < numFields - 1)
                codeBlock.add(", ");
        }
        codeBlock.add(")\";\n");

        if(replaceEnabled) {
            codeBlock.beginControlFlow("if($T.PRODUCT_NAME_POSTGRES.equals(_db.getJdbcProductName()))",
                            JdbcDatabaseUtils.class)
                        .add("$L += $L;\n", querySqlVarName, postgresReplaceSuffixVarName)
                    .nextControlFlow("else if($T.PRODUCT_NAME_SQLITE.equals(_db.getJdbcProductName()))",
                            JdbcDatabaseUtils.class)
                        .add("$1L = \"REPLACE\" + $1L.substring(" + "INSERT".length() + ");\n",
                                querySqlVarName)
                    .endControlFlow();
        }


        return codeBlock.build();
    }


    /**
     * Generate an implementation for methods annotated with UmUpdate
     *
     * @param daoMethod daoMethod to generate an implementation for
     * @param dbType The database class
     * @param daoClass The DAO class being implemented
     * @param daoBuilder The builder for the dao being generated, to which the new method will be added
     * @param identifierQuote The quote character to use to quote SQL identifiers
     */
    public void addUpdateMethod(ExecutableElement daoMethod, TypeElement daoClass,
                                TypeElement dbType, TypeSpec.Builder daoBuilder,
                                char identifierQuote) {
        String identifierQuoteStr = StringEscapeUtils.escapeJava(String.valueOf(identifierQuote));
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        MethodSpec.Builder methodBuilder = overrideAndResolve(daoMethod, daoClass, processingEnv);
        DaoMethodInfo methodInfo = new DaoMethodInfo(daoMethod, daoClass, processingEnv);

        TypeMirror resultType = methodInfo.resolveResultType();

        TypeMirror entityType = methodInfo.resolveEntityParameterComponentType();
        TypeElement entityTypeElement = (TypeElement)processingEnv.getTypeUtils().asElement(entityType);

        boolean isListOrArray = methodInfo.hasEntityListParam() || methodInfo.hasEntityArrayParam();

        if(methodInfo.isAsyncMethod()) {
            codeBlock.beginControlFlow("_db.getExecutor().execute(() ->");
        }

        codeBlock.add("$T numUpdates = 0;\n", TypeName.INT);

        codeBlock.add("try (\n").indent()
                .add("$T _connection = _db.getConnection();\n", Connection.class)
                .add("$T _stmt = _connection.prepareStatement(\"", PreparedStatement.class)
                .add("UPDATE $L$L$L SET ", identifierQuoteStr, entityTypeElement.getSimpleName().toString(),
                        identifierQuoteStr);

        Map<String, Integer> fieldNameToPositionMap = new HashMap<>();

        boolean commaRequired = false;
        int positionCounter = 1;
        Element pkElement = null;
        for(Element subElement : DbProcessorUtils.getEntityFieldElements(entityTypeElement,
                processingEnv, true)) {
            if(subElement.getAnnotation(UmPrimaryKey.class) != null) {
                pkElement = subElement;
                continue;
            }


            if(commaRequired)
                codeBlock.add(", ");

            codeBlock.add("$L$L$L = ?", identifierQuoteStr, subElement.getSimpleName().toString(),
                    identifierQuoteStr);
            commaRequired = true;
            fieldNameToPositionMap.put(subElement.getSimpleName().toString(),
                    positionCounter);
            positionCounter++;
        }

        if(pkElement == null){
            String message = " UmUpdate method : on primary key field found on " +
                    entityTypeElement.getQualifiedName().toString();
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod) + ": " + message);
            throw new IllegalArgumentException(message);
        }
        fieldNameToPositionMap.put(pkElement.getSimpleName().toString(), positionCounter);

        codeBlock.add(" WHERE $L$L$L = ?\");\n", identifierQuoteStr,
                pkElement.getSimpleName().toString(), identifierQuoteStr);

        codeBlock.unindent().beginControlFlow(")");

        String entityName = isListOrArray ? "_element" :
                daoMethod.getParameters().get(0).getSimpleName().toString();

        if(isListOrArray) {
            codeBlock.beginControlFlow("for($T _element : $L)",
                    entityTypeElement, daoMethod.getParameters().get(0).getSimpleName().toString());
        }

        for(Map.Entry<String, Integer> entry : fieldNameToPositionMap.entrySet()) {
            String propertyName = entry.getKey();
            List<ExecutableElement> getterCallChain = findGetterOrSetter("get", daoMethod,
                    propertyName, entityTypeElement, new ArrayList<>(), true);
            if(getterCallChain == null || getterCallChain.size() != 1) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        formatMethodForErrorMessage(daoMethod) +
                                " could not find setter for " + propertyName);
                return;
            }

            codeBlock.add("_stmt.$L($L, $L.$L());\n",
                    getPreparedStatementSetterMethodName(getterCallChain.get(0).getReturnType()),
                    entry.getValue(),
                    entityName,
                    getterCallChain.get(0).getSimpleName().toString());
        }

        if(isListOrArray) {
            codeBlock.add("_stmt.addBatch();\n")
                    .endControlFlow()
                    .add("numUpdates = $T.sumUpdateTotals(_stmt.executeBatch());\n",
                            JdbcDatabaseUtils.class);
        }else {
            codeBlock.add("numUpdates = _stmt.executeUpdate();\n");
        }

        codeBlock.beginControlFlow("if(numUpdates > 0)")
                .add("_db.handleTablesChanged($S);\n", entityTypeElement.getSimpleName().toString())
                .endControlFlow();

        codeBlock.nextControlFlow("catch($T e)", SQLException.class)
                .add("e.printStackTrace();\n")
                .endControlFlow();



        if(methodInfo.isAsyncMethod()) {
            codeBlock.add("$T.onSuccessIfNotNull($L, $L);\n",
                    UmCallbackUtil.class, daoMethod.getParameters().get(methodInfo.getAsyncParamIndex())
                            .getSimpleName().toString(),
                    isVoid(resultType) ? "null" : "numUpdates");
            codeBlock.endControlFlow(")");
        }else if(!isVoid(resultType)) {
            codeBlock.add("return numUpdates;\n");
        }

        methodBuilder.addCode(codeBlock.build());
        daoBuilder.addMethod(methodBuilder.build());
    }


    public void addDeleteMethod(ExecutableElement daoMethod, TypeSpec.Builder daoBuider,
                                char identifierQuote) {
        MethodSpec.Builder methodBuilder = MethodSpec.overriding(daoMethod);
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        boolean isListOrArray = false;
        String identifierQuoteStr = StringEscapeUtils.escapeJava(String.valueOf(identifierQuote));

        TypeElement umCallbackTypeElement = processingEnv.getElementUtils().getTypeElement(
                UmCallback.class.getName());
        List<Element> variableTypeElements = getMethodParametersAsElements(daoMethod);
        int asyncParamIndex = variableTypeElements.indexOf(umCallbackTypeElement);
        boolean asyncMethod = asyncParamIndex != -1;

        TypeMirror resultType;
        if(asyncMethod) {
            codeBlock.beginControlFlow("_db.getExecutor().execute(() -> ");
            DeclaredType declaredType = (DeclaredType)daoMethod.getParameters().get(asyncParamIndex)
                    .asType();
            resultType = declaredType.getTypeArguments().get(0);
        }else {
            resultType = daoMethod.getReturnType();
        }

        TypeMirror entityType = daoMethod.getParameters().get(0).asType();
        TypeElement entityTypeElement = entityType.getKind().equals(TypeKind.DECLARED) ?
                (TypeElement)processingEnv.getTypeUtils().asElement(entityType) : null;

        if(entityTypeElement != null &&
                entityTypeElement.equals(processingEnv.getElementUtils().getTypeElement(
                        List.class.getName()))) {
            entityType = ((DeclaredType)entityType).getTypeArguments().get(0);
            entityTypeElement = (TypeElement)processingEnv.getTypeUtils().asElement(entityType);
            isListOrArray = true;
        }else if(entityType.getKind().equals(TypeKind.ARRAY)) {
            entityType = ((ArrayType)entityType).getComponentType();
            entityTypeElement = (TypeElement)processingEnv.getTypeUtils().asElement(entityType);
            isListOrArray = true;
        }


        VariableElement pkElement = findPrimaryKey(entityTypeElement);
        if(pkElement == null) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod) + " no primary key found on" +
                    entityTypeElement.getQualifiedName());
            return;
        }

        codeBlock.add("int numDeleted = 0;\n")
                .add("try (\n").indent()
                    .add("$T connection = _db.getConnection();\n", Connection.class)
                    .add("$T stmt = connection.prepareStatement(\"", PreparedStatement.class)
                    .add("DELETE FROM $1L$2L$1L WHERE $1L$3L$1L = ?\");\n", identifierQuoteStr,
                            entityTypeElement.getSimpleName().toString(),
                            pkElement.getSimpleName().toString())
                .unindent().beginControlFlow(")");

        if(isListOrArray) {
            codeBlock.beginControlFlow("for($T _entity : $L)",
                    entityTypeElement, daoMethod.getParameters().get(0).getSimpleName().toString());
        }

        PreparedStatement stmt;
        String pkGetterMethod = pkElement.getSimpleName().toString();
        pkGetterMethod = Character.toUpperCase(pkGetterMethod.charAt(0))
                + pkGetterMethod.substring(1);
        codeBlock.add("stmt.$L(1, $L.get$L());\n",
                getPreparedStatementSetterMethodName(pkElement.asType()),
                isListOrArray ? "_entity" : daoMethod.getParameters().get(0).getSimpleName().toString(),
                pkGetterMethod);

        if(isListOrArray) {
            codeBlock.add("stmt.addBatch();\n")
                    .endControlFlow()
                    .add("numDeleted = $T.sumUpdateTotals(stmt.executeBatch());\n",
                            JdbcDatabaseUtils.class);
        }else {
            codeBlock.add("numDeleted = stmt.executeUpdate();\n");
        }

        codeBlock.beginControlFlow("if(numDeleted > 0)")
                .add("_db.handleTablesChanged($S);\n", entityTypeElement.getSimpleName().toString())
                .endControlFlow();

        codeBlock.nextControlFlow("catch($T e)", SQLException.class)
                .add("e.printStackTrace();\n")
                .endControlFlow();

        if(asyncMethod) {
            codeBlock.add("$T.onSuccessIfNotNull($L, $L);\n",
                    UmCallbackUtil.class,
                    daoMethod.getParameters().get(asyncParamIndex).getSimpleName(),
                    isVoid(resultType) ? "null" : "numDeleted")
                    .endControlFlow(")");
        }else if(!isVoid(resultType)) {
            codeBlock.add("return numDeleted;\n");
        }

        methodBuilder.addCode(codeBlock.build());
        daoBuider.addMethod(methodBuilder.build());
    }


    public void addQueryMethod(ExecutableElement daoMethod, TypeElement daoType, TypeElement dbType,
                               TypeSpec.Builder daoBuilder,
                               char identifierQuote, String querySql) {
        //we need to run the query, find the columns, and then determine the appropriate setter methods to run
        DaoMethodInfo daoMethodInfo = new DaoMethodInfo(daoMethod, daoType, processingEnv);
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        MethodSpec.Builder methodBuilder = AbstractDbProcessor.overrideAndResolve(daoMethod, daoType,
                processingEnv);
        String querySqlTrimmedLower = querySql.toLowerCase().trim();

        boolean isUpdateOrDelete = false;
        if(querySqlTrimmedLower.startsWith("update") || querySqlTrimmedLower.startsWith("delete")) {
            isUpdateOrDelete = true;
        }

        TypeElement umCallbackTypeElement = processingEnv.getElementUtils().getTypeElement(
                UmCallback.class.getName());
        List<Element> variableTypeElements = getMethodParametersAsElements(daoMethod);
        int asyncParamIndex = variableTypeElements.indexOf(umCallbackTypeElement);
        boolean asyncMethod = asyncParamIndex != -1;


        if(asyncMethod) {
            codeBlock.beginControlFlow("_db.getExecutor().execute(() -> ");
        }

        TypeMirror resultType = daoMethodInfo.resolveResultType();

        List<String> namedParams = getNamedParameters(querySql);
        String preparedStmtSql = querySql;
        for(String paramName : namedParams) {
            preparedStmtSql = preparedStmtSql.replace(":" + paramName, "?");
        }

        boolean returnsList = false;
        boolean returnsArray = false;
        boolean returnsLiveData = false;

        Element resultTypeElement = processingEnv.getTypeUtils().asElement(resultType);

        //TODO: This is temporary only for purposes of testing compilation
        if(resultTypeElement != null && resultTypeElement.equals(processingEnv
            .getElementUtils().getTypeElement(UmProvider.class.getName()))) {
            codeBlock.add("return null;\n");
            methodBuilder.addCode(codeBlock.build());
            daoBuilder.addMethod(methodBuilder.build());
            return;
        }


        if(resultTypeElement != null && resultTypeElement.equals(processingEnv
            .getElementUtils().getTypeElement(UmLiveData.class.getName()))) {

            List<String> tableList;
            try {
                Select select = (Select) CCJSqlParserUtil.parse(preparedStmtSql);
                TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
                tableList = tablesNamesFinder.getTableList(select);
                codeBlock.add("// Table names = " + Arrays.toString(tableList.toArray()))
                        .add("\n");
            }catch(JSQLParserException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, formatMethodForErrorMessage(daoMethod) +
                        " exception parsing query 2 \"" + preparedStmtSql + "\" to determine tables: " + e.getMessage());
                return;
            }

            returnsLiveData = true;
            DeclaredType declaredResultType = (DeclaredType)resultType;
            resultType = declaredResultType.getTypeArguments().get(0);
            resultTypeElement = processingEnv.getTypeUtils().asElement(resultType);
            codeBlock.beginControlFlow("return new $T<$T>() ", UmLiveDataJdbc.class, resultType)
                .beginControlFlow("")
                    .add("super.setDatabase(_db);\n")
                    .add("super.setTablesToMonitor(");
            boolean commaRequired = false;
            for(String tableName : tableList) {
                if(commaRequired)
                    codeBlock.add(", ");
                codeBlock.add("$S", tableName);
                commaRequired = true;
            }
            codeBlock.add(");\n");

            codeBlock.endControlFlow()
                .beginControlFlow("public $T fetchValue()", resultType);

            //add code here so that we generate the next stuff in the onFetch method of UmLiveDataJdbc
        }

        if(resultType.getKind().equals(TypeKind.ARRAY)) {
            ArrayType arrayType = (ArrayType)resultType;
            resultType = arrayType.getComponentType();
            resultTypeElement = processingEnv.getTypeUtils().asElement(resultType);
            codeBlock.add("$T[] result = null;\n", resultType);
            returnsArray = true;
        }else if(resultTypeElement != null && resultTypeElement.equals(processingEnv
                .getElementUtils().getTypeElement(List.class.getCanonicalName()))) {
            DeclaredType declaredResultType = (DeclaredType)resultType;

            resultType = declaredResultType.getTypeArguments().get(0);
            resultTypeElement = processingEnv.getTypeUtils().asElement(resultType);
            returnsList = true;

            codeBlock.add("$T<$T> result = new $T<>();\n", List.class,
                    resultType.getKind().isPrimitive() ? processingEnv.getTypeUtils().boxedClass(
                            (PrimitiveType)resultType) : resultType,
                    ArrayList.class);
        }else if(!isVoid(resultType)){
            codeBlock.add("$T result = $L;\n", resultType, defaultValue(resultType));
        }

        TypeName returnTypeName = TypeName.get(resultType);
        boolean primitiveOrStringReturn = returnTypeName.isPrimitive()
                || returnTypeName.isBoxedPrimitive()
                || returnTypeName.equals(ClassName.get(String.class));

        if(!isUpdateOrDelete) {
            codeBlock.add("$T resultSet = null;\n", ResultSet.class);
        }


        Map<String, String> arrayParameters = findArrayParameters(daoMethod.getParameters());
        for(String arrayParamName : arrayParameters.keySet()) {
            codeBlock.add("$T _$L_sqlArr = null;\n", Array.class, arrayParamName);
        }

        codeBlock.add("$T _querySql = $S;\n", String.class, preparedStmtSql);
        if(daoMethodInfo.hasArrayOrListParameter()) {
            codeBlock.beginControlFlow("if($T.PRODUCT_NAME_POSTGRES.equals(_db.getJdbcProductName()))",
                    JdbcDatabaseUtils.class)
                    .add("_querySql = $T.convertSelectInForPostgres(_querySql);\n", JdbcDatabaseUtils.class)
                    .endControlFlow();
        }

        codeBlock.add("try (\n").indent()
            .add("$T connection = _db.getConnection();\n", Connection.class);
        if(daoMethodInfo.hasArrayOrListParameter()) {
            codeBlock.add("$T stmt = _db.isArraySupported() ? connection.prepareStatement(_querySql) : " +
                            "new $T(_querySql, connection);\n", PreparedStatement.class,
                    PreparedStatementArrayProxy.class);
        }else {
            codeBlock.add("$T stmt = connection.prepareStatement(_querySql);\n",
                    PreparedStatement.class);
        }


        codeBlock.unindent().beginControlFlow(")");


        for(int i = 0; i < namedParams.size(); i++) {
            VariableElement paramVariableElement = null;
            for(VariableElement variableElement : daoMethod.getParameters()) {
                if(variableElement.getSimpleName().toString().equals(namedParams.get(i))) {
                    paramVariableElement = variableElement;
                    break;
                }
            }

            if(paramVariableElement == null) {
                messager.printMessage(Diagnostic.Kind.ERROR, formatMethodForErrorMessage(daoMethod)
                        + " has no parameter named " + namedParams.get(i));
                return;
            }

            String paramVariableName = paramVariableElement.getSimpleName().toString();

            TypeMirror sqlVariableType = paramVariableElement.asType();
            if(arrayParameters.containsKey(paramVariableElement.getSimpleName().toString())) {
                boolean isArray = paramVariableElement.asType().getKind().equals(TypeKind.ARRAY);
                boolean isList = !isArray;//otherwise it would not be in the arrayParameters hashtable
                TypeMirror arrayElementType;
                String arrayVariableName;

                if(isList) {
                    arrayElementType = ((DeclaredType)paramVariableElement.asType())
                            .getTypeArguments().get(0);
                    codeBlock.add("$1T[] _$2L_arr = $2L.toArray(new $1T[$2L.size()]);\n",
                            arrayElementType, paramVariableName);
                    arrayVariableName = "_" + paramVariableName + "_arr";
                }else {
                    arrayElementType = ((ArrayType)paramVariableElement.asType())
                            .getComponentType();
                    arrayVariableName = paramVariableName;
                }

                codeBlock.add("_$1L_sqlArr = _db.isArraySupported() ? " +
                        " connection.createArrayOf($2S, $3L) : $4T.createArrayOf($2S, $3L);\n",
                        paramVariableName, makeSqlTypeDeclaration(arrayElementType),
                        arrayVariableName, PreparedStatementArrayProxy.class);
                sqlVariableType = processingEnv.getElementUtils().getTypeElement(Array.class.getName())
                        .asType();
                paramVariableName = "_" + paramVariableName + "_sqlArr";
            }

            codeBlock.add("stmt.$L($L, $L);\n",
                    getPreparedStatementSetterMethodName(sqlVariableType),  i + 1,
                    paramVariableName);
        }

        if(!isUpdateOrDelete) {
            addMapResultSetToValuesToCodeBlock(dbType, daoMethod, querySql, primitiveOrStringReturn,
                    returnsList, returnsArray, resultType, resultTypeElement, codeBlock);
        }else {
            TypeMirror updateResultTypeMirror = resultType;
            if(updateResultTypeMirror.getKind().equals(TypeKind.DECLARED)) {
                TypeElement updateResultTypeElement = (TypeElement)resultTypeElement;
                if(updateResultTypeElement != null && updateResultTypeElement.equals(
                        processingEnv.getElementUtils().getTypeElement("java.lang.Void"))) {
                    updateResultTypeMirror = processingEnv.getTypeUtils().getNoType(TypeKind.VOID);
                }
            }

            if(updateResultTypeMirror.getKind().equals(TypeKind.DECLARED)) {
                try {
                    updateResultTypeMirror = processingEnv.getTypeUtils().unboxedType(resultType);
                }catch(Exception e) {
                    messager.printMessage(Diagnostic.Kind.ERROR,
                            formatMethodForErrorMessage(daoMethod) + " : " +
                        "update or delete query method must return void or integer type.");
                }
            }

            if(updateResultTypeMirror.getKind().equals(TypeKind.VOID)){
                codeBlock.add("int result = stmt.executeUpdate();\n");
            }else {
                codeBlock.add("result = stmt.executeUpdate();\n");
            }

            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tableNames;
            try {
                net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(preparedStmtSql);
                if(querySqlTrimmedLower.startsWith("update")){
                    tableNames = tablesNamesFinder.getTableList((Update)statement);
                }else if(querySqlTrimmedLower.startsWith("delete")) {
                    tableNames = tablesNamesFinder.getTableList((Delete)statement);
                }else {
                    messager.printMessage(Diagnostic.Kind.ERROR,
                            formatMethodForErrorMessage(daoMethod) + ": " +
                            " query was not select, expecting update or delete statement to " +
                            " determine table changes, found something else: " + preparedStmtSql);
                    throw new IllegalArgumentException("Query must be select, update, or delete");
                }

                codeBlock.beginControlFlow("if(result > 0)")
                        .add("_db.handleTablesChanged(");
                boolean commaRequired = false;
                for(String tableName : tableNames){
                    if(commaRequired)
                        codeBlock.add(", ");
                    codeBlock.add("$S", tableName);

                    commaRequired = true;
                }
                codeBlock.add(");\n").endControlFlow();
            }catch(Exception je) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        formatMethodForErrorMessage(daoMethod, daoType) +
                    " exception parsing update/delete query: " + je.getClass().getSimpleName() +
                    ": " + je.getMessage());
            }



        }

        codeBlock.nextControlFlow("catch($T e)", SQLException.class)
            .add("e.printStackTrace();\n");

        boolean hasFinally = !isUpdateOrDelete || !arrayParameters.isEmpty();
        if(hasFinally) {
            codeBlock.nextControlFlow("finally");
        }

        if(!isUpdateOrDelete) {
            codeBlock.add("$T.closeQuietly(resultSet);\n", JdbcDatabaseUtils.class);
        }

        for(String arrayParamName : arrayParameters.keySet()) {
            codeBlock.add("$T.freeArrayQuietly(_$L_sqlArr);\n", JdbcDatabaseUtils.class, arrayParamName);
        }

        codeBlock.endControlFlow();

        if(asyncMethod) {
            codeBlock.add("$T.onSuccessIfNotNull($L, $L);\n",
                    UmCallbackUtil.class,
                    daoMethod.getParameters().get(asyncParamIndex).getSimpleName().toString(),
                    !isVoid(resultType) ? "result" : "null");
            codeBlock.endControlFlow(")");
        }else if(!daoMethod.getReturnType().getKind().equals(TypeKind.VOID)){
            codeBlock.add("return result;\n");
        }

        if(returnsLiveData) {
            //end the method and inner class
            codeBlock.endControlFlow().endControlFlow().add(";\n");
        }


        methodBuilder.addCode(codeBlock.build());
        daoBuilder.addMethod(methodBuilder.build());

    }

    /**
     * Generates a block of code that will convert the JDBC ResultSet from a SELECT query into the
     * desired result, which could be a single entity / POJO object, a list of objects, or String/
     * primitive result types.
     *
     * @param dbType TypeElement representing the database class
     * @param daoMethod ExecutableElement representing the DAO Method for which an implementation is
     *                  being generated
     * @param querySql The SQL query as per the UmQuery annotation
     * @param primitiveOrStringReturn true if the method returns a primitive result or string, false
     *                                otherwise (e.g. returns a POJO/entity)
     * @param returnsList true if the method returns a java.util.List, false otherwise
     * @param returnsArray true if the method returns an Array, false otherwise
     * @param resultType The TypeMirror representing the return type.
     * @param resultTypeElement Element representation of resultType
     * @param codeBlock CodeBlock.Builder the generated code will be added to
     */
    protected void addMapResultSetToValuesToCodeBlock(TypeElement dbType, ExecutableElement daoMethod,
                                                      String querySql,
                                                      boolean primitiveOrStringReturn,
                                                      boolean returnsList, boolean returnsArray,
                                                      TypeMirror resultType,
                                                      Element resultTypeElement,
                                                      CodeBlock.Builder codeBlock) {
        codeBlock.add("resultSet = stmt.executeQuery();\n");


        try(
            Connection dbConnection = nameToDataSourceMap.get(dbType.getQualifiedName().toString())
                    .getConnection();
            Statement stmt = dbConnection.createStatement();
            ResultSet results = stmt.executeQuery(querySql);
        ) {
            ResultSetMetaData metaData = results.getMetaData();
            if(primitiveOrStringReturn && metaData.getColumnCount() != 1) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        formatMethodForErrorMessage(daoMethod) +
                                ": returns a String or primitive. SQL must have 1 column only. " +
                                "found " + metaData.getColumnCount() + " columns");
            }


            if(returnsList) {
                codeBlock.beginControlFlow("while(resultSet.next())");
            } else if (returnsArray) {
                codeBlock.add("$T<$T> resultList = new $T<>();\n", ArrayList.class,
                        resultType.getKind().isPrimitive() ? processingEnv.getTypeUtils()
                            .boxedClass((PrimitiveType)resultType) : resultType,
                        ArrayList.class)
                        .beginControlFlow("while(resultSet.next())");
            } else {
                codeBlock.beginControlFlow("if(resultSet.next())");
            }

            if(!primitiveOrStringReturn){
                addCreateNewEntityFromResultToCodeBlock((TypeElement)resultTypeElement,  daoMethod,
                        "entity", "resultSet", metaData, codeBlock);
            }else{
                codeBlock.add("$T entity = resultSet.get$L(1);\n", resultType,
                        getPreparedStatementSetterGetterTypeName(resultType));
            }

            if(returnsList) {
                codeBlock.add("result.add(entity);\n")
                        .endControlFlow();
            }else if(returnsArray) {
                codeBlock.add("resultList.add(entity);\n")
                        .endControlFlow();
                if(!resultType.getKind().isPrimitive()) {
                    codeBlock.add("result = resultList.toArray(new $T[resultList.size()]);\n",
                            resultType);
                }else {
                    codeBlock.add(
                        "result = $T.toPrimitive(resultList.toArray(new $T[resultList.size()]));\n",
                        ArrayUtils.class,
                        processingEnv.getTypeUtils().boxedClass((PrimitiveType)resultType));
                }

            }else {
                codeBlock.add("result = entity;\n")
                        .endControlFlow();
            }

        } catch(SQLException e) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Exception generating query method for: " +
                            formatMethodForErrorMessage(daoMethod) + ": " + e.getMessage());
        }
    }

    /**
     * Generate a block of code that will initialize a new POJO / Entity from an JDBC ResultSet.
     * It will generate code along the lines of:
     *
     *  EntityType entityVariableName = new EntityType();
     *  entityVariableName.setUid(resultSetVariableName.getInt(1));
     *  entityVariableName.setName(resultSetVariableName.getString(2));
     *  entityVariableName.setEmbeddedObject(new EmbeddedObject());
     *  entityVariableName.getEmbeddedObject().setEmbeddedValue(resultSetVariableName.getInt(3));
     *
     *
     * @param entityElement The POJO / Entity that is being initialized
     * @param daoMethodElement The DAO method this is being generated for. Used to generate useful
     *                         error messages
     * @param entityVariableName The variable name to use for the entity being initialized
     * @param resultSetVariableName The variable name of the JDBC ResultSet from which values are
     *                              to be fetched
     * @param metaData JDBC Metadata used to find the columns that are returned by running the
     *                 query, so that they can be mapped to to setters.
     * @param codeBlock CodeBlock.Builder to add the generate code block to
     */
    private void addCreateNewEntityFromResultToCodeBlock(TypeElement entityElement,
                                                         ExecutableElement daoMethodElement,
                                                         String entityVariableName,
                                                         String resultSetVariableName,
                                                         ResultSetMetaData metaData,
                                                         CodeBlock.Builder codeBlock) {
        codeBlock.add("$T $L = new $T();\n", entityElement.asType(), entityVariableName,
                entityElement.asType());
        try {
            List<String> initializedEmbeddedObjects = new ArrayList<>();
            for(int i = 0; i < metaData.getColumnCount(); i++) {
                List<ExecutableElement> callChain = findSetterMethod(daoMethodElement,
                        metaData.getColumnLabel(i + 1), entityElement, new ArrayList<>(), true);
                if(callChain == null || callChain.isEmpty()) {
                    messager.printMessage(Diagnostic.Kind.WARNING,
                            formatMethodForErrorMessage(daoMethodElement) +
                                    " : Could not find setter for field: '" +
                                    metaData.getColumnLabel(i + 1) +"' on return class " +
                                    entityElement.getQualifiedName());
                    continue;
                }

                CodeBlock.Builder setFromResultCodeBlock = CodeBlock.builder()
                        .add(entityVariableName);
                String callChainStr = "";

                for(int j = 0; j < callChain.size(); j++) {
                    ExecutableElement method = callChain.get(j);
                    callChainStr += "." + method.getSimpleName() + "()";
                    if(method.getSimpleName().toString().startsWith("set")) {
                        HashMap<String, Object> paramMap = new HashMap<>();
                        paramMap.put("setterName", method.getSimpleName().toString());
                        paramMap.put("resultSetGetter",
                                "get" + getPreparedStatementSetterGetterTypeName(method.getParameters()
                                        .get(0).asType()));
                        paramMap.put("resultSetVarName", resultSetVariableName);
                        paramMap.put("colname", metaData.getColumnLabel(i+1));
                        setFromResultCodeBlock.addNamed(".$setterName:L($resultSetVarName:L.$resultSetGetter:L($colname:S));\n",
                                paramMap);
                    }else if(method.getSimpleName().toString().startsWith("get")) {
                        //this is an embedded field

                        //Check if the embedded field has been initialized with a blank new object.
                        // If not, we must do so to avoid a NullPointerException
                        if(!initializedEmbeddedObjects.contains(callChainStr)) {
                            CodeBlock.Builder initBuilder = CodeBlock.builder()
                                    .add(entityVariableName);
                            for(int k = 0; k < j; k++) {
                                initBuilder.add(".$L()", callChain.get(k).getSimpleName().toString());
                            }

                            initBuilder.add(".set$L(new $T());\n",
                                    callChain.get(j).getSimpleName().toString()
                                            .substring("get".length()),
                                    callChain.get(j).getReturnType());
                            codeBlock.add(initBuilder.build());
                            initializedEmbeddedObjects.add(callChainStr);
                        }

                        setFromResultCodeBlock.add(".$L()", method.getSimpleName().toString());
                    }
                }

                codeBlock.add(setFromResultCodeBlock.build());
            }
        }catch(SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * For SQL with named parameters (e.g. "SELECT * FROM Table WHERE uid = :paramName") return a
     * list of all named parameters.
     *
     * @param querySql SQL that may contain named parameters
     * @return String list of named parameters (e.g. "paramName"). Empty if no named parameters are present.
     */
    private List<String> getNamedParameters(String querySql) {
        List<String> namedParams = new ArrayList<>();
        boolean insideQuote = false;
        boolean insideDoubleQuote = false;
        char lastC = 0;
        int startNamedParam = -1;
        for(int i  = 0; i < querySql.length(); i++) {
            char c = querySql.charAt(i);
            if(c == '\'' && lastC != '\\')
                insideQuote = !insideQuote;
            if(c == '\"' && lastC != '\\')
                insideDoubleQuote = !insideDoubleQuote;

            if(!insideQuote && !insideDoubleQuote) {
                if(c == ':'){
                    startNamedParam = i;
                }else if(!Character.isLetterOrDigit(c) && startNamedParam != -1){
                    //process the parameter
                    namedParams.add(querySql.substring(startNamedParam + 1, i ));
                    startNamedParam = -1;
                }else if(i == (querySql.length()-1) && startNamedParam != -1) {
                    namedParams.add(querySql.substring(startNamedParam + 1, i +1));
                    startNamedParam = -1;
                }
            }


            lastC = c;
        }

        return namedParams;
    }

    private Map<String, String> findArrayParameters(List<? extends VariableElement> paramList) {
        Map<String, String> arrayNameToTypeMap = new HashMap<>();
        TypeElement listTypeElement = processingEnv.getElementUtils().getTypeElement(List.class.getName());

        for(VariableElement param : paramList) {
            if(listTypeElement.equals(processingEnv.getTypeUtils().asElement(param.asType()))) {
                DeclaredType declaredType =(DeclaredType)param.asType();
                arrayNameToTypeMap.put(param.getSimpleName().toString(),
                        makeSqlTypeDeclaration(declaredType.getTypeArguments().get(0)));
            }else if(param.asType().getKind().equals(TypeKind.ARRAY)) {
                ArrayType arrayType = (ArrayType)param.asType();
                arrayNameToTypeMap.put(param.getSimpleName().toString(),
                        makeSqlTypeDeclaration(arrayType.getComponentType()));
            }
        }

        return arrayNameToTypeMap;
    }


    /**
     * Find the setter method for a given row name (e.g. "fieldName") on a given Java object (e.g.
     * POJO or Entity). This method will recursively check parent classes and any field with the
     * UmEmbedded annotation.
     *
     * @see UmEmbedded
     *
     * @param methodType "get" or "set
     * @param daoMethod The daoMethod currently being generated. Used to generate meaningful error
     *                  messages.
     * @param rowName The rowName as it was returned by the query
     * @param entityElement The java object to search to find an appropriate setter method
     * @param callChain The current call chain to get to this object.
     * @param checkEmbedded If true, look through any objects annotated @UmEmbedded
     *
     * @return A list of methods that represent the chain of methods that need to be called.
     *  For simple setters that are directly on the object itself, this is simply a list with the
     *  setter method. If the field resides in an embedded object, this includes the getter methods
     *  to reach the setter method e.g. getEmbeddedObject, setField.
     */
    private List<ExecutableElement> findGetterOrSetter(String methodType,
                                                       ExecutableElement daoMethod,
                                                       String rowName,
                                                       TypeElement entityElement,
                                                       List<ExecutableElement> callChain,
                                                       boolean checkEmbedded) {
        //go through the methods on this TypeElement to find a setter
        String methodPostfix = Character.toUpperCase(rowName.charAt(0)) + rowName.substring(1);
        String targetMethodName = methodType + methodPostfix;
        String altTargetMethodName = methodType.equals("get") ? "is" + methodPostfix : null;

        for(Element subElement : entityElement.getEnclosedElements()) {
            if(subElement.getKind() != ElementKind.METHOD)
                continue;

            String methodName = subElement.getSimpleName().toString();
            if(methodName.equals(targetMethodName) || methodName.equals(altTargetMethodName)) {
                callChain.add((ExecutableElement)subElement);
                return callChain;
            }
        }


        //check @UmEmbedded objects
        if(checkEmbedded) {
            for(Element subElement : entityElement.getEnclosedElements()) {
                if(subElement.getKind().equals(ElementKind.FIELD)
                        && subElement.getAnnotation(UmEmbedded.class) != null) {
                    VariableElement varElement = (VariableElement)subElement;
                    String getterName = subElement.getSimpleName().toString();
                    getterName = "get" + Character.toUpperCase(getterName.charAt(0)) +
                            getterName.substring(1);

                    ExecutableElement getterMethod = null;
                    for(Element subElement2 : entityElement.getEnclosedElements()){
                        if(subElement2.getSimpleName().toString().equals(getterName)
                                && subElement2.getKind().equals(ElementKind.METHOD)
                                && ((ExecutableElement)subElement2).getParameters().isEmpty()){
                            getterMethod = (ExecutableElement)subElement2;
                            break;
                        }
                    }

                    if(getterMethod == null) {
                        messager.printMessage(Diagnostic.Kind.ERROR,
                                formatMethodForErrorMessage(daoMethod) + ": " +
                                    entityElement.getQualifiedName() + "." + subElement.getSimpleName() +
                                    " is annotated with @UmEmbedded but has no getter method");
                        return null;
                    }

                    callChain.add(getterMethod);
                    List<ExecutableElement> retVal = findGetterOrSetter(methodType,daoMethod, rowName,
                            (TypeElement)processingEnv.getTypeUtils().asElement(varElement.asType()),
                            callChain, checkEmbedded);

                    if(retVal != null)
                        return retVal;
                    else
                        callChain.remove(getterMethod);
                }
            }
        }


        //Check parent classes
        if(entityElement.getSuperclass() != null
                && !entityElement.getSuperclass().getKind().equals(TypeKind.NONE)) {
            return findGetterOrSetter(methodType, daoMethod, rowName, (TypeElement) processingEnv.getTypeUtils()
                    .asElement(entityElement.getSuperclass()), callChain, checkEmbedded);
        }else {
            return null;
        }
    }

    private List<ExecutableElement> findSetterMethod(ExecutableElement daoMethod,
                                                     String rowName,
                                                     TypeElement entityElement,
                                                     List<ExecutableElement> callChain,
                                                     boolean checkEmbedded) {
        return findGetterOrSetter("set", daoMethod, rowName, entityElement, callChain,
                checkEmbedded);
    }




    private void addSetPreparedStatementValueToCodeBlock(TypeElement entityTypeElement,
                                                 String preparedStatementVariableName,
                                                 String entityVariableName, int index,
                                                 VariableElement field,
                                                 CodeBlock.Builder codeBlock,
                                                 ExecutableElement daoMethod) {
        codeBlock.add(preparedStatementVariableName);
        codeBlock.add(".$L(", getPreparedStatementSetterMethodName(field.asType()));


        //TODO: add error message here if there is no match
        List<ExecutableElement> getterCallchain = findGetterOrSetter("get", daoMethod,
                field.getSimpleName().toString(), entityTypeElement, new ArrayList<>(), false);
        codeBlock.add("$L, $L.", index, entityVariableName);

        if(getterCallchain != null && !getterCallchain.isEmpty()) {
            codeBlock.add(getterCallchain.get(0).getSimpleName().toString()).add("());\n");
        }else {
            messager.printMessage(Diagnostic.Kind.ERROR, "Error: field " +
                    field.getSimpleName() + " on " + entityTypeElement.getQualifiedName() +
                    ": cannot find getter method. Attempting to generate " +
                    daoMethod.getSimpleName());
        }
    }

    private String getPreparedStatementSetterMethodName(TypeMirror variableType) {
        return "set" + getPreparedStatementSetterGetterTypeName(variableType);
    }

    /**
     * Get the suffix to use on get/set methods of PreparedStatement according to the type of variable.
     * Used when generating code such as preparedStatement.setString / preparedStatement.setInt etc.
     *
     * @param variableType Variable type to set/get on a prepared statement
     * @return suffix to use e.g. "Int" for integers, "String" for Strings, etc.
     */
    private String getPreparedStatementSetterGetterTypeName(TypeMirror variableType) {
        if(variableType.getKind().equals(TypeKind.INT)) {
            return "Int";
        }else if(variableType.getKind().equals(TypeKind.LONG)) {
            return "Long";
        }else if(variableType.getKind().equals(TypeKind.FLOAT)) {
            return "Float";
        }else if(variableType.getKind().equals(TypeKind.DOUBLE)) {
            return "Double";
        }else if(variableType.getKind().equals(TypeKind.BOOLEAN)) {
            return "Boolean";
        }else if(variableType.getKind().equals(TypeKind.DECLARED)) {
            String className = ((TypeElement)processingEnv.getTypeUtils().asElement(variableType))
                    .getQualifiedName().toString();
            switch(className) {
                case "java.sql.Array":
                    return "Array";
                case "java.lang.String":
                    return "String";
                case "java.lang.Integer":
                    return "Int";
                case "java.lang.Long":
                    return "Long";
                case "java.lang.Float":
                    return "Float";
                case "java.lang.Double":
                    return "Double";
                case "java.lang.Boolean":
                    return "Boolean";
            }
        }

        return null;
    }



    @Override
    protected void onDone() {
        super.onDone();

        if(dbTmpFile != null && dbTmpFile.exists()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "DbProcessorJdbc: " +
                    " Cleanup db tmp file: " + dbTmpFile.getAbsolutePath() +
                    " deleted: " + dbTmpFile.delete());
        }
    }
}
