# MiringValidator  
##### Semantic Validator for MIRING-compliant HML  
##### National Marrow Donor Program (NMDP)  
  
This is a REST service that validates an HML file according to a MIRING checklist, and returns a XML report specifying any encountered MIRING or HML infractions.  
  
You can import this project into eclipse (Import as a Maven Project), or use the text editor of your choice.  I recommend you use java 1.8 and MAVEN to build.  
  
Build MiringValidator.war file using MAVEN:  
$ mvn clean install  
  
Deploy /target/MiringValidator.war to your Tomcat 8.0 server, or run within MAVEN:  
$ mvn clean install tomcat7:run-war  
  
Once your tomcat server is running, you should find the web interface at:  
http://localhost:8080/MiringValidator  
  
### MIRING:  
  
MIRING stands for Minimum Information for Reporting Next Generation Sequence Genotyping, and represents a minimum checklist of data to be included in a NGS report.  
  
MIRING is independant of data platform, but this validator is based on HML 1.0.1.  
  
See a MIRING Compliant HML sample at http://schemas.nmdp.org  
  
MIRING: http://biorxiv.org/content/early/2015/02/16/015230  
HML: http://biorxiv.org/content/early/2015/02/06/014951  
  
MIRING rules are based on 8 general elements:  
  
##### MIRING Element 1 - Message Annotation  
Message Generator Contact Information & Document Identification  
  
##### MIRING Element 2 - Reference Context  
##### MIRING Element 3 - Full Genotype  
##### MIRING Element 4 - Consensus Sequence  
##### MIRING Element 5 - Novel Polymorphisms  
##### MIRING Element 6 - Platform Documentation  
##### MIRING Element 7 - Read Processing Documentation  
##### MIRING Element 8 - Primary Data  

  
### RULES  
  
The validator further divides these elements into rules.  For more information on MIRING elements and a rule ID reference, see http://miring.b12x.org/MoreInfo.html or the local github wiki [HERE](https://github.com/nmdp-bioinformatics/MiringValidator/wiki/Rules).

  
  
This validator engine is powered by [SAX Parser](http://docs.oracle.com/javase/7/docs/api/javax/xml/parsers/SAXParser.html) and [Probatron4j](http://www.probatron.org/probatron4j.html), an excellent implementation of [Schematron](http://www.schematron.com/)

# MIRING Validator REST API: Correct Usage

## Endpoint
- The Jersey REST endpoint for MIRING validation is:
  `http://localhost:8080/MiringValidator/validator/ValidateMiring`
- This is based on:
  - WAR deployed as `MiringValidator.war` (context path: `/MiringValidator`)
  - Servlet mapping: `/validator/*`
  - Jersey resource: `@Path("/ValidateMiring")`

## Form Parameter
- The REST service expects a form parameter named `xml` (not `xml[]`).
- The XML payload should be sent as the value of this parameter.

## Working curl Command
```sh
curl -X POST --data-urlencode "xml=$(cat WebContent/hml/hml_1_0_1_example_miring.xml)" \
  http://localhost:8080/MiringValidator/validator/ValidateMiring
```
- This will POST the contents of your HML XML file to the correct endpoint and parameter.

## Troubleshooting
- 404 errors usually mean the context path, servlet mapping, or resource path is incorrect.
- Always check WAR name, web.xml servlet mapping, and Jersey `@Path` annotations.

---
_Last updated: 2026-01-16 by Jason Brelsford

~










