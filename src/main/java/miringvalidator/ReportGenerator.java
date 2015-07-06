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
package main.java.miringvalidator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import main.java.miringvalidator.ValidationResult.Severity;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ReportGenerator
{
    private static final Logger logger = LogManager.getLogger(ReportGenerator.class);
    
    public static DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    
    /**
     * Generate a Miring Results Report
     *
     * @param validationErrors an array of ValidationError objects
     * @param root the root attribute on an HMLID node on the source XML.  If it exists, you should include it in the report
     * @param extension the extension attribute on an HMLID node on the source XML.  If it exists, you should include it in the report
     * @return a String containing MIRING Results Report
     */
    public static String generateReport(ValidationResult[] validationErrors, String root, String extension, HashMap<String,String> properties, Sample[] sampleIDs)
    {
        validationErrors = assignSampleIDs(validationErrors,sampleIDs);
        validationErrors = combineSimilarErrors(validationErrors);
        
        try 
        {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            //DOCUMENT
            Document doc = docBuilder.newDocument();
            
            //MIRINGREPORT ROOT
            Element rootElement = doc.createElement("miring-report");
            String currentDate = (dateFormat.format(new Date()));
            rootElement.setAttribute("timestamp", currentDate);
            doc.appendChild(rootElement);
            
            //NAMESPACES
            rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            rootElement.setAttribute("xsi:noNamespaceSchemaLocation", "../../../main/resources/schema/miringreport.xsd");

            addMiringCompliantNode(validationErrors, doc);
            
            addHmlidElement(root, extension, doc);
            
            addSampleElements(validationErrors, sampleIDs, doc);
            
            addPropertyElements(properties, doc);
            
            addValidationResultElements(validationErrors, doc);

            return(Utilities.getStringFromDoc(doc));
        }
        catch (ParserConfigurationException pce) 
        {
            logger.error("Exception in Report Generator: " + pce);
        } 
        catch (Exception e) 
        {
            logger.error("Exception in Report Generator: " + e);
        }
        
        //Oops, something went wrong.
        logger.error("Error during Miring Validation Report Generation.");
        return null;
    }

    private static void addMiringCompliantNode(ValidationResult[] validationErrors, Document doc)
    {
        Element compliantElement = doc.createElement("miring-compliant");
        compliantElement.setTextContent(
            (validationErrors == null)?"false"
            :(validationErrors.length==0)?"true"
            :(Utilities.isMiringCompliant(validationErrors))?"true"
            :"false"
        );

        doc.getDocumentElement().appendChild(compliantElement);
    }

    private static void addHmlidElement(String root, String extension, Document doc)
    {
        Element hmlidElement = doc.createElement("hmlid");
        if(root != null && root.length()>0)
        {
            hmlidElement.setAttribute("root",root);
        }
        if(extension != null && extension.length()>0)
        {
            hmlidElement.setAttribute("extension", extension);
        }
        doc.getDocumentElement().appendChild(hmlidElement);
    }

    private static void addPropertyElements(HashMap<String, String> properties, Document doc)
    {
        if(properties != null)
        {
            Iterator it = properties.entrySet().iterator();
            while (it.hasNext()) 
            {
                Map.Entry pair = (Map.Entry)it.next();
                String name = pair.getKey().toString();
                String value = pair.getValue().toString();

                it.remove();
                Element property = doc.createElement("property");

                property.setAttribute("name", name);
                property.setAttribute("value",value);
                
                doc.getDocumentElement().appendChild(property);
            }
        }
    }

    private static void addSampleElements(ValidationResult[] validationErrors, Sample[] sampleIDs, Document doc)
    {
        if(sampleIDs != null && sampleIDs.length > 0)
        {
            int numberSampleIDs = sampleIDs.length;
            int numberBadSamples = 0;
            int numberGoodSamples = 0;
            Element samplesElement = doc.createElement("samples");

            for(int i = 0; i < sampleIDs.length; i++)
            {
                Element currentSampleElement = doc.createElement("sample");
                String sampleID = sampleIDs[i].id;
                String centerCode = sampleIDs[i].centerCode;
                
                currentSampleElement.setAttribute("id",sampleID);
                if(!(centerCode==null) && !(centerCode.isEmpty()))
                {
                    currentSampleElement.setAttribute("center-code",centerCode);
                }

                if(doesSampleHaveMiringErrors(sampleID, validationErrors))
                {
                    currentSampleElement.setAttribute("miring-compliant", "false");
                    numberBadSamples++;
                }
                else
                {
                    currentSampleElement.setAttribute("miring-compliant", "true");
                    numberGoodSamples++;
                }

                samplesElement.appendChild(currentSampleElement);
            }
            
            samplesElement.setAttribute("sample-count", ("" + numberSampleIDs));
            samplesElement.setAttribute("noncompliant-sample-count", ("" + numberBadSamples));
            samplesElement.setAttribute("compliant-sample-count", ("" + numberGoodSamples));
            
            doc.getDocumentElement().appendChild(samplesElement);
        }
    }

    private static void addValidationResultElements(ValidationResult[] validationErrors, Document doc)
    {
        ValidationResult[] fatalErrors = getErrorsBySeverity(validationErrors,Severity.FATAL);
        ValidationResult[] miringErrors = getErrorsBySeverity(validationErrors,Severity.MIRING);
        ValidationResult[] warnings = getErrorsBySeverity(validationErrors,Severity.WARNING);
        ValidationResult[] info = getErrorsBySeverity(validationErrors,Severity.INFO);

        if(fatalErrors != null && fatalErrors.length > 0)
        {
            Element fatalErrorsElement = doc.createElement("fatal-validation-errors");
            for(int i = 0; i < fatalErrors.length; i++)
            {
                fatalErrorsElement.appendChild(generateValidationErrorNode(doc, fatalErrors[i]));
            }
            doc.getDocumentElement().appendChild(fatalErrorsElement);
        }
        if(miringErrors != null && miringErrors.length > 0)
        {
            Element miringErrorsElement = doc.createElement("miring-validation-errors");
            for(int i = 0; i < miringErrors.length; i++)
            {
                miringErrorsElement.appendChild(generateValidationErrorNode(doc, miringErrors[i]));
            }
            doc.getDocumentElement().appendChild(miringErrorsElement);
        }
        if(warnings != null && warnings.length > 0)
        {
            Element warningsElement = doc.createElement("validation-warnings");
            for(int i = 0; i < warnings.length; i++)
            {
                warningsElement.appendChild(generateValidationErrorNode(doc, warnings[i]));
            }
            doc.getDocumentElement().appendChild(warningsElement);
        }
        if(info != null && info.length > 0)
        {
            Element infoElement = doc.createElement("validation-info");
            for(int i = 0; i < info.length; i++)
            {
                infoElement.appendChild(generateValidationErrorNode(doc, info[i]));
            }
            doc.getDocumentElement().appendChild(infoElement);
        }
    }
    
    private static boolean doesSampleHaveMiringErrors(String sampleID, ValidationResult[] validationErrors)
    {
        if(validationErrors != null && validationErrors.length > 0)
        {
            for(int i = 0; i < validationErrors.length; i++)
            {
                ValidationResult tempResult = validationErrors[i];
                String currentSampleID = tempResult.getSampleID();
                Severity currentSeverity = tempResult.getSeverity();
                if(currentSampleID != null && currentSampleID.equals(sampleID))
                {
                    if(currentSeverity != null && (
                        currentSeverity.equals(Severity.FATAL)
                        || currentSeverity.equals(Severity.MIRING)))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static ValidationResult[] assignSampleIDs(ValidationResult[] validationErrors, Sample[] sampleIDs)
    {
        try
        {
            if(validationErrors != null
                && validationErrors.length > 0 
                && sampleIDs != null 
                && sampleIDs.length > 0)
            {
                for(int i = 0; i < validationErrors.length; i++)
                {
                    ValidationResult currentError = validationErrors[i];
                    List<String> xPaths = currentError.getXPaths();
                    if(xPaths.size()!=0)
                    {
                        //Only getting the very first xPath here.  What if there are more xPaths?  I dunno?
                        String xPath = xPaths.get(0);
                        String sampleID = Utilities.getSampleID(xPath,sampleIDs);
                        if(sampleID != null && sampleID.length() > 0)
                        {
                            currentError.setSampleID(sampleID);
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            logger.error("Error during assignSampleIDs: " + e);
        }
        return validationErrors;
    }

    private static ValidationResult[] getErrorsBySeverity(ValidationResult[] validationErrors, Severity severity)
    {
        if(validationErrors != null && validationErrors.length > 0)
        {
            List<ValidationResult> results = new ArrayList<ValidationResult>();
            for(int i = 0; i < validationErrors.length; i++)
            {
                if(validationErrors[i].severity.equals(severity))
                {
                    results.add(validationErrors[i]);
                }
            }
            return results.toArray(new ValidationResult[results.size()]);
        }
        else
        {
            return null;
        }
    }

    private static ValidationResult[] combineSimilarErrors(ValidationResult[] validationErrors)
    {
        //Many errors are very similar to eachother.  If they have the same error text, then we should combine them.  Hopefully they have distinct xpaths.
        List<ValidationResult> newErrorList = new ArrayList<ValidationResult>();
        
        for(int i = 0; i < validationErrors.length; i++)
        {
            ValidationResult oldError = validationErrors[i];
            //Scan existing list for an error that is a close match.
            boolean foundMatch = false;
            for (ValidationResult newError: newErrorList)
            {
                if(oldError.miringRule.equals(newError.miringRule)
                    && oldError.errorText.equals(newError.errorText)
                    && oldError.sampleID.equals(newError.sampleID))
                {
                    foundMatch = true;
                    //Add all the xpaths to the existing new error.
                    for (String xPath:oldError.xPaths)
                    {
                        if(!newError.xPaths.contains(xPath))
                        {
                            newError.addXPath(xPath);
                        }
                    }
                    Collections.sort(newError.xPaths);
                }
            }
            
            if(!foundMatch)
            {
                newErrorList.add(oldError);
            }
        }
        
        ValidationResult[] results = new ValidationResult[newErrorList.size()];
        for(int i = 0; i < newErrorList.size(); i++)
        {
            results[i]=newErrorList.get(i);
        }
        
        return results;
    }

    private static Element generateValidationErrorNode(Document doc, ValidationResult validationError)
    {
        //Change a validation error into an XML Node to put in our report.
        Element invMiringElement = doc.createElement("miring-result");
        
        //miringElementID
        Attr miringElementAttr = doc.createAttribute("miring-rule-id");
        miringElementAttr.setValue(validationError.getMiringRule());
        invMiringElement.setAttributeNode(miringElementAttr);
        
        //severity
        Attr fatalAttr = doc.createAttribute("severity");
        fatalAttr.setValue(validationError.getSeverity()==Severity.FATAL?"fatal":
            validationError.getSeverity()==Severity.MIRING?"miring":
            validationError.getSeverity()==Severity.WARNING?"warning":
            validationError.getSeverity()==Severity.INFO?"info":
                "?");
        invMiringElement.setAttributeNode(fatalAttr);
        
        //sampleID
        Attr sampleIDAttr = doc.createAttribute("sample-id");
        if(validationError.getSampleID() != null && validationError.getSampleID().length() > 0)
        {
            sampleIDAttr.setValue(validationError.getSampleID());
            invMiringElement.setAttributeNode(sampleIDAttr);
        }
        
        //description
        Element descriptionElement = doc.createElement("description");
        descriptionElement.appendChild(doc.createTextNode(validationError.getErrorText()));
        invMiringElement.appendChild(descriptionElement);
        
        //solution
        Element solutionElement = doc.createElement("solution");
        solutionElement.appendChild(doc.createTextNode(validationError.getSolutionText()));
        invMiringElement.appendChild(solutionElement);
        
        //xPath
        if(validationError.getXPaths() != null && validationError.getXPaths().size() > 0)
        {
            List<String> xPaths = validationError.getXPaths();
            for(int i = 0; i < xPaths.size(); i++)
            {
                Element xPathElement = doc.createElement("xpath");
                xPathElement.appendChild(doc.createTextNode(xPaths.get(i)));
                invMiringElement.appendChild(xPathElement);
            }
        }
        
        return invMiringElement;
    }
    
}
