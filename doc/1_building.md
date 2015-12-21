# Documentation

## Building

In order to build the Performance Test Harness you will need to run [Apache Maven](http://maven.apache.org/run-maven/index.html). The Performance Test Harness is setup into four sub projects. The ```performance-test-harness``` project builds runnable jar's which are used on the command line to execute performance tests. The projects include a Graphical User Interface too based on [JavaFX](http://docs.oracle.com/javase/8/javafx/get-started-tutorial/jfx-overview.htm).  If you run jar without any command line arguments the GUI should start.

### Instructions
1. Navigate to the root of the project.
2. Open a command prompt 
3. Execute the following command ```mvn clean install```
4. Done.

After the build completes successfully, binary jar files will be build in the "app" directory.  You should have Producer.jar, Consumer.jar, and Orchestrator.jar.

		

After the binaries have been built successfully, you can run the applications 

     - app/
		+ config/ 		(contains GeoEvent configuration files)
		+ fixtures/		(contains all of your test fixture configuration files)
		+ lib/ 			(all library dependencies are stored and referenced here)
		+ reports/		(contains all of the generated reports)
		+ simulation/	(contains all of your simulation files)
		+ Producer.jar, Consumer.jar, Orchestrator.jar 	(binary jar file)

[Next - Configuration](2_configuration.md)
