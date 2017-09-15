package com.hellostu.decorator.compiler;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.element.TypeElement;
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
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

    private void processTypeElement(TypeElement typeElement) {
        if (typeElement.getKind().isInterface() == false) {
            messager.printMessage(ERROR, "Listener tag only be placed on an Interface type");
            return;
        }


    }
}
