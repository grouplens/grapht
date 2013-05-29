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

Grapht can be depended on from Maven's Central Repository by adding the 
following to the dependencies section in your POM:

    <dependency>
      <groupId>org.grouplens.grapht</groupId>
      <artifactId>grapht</artifactId>
      <version>0.3.0</version>
    </dependency>
    
## Release Notes

### 0.5.0 (in progress)

See [closed issues][issues-0.5] for more details.

- Rewrite serialization logic to be more robust (#54)
- Return immutable views rather than copies of sets from `Graph` (#58)
- Be smarter about checking provider types (#35, #36)
- Add anchored context matchers (`at` matching) (#41)
- Improve diagnostic warnings and validity checking
- Rename `Edge.getLabel()` to `getDesireChain()`
- Change `Graph.updateEdgeLabel` to `replaceEdge`
- Remove `Binding.finalBinding` in favor of boolean parameter on `to` (#46)
- Add a visitor for satisfactions (#33)
- Add support for specifying default implementations and providers in properties files under the
  `META-INF` directory (#51)
- `Module.bind` is now called `Module.configure`
- **Incompatible change:** Changed default binding policy with respect to qualfier matching.  Now, if you bind a type without specifying any qualifier, it defaults to binding unqualified dependencies and dependencies whose qualifiers are annotated with `@AllowUnqualifiedMatch`.  To get the old behavior of matching irrespective of qualifier matcher, do `bind(Type.class).withAnyQualifier()` (or `bindAny(Type.class)`).

[issues-0.5]: https://bitbucket.org/grouplens/grapht/issues?status=duplicate&status=invalid&status=resolved&status=wontfix&milestone=0.5.0

### 0.4.3

* Fix serialization of inner classes

### 0.4.2

* Fix serialization of primitive types in graphs

### 0.4.1

* Add `DefaultNull` annotation and support for it.

### 0.4.0

See [closed issues][issues-0.4] for more details.

* Remove `Parameter` anotation
* Add basic thread safety for injectors
* Add more error detection
* Add convenience method to bind qualified types
* Allow explicit null bindings to be created

[issues-0.4]: https://bitbucket.org/grouplens/grapht/issues?status=duplicate&status=invalid&status=resolved&status=wontfix&milestone=0.4.0

### 0.3.0
* Refactor SPI and bind rules to allow for more flexible binding functions.
  This brings the implementation much closer to the theoretical formulation
  presented in our paper.
* Implement Provider injection, including breaking dependency cycles with
  Provider injection.
* Pass the JSR 330 TCK.
* Simplify and clean up Graph API to no longer take type parameters.

### 0.2.1
* Rename getFunction() to build() in BindingFunctionBuilder.

### 0.2.0

* Make dependency graph solutions serializable using Java's serialization
  framework.
* Add CachePolicy lifecycle specification for instances (e.g. new, memoize, etc)
* Add support for generic attribute annotations on injection points that are
  carried through the solution graph.
* Add slf4j logging to grapht

### 0.1.0
* Initial published release of grapht
* Supports basic and context-aware dependency injection
