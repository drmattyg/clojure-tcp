clojure-tcp
===========

A TCP stack in Clojure (with Java sockets).  An futile exercise purely for fun, done mostly while on vacation.  Uses rocksaw for java raw sockets.  

Note that I am manually managing dependencies because of the dependency on rocksaw.  Required libs are in the lib directory, but you should probably recompile rocksaw if you want to run this, definitely if you are not running it on OSX.

If you want to run this, set your JAVA_HOME and your CLOJURE_HOME, and use "sudo -E ./run-tcp.sh".  Must run as a sudoer since raw sockets require sudo privs.
