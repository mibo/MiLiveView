# mibo Live View (miLiVi)

A very simple tool to check modifications for a file and render a live preview (`asciidoc` or `markdown`).

## Start

Start with `sbt "run --file=<path to file>"` to build and run live view for given file.

## Building

To build a executable (_fat_) `jar` just run `sbt assembly`. This build the jar in `target/scala-2.11` as `MiLiVi-assembly-0.1.jar`.
 This can be executed with a `JRE 1.8+`.

### Java Packager

See [official documentation](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javapackager.html) for more details.

Run `java_packager_gen.sh` to generate a `all-in` bundle in `target/bundle`. This contains all (also JRE) which is necessary to run the application.

## Dependencies

  * [PlantUML](https://plantuml.com/)
  * [asciidoctorj](http://asciidoctor.org/docs/asciidoctorj/)
