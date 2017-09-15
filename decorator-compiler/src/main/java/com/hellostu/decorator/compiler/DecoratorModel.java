package com.hellostu.decorator.compiler;

import com.squareup.javapoet.JavaFile;

import javax.lang.model.type.TypeMirror;

public class DecoratorModel {

    private String                          packageName;
    private String                          className;
    private TypeMirror                      typeMirror;

    ///////////////////////////////////////////////////////////////
    // LIFECYCLE
    ///////////////////////////////////////////////////////////////

    public DecoratorModel(String packageName, String className, TypeMirror typeMirror) {
        this.className = className;
        this.packageName = packageName;
        this.typeMirror = typeMirror;
    }

    ///////////////////////////////////////////////////////////////
    // LIFECYCLE
    ///////////////////////////////////////////////////////////////

    public JavaFile makeJavaFile() {
        return null;
    }

}
