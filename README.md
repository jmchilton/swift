Swift
=====

Search multiple tandem mass spec. datafiles using multiple search engines at once: Mascot, Sequest, X!Tandem, OMSSA and Myrimatch.

### Swift inputs

Swift accepts one or many raw or mgf files. You can process separate files or entire directories.

### Swift outputs

Swift produces Scaffold 3 reports (.sf3 files). You can view these reports on your own computer, just download and install the free Scaffold 3 viewer. There are several possibilities how to map input files to Scaffold reports. 

Build
-----

	mvn install -T 1C -f pom.xml -DskipTests

	mvn -f swift/launcher/pom.xml assembly:assembly -DskipTests
	mvn -f swift/core/pom.xml assembly:assembly -DskipTests

TODO: We would like to simplify this to be ran directly with one maven command.


