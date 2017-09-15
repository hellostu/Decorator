package com.hellostu.decorator.compiler;

import com.squareup.javapoet.*;
import com.sun.tools.javac.code.Symbol;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

public class DecoratorModel {

    private String                          packageName;
    private String                          className;
    private TypeMirror                      typeMirror;
    private Iterable<TypeVariableName>      typeVariableNames;
    private ArrayList<Symbol.MethodSymbol>  methods;

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

    public void addMethod(Symbol.MethodSymbol method) {
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

        for(Symbol.MethodSymbol methodSymbol : methods) {
            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(methodSymbol.name.toString())
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class);

            List<Symbol.VarSymbol> params = methodSymbol.params();
            for(Symbol.VarSymbol varSymbol : params) {
                methodSpecBuilder.addParameter(TypeName.get(varSymbol.type), varSymbol.name.toString());
            }

            StringBuilder stringBuilder = new StringBuilder();
            if(methodSymbol.getReturnType().toString().equals("void") == false) {
                stringBuilder.append("return ");
            }
            stringBuilder.append("this.decorated.");
            stringBuilder.append(methodSymbol.name.toString());
            stringBuilder.append("(");

            for(int i = 0; i < params.size(); i++) {
                Symbol.VarSymbol varSymbol = params.get(i);
                if(i != 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(varSymbol.name);
            }
            stringBuilder.append(")");

            methodSpecBuilder.returns(TypeName.get(methodSymbol.getReturnType()));
            methodSpecBuilder.addStatement(stringBuilder.toString());

            typeSpecBuilder.addMethod(methodSpecBuilder.build());
        }
        typeSpecBuilder.addTypeVariables(typeVariableNames);

        return JavaFile.builder(packageName, typeSpecBuilder.build()).build();
    }

}
