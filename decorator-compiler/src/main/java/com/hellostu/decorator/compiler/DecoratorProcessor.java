package com.hellostu.decorator.compiler;

import com.google.auto.service.AutoService;
import com.hellostu.decorator.Decoratable;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class)
public final class DecoratorProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Decoratable.class.getName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Decoratable.class);

        for(Element element : elements) {
            if(element instanceof TypeElement) {
                try {
                    processTypeElement((TypeElement)element).makeJavaFile().writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    private DecoratorModel processTypeElement(TypeElement typeElement) {
        if (typeElement.getKind().isInterface() == false) {
            messager.printMessage(ERROR, "Listener tag only be placed on an Interface type");
            return null;
        }

        String[] classNameElements = typeElement.getQualifiedName().toString().split("\\.");
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0; i < classNameElements.length-1; i++) {
            if(stringBuilder.length() != 0) {
                stringBuilder.append(".");
            }
            stringBuilder.append(classNameElements[i]);
        }

        String className = typeElement.getQualifiedName().toString();
        TypeElement parentElement = typeElement;
        while(parentElement.getNestingKind().isNested()) {
            parentElement = (TypeElement)parentElement.getEnclosingElement();
        }
        String packageName = parentElement.getEnclosingElement().toString();
        className = className.substring(packageName.length() + 1);

        ArrayList<TypeVariableName> typeVariableNames = new ArrayList<>();
        for(TypeParameterElement typeParameterElement : typeElement.getTypeParameters()) {
            typeVariableNames.add(TypeVariableName.get(typeParameterElement));
        }
        DecoratorModel decoratorModel = new DecoratorModel(packageName, className, typeElement.asType(), typeVariableNames);
        addInterfaceMethods(decoratorModel, typeElement);
        return decoratorModel;
    }

    private void addInterfaceMethods(DecoratorModel decoratorModel, TypeElement typeElement) {
        for(Element element : typeElement.getEnclosedElements()) {
            if(element.getKind() == ElementKind.METHOD) {
                ExecutableElement executableElement = (ExecutableElement) element;
                decoratorModel.addMethod(executableElement);
            }
        }

        for(TypeMirror type : typeElement.getInterfaces()) {
            TypeElement interfaceTypeElement = (TypeElement)((DeclaredType)type).asElement();
            addInterfaceMethods(decoratorModel, interfaceTypeElement);
        }
    }

}
