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
import java.util.Arrays;
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

import org.nmdp.miring.ValidationResult.Severity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** 
 * This class is used to generate an XML results report, based on the results of a MIRING Validation.
*/
public class ReportGenerator
{
    static Logger logger = LoggerFactory.getLogger(ReportGenerator.class);
    
    public static DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    public static int hmls;
    
    /**
     * Generate a Miring Results Report
     *
     * @param validationResults an array of ValidationError objects
     * @param root the root attribute on an HMLID node on the source XML.  If it exists, you should include it in the report
     * @param extension the extension attribute on an HMLID node on the source XML.  If it exists, you should include it in the report
     * @param properties a HashMap<String,String> of property values to include on the results report
     * @param sampleIDs an array of Sample objects to list on the report.
     * @return a String containing MIRING Results Report
     */
    public static String generateReport(ValidationResult[] validationResults, String root, String extension, HashMap<String,String> properties, Sample[] sampleIDs, int hmlstart)
    {
        validationResults = assignSampleIDs(validationResults,sampleIDs);
        validationResults = combineSimilarResults(validationResults);
        hmls=hmlstart;
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
            rootElement.setAttribute("xsi:noNamespaceSchemaLocation", "http://schemas.nmdp.org/spec/miringreport/1.0/miringreport.xsd");
            
            addHMLCompliantElement(validationResults,doc);
            
            addMiringCompliantElement(validationResults, doc);
            
            addHmlidElement(root, extension, doc);
            
            addSampleElements(validationResults, sampleIDs, doc);
            
            addPropertyElements(properties, doc);
            
            addValidationResultElements(validationResults, doc);

            return(Utilities.getStringFromDoc(doc));
        }
        catch (ParserConfigurationException pce) 
        {
            logger.error("Parser Configuration Exception in ReportGenerator", pce);
        } 
        catch (Exception e) 
        {
            logger.error("Exception in ReportGenerator", e);
        }
        
