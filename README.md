# MiringValidator
##### Semantic Validator for MIRING-compliant HML
##### National Marrow Donor Program (NMDP)

This is a REST service that validates an HML file according to a MIRING checklist, and returns a XML report specifying any encountered MIRING infractions.

Build MiringValidator.war file using MAVEN:
$ mvn clean install

Deploy /target/MiringValidator.war to your Tomcat 8.0 server, or run within MAVEN:
$ mvn clean install tomcat7:run-war

Once your tomcat server is running, you should find the web interface at:
http://localhost:8080/MiringValidator

### Rule Reference:

MIRING stands for Minimum Information for Reporting Next Generation Sequence Genotyping, and represents a minimum checklist of data to be included in a NGS report.

MIRING is meant to be independant of data platform, this implementation is based on HML 1.0.1.

See a MIRING Compliant HML sample at schemas.nmdp.org

MIRING: http://biorxiv.org/content/early/2015/02/16/015230
HML: http://biorxiv.org/content/early/2015/02/06/014951

MIRING rules are based on 7 general elements:

##### MIRING Element 1 - Message Annotation
Message Generator Contact Information
Document Identification

##### MIRING Element 2 - Reference Context
Reference Sequences and Databases

##### MIRING Element 3 - Full Genotype
GLStrings and Typings

##### MIRING Element 4 - Consensus Sequence
Gene-length sequences

##### MIRING Element 5 - Novel Polymorphisms
Sequence variants relative to the reference sequence

##### MIRING Element 6 - Platform Documentation
Citations to reference each instrument and method

##### MIRING Element 7 - Read Processing Documentation
References for processing methodologies and software

##### MIRING Element 8 - Primary Data
References to raw NGS data in a public database














