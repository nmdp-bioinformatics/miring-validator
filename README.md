# MiringValidator  
##### Semantic Validator for MIRING-compliant HML  
##### National Marrow Donor Program (NMDP)  

## Version

Current: **1.3.0**

## What's New in 1.3.0

Security hardening based on DAST scan findings:
- Added HSTS, Permissions-Policy, expanded CSP, X-Frame-Options on all responses
- Session cookie hardening (HttpOnly, Secure, COOKIE tracking)
- Removed information leakage (dead PHP block, server headers)
- Added Subresource Integrity (SRI) for CDN scripts
- Fixed JavaScript memory leaks (global variables, unclosed iframe document)
- Tightened CORS (removed Allow-Credentials with wildcard origin)
- CI/CD: all branch types now build and deploy to dev1, auto-versioning enabled

See [CHANGELOG](WebContent/CHANGELOG.html) for full history.

## Build
  
This is a REST service that validates an HML file according to a MIRING checklist, and returns a XML report specifying any encountered MIRING or HML infractions.  
  
You can import this project into eclipse (Import as a Maven Project), or use the text editor of your choice.  I recommend you use java 1.8 and MAVEN to build.  
  
Build MiringValidator.war file using MAVEN:  
$ mvn clean install  
  
Deploy /target/MiringValidator.war to your Tomcat 8.0 server, or run within MAVEN:  
$ mvn clean install tomcat7:run-war  
  
Once your tomcat server is running, you should find the web interface at:  
http://localhost:8080/MiringValidator

## Environments

| Environment | URL |
|---|---|
| Dev | https://dev1.miring.nmdp.org |
| QA | https://qa1.miring.nmdp.org |
| PreProd | https://preprod1.miring.nmdp.org |
| Prod | https://miring.b12x.org |

## Versioning

This project uses Maven auto-versioning via GitLab CI/CD:
- Feature/bugfix branches: patch increment + `-SNAPSHOT` suffix (e.g., `1.3.1-SNAPSHOT`)
- Merge to master (release): strips `-SNAPSHOT`, publishes release, creates git tag
- Next development cycle: minor increment (e.g., `1.3.0` → `1.4.0-SNAPSHOT`)  
  
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

~

~











