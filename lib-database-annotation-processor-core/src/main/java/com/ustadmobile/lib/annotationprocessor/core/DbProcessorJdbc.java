package com.ustadmobile.lib.annotationprocessor.core;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.ustadmobile.lib.database.annotation.UmDatabase;
import com.ustadmobile.lib.database.annotation.UmDbContext;
import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.jdbc.JdbcDatabaseUtils;
import com.ustadmobile.lib.database.jdbc.UmJdbcDatabase;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.tools.Diagnostic;

import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorCore.OPT_JDBC_OUTPUT;

public class DbProcessorJdbc extends AbstractDbProcessor {

    private static String SUFFIX_JDBC_DBMANAGER = "_Jdbc";

    private static final String SUFFIX_JDBC_DAO = "_JdbcDaoImpl";

    //Map of fully qualified database class name to a connection that has that database
    private Map<String, Connection> nameToConnectionMap = new HashMap<>();

    public DbProcessorJdbc() {
        setOutputDirOpt(OPT_JDBC_OUTPUT);
    }

    public void processDbClass(TypeElement dbType, File destinationDir) throws IOException {
        String jdbcDbClassName = dbType.getSimpleName() + SUFFIX_JDBC_DBMANAGER;
        PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(dbType);

        ClassName initialContextClassName = ClassName.get(InitialContext.class);
        TypeSpec.Builder jdbcDbTypeSpec = TypeSpec.classBuilder(jdbcDbClassName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(dbType))
                .addSuperinterface(ClassName.get(UmJdbcDatabase.class))
                .addJavadoc("Generated code - DO NOT EDIT!\n")
                .addField(ClassName.get(Object.class), "_context", Modifier.PRIVATE)
                .addField(ClassName.get(DataSource.class), "_dataSource", Modifier.PRIVATE)
                .addField(ClassName.get(Connection.class), "_connection", Modifier.PRIVATE)
                .addField(ClassName.get(ExecutorService.class), "_executor", Modifier.PRIVATE)
                .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(TypeName.get(Object.class), "context")
                    .addParameter(TypeName.get(String.class), "dbName")
                    .addCode(CodeBlock.builder().add("\tthis._context = context;\n")
                        .beginControlFlow("try ")
                            .add("$T iContext = new $T();\n", initialContextClassName, initialContextClassName)
                            .add("this._dataSource = (DataSource)iContext.lookup(\"java:/comp/env/jdbc/\"+dbName);\n")
                            .add("this._connection = _dataSource.getConnection();\n")
                            .add("createAllTables(_connection);\n")
                        .endControlFlow()
                        .beginControlFlow("catch($T|$T e)",
                                ClassName.get(NamingException.class), ClassName.get(SQLException.class))
                            .add("throw new RuntimeException(e);\n")
                        .endControlFlow()
                        .build())
                .build())
                .addMethod(MethodSpec.methodBuilder("getExecutor")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.get(ExecutorService.class))
                        .addCode("return this._executor;\n").build())
                .addMethod(MethodSpec.methodBuilder("getConnection")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.get(Connection.class))
                        .addCode("return this._connection;\n").build());

        TypeSpec.Builder factoryClassSpec = DbProcessorUtils.makeFactoryClass(dbType, jdbcDbClassName);

        addCreateTablesMethodToClassSpec(dbType, jdbcDbTypeSpec);

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



        JavaFile.builder(packageElement.getQualifiedName().toString(), factoryClassSpec.build())
                .indent("    ").build().writeTo(destinationDir);
        JavaFile.builder(packageElement.getQualifiedName().toString(), jdbcDbTypeSpec.build())
                .indent("    ").build().writeTo(destinationDir);
    }

    /**
     * Generate a createAllTables method that will run the SQL required to generate all tables for
     * all entities on the given database type.
     *
     * @param dbType TypeElement representing the class annotated with @UmDatabase
     * @param classBuilder TypeSpec.Builder being used to generate the JDBC database class implementation
     */
    protected void addCreateTablesMethodToClassSpec(TypeElement dbType, TypeSpec.Builder classBuilder) {
        MethodSpec.Builder createMethod = MethodSpec.methodBuilder("createAllTables");
        createMethod.addParameter(ClassName.get(Connection.class), "con");
        CodeBlock.Builder createCb = CodeBlock.builder();
        createCb.beginControlFlow("try")
                .add("$T _existingTableNames = $T.getTableNames(_connection);\n",
                        ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)),
                        ClassName.get(JdbcDatabaseUtils.class))
                .add("$T _stmt = con.createStatement();\n", ClassName.get(Statement.class));

        for(TypeElement entityTypeElement : findEntityTypes(dbType)) {
            addCreateTableStatements(createCb, "_stmt", entityTypeElement, '`');
        }

        createCb.endControlFlow() //end try/catch control flow
                .beginControlFlow("catch($T e)\n", SQLException.class)
                .add("throw new $T(e);\n", RuntimeException.class)
                .endControlFlow();



        createMethod.addCode(createCb.build());
        classBuilder.addMethod(createMethod.build());
    }

    /**
     * Get the TypeElements that correspond to the entities on the @UmDatabase annotation of the given
     * TypeElement
     *
     * TODO: make sure that we check each annotation, this currently **ASSUMES** the first annotation is @UmDatabase
     *
     * @param dbTypeElement TypeElement representing the class with the @UmDatabase annotation
     * @return List of TypeElement that represents the values found on entities
     */
    private List<TypeElement> findEntityTypes(TypeElement dbTypeElement){
        List<TypeElement> entityTypeElements = new ArrayList<>();
        Map<? extends ExecutableElement, ? extends AnnotationValue> annotationEntryMap =
                dbTypeElement.getAnnotationMirrors().get(0).getElementValues();
        for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                annotationEntryMap.entrySet()) {
            String key = entry.getKey().getSimpleName().toString();
            Object value = entry.getValue().getValue();
            if (key.equals("entities")) {
                List<? extends AnnotationValue> typeMirrors =
                        (List<? extends AnnotationValue>) value;
                for(AnnotationValue entityValue : typeMirrors) {
                    entityTypeElements.add((TypeElement) processingEnv.getTypeUtils()
                            .asElement((TypeMirror) entityValue.getValue()));
                }
            }
        }

        return entityTypeElements;
    }

    /**
     * Generates code that will execute CREATE TABLE and CREATE INDEX as required for the given
     * entity.
     *
     * @param codeBlock CodeBlock.Builder that this code will be added to
     * @param stmtVariableName Name of the SQL Statement object variable in the CodeBlock
     * @param entitySpec The TypeElement representing the entity for which the statements are being generated
     * @param quoteChar The quote char used to contain SQL table names e.g. '`' for MySQL and Sqlite
     *
     * @return SQL for table creation only, to be used within the annotation processor itself
     */
    protected void addCreateTableStatements(CodeBlock.Builder codeBlock, String stmtVariableName,
                                                 TypeElement entitySpec, char quoteChar) {

        Map<String, List<String>> indexes = new HashMap<>();
        for(Element subElement : entitySpec.getEnclosedElements()) {
            if(subElement.getKind() != ElementKind.FIELD)
                continue;

            VariableElement fieldVariable = (VariableElement)subElement;

            if(fieldVariable.getAnnotation(UmIndexField.class) != null) {
                indexes.put("index_" + entitySpec.getSimpleName() + '_' + fieldVariable.getSimpleName(),
                        Collections.singletonList(fieldVariable.getSimpleName().toString()));
            }

        }

        codeBlock.beginControlFlow("if(!_existingTableNames.contains($S))",
                entitySpec.getSimpleName().toString())
                .add("$L.executeUpdate($S);\n", stmtVariableName,
                        makeCreateTableStatement(entitySpec, quoteChar));
        for(Map.Entry<String, List<String>> index : indexes.entrySet()) {
            Map<String, String> formatArgs = new HashMap<>();
            formatArgs.put("quot", String.valueOf(quoteChar));
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
    private String makeCreateTableStatement(TypeElement entitySpec, char quoteChar) {
        boolean fieldVariablesStarted = false;

        StringBuffer sbuf = new StringBuffer()
                .append("CREATE TABLE IF NOT EXISTS ").append(quoteChar)
                .append(entitySpec.getSimpleName()).append(quoteChar)
                .append(" (");
        for(Element subElement : entitySpec.getEnclosedElements()) {
            if (subElement.getKind() != ElementKind.FIELD)
                continue;

            if (fieldVariablesStarted)
                sbuf.append(", ");

            VariableElement fieldVariable = (VariableElement)subElement;
            UmPrimaryKey primaryKeyAnnotation = fieldVariable.getAnnotation(UmPrimaryKey.class);

            sbuf.append(quoteChar).append(fieldVariable.getSimpleName().toString())
                    .append(quoteChar).append(' ').append(makeSqlTypeDeclaration(fieldVariable));

            if(primaryKeyAnnotation!= null) {
                sbuf.append(" PRIMARY KEY ");
                if(primaryKeyAnnotation.autoIncrement())
                    sbuf.append(" AUTOINCREMENT ");
                sbuf.append(" NOT NULL ");
            }

            fieldVariablesStarted = true;
        }

        sbuf.append(')');


        return sbuf.toString();
    }

    protected String makeSqlTypeDeclaration(VariableElement field) {
        TypeMirror fieldType = field.asType();

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

        //didn't recognize that.
        messager.printMessage(Diagnostic.Kind.ERROR,
                "Could not determine SQL data type for field: " + field.getEnclosingElement() +
                "." + field.getSimpleName().toString());

        return null;
    }


    @Override
    public void processDbDao(TypeElement daoType, TypeElement dbType, File destinationDir) throws IOException {
        String daoClassName = daoType.getSimpleName() + SUFFIX_JDBC_DAO;
        TypeSpec.Builder jdbcDaoClassSpec = TypeSpec.classBuilder(daoClassName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(daoType))
                .addField(ClassName.get(UmJdbcDatabase.class), "_db", Modifier.PRIVATE)
                .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get(UmJdbcDatabase.class), "_db")
                    .addCode("this._db = _db;\n").build());

        for(Element subElement : daoType.getEnclosedElements()) {
            if (subElement.getKind() != ElementKind.METHOD)
                continue;

            ExecutableElement daoMethod = (ExecutableElement)subElement;

            if(daoMethod.getAnnotation(UmInsert.class) != null) {
                addInsertMethod(daoMethod, jdbcDaoClassSpec, '`');
            }
        }


        JavaFile.builder(processingEnv.getElementUtils().getPackageOf(daoType).toString(),
                jdbcDaoClassSpec.build()).build().writeTo(destinationDir);
    }

    public void addInsertMethod(ExecutableElement daoMethod, TypeSpec.Builder daoBuilder,
                                char identifierQuote) {
        MethodSpec.Builder methodBuilder = MethodSpec.overriding(daoMethod)
                .addModifiers(Modifier.SYNCHRONIZED);

        VariableElement insertedElement = daoMethod.getParameters().get(0);
        TypeElement entityTypeElement = (TypeElement)processingEnv.getTypeUtils()
                .asElement(insertedElement.asType());
        if(entityTypeElement.getAnnotation(UmEntity.class) == null) {
            messager.printMessage(Diagnostic.Kind.ERROR, daoMethod.getEnclosingElement().getSimpleName() +
                    "." + daoMethod.getSimpleName() +
                    "@UmInsert first parameter must be an entity, array of entities, or list of entities");
            return;
        }


        String preparedStmtVarName = "_stmt";

        String identifierQuoteStr = String.valueOf(identifierQuote);
        CodeBlock.Builder codeBlock = CodeBlock.builder()
                .add("$T _stmt = null;\n", PreparedStatement.class)
                .beginControlFlow("try")
                .add("_stmt = _db.getConnection().prepareStatement(\"INSERT INTO $L$L$L (",
                        identifierQuoteStr, entityTypeElement.getSimpleName().toString(),
                        identifierQuoteStr);
        List<VariableElement> entityFields = new ArrayList<>();
        boolean commaRequired = false;
        for(Element fieldElement : entityTypeElement.getEnclosedElements()) {
            if(fieldElement.getKind() != ElementKind.FIELD)
                continue;

            if(commaRequired)
                codeBlock.add(", ");

            entityFields.add((VariableElement)fieldElement);
            codeBlock.add(identifierQuoteStr).add(fieldElement.getSimpleName().toString())
                    .add(identifierQuoteStr);
            commaRequired = true;
        }
        codeBlock.add(") VALUES (");
        for(int i = 0; i < entityFields.size(); i++) {
            codeBlock.add("?");
            if(i < entityFields.size() - 1)
                codeBlock.add(", ");
        }
        codeBlock.add(")\");\n");

        for(int i = 0; i < entityFields.size(); i++) {
            setPreparedStatementValue(preparedStmtVarName, insertedElement.getSimpleName().toString(),
                    i + 1, entityFields.get(i), codeBlock);
        }

        codeBlock.add("$L.execute();\n", preparedStmtVarName);
        codeBlock.endControlFlow().beginControlFlow("catch($T e)", SQLException.class)
                .add("e.printStackTrace();\n").endControlFlow()
                .beginControlFlow("finally")
                .add("$T.closeStatement(_stmt);\n", ClassName.get(JdbcDatabaseUtils.class))
                .endControlFlow();


        methodBuilder.addCode(codeBlock.build());
        daoBuilder.addMethod(methodBuilder.build());
    }

    public void addQueryMethod(ExecutableElement daoMethod, TypeSpec.Builder daoBuilder,
                               char identifierQuote) {

    }


    private void setPreparedStatementValue(String preparedStatementVariableName,
                                           String entityVariableName, int index,
                                           VariableElement field, CodeBlock.Builder codeBlock) {
        codeBlock.add(preparedStatementVariableName);
        TypeMirror stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String")
                .asType();
        TypeMirror variableType = field.asType();
        if(variableType.getKind().equals(TypeKind.INT)) {
            codeBlock.add(".setInt(");
        }else if(variableType.getKind().equals(TypeKind.LONG)) {
            codeBlock.add(".setLong(");
        }else if(variableType.getKind().equals(TypeKind.FLOAT)) {
            codeBlock.add(".setFloat(");
        }else if(variableType.getKind().equals(TypeKind.DOUBLE)) {
            codeBlock.add(".setDouble(");
        }else if(variableType.getKind().equals(TypeKind.DECLARED)
                && variableType.equals(stringType)) {
            codeBlock.add(".setString(");
        }
        //TODO: add error message here if there is no match
        codeBlock.add("$L, $L.get", index, entityVariableName);
        String fieldName = field.getSimpleName().toString();

        codeBlock.add(Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1))
                .add("());\n");
    }

}
