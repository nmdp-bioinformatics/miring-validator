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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

public class SchematronValidator
{
    private static final Logger logger = LogManager.getLogger(SchematronValidator.class);
    
    static ClassLoader loadedProbatronClasses;
    static String schemaPath = "/schematron/";
    static String jarFileName = "/jar/probatron.jar";

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
                String schemaFileName = schemaPath + schemaFileNames[i];
                
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
    
    //This method mimics Probatron's Session.doValidation.
    //Except we're loading the classes via reflection, because everything is in a jar
    //It returns a org.probatron.ValidationReport object.
    private static Object doValidation(String xml, String schemaLocation) 
    {
        //org.probatron.ValidationReport.
        Object vr = null;
        //org.probatron.SchematronSchema theSchema;
        Object theSchema = null;
        
        try 
        {
<<<<<<< HEAD
            URL schemaFileURL = new URL("file:" + schemaLocation);
            InputStream xmlInputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
=======
            URL schemaFileURL = SchematronValidator.class.getResource(schemaLocation);
            InputStream xmlInputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));            
>>>>>>> schematron
           
            //A org.probatron.SchematronSchema object needs to have a Session object when it calls validateCandidate(), or else Null Pointers.
            //So I create a session object here to please it.  As you can see, not much goes into a session.
            Class sessionClass= loadedProbatronClasses.loadClass("org.probatron.Session");
            Object currentSession = sessionClass.newInstance();
            
            //Create a SchematronSchema object, using constructor that takes a Session and a schema URL
            Class schematronSchemaClass= loadedProbatronClasses.loadClass("org.probatron.SchematronSchema");
            Constructor ctor = schematronSchemaClass.getDeclaredConstructor(sessionClass, URL.class);
            //ctor is already public, lets not mess with accessibility
            //ctor.setAccessible(true);
            theSchema = ctor.newInstance(currentSession, schemaFileURL);
            
            //Validate against a schematron schema.
            vr = Utilities.callReflectedMethod(theSchema,"validateCandidate", xmlInputStream, Class.forName("java.io.InputStream"));
        } 
        catch(Exception e)
        {
            logger.error("Exception in doValidation: " + e);
        }
        logger.debug ("Validation Report = " + vr);
        return vr;
    }
    
    private static ValidationError[] getValidationErrorsFromSchematronReport(String xml)
    {
        List<ValidationError> validationErrors = new ArrayList<ValidationError>();

        try
        {
            Element rootElement = Utilities.xmlToDomObject(xml);  
            NodeList list = rootElement.getElementsByTagName("svrl:text");

            if (list != null && list.getLength() > 0) 
            {
                for(int i = 0; i < list.getLength(); i++)
                {
                    Node n = list.item(i);

                        Node childNode = n.getFirstChild();

                        String errorMessage = childNode.getNodeValue();
                        
                        ValidationError ve = new ValidationError(
                                errorMessage
                                ,true);
                        ve.setMiringRule("4.2.3.a");
                        ve.setSolutionText("Please verify the start and end attributes on your reference-sequence node.");
                        
                        Utilities.addValidationError(validationErrors, ve);
                }
            }
        }
        catch(Exception e)
        {
            logger.error("Error forming DOM from schematron results: " + e);
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
}
