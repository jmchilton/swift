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

1) Java Development Kit 6 ( http://www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-javase6-419409.html#jdk-6u27-oth-JPR )
2) Maven 3.0.3 ( http://maven.apache.org/download.html )
3) git (optional) to obtain the source

Once you have Java and Maven setup, you can build Swift as follows:

1) Get Swift from github

	git clone git://github.com/romanzenka/swift.git

2) Create Swift's binaries

	cd swift

	# First run an install on all. If you want to see the unit tests performance, remove the -DskipTests
	mvn install -DskipTests

	# Make launcher.jar for launching Swift's web interface
	# swift.war is already built by the swift/web project
	mvn -f swift/swift/launcher/pom.xml assembly:assembly -DskipTests

	# Make swift.jar that contains all Swift's functionality
	mvn -f swift/swift/core/pom.xml assembly:assembly -DskipTests

In order to make your own installable package from Swift, you need additional scripts and data Swift depends on.

Download the swift-install.zip package from GitHub.

	# Get out of the "swift" folder you created
	cd ..

	wget --no-check-certificate https://github.com/downloads/romanzenka/swift/swift-install.zip

Unzip the package

	unzip swift-install.zip

Now copy your compiled files over the existing swift-install ones.

	VERSION=3.0-SNAPSHOT
	TARGET=swift-install/bin/swift

	cp swift/swift/launcher/target/launcher-${VERSION}-all.jar ${TARGET}/launcher.jar
	cp swift/swift/core/target/swift-core-${VERSION}-all.jar ${TARGET}/swift.jar
	cp swift/swift/web/target/swift-ui.war ${TARGET}/swift.war

(TODO: We would like to simplify this to be ran directly with one maven command.)

Congratulations! You obtained a swift-install folder that you can now distribute and install Swift from. Please see

	swift-install/INSTALL.txt

for more information on installing Swift.

