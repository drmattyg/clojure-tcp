#!/bin/sh
$JAVA_HOME/bin/java -cp $CLOJURE_HOME/clojure-1.6.0.jar:src/tcp:lib/\* -Djava.library.path=`pwd`/lib clojure.main ./src/tcp/test.clj