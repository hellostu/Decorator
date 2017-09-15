package com.hellostu.decorator.compiler;

import com.squareup.javapoet.*;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

public class DecoratorModel {

    private String                          packageName;
    private String                          className;
    private TypeMirror                      typeMirror;
    private Iterable<TypeVariableName>      typeVariableNames;
    private ArrayList<ExecutableElement>    methods;

    ///////////////////////////////////////////////////////////////
    // LIFECYCLE
    ///////////////////////////////////////////////////////////////

    public DecoratorModel(String packageName, String className, TypeMirror typeMirror, Iterable<TypeVariableName> typeVariableNames) {
        this.className = className;
        this.packageName = packageName;
        this.typeMirror = typeMirror;
        this.typeVariableNames = typeVariableNames;
        this.methods = new ArrayList<>();
    }

    ///////////////////////////////////////////////////////////////
    // LIFECYCLE
    ///////////////////////////////////////////////////////////////

    public void addMethod(ExecutableElement method) {
        methods.add(method);
    }

    public JavaFile makeJavaFile() {
        FieldSpec decoratedField = FieldSpec.builder(TypeName.get(typeMirror), "decorated")
                .addModifiers(Modifier.PRIVATE)
                .build();

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(typeMirror), "decorated")
                .addStatement("this.decorated = decorated")
                .build();

        String[] classNameElements = className.split("\\.");
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(classNameElements[classNameElements.length-1] + "Decorator")
                .addSuperinterface(TypeName.get(typeMirror))
                .addField(decoratedField)
                .addMethod(constructor);

        for(ExecutableElement executableElement : methods) {
            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(executableElement.getSimpleName().toString())
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class);

            List<? extends VariableElement> params = executableElement.getParameters();
            for(VariableElement param : params) {
                methodSpecBuilder.addParameter(TypeName.get(param.asType()), param.getSimpleName().toString());
            }

            StringBuilder stringBuilder = new StringBuilder();
            if(executableElement.getReturnType().toString().equals("void") == false) {
                stringBuilder.append("return ");
            }
            stringBuilder.append("this.decorated.");
            stringBuilder.append(executableElement.getSimpleName().toString());
            stringBuilder.append("(");

            for(int i = 0; i < params.size(); i++) {
                VariableElement param = params.get(i);
                if(i != 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(param.getSimpleName().toString());
            }
            stringBuilder.append(")");

            methodSpecBuilder.returns(TypeName.get(executableElement.getReturnType()));
            methodSpecBuilder.addStatement(stringBuilder.toString());

            typeSpecBuilder.addMethod(methodSpecBuilder.build());
        }
        typeSpecBuilder.addTypeVariables(typeVariableNames);

        return JavaFile.builder(packageName, typeSpecBuilder.addModifiers(Modifier.PUBLIC).build()).build();
    }

}
