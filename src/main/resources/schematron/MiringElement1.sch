<?xml version="1.0" encoding="utf-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron">
    <ns prefix="hml" uri="http://schemas.nmdp.org/spec/hml/1.0.1" />

    <pattern name="Check the HMLID">
        <rule context="hml:hmlid">
            <assert test="@root">An hmlid node must have a root attribute.</assert>
            <assert test="@root = '1234'">The root attribute for an HMLID should be 1234.</assert>


            
            <!-- Those assertions are kinda bogus.  I'm not sure what to check for HMLID.  Need to finalize this.  
            Need to check somehow if it's an OID or UUID
            OID is strictly digits and dots.
            UUID de305d54-75b4-431b-adb2-eb6b9e546014
            Rules 1.1.c and 1.1.d-->
            
            
        </rule>
    </pattern>
    
    <pattern name="Check the HMLID format">
        <rule context="hml:hmlid">
            <assert test="@root">An hmlid node must have a root attribute.</assert>
            <assert test="@root = '1234'">The root attribute for an HMLID should be 1234.</assert>


            
            <!-- Those assertions are kinda bogus.  I'm not sure what to check for HMLID.  Need to finalize this.  
            Need to check somehow if it's an OID or UUID
            OID is strictly digits and dots.
            UUID de305d54-75b4-431b-adb2-eb6b9e546014
            Rules 1.1.c and 1.1.d-->
            
            
        </rule>
    </pattern>
    
    <!--
    
       <rule context="x">
     <let name="grammar" value=" 'a b( c)*' " />
     <let name="contents"
        value="string-join(for $e in * return  local-name ( $e ), ' ') " />
     <assert test="matches( $contents, $grammar )"
     >The contents [<value-of select="$contents"/>] 
     should match grammar [<value-of select="$grammar"/>] </assert>
   </rule>
   
   -->
    
    
    
    
    
    
    <pattern name="Check the HMLID2">
        <rule context="hml:sbt-ngs">

            <!-- Rules 1.3.b
            test-id and test-id-source should look like an NCBI-GTR format.
            -->
            
            
        </rule>
    </pattern>
    
        <pattern name="Check the HMLI3D">
        <rule context="hml:raw-reads">

            <!-- Rule 1.6.1
            if raw-reads has availability="true", it must have a URI attribute.
            -->
            
            
        </rule>
    </pattern>

</schema>