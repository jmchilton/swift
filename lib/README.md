Mayo Proteomics Core Library
============================

This is a main library of proteomics-related code
used for various projects within Mayo Proteomics Core.

Libraries
---------

* chem - chemistry - e.g. Averagine isotope distribution
* commandline - command line parsing
* config, daemon, messaging, filesharing - a system for configuring and running modular, distributed applications
* database - Hibernate support
* db-curator - editing FASTA files
* integration - resources for doing integration tests
* math - Non-Negative Least Squares
* params - editing parameter files for search engines
* search-engine - generic base for writing search engine modules
* sge - Sun Grid Engine support
* textio - I/O of text files support
* util - generic utilities (file handling, exception handling)
* webscraping - support for web form scraping and submission
* workflow-engine - executing tasks with dependencies
* workspace - simple support of users and their preferences (very incomplete)

File Parsing Utilities
----------------------

* .FASTA
* .mgf
* .mzxml
* .tar
* unimod.xml
* mprcfile - a SQLite database-based format for storing multiple data tables
* Scaffold .xml report
* Scaffold Batch .scafml writer

Compilation
-----------

    mvn install

Dependencies
------------

All the modules depend on several third-party libraries that are obtained from http://informatics.mayo.edu/maven Nexus repository.

