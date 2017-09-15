package com.hellostu.decorator;

import com.google.testing.compile.JavaFileObjects;
import com.hellostu.decorator.compiler.DecoratorProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

@RunWith(JUnit4.class)
public class DecoratorTests {

    @Test
    public void testDecoratableAnnotation() {
        JavaFileObject javaFileObject = JavaFileObjects.forSourceLines("HelloWorld",
                "package com.example.helloworld;",
                "",
                "import com.hellostu.decorator.Decoratable;",
                "",
                "@Decoratable",
                "public interface HelloWorld {",
                "void doAThing();",
                "int processAThing(int initialThing);",
                "}");

        JavaFileObject javaFileObject2 = JavaFileObjects.forSourceLines("HelloWorldDecorator",
                "package com.example.helloworld;",
                "",
                "import java.lang.Override;",
                "",
                "class HelloWorldDecorator implements HelloWorld {",
                "  private HelloWorld decorated;",
                "",
                "  public HelloWorldDecorator(HelloWorld decorated) {",
                "    this.decorated = decorated;",
                "  }",
                "",
                "  @Override",
                "  public void doAThing() {",
                "    this.decorated.doAThing();",
                "  }",
                "",
                "  @Override",
                "  public int processAThing(int initialThing) {",
                "    return this.decorated.processAThing(initialThing);",
                "  }",
                "}");
        assert_().about(javaSource())
                .that(javaFileObject)
                .processedWith(new DecoratorProcessor())
                .compilesWithoutError()
                .and().generatesSources(javaFileObject2);
    }

    @Test
    public void testDecoratableAnnotationWithGenerics() {
        JavaFileObject javaFileObject = JavaFileObjects.forSourceLines("HelloWorld",
                "package com.example.helloworld;",
                "import java.lang.String;",
                "import com.hellostu.decorator.Decoratable;",
                "",
                "@Decoratable",
                "public interface HelloWorld<A extends String> {",
                "A processAThing(A initialThing);",
                "}");

        JavaFileObject javaFileObject2 = JavaFileObjects.forSourceLines("HelloWorldDecorator",
                "package com.example.helloworld;",
                "",
                "import java.lang.Override;",
                "import java.lang.String;",
                "",
                "class HelloWorldDecorator<A extends String> implements HelloWorld<A> {",
                "  private HelloWorld<A> decorated;",
                "",
                "  public HelloWorldDecorator(HelloWorld<A> decorated) {",
                "    this.decorated = decorated;",
                "  }",
                "",
                "  @Override",
                "  public A processAThing(A initialThing) {",
                "    return this.decorated.processAThing(initialThing);",
                "  }",
                "}");
        assert_().about(javaSource())
                .that(javaFileObject)
                .processedWith(new DecoratorProcessor())
                .compilesWithoutError()
                .and().generatesSources(javaFileObject2);
    }

}
