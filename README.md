OHunterServer (OHS)
===================

This project provides a server storing data about users, counting demanding problems and providing interface to access information from Google.

It is one of the two main parts of the O-Hunter project.

Build
-----
The simplest way is to use [NetBeans IDE](https://netbeans.org/) to import this project.

Few additional libraries are necessary to build this project.
 - **OHunterLibrary**
   - https://github.com/kappalabs/OHL
 - **JSON Simple** (version 1.1.1)
   - http://mvnrepository.com/artifact/com.googlecode.json-simple/json-simple
   - direct link:
     - http://central.maven.org/maven2/com/googlecode/json-simple/json-simple/1.1.1/json-simple-1.1.1.jar
 - **Apache Derby** (version 10.12.1.1)
   - https://db.apache.org/derby/
   - use */lib/derby.jar* from this package:
     - http://apache.miloslavbrada.cz//db/derby/db-derby-10.12.1.1/db-derby-10.12.1.1-bin.tar.gz
 - **SCPSolver**
   - http://scpsolver.org/
   - consists of 3 parts:
     - https://bitbucket.org/hplanatscher/scpsolver/downloads/SCPSolver.jar
     - https://bitbucket.org/hplanatscher/scpsolver/downloads/GLPKSolverPack.jar
     - https://bitbucket.org/hplanatscher/scpsolver/downloads/LPSOLVESolverPack.jar

Documentation
-------------
Several possible ways can be found:
 - Use [NetBeans IDE](https://netbeans.org/) to generate javadoc.
 - Use [Javadoc Tool](http://www.oracle.com/technetwork/java/javase/documentation/index-jsp-135444.html) to generate the documentation.
 - Use ```$ ant``` to build and generate javadoc at the same time.

Links
-----
 - OHunterClient
    - https://github.com/kappalabs/OHC
 - OHunterLibrary
    - https://github.com/kappalabs/OHL
