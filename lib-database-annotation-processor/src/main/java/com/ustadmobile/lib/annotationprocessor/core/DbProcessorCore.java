package com.ustadmobile.lib.annotationprocessor.core;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmDatabase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorCore.OPT_JDBC_OUTPUT;
import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorCore.OPT_JERSEY_RESOURCE_OUT;
import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorCore.OPT_RETROFIT_OUTPUT;
import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorCore.OPT_ROOM_OUTPUT;

/**
 * DbProcessorCore will generate a factory class for each database, and then run annotation
 * processors for each implementation to be generated.
 */

@SupportedOptions({OPT_ROOM_OUTPUT, OPT_JDBC_OUTPUT,
        OPT_JERSEY_RESOURCE_OUT, OPT_RETROFIT_OUTPUT})
public class DbProcessorCore extends AbstractProcessor{

    public static final String OPT_ROOM_OUTPUT = "umdb_room_out";

    public static final String OPT_JDBC_OUTPUT = "umdb_jdbc_out";

    public static final String OPT_JERSEY_RESOURCE_OUT = "umdb_jersey_res_out";

    public static final String OPT_RETROFIT_OUTPUT = "umdb_retrofit_out";

    private Messager messager;

    private Filer filer;

    private DbProcessorRoom dbProcessorRoom;

    private DbProcessorJdbc dbProcessorJdbc;

    private DbProcessorJerseyResource dbProcessorJerseyResource;

    private DbProcessorRetrofitRepository dbProcessorRetrofitRepository;

    // Reserved types as per: https://www.postgresql.org/docs/8.1/sql-keywords-appendix.html
    private static final List<String> SQL_RESERVED_WORDS = Arrays.asList("ALL",
            "ANALYSE",
            "ANALYZE",
            "AND",
            "ANY",
            "ARRAY",
            "AS",
            "ASC",
            "ASYMMETRIC",
            "BOTH",
            "CASE",
            "CAST",
            "CHECK",
            "COLLATE",
            "COLUMN",
            "CONSTRAINT",
            "CREATE",
            "CURRENT_DATE",
            "CURRENT_ROLE",
            "CURRENT_TIME",
            "CURRENT_TIMESTAMP",
            "CURRENT_USER",
            "DEFAULT",
            "DEFERRABLE",
            "DESC",
            "DISTINCT",
            "DO",
            "ELSE",
            "END",
            "EXCEPT",
            "FALSE",
            "FOR",
            "FOREIGN",
            "FROM",
            "GRANT",
            "GROUP",
            "HAVING",
            "IN",
            "INITIALLY",
            "INTERSECT",
            "INTO",
            "LEADING",
            "LIMIT",
            "LOCALTIME",
            "LOCALTIMESTAMP",
            "NEW",
            "NOT",
            "NULL",
            "OFF",
            "OFFSET",
            "OLD",
            "ON",
            "ONLY",
            "OR",
            "ORDER",
            "PLACING",
            "PRIMARY",
            "REFERENCES",
            "SELECT",
            "SESSION_USER",
            "SOME",
            "SYMMETRIC",
            "TABLE",
            "THEN",
            "TO",
            "TRAILING",
            "TRUE",
            "UNION",
            "UNIQUE",
            "USER",
            "USING",
            "WHEN",
            "WHERE");

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(UmDatabase.class.getCanonicalName());
        set.add(UmDao.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();

        dbProcessorRoom = new DbProcessorRoom();
        dbProcessorRoom.init(processingEnvironment);
        dbProcessorJdbc = new DbProcessorJdbc();
        dbProcessorJdbc.init(processingEnvironment);
        dbProcessorJerseyResource = new DbProcessorJerseyResource();
        dbProcessorJerseyResource.init(processingEnvironment);
        dbProcessorRetrofitRepository = new DbProcessorRetrofitRepository();
        dbProcessorRetrofitRepository.init(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        Set<? extends Element> daoSet = roundEnvironment.getElementsAnnotatedWith(UmDao.class);

        for(Element dbClassElement : roundEnvironment.getElementsAnnotatedWith(UmDatabase.class)) {
            TypeElement dbClassTypeEl = (TypeElement)dbClassElement;
            validateDatabase(dbClassTypeEl);

            for(TypeElement entityTypeEl : DbProcessorUtils.findEntityTypes(dbClassTypeEl,
                    processingEnv)) {
                validateEntity(entityTypeEl);
            }
        }

        boolean result = dbProcessorRoom.process(annotations, roundEnvironment);
        result &= dbProcessorJdbc.process(annotations, roundEnvironment);
        result &= dbProcessorJerseyResource.process(annotations, roundEnvironment);
        result &= dbProcessorRetrofitRepository.process(annotations, roundEnvironment);

        return result;
    }

    private void validateDatabase(TypeElement dbClass) {

    }

    private void validateEntity(TypeElement entityType) {
        if(SQL_RESERVED_WORDS.contains(entityType.getSimpleName().toString().toUpperCase())) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Entity class " +
                entityType.getQualifiedName() + " clashes with an SQL reserved word", entityType);
        }

        for(VariableElement fieldVariable : DbProcessorUtils.getEntityFieldElements(entityType,
                processingEnv)){
            if(SQL_RESERVED_WORDS.contains(fieldVariable.getSimpleName().toString().toUpperCase())) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Fieldname \"" +
                        fieldVariable.getSimpleName() + "\" on " + entityType.getQualifiedName() +
                        " is an SQL reserved word", entityType);
            }
        }
    }


}
