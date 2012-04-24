# Grapht

Grapht is a light-weight dependency injector. It converts the dependency
injection problem into a graph-based problem that can be solved and analyzed
without constructing any components until a solution is guaranteed. The solution
graph is also exposed to enable flexible extensions such as static analysis, 
and visualizations.

Grapht also supports specifying dependency bindings based on where in the
graph the components must be injected. This allows a programmer to specify that
a type Foo must be used in the context of type A, but a Bar should be used in
any other context. This can be used to compliment the idea of qualifying
injection points using annotations, as specified in [JSR 330][jsr330].

Grapht provides a fluent configuration API very similar to that of 
[Guice's][guice].

[jsr330]: http://code.google.com/p/atinject/
[guice]: http://code.google.com/p/google-guice/

## Maven

Grapht fully supports Maven, and will be released to the Central Repository
shortly. In the mean time, the current SNAPSHOT can be installed locally
and then depended on with:

    <dependency>
      <groupId>org.grouplens.grapht</groupId>
      <artifactId>grapht</artifactId>
      <version>0.1.0-SNAPSHOT</version>
    </dependency>
