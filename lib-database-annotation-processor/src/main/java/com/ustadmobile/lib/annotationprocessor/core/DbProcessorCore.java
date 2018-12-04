package com.ustadmobile.lib.annotationprocessor.core;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmDatabase;

import java.util.HashSet;
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

        boolean result = dbProcessorRoom.process(annotations, roundEnvironment);
        result &= dbProcessorJdbc.process(annotations, roundEnvironment);
        result &= dbProcessorJerseyResource.process(annotations, roundEnvironment);
        result &= dbProcessorRetrofitRepository.process(annotations, roundEnvironment);

        return result;
    }


}
