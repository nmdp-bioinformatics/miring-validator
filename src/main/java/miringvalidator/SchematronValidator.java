/*

    MiringValidator  Semantic Validator for MIRING compliant HML
    Copyright (c) 2014-2015 National Marrow Donor Program (NMDP)

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
package main.java.miringvalidator;

import static org.junit.Assert.assertTrue;

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

import main.java.miringvalidator.ValidationError.Severity;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

public class SchematronValidator
{
    private static final Logger logger = LogManager.getLogger(SchematronValidator.class);
    
    static ClassLoader loadedProbatronClasses;
    static String jarFileName = "/jar/probatron.jar";
    static String namespaceText = "{http://schemas.nmdp.org/spec/hml/1.0.1}";

    /**
     * Perform a schematron validation for an xml string against an array of schemaFileName strings.
     *
     * @param xml a String containing the xml to validate
     * @param schemaFileNames an array of Strings containing the names of the schema file resources to validate against
     * @return an array of ValidationError objects found during validation
     */
    public static ValidationError[] validate(String xml, String[] schemaFileNames)
    {
        ValidationError[] results = new ValidationError[0];
        try
        {
            logger.debug("Opening jar file: " + jarFileName);
            URL jarURL = SchematronValidator.class.getResource(jarFileName);
            URI jarURI = jarURL.toURI();
            loadedProbatronClasses = Utilities.loadJarElements(new File(jarURI));
            
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
                ValidationError[] currentResultErrors = getValidationErrorsFromSchematronReport(resultString);
                logger.debug(currentResultErrors.length + " schema validation errors found");

                //Add any errors to the tier2 results.
                results = Utilities.combineArrays(results, currentResultErrors);
            }
        }
        catch(Exception e )
        {
            logger.error("Error during schematron validation: " + e);
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
        //vr is an org.probatron.ValidationReport
        Object vr = null;
        //theSchema is an org.probatron.SchematronSchema
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
            //Already public, lets not mess with accessibility
            //ctor.setAccessible(true);
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
     * Convert a org.probatron.ValidationReport into an array of ValidationError objects
     *
     * @param xml a String containing a probatron ValidationReport 
     * @return an array of ValidationError objects generated from the probatron ValidationReport report.
     */
    private static ValidationError[] getValidationErrorsFromSchematronReport(String xml)
    {
        List<ValidationError> validationErrors = new ArrayList<ValidationError>();

        try
        {
            Element rootElement = Utilities.xmlToDomObject(xml);  

            //I guess I'm handling successful-reports and failed-asserts in the same way
            NodeList successfulReportList = rootElement.getElementsByTagName("svrl:successful-report");
            NodeList failedAssertList = rootElement.getElementsByTagName("svrl:failed-assert");
            Node[] combinedList = combineNodeLists(successfulReportList, failedAssertList);
            
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
                    String testText = null;
                    String locationText = null;
                    String errorText = null;
                    
                    Node currentSchematronNode = combinedList[i];
                    NamedNodeMap currAttributes = currentSchematronNode.getAttributes();
                    
                    //testText contains the actual test that schematron ran to get this report. 
                    //Not sure if we'll use that information at all
                    testText = currAttributes.getNamedItem("test").getNodeValue();
                    
                    //locationText contains information about exactly where in the HML file we found the problem
                    //I believe it is or contains an xpath.  We're gonna put that on the report.
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

                    ValidationError validationError = generateValidationError(errorText, locationText);
                    Utilities.addValidationError(validationErrors, validationError);
                }
            }
        }
        catch(Exception e)
        {
            logger.error("Error forming DOM from schematron results: " + e);
            validationErrors.add(new ValidationError(
                "Unhandled Schematron validation exception: " + e, Severity.FATAL
            ));
            
        }

        if(validationErrors.size() > 0)
        {
            //List -> Array
            ValidationError[] array = validationErrors.toArray(new ValidationError[validationErrors.size()]);
            return array;
        }
        else
        {
            //Empty.  Not null.  No problems found.
            return new ValidationError[0];
        }
    }

    private static Node[] combineNodeLists(NodeList successfulReportList, NodeList failedAssertList)
    {
        Node[] newList = new Node[successfulReportList.getLength() + failedAssertList.getLength()];
        
        for(int i = 0; i < successfulReportList.getLength(); i++)
        {
            newList[i] = successfulReportList.item(i);
        }
        for(int i = 0; i < failedAssertList.getLength(); i++)
        {
            newList[i + successfulReportList.getLength()] = failedAssertList.item(i);
        }
        return newList;
    }

    /**
     * Generate a single ValidationError object
     * Lots of Miring logic happens here.
     *
     * @param errorMessage an error message generated by probatron
     * @param locationText an Xpath containing the location of the error in the HML document
     * @param testText text containing the actual test that probatron ran to generate this error
     * @return a ValidationError object describing the miring validation problem
     */
    private static ValidationError generateValidationError(String errorMessage, String locationText)
    {
        ValidationError ve = new ValidationError(errorMessage,Severity.MIRING);
        
        if(locationText != null)
        {
            ve.setXPath(stripNamespace(locationText));
        }
        
        if(errorMessage.contains("The hmlid root is formatted like an OID."))
        {
            ve.setMiringRule("1.1.c");
            ve.setSolutionText("No solution needed.  This is a good thing.");
            ve.setSeverity(Severity.INFO);
        }
        else if(errorMessage.contains("The hmlid root is not formatted like an OID."))
        {
            ve.setMiringRule("1.1.c");
            ve.setSolutionText("Please format the hmlid node's root attribute like an OID:  11.234.55555.65");
            ve.setSeverity(Severity.INFO);
        }
        else if(errorMessage.contains("On a sbt-ngs node, test-id is not formatted like a GTR test ID."))
        {
            ve.setMiringRule("1.3.b");
            ve.setSolutionText("Other options are allowed. It isn't necessary to onluy use a GTR test ID: GTR000000000.0");
            ve.setSeverity(Severity.INFO);
        }
        else if(errorMessage.contains("On a sbt-ngs node, the test-id-source is not explicitly 'NCBI-GTR'."))
        {
            ve.setMiringRule("1.3.b");
            ve.setSolutionText("Other options are allowed. It isn't necessary to only use NCBI-GTR'");
            ve.setSeverity(Severity.INFO);
        }
        else if(errorMessage.contains("On a reference sequence node, end attribute should be greater than or equal to the start attribute."))
        {
            ve.setMiringRule("2.2.c");
            ve.setSolutionText("The end attribute should be greater than or equal to the start attribute.");
        }
        else if(errorMessage.contains("A reference-sequence node has an id attribute with no corresponding consensus-sequence-block id attribute."))
        {
            ve.setMiringRule("2.2.1.c");
            ve.setSolutionText("This is a warning, not a serious error.  consensus-sequence-block:reference-sequence-id must have a corresponding reference-sequence:id, but the opposite is not necessarily true.");
            ve.setSeverity(Severity.WARNING);
        }
        else if(errorMessage.contains("The start attribute on a consensus-sequence-block node should be greater than or equal to the start attribute")
             || errorMessage.contains("The end attribute on a consensus-sequence-block node should be less than or equal to the end attribute"))
        {
            ve.setMiringRule("4.2.3.d");
            ve.setSolutionText("Verify that the start and end attributes on the consensus-sequence-block are >= and <= to the start and end attributes on the corresponding reference sequence.");
        }
        else if(errorMessage.contains("For every consensus-sequence-block node, the child sequence node must have a length of (end - start)."))
        {
            ve.setMiringRule("4.2.3.e");
            ve.setSolutionText("Please check the sequence length against the start and end attributes.");
        }
        else if(errorMessage.contains("On a consensus-sequence-block node, the phasing-group attribute is deprecated."))
        {
            ve.setMiringRule("4.2.4.b");
            ve.setSolutionText("Please use phase-set instead.");
        }
        else if(errorMessage.contains("A consensus-sequence-block with attribute continuity=\"true\" does not appear to be continuous"))
        {
            ve.setMiringRule("4.2.7.b");
            ve.setSolutionText("Any consensus-sequence-block node with continuity=\"true\" is expected to be continuous with the previous sibling node.  Start=End(previous).  The previous node will have the same reference-sequence-id and phase-set, if applicable.");
        }
        else if(errorMessage.contains("On a variant node, end attribute should be greater than or equal to the start attribute"))
        {
            ve.setMiringRule("5.2.b");
            ve.setSolutionText("The end attribute should be greater than or equal to the start attribute.");
        }
        else if(errorMessage.contains("The start attribute on a variant node should be greater than or equal to the start attribute")
                || errorMessage.contains("The end attribute on a variant node should be less than or equal to the end attribute"))
        {
            ve.setMiringRule("5.2.d");
            ve.setSolutionText("Verify that the start and end attributes on the variant are >= and <= to the start and end attributes on the corresponding reference sequence.");
        }
        
        else
        {
            ve.setMiringRule("Unhandled Miring Rule");
            ve.setSolutionText("Unhandled Solution Text");
        }
        
        return ve;
    }

    private static String stripNamespace(String locationText)
    {
        return locationText.replace(namespaceText, "");
    }
}
