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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
    
    /**
     * Generate a Miring Results Report
     *
     * @param validationErrors an array of ValidationError objects
     * @param root the root attribute on an HMLID node on the source XML.  If it exists, you should include it in the report
     * @param extension the extension attribute on an HMLID node on the source XML.  If it exists, you should include it in the report
     * @return a String containing MIRING Results Report
     */
    public static String generateReport(ValidationResult[] validationErrors, String root, String extension, HashMap<String,String> properties)
    {
        validationErrors = combineSimilarErrors(validationErrors);
        try 
        {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
             
            //MIRINGREPORT ROOT
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("MiringReport");
            doc.appendChild(rootElement);
            
            //MIRINGCOMPLIANT ATTRIBUTE
            Attr compliantAttr = doc.createAttribute("miringCompliant");
            compliantAttr.setValue(
                (validationErrors == null)?"false"
                :(validationErrors.length==0)?"true"
                :(Utilities.isMiringCompliant(validationErrors))?"true"
                :"false"
            );
            rootElement.setAttributeNode(compliantAttr);
            
            //HMLID element
            Element hmlidElement = doc.createElement("hmlid");
            if(root != null && root.length()>0)
            {
                Attr rootAttr = doc.createAttribute("root");
                rootAttr.setValue(root);
                hmlidElement.setAttributeNode(rootAttr);
            }
            if(extension != null && extension.length()>0)
            {
                Attr extAttr = doc.createAttribute("extension");
                extAttr.setValue(extension);
                hmlidElement.setAttributeNode(extAttr);
            }
            rootElement.appendChild(hmlidElement);
            
            //PROPERTIES
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
                    
                    //name
                    Attr attr = doc.createAttribute("name");
                    attr.setValue(name);
                    property.setAttributeNode(attr);
                    
                    //name
                    attr = doc.createAttribute("value");
                    attr.setValue(value);
                    property.setAttributeNode(attr);
                    
                    rootElement.appendChild(property);
                }
            }
            
            //MIRINGRESULT ELEMENTS            
            ValidationResult[] fatalErrors = getErrorsBySeverity(validationErrors,Severity.FATAL);
            ValidationResult[] miringErrors = getErrorsBySeverity(validationErrors,Severity.MIRING);
            ValidationResult[] warnings = getErrorsBySeverity(validationErrors,Severity.WARNING);
            ValidationResult[] info = getErrorsBySeverity(validationErrors,Severity.INFO);

            if(fatalErrors != null && fatalErrors.length > 0)
            {
                Element fatalErrorsElement = doc.createElement("FatalValidationErrors");
                for(int i = 0; i < fatalErrors.length; i++)
                {
                    fatalErrorsElement.appendChild(generateValidationErrorNode(doc, fatalErrors[i]));
                }
                rootElement.appendChild(fatalErrorsElement);
            }
            if(miringErrors != null && miringErrors.length > 0)
            {
                Element miringErrorsElement = doc.createElement("MiringValidationErrors");
                for(int i = 0; i < miringErrors.length; i++)
                {
                    miringErrorsElement.appendChild(generateValidationErrorNode(doc, miringErrors[i]));
                }
                rootElement.appendChild(miringErrorsElement);
            }
            if(warnings != null && warnings.length > 0)
            {
                Element warningsElement = doc.createElement("FatalValidationErrors");
                for(int i = 0; i < warnings.length; i++)
                {
                    warningsElement.appendChild(generateValidationErrorNode(doc, warnings[i]));
                }
                rootElement.appendChild(warningsElement);
            }
            if(info != null && info.length > 0)
            {
                Element infoElement = doc.createElement("FatalValidationErrors");
                for(int i = 0; i < info.length; i++)
                {
                    infoElement.appendChild(generateValidationErrorNode(doc, info[i]));
                }
                rootElement.appendChild(infoElement);
            }

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
                    && oldError.errorText.equals(newError.errorText))
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
        Element invMiringElement = doc.createElement("MiringResult");
        
        //miringElementID
        Attr miringElementAttr = doc.createAttribute("miringRuleID");
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