        //Oops, something went wrong.
        logger.error("Unknown Error during Miring Validation Report Generation.  Returning Null");
        return null;
    }

    /**
     * Add a hml-compliant element to the document.
     *
     * @param validationResults an array of the validationError objects from this validation.
     * @param doc a Document to add the element to
     */
    private static void addHMLCompliantElement(ValidationResult[] validationResults, Document doc)
    {

        ValidationResult[] hmlErrors=validationResults;
    	Element compliantElement = doc.createElement("hml-compliant");
    	compliantElement.setTextContent(
    	(hmlErrors == null)? "false"
    	:(hmlErrors.length==0)?"true"
        :(Utilities.hasRejects(hmlErrors))?"reject"
    	:(Utilities.isHMLCompliant(hmlErrors))?"true" 
    	:"false"
        );
        doc.getDocumentElement().appendChild(compliantElement);
    } 
    
    /**
     * Add a miring-compliant element to the document.
     *
     * @param validationResults an array of the validationError objects from this validation.
     * @param doc a Document to add the element to
     */
    private static void addMiringCompliantElement(ValidationResult[] validationResults, Document doc)
    {
        Element compliantElement = doc.createElement("miring-compliant");
        compliantElement.setTextContent(
            (validationResults == null)?"false"
            :(validationResults.length==0)?"true"
            :(Utilities.hasRejects(validationResults))?"reject"
            :(Utilities.hasHMLFatalErrors(validationResults))?"false"
            :(Utilities.hasWarnings(validationResults))?"warnings"
            :(Utilities.isMiringCompliant(validationResults))?"true"
            :"false"
        );

        doc.getDocumentElement().appendChild(compliantElement);
    }

    /**
     * Add an hmlid element to the document.
     *
     * @param root the hmlid's root attribute
     * @param extension the hmlid's extension attribute
     * @param doc a Document to add the element to
     */
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
    
    /**
     * Add property elements to the document.
     *
     * @param properties a HashMap containing key-value pairs of properties to include on the report
     * @param doc a Document to add the elements to
     */
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

    /**
     * Add Sample elements to the document.
     *
     * @param validationResults an array of ValidationResults to assign samples to
     * @param sampleIDs an array of Sample objects to include on the report
     * @param doc a Document to add the elements to
     */
    private static void addSampleElements(ValidationResult[] validationResults, Sample[] sampleIDs, Document doc)
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
                //Make one for HML? Probably
                if(doesSampleHaveMiringErrors(sampleID, validationResults))
                {
                    currentSampleElement.setAttribute("miring-compliant", "false");
                    numberBadSamples++;
                }
                else
                {
                    currentSampleElement.setAttribute("miring-compliant", "true");
                    numberGoodSamples++;
                }
                if(doesSampleHaveHMLErrors(sampleID, validationResults))
                {
                	currentSampleElement.setAttribute("hml-compliant", "false");
                	numberBadSamples++;//Do we need seperate variable and new setAttributes for hml? I feel like its a waste.
                }
                else
                {
                	currentSampleElement.setAttribute("hml-compliant", "true");
                	numberGoodSamples++;//do we need?
                }

                samplesElement.appendChild(currentSampleElement);
            }
            
            samplesElement.setAttribute("sample-count", ("" + numberSampleIDs));
            samplesElement.setAttribute("noncompliant-sample-count", ("" + numberBadSamples));
            samplesElement.setAttribute("compliant-sample-count", ("" + numberGoodSamples));
            
            doc.getDocumentElement().appendChild(samplesElement);
        }
    }

    /**
     * Add ValidationResult elements to the document.
     *
     * @param validationResults an array of ValidationResult objects to include on the report
     * @param doc a Document to add the elements to
     */
    private static void addValidationResultElements(ValidationResult[] validationResults, Document doc)
    {
        ValidationResult[] fatalErrors = getResultsBySeverity(validationResults,Severity.FATAL);
        ValidationResult[] miringErrors = getResultsBySeverity(validationResults,Severity.MIRING);
        ValidationResult[] warnings = getResultsBySeverity(validationResults,Severity.WARNING);
        ValidationResult[] info = getResultsBySeverity(validationResults,Severity.INFO);
        ValidationResult[] hmlErrors = getResultsBySeverity(validationResults,Severity.HML);
        ValidationResult[] hmlFatal = getResultsBySeverity(validationResults,Severity.HMLFATAL);
        ValidationResult[] hmlWarnings = getResultsBySeverity(validationResults,Severity.HMLWARNING);

        if(fatalErrors != null && fatalErrors.length > 0)
        {
            Element fatalErrorsElement = doc.createElement("fatal-validation-errors");
            for(int i = 0; i < fatalErrors.length; i++)
            {
                fatalErrorsElement.appendChild(generateMiringResultElement(doc, fatalErrors[i]));
            }
            doc.getDocumentElement().appendChild(fatalErrorsElement);
        }
        if(miringErrors != null && miringErrors.length > 0)
        {
            Element miringErrorsElement = doc.createElement("miring-validation-errors");
            for(int i = 0; i < miringErrors.length; i++)
            {
                miringErrorsElement.appendChild(generateMiringResultElement(doc, miringErrors[i]));
            }
            doc.getDocumentElement().appendChild(miringErrorsElement);
        }
        if(warnings != null && warnings.length > 0)
        {
            Element warningsElement = doc.createElement("validation-warnings");
            for(int i = 0; i < warnings.length; i++)
            {
                warningsElement.appendChild(generateMiringResultElement(doc, warnings[i]));
            }
            doc.getDocumentElement().appendChild(warningsElement);
        }
        if(info != null && info.length > 0)
        {
            Element infoElement = doc.createElement("validation-info");
            for(int i = 0; i < info.length; i++)
            {
                infoElement.appendChild(generateMiringResultElement(doc, info[i]));
            }
            doc.getDocumentElement().appendChild(infoElement);
        }
        if(hmlFatal != null && hmlFatal.length > 0)
        {
            Element hmlFatalElement = doc.createElement("fatal-hml-schema-validation-errors");
            
            for(int i = 0; i < hmlFatal.length; i++)
            {
                hmlFatalElement.appendChild(generateHMLResultElement(doc, hmlFatal[i]));
            }
            doc.getDocumentElement().appendChild(hmlFatalElement);
        }
        if(hmlErrors != null && hmlErrors.length > 0)
        {
            Element hmlErrorsElement = doc.createElement("hml-schema-validation-errors");
            
            for(int i = 0; i < hmlErrors.length; i++)
            {
                hmlErrorsElement.appendChild(generateHMLResultElement(doc, hmlErrors[i]));
            }
            doc.getDocumentElement().appendChild(hmlErrorsElement);
        }
        if(hmlWarnings != null && hmlWarnings.length > 0)
        {
            Element hmlWarningsElement = doc.createElement("hml-schema-validation-warnings");
            
            for(int i = 0; i < hmlErrors.length; i++)
            {
                hmlWarningsElement.appendChild(generateHMLResultElement(doc, hmlWarnings[i]));
            }
            doc.getDocumentElement().appendChild(hmlWarningsElement);
        }
    }
    
    /**
     * Check if a sampleID has any assigned validationResult objects with severity of either FATAL or MIRING
     *
     * @param sampleID the sample's ID
     * @param validationResults an array of ValidationResult objects to compare against the sampleID
     */
    private static boolean doesSampleHaveMiringErrors(String sampleID, ValidationResult[] validationResults)
    {
        if(validationResults != null && validationResults.length > 0)
        {
            for(int i = 0; i < validationResults.length; i++)
            {
                ValidationResult tempResult = validationResults[i];
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
    private static boolean doesSampleHaveHMLErrors(String sampleID, ValidationResult[] validationResults)
    {
        if(validationResults != null && validationResults.length > 0)
        {
            for(int i = hmls; i < validationResults.length; i++)
            {
                ValidationResult tempResult = validationResults[i];
                String currentSampleID = tempResult.getSampleID();
                Severity currentSeverity = tempResult.getSeverity();
                if(currentSampleID != null && currentSampleID.equals(sampleID))
                {
                    if(currentSeverity != null && (
                        currentSeverity.equals(Severity.HMLFATAL)
                        || currentSeverity.equals(Severity.HML)))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * For each ValidationResult, assign a SampleID if possible
     *
     * @param validationResults an array of ValidationResult objects to assign sampleIDs to
     * @param sampleIDs an array of sampleIDs found in the HML
     */
    private static ValidationResult[] assignSampleIDs(ValidationResult[] validationResults, Sample[] sampleIDs)
    {
        try
        {
            if(validationResults != null
                && validationResults.length > 0 
                && sampleIDs != null 
                && sampleIDs.length > 0)
                
            {
              
                for(int i = 0; i < validationResults.length; i++)
                {
                    ValidationResult currentError = validationResults[i];
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
            logger.error("Error during assignSampleIDs.", e);
        }
        return validationResults;
    }

    /**
     * Get ValidationResults of a specific severity.
     *
     * @param validationResults an array of ValidationResult objects to pull from
     * @param severity a ValidationResult.Severity.  One of "FATAL" "MIRING" "INFO" "WARNING" "HML" "HMLFATAL 
     * "REJECT" "HMLWARNING".
     */
    private static ValidationResult[] getResultsBySeverity(ValidationResult[] validationResults, Severity severity)
    {
        if(validationResults != null && validationResults.length > 0)
        {
            List<ValidationResult> results = new ArrayList<ValidationResult>();
            for(int i = 0; i < validationResults.length; i++)
            {
                if(validationResults[i].severity.equals(severity))
                {
                    results.add(validationResults[i]);
                }
            }
            return results.toArray(new ValidationResult[results.size()]);
        }
        else
        {
            return null;
        }
    }

    /**
     * Combine similar results.  They are considered similar if they have the same error text.  The results are combined, with multiple xpaths.
     *
     * @param validationResults an array of ValidationResult objects to combine
     */
    private static ValidationResult[] combineSimilarResults(ValidationResult[] validationResults)
    {
        List<ValidationResult> newResultList = new ArrayList<ValidationResult>();
        try{
        for(int i = 0; i < validationResults.length; i++)
        {
            ValidationResult oldResult = validationResults[i];
            //Scan existing list for an error that is a close match.
            boolean foundMatch = false;
            for (ValidationResult newResult: newResultList)
            {
                if(oldResult.miringRule.equals(newResult.miringRule)
                   && oldResult.errorText.equals(newResult.errorText)
                    && oldResult.sampleID.equals(newResult.sampleID)
                   )
                {
                    foundMatch = true;
                    //Add all the xpaths to the existing new error.
                    for (String xPath:oldResult.xPaths)
                    {
                        if(!newResult.xPaths.contains(xPath))
                        {
                            newResult.addXPath(xPath);
                        }
                    }
                    Collections.sort(newResult.xPaths);
                }
            }
            
            if(!foundMatch)
            {
                newResultList.add(oldResult);
            }
        }
        
        ValidationResult[] newResults = new ValidationResult[newResultList.size()];
        for(int i = 0; i < newResultList.size(); i++)
        {
            newResults[i]=newResultList.get(i);
        }
        
        return newResults;
        }
        catch(Exception e)
        {
            logger.error("Can not combine similar results due to some unknown reason "+ e);
            return validationResults;
        }
    }
    
    /**
     * Generate a single miring-result element
     *
     * @param doc the original document to add to
     * @param validationResult an object containing the result information
     */
    private static Element generateMiringResultElement(Document doc, ValidationResult validationResult)
    {
        //Change a validation error into an XML Node to put in our report.
        Element invMiringElement = doc.createElement("miring-result");
        
        //miringElementID
        Attr miringElementAttr = doc.createAttribute("miring-rule-id");
        miringElementAttr.setValue(validationResult.getMiringRule());
        invMiringElement.setAttributeNode(miringElementAttr);
        
        //severity
        Attr fatalAttr = doc.createAttribute("severity");
        fatalAttr.setValue(validationResult.getSeverity()==Severity.FATAL?"fatal":
            validationResult.getSeverity()==Severity.MIRING?"miring":
            validationResult.getSeverity()==Severity.WARNING?"warning":
            validationResult.getSeverity()==Severity.INFO?"info":
                "?");
        invMiringElement.setAttributeNode(fatalAttr);
        
        //sampleID
        Attr sampleIDAttr = doc.createAttribute("sample-id");
        if(validationResult.getSampleID() != null && validationResult.getSampleID().length() > 0)
        {
            sampleIDAttr.setValue(validationResult.getSampleID());
            invMiringElement.setAttributeNode(sampleIDAttr);
        }
        
        //description
        Element descriptionElement = doc.createElement("description");
        descriptionElement.appendChild(doc.createTextNode(validationResult.getErrorText()));
        invMiringElement.appendChild(descriptionElement);
        
        //solution
        Element solutionElement = doc.createElement("solution");
        solutionElement.appendChild(doc.createTextNode(validationResult.getSolutionText()));
        invMiringElement.appendChild(solutionElement);
        
        //xPath
        if(validationResult.getXPaths() != null && validationResult.getXPaths().size() > 0)
        {
            List<String> xPaths = validationResult.getXPaths();
            for(int i = 0; i < xPaths.size(); i++)
            {
                Element xPathElement = doc.createElement("xpath");
                xPathElement.appendChild(doc.createTextNode(xPaths.get(i)));
                invMiringElement.appendChild(xPathElement);
            }
        }
        
        return invMiringElement;
    }
    private static Element generateHMLResultElement(Document doc, ValidationResult validationResult)
    {
        //Change a validation error into an XML Node to put in our report.
        Element invHMLElement = doc.createElement("hml-result");
        
        
        //severity
        Attr fatalAttr = doc.createAttribute("severity");
        fatalAttr.setValue(validationResult.getSeverity()==Severity.HMLFATAL?"fatal":
            validationResult.getSeverity()==Severity.HML?"hml":
            validationResult.getSeverity()==Severity.HMLWARNING?"warning"://make hml-warning
            validationResult.getSeverity()==Severity.INFO?"info":
                "?");
        invHMLElement.setAttributeNode(fatalAttr);
        
        //sampleID
        Attr sampleIDAttr = doc.createAttribute("sample-id");
        if(validationResult.getSampleID() != null && validationResult.getSampleID().length() > 0)
        {
            sampleIDAttr.setValue(validationResult.getSampleID());
            invHMLElement.setAttributeNode(sampleIDAttr);
        }
        
        //description
        Element descriptionElement = doc.createElement("description");
        descriptionElement.appendChild(doc.createTextNode(validationResult.getErrorText()));
        invHMLElement.appendChild(descriptionElement);
        
        //solution
        Element solutionElement = doc.createElement("solution");
        solutionElement.appendChild(doc.createTextNode(validationResult.getSolutionText()));
        invHMLElement.appendChild(solutionElement);
        
        //xPath
        if(validationResult.getXPaths() != null && validationResult.getXPaths().size() > 0)
        {
            List<String> xPaths = validationResult.getXPaths();
            for(int i = 0; i < xPaths.size(); i++)
            {
                Element xPathElement = doc.createElement("xpath");
                xPathElement.appendChild(doc.createTextNode(xPaths.get(i)));
                invHMLElement.appendChild(xPathElement);
            }
        }
        
        return invHMLElement;
    }
}
