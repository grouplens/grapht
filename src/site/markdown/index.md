<head>
<title>About</title>
</head>

# Grapht Dependency Injector

Grapht is a [dependency injector](http://en.wikipedia.org/wiki/Dependency_injection) for Java.
Unlike other DI containers that resolve dependencies lazily when instances are requested, Grapht
can compute dependency graphs in advance.  These graphs can be modified and analyzed prior to
instantiation to achieve a variety of effects.

## Publication

The design of Grapht is described in the following paper:

Michael D. Ekstrand and Michael Ludwig. 2016. [Dependency Injection with Static Analysis and Context-Aware Policy](http://md.ekstrandom.net/research/pubs/grapht/). 
Journal of Object Technology 15, 1 (February 2016), pp. 1:1–31.
DOI=[10.5381/jot.2016.15.5.a1](http://dx.doi.org/10.5381/jot.2016.15.5.a1).

You can [download the PDF directly](jot-paper.pdf).
