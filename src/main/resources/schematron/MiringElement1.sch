<?xml version="1.0" encoding="utf-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron">
    <ns prefix="hml" uri="http://schemas.nmdp.org/spec/hml/1.0.1" />

    <pattern name="Check the HMLID format">
        <rule context="hml:hmlid">        
        <!--
            Rule 1.1.d
            UUID roots look like this: de305d54-75b4-431b-adb2-eb6b9e546014
            8 alpha numerics, dash, 3 groups of 4 alphanumerics with dash, 12 alphanumerics
            HML 1.0.1 allows only OID, so I dont think I can check for this.  
            Future versions of HML may allow GUID.  Double check the regex here,
            I definitely wrote it on the fly and it's untested.
        -->
        <let name="regExpUUID" value=" '.{8}\-[.{4}\-]{3}.{12}' " />
        
        <!--
            Rule 1.1.c
            OID roots have just dots and digits: 11.222.3.44444.5
            They start and end with a digit.
            It must have an extension, but I'm not comparing the extension to a regex yet
        -->
        <let name="regExpOID" value=" '[\d+\.]+\d+' " />

        <assert test="matches( @root, $regExpOID ) and @extension">The hmlid root is not formatted like an OID.</assert>
        <report test="matches( @root, $regExpOID ) and @extension">The hmlid root is formatted like an OID.</report>
        <!-- 
        <assert test="matches( @root, $regExpOID )">The hmlid root is not formatted like a UUID. </assert>
        <report test="matches( @root, $regExpOID )">The hmlid root is formatted like a UUID. </report>
        -->
        </rule>
    </pattern>

    <pattern name="Check the sbt-ngs:test-id and test-id-source">
        <rule context="hml:sbt-ngs">
        <!-- 
        Rules 1.3.b
        test-id and test-id-source should look like an NCBI-GTR format.
        "GTR000000000.0"
        -->
        <let name="regExpGTR" value=" '[GTR][\d]{9}[\.][\d]' " />

        <assert test="matches( @test-id, $regExpGTR )">On a sbt-ngs node, test-id is not formatted like a GTR test ID.</assert>
        <assert test="matches( @test-id-source, 'NCBI-GTR')">On a sbt-ngs node, the test-id-source is not explicitly 'NCBI-GTR'.</assert>

        </rule>
    </pattern>

</schema>