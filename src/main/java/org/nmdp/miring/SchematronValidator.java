/*

    MiringValidator  Semantic Validator for MIRING compliant HML
    Copyright (c) 2015 National Marrow Donor Program (NMDP)

    This library is free software; you can redistribute it and/or modify it
    under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation; either version 3 of the License, or (at
    your option) any later version.

    This library is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; with out even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
    License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this library;  if not, write to the Free Software Foundation,
    Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA.

    > http://www.gnu.org/licenses/lgpl.html

*/
package org.nmdp.miring;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.nmdp.miring.ValidationResult.Severity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** 
 * SchematronValidator is a class used to validate an XML document against a set of schematron rules.  The schematron logic is handled, in this case, by Probatron.
 * 
 * Probatron is distributed as an executable jar, but it is rather inconvenient to write XML files to hard drive in order to use them.
 * I'm using reflection to call Probatron's methods in a few spots, rather than using their main method as a starting point.
 * 
 * For reference, see Probatron's documentation:
 * http://www.probatron.org/probatron4j.html
 * https://code.google.com/p/probatron4j/source/browse/#svn/trunk/
 * 
 * The source code is available at google right now, but I'm not sure how long it will be available since Google Code is ending...
 * 
*/
public class SchematronValidator
{
    static Logger logger = LoggerFactory.getLogger(SchematronValidator.class);
    
    static ClassLoader loadedProbatronClasses;
    static String jarFileName = "/org/nmdp/miring/jar/probatron.jar";
    static String namespaceText = "{http://schemas.nmdp.org/spec/hml/1.0.1}";
    public static Document schematronRuleTemplate = null;

    /**
     * Perform a schematron validation for an xml string against an array of schemaFileName strings.
     *
     * @param xml a String containing the xml to validate
     * @param schemaFileNames an array of Strings containing the names of the schema file resources to validate against
     * @return an array of ValidationError objects found during validation
     */
    public static ValidationResult[] validate(String xml, String[] schemaFileNames)
    {
        ValidationResult[] results = new ValidationResult[0];
        
        try
        {
            logger.debug("Opening jar file: " + jarFileName);
            URL jarURL = SchematronValidator.class.getResource(jarFileName);
            URI jarURI = jarURL.toURI();
            loadedProbatronClasses = Utilities.loadJarElements(new File(jarURI));            
            schematronRuleTemplate = Utilities.xmlToDocumentObject(Utilities.readXmlResource("/org/nmdp/miring/ruletemplates/SchematronRuleTemplate.xml"));
            
            for(int i = 0; i < schemaFileNames.length; i++)
            {
                String schemaFileName = schemaFileNames[i];
                
                logger.debug("Starting a schematron validation with schema " + schemaFileName + " and xml length " + xml.length());

                //Create an org.probatron.ValidationReport object
                Object validationReportObject = doValidation(xml, schemaFileName);

                //Stream out the schematron report to a String
                ByteArrayOutputStream myBaos = new ByteArrayOutputStream();
                Utilities.callReflectedMethod(validationReportObject, "streamOut", myBaos, Class.forName("java.io.OutputStream"));
                String resultString = myBaos.toString();

                //Create MIRING specific validation errors
                ValidationResult[] currentResultErrors = translateSchematronReportToValidationResults(resultString);
                logger.debug(currentResultErrors.length + " schema validation errors found");

                //Add any errors to the tier2 results.
                results = Utilities.combineArrays(results, currentResultErrors);
            }
        }
        catch(Exception e )
        {
            logger.error("Exception in SchematronValidation", e);
            return Utilities.combineArrays(results, new ValidationResult[]{new ValidationResult("Failed Schematron Validation: " + e.toString(),Severity.FATAL)});
        }
        logger.debug(results.length + " validation errors detected in schematron validator.");
        return results;
    }

    /**
     * Perform a schematron validation for an xml string against an single schematron schema.
     * This method mimics Probatron's Session.doValidation.
     * 
     * @param xml a String containing the xml to validate
     * @param schemaLocation an String containing the name of the schema file resource to validate against
     * @return an object which is an org.probatron.ValidationReport objects.
     */
    private static Object doValidation(String xml, String schemaLocation) 
    {
        //We're using some reflection here, so object types are vague
        //vr = org.probatron.ValidationReport
        Object vr = null;
        //theSchema = org.probatron.SchematronSchema
        Object theSchema = null;
        
        try 
        {
            URL schemaFileURL = SchematronValidator.class.getResource(schemaLocation);
            InputStream xmlInputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));            
           
            //A org.probatron.SchematronSchema object needs to have a Session object when it calls validateCandidate(), or else Null Pointers.
            //So I create a session object here to please it.
            Class sessionClass= loadedProbatronClasses.loadClass("org.probatron.Session");
            Object currentSession = sessionClass.newInstance();
            
            //Create a SchematronSchema object, using constructor that takes a Session and a schema URL
            Class schematronSchemaClass= loadedProbatronClasses.loadClass("org.probatron.SchematronSchema");
            Constructor ctor = schematronSchemaClass.getDeclaredConstructor(sessionClass, URL.class);
            theSchema = ctor.newInstance(currentSession, schemaFileURL);
            
            //Validate against a schematron schema, using probatron's validateCandidate method
            vr = Utilities.callReflectedMethod(theSchema,"validateCandidate", xmlInputStream, Class.forName("java.io.InputStream"));
        } 
        catch(Exception e)
        {
            logger.error("Exception in doValidation: " + e);
        }
        logger.debug ("Validation Report = " + vr);
        return vr;
    }

    /**
     * Translate a org.probatron.ValidationReport into an array of ValidationResult objects
     *
     * @param xml a String containing a probatron ValidationReport 
     * @return an array of ValidationResult objects generated from the probatron ValidationReport report.
     */
    private static ValidationResult[] translateSchematronReportToValidationResults(String xml)
    {
        List<ValidationResult> validationErrors = new ArrayList<ValidationResult>();

        try
        {
            Element rootElement = Utilities.xmlToRootElement(xml);  

            //I guess I'm handling successful-reports and failed-asserts in the same way.
            NodeList successfulReportList = rootElement.getElementsByTagName("svrl:successful-report");
            NodeList failedAssertList = rootElement.getElementsByTagName("svrl:failed-assert");
            Node[] combinedList = Utilities.combineNodeLists(successfulReportList, failedAssertList);
            
            if (combinedList != null) 
            {
                /*
                These nodes look like this:
                <svrl:successful-report 
                    test="matches( @root, $regExpOID )"
                    location="/{http://schemas.nmdp.org/spec/hml/1.0.1}hml[1]/{http://schemas.nmdp.org/spec/hml/1.0.1}hmlid[1]">
                    <svrl:text>
                        The hmlid root is formatted like an OID. 
                    </svrl:text>
                </svrl:successful-report>
                
                OR
                
                <svrl:failed-assert 
                    test="number(@start) = '0'"
                    location="/{http://schemas.nmdp.org/spec/hml/1.0.1}hml[1]/{http://schemas.nmdp.org/spec/hml/1.0.1}sample[1]/{http://schemas.nmdp.org/spec/hml/1.0.1}typing[1]/{http://schemas.nmdp.org/spec/hml/1.0.1}consensus-sequence[1]/{http://schemas.nmdp.org/spec/hml/1.0.1}reference-database[1]/{http://schemas.nmdp.org/spec/hml/1.0.1}reference-sequence[1]">
                    <svrl:text>
                        start attribute on reference-sequence nodes should be 0.
                    </svrl:text>
                </svrl:failed-assert>
                
                 */
                for(int i = 0; i < combinedList.length; i++)
                {
                    //String testText = null;
                    String locationText = null;
                    String errorText = null;
                    
                    Node currentSchematronNode = combinedList[i];
                    NamedNodeMap currAttributes = currentSchematronNode.getAttributes();
                    
                    //testText contains the actual test that schematron ran to get this report. 
                    //I don't think we need this information.
                    //testText = currAttributes.getNamedItem("test").getNodeValue();
                    
                    //locationText is an xpath.  We need this info.
                    //Probably need to skim out the namespaces, because they clutter things badly, and because who cares?
                    locationText = currAttributes.getNamedItem("location").getNodeValue();
                    
                    //Dig into the "svrl:successful-report" node to get the error text.
                    NodeList childrenNodes = currentSchematronNode.getChildNodes();
                    if(childrenNodes != null)
                    {
                        for(int j = 0; j < childrenNodes.getLength(); j++)
                        {
                            //one of the children of the svrl:successful-report node should be a "svrl:text" node
                            //in my experience it's at j=1, but I don't understand why.
                            Node currentChildNode= childrenNodes.item(j);
                            if(currentChildNode.getNodeName().equals("svrl:text"))
                            {
                                errorText = currentChildNode.getTextContent();
                                break;
                            }
                        }
                    }

                    ValidationResult validationError = generateValidationError(errorText, locationText);
                    Utilities.addValidationError(validationErrors, validationError);
                }
            }
        }
        catch(Exception e)
        {
            logger.error("Error forming DOM from schematron results.", e);
            validationErrors.add(new ValidationResult("Unhandled Schematron validation exception: " + e, Severity.FATAL));
        }

        if(validationErrors.size() > 0)
        {
            //List -> Array
            ValidationResult[] array = validationErrors.toArray(new ValidationResult[validationErrors.size()]);
            return array;
        }
        else
        {
            //Empty.  Not null.  No problems found.
            return new ValidationResult[0];
        }
    }

    /**
     * Generate a single ValidationError object
     * Lots of Miring logic happens here.
     *
     * @param errorMessage an error message generated by probatron
     * @param locationText an Xpath containing the location of the error in the HML document
     * @return a ValidationError object describing the miring validation problem
     */
    private static ValidationResult generateValidationError(String errorMessage, String locationText)
    {
        ValidationResult ve = new ValidationResult(errorMessage,Severity.MIRING);
        
        //Specific logic for various MIRING errors
        try
        {
            boolean matchFound = false;
            NodeList ruleNodes = schematronRuleTemplate.getElementsByTagName("rule");
            for(int i = 0; i < ruleNodes.getLength(); i++)
            {
                NamedNodeMap ruleAttributes = ruleNodes.item(i).getAttributes();

                String templateErrorMessage = Utilities.getAttribute(ruleAttributes, "error-text");
                
                if(errorMessage.contains(templateErrorMessage))
                {
                    matchFound = true;
                    
                    String miringRule = Utilities.getAttribute(ruleAttributes, "miring-rule-id");
                    String templateSeverity = Utilities.getAttribute(ruleAttributes, "severity");
                    String templateSolution = Utilities.getAttribute(ruleAttributes, "solution-text");
                    
                    Severity severity = 
                        templateSeverity.equals("fatal")?Severity.FATAL:
                        templateSeverity.equals("miring")?Severity.MIRING:
                        templateSeverity.equals("warning")?Severity.WARNING:
                        templateSeverity.equals("info")?Severity.INFO:
                        Severity.FATAL;
                    
                    ve =  new ValidationResult(errorMessage,severity);
                    ve.setSolutionText(templateSolution);
                    ve.setMiringRule(miringRule);
                    
                    if(locationText != null)
                    {
                        ve.addXPath(stripNamespace(locationText));
                    }
                    
                    break;
                }
            }
            if(!matchFound)
            {
                throw new Exception("No Rule template found !: " + errorMessage);
            }
        }
        catch(Exception e)
        {
            logger.error("Exception during generateValidationError" ,e);
            return new ValidationResult(e.toString(), Severity.FATAL);
        }

        return ve;
    }

    private static String stripNamespace(String locationText)
    {
        return locationText.replace(namespaceText, "");
    }
}
