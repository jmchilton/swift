Swift
=====

Search multiple tandem mass spec. datafiles using multiple search engines at once: Mascot, Sequest, X!Tandem, OMSSA and Myrimatch.

### Swift inputs

Swift accepts one or many raw or mgf files. You can process separate files or entire directories.

### Swift outputs

Swift produces Scaffold 3 reports (.sf3 files). You can view these reports on your own computer, just download and install the free Scaffold 3 viewer. There are several possibilities how to map input files to Scaffold reports. 

Build
-----

To build Swift, the following is required:

* Java Development Kit 6 ( http://www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-javase6-419409.html#jdk-6u27-oth-JPR )
* Maven 3.0.3 ( http://maven.apache.org/download.html )
* git (optional) to obtain the source

Once you have Java and Maven setup, you can build Swift as follows:

#### Get Swift from github

	git clone git://github.com/romanzenka/swift.git

#### Create swift-3.0-install.zip

	cd swift
	mvn package -DskipTests
	cd target
	ls
	# You should see swift-3.0-install.zip

* If you want to run all the unit tests, feel free to omit the -DskipTests clause!


#### Congratulations!

You are ready to install Swift.

* Copy the target/swift-3.0-install.zip to your target machine.
* Unzip it
* Install instructions are in
	swift-install/INSTALL.txt

Please mail zenka.roman@mayo.edu if you have any problems!