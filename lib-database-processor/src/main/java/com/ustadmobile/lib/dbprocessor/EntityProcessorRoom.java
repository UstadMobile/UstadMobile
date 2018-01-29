package com.ustadmobile.lib.dbprocessor;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmRelation;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mike on 1/20/18.
 */

public class EntityProcessorRoom {

    public static void processorDir(File inDir, File outDir) throws IOException{
        JavaClassSource classSource = null;
        if(!outDir.exists())
            outDir.mkdirs();

        for(File srcFile : inDir.listFiles()) {
            File outFile = new File(outDir, srcFile.getName());

            if(!srcFile.getName().endsWith(".java"))
                continue;

            JavaType parsedSource = Roaster.parse(srcFile);

            if(!(parsedSource instanceof JavaClassSource)) {
                Files.write(Paths.get(outFile.toURI()), parsedSource.toString().getBytes("UTF-8"));
                continue;
            }


            classSource = (JavaClassSource)parsedSource;

            String[] primaryKeys = new String[0];

            if(classSource.hasAnnotation(UmEntity.class)) {
                AnnotationSource entityAnnotation = classSource
                        .addAnnotation("android.arch.persistence.room.Entity");

                AnnotationSource annotationSource = classSource.getAnnotation(UmEntity.class);
                if(annotationSource.getLiteralValue("primaryKeys") != null) {
                    primaryKeys = annotationSource.getStringArrayValue("primaryKeys");
                    if(primaryKeys != null && primaryKeys.length > 0) {
                        entityAnnotation.setStringArrayValue("primaryKeys", primaryKeys);
                    }
                }

                if(annotationSource.getLiteralValue("indices") != null) {
                    AnnotationSource[] srcIndicesAnnotation =
                            annotationSource.getAnnotationArrayValue("indices");
                    classSource.addImport("android.arch.persistence.room.Index");

                    StringBuffer dstValueSb = new StringBuffer();
                    String name;
                    String unique;
                    String[] value;

                    for(int i = 0; i < srcIndicesAnnotation.length; i++) {
                        dstValueSb.append("{@Index(");
                        name = srcIndicesAnnotation[i].getStringValue("name");
                        if(name != null)
                            dstValueSb.append("name = \"" + name + "\", ");

                        unique = srcIndicesAnnotation[i].getLiteralValue("unique");
                        if(unique != null)
                            dstValueSb.append("unique = " + unique  +", ");

                        value  = srcIndicesAnnotation[i].getStringArrayValue();
                        if(value != null) {
                            dstValueSb.append("value = {");
                            for(int j = 0; j < value.length; j++){
                                dstValueSb.append("\"" + value[j] + "\"");
                                if(j < value.length -1 )
                                    dstValueSb.append(",");
                            }
                            dstValueSb.append("}");
                        }

                        dstValueSb.append(")}");
                        if(i < srcIndicesAnnotation.length - 1)
                            dstValueSb.append(',').append('\n');
                    }


                    entityAnnotation.setLiteralValue("indices", dstValueSb.toString());
                }
            }

            List<String> primaryKeyList = Arrays.asList(primaryKeys);

            //iterate over fields
            for(FieldSource fieldSource: classSource.getFields()) {
                if(fieldSource.hasAnnotation(UmPrimaryKey.class)) {
                    AnnotationSource umAnnotation = fieldSource.getAnnotation(UmPrimaryKey.class);
                    AnnotationSource pkAnnotation = fieldSource
                            .addAnnotation("android.arch.persistence.room.PrimaryKey");

                    String autoIncrementVal = umAnnotation.getLiteralValue("autoIncrement");
                    if(autoIncrementVal != null && autoIncrementVal.equals("true"))
                        pkAnnotation.setLiteralValue("autoGenerate", "true");

                    if(!fieldSource.getType().isPrimitive())
                        fieldSource.addAnnotation("android.support.annotation.NonNull");
                }

                if(primaryKeyList.contains(fieldSource.getName()))
                    fieldSource.addAnnotation("android.support.annotation.NonNull");


                if(fieldSource.hasAnnotation(UmRelation.class)) {
                    AnnotationSource umRelationAnnotation = fieldSource.getAnnotation(UmRelation.class);
                    AnnotationSource roomRelationAnnotation = fieldSource
                            .addAnnotation("android.arch.persistence.room.Relation");
                    roomRelationAnnotation.setStringValue("parentColumn",
                            umRelationAnnotation.getStringValue("parentColumn"));
                    roomRelationAnnotation.setStringValue("entityColumn",
                            umRelationAnnotation.getStringValue("entityColumn"));
                }

                if(fieldSource.hasAnnotation(UmIndexField.class)) {
                    fieldSource.addAnnotation("android.arch.persistence.room.ColumnInfo")
                            .setLiteralValue("index", "true");
                }
            }

            String srcToWrite = "/* GENERATED FILE : DO NOT EDIT */\n\n" + classSource.toString();
            Files.write(Paths.get(outFile.toURI()), srcToWrite.getBytes("UTF-8"));
        }
    }

    public static void main(String[] args) throws Exception{
        processorDir(new File(args[0]), new File(args[1]));
    }

}
