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
package miringvalidator.main;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ReportGenerator
{
    /**
     * Generate a Miring Results Report
     *
     * @param tier1ValidationErrors an array of ValidationError objects
     * @param tier2ValidationErrors an array of ValidationError objects
     * @param root the root attribute on an HMLID node on the source XML.  If it exists, you should include it in the report
     * @param extension the extension attribute on an HMLID node on the source XML.  If it exists, you should include it in the report
     * @return a String containing MIRING Results Report
     */
    public static String generateReport(ValidationError[] tier1ValidationErrors, ValidationError[] tier2ValidationErrors, String root, String extension)
    {
        
        try 
        {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
             
            //MIRINGREPORT ROOT
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("MiringReport");
            doc.appendChild(rootElement);
            
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
            
            //QUALITY SCORE
            //No errors = 1. 
            //No fatal errors = 2.
            //Some fatal errors = 3.            
            String quality = (tier1ValidationErrors.length==0 && tier2ValidationErrors.length==0)?"1"
                    :(!containsFatalErrors(tier1ValidationErrors) && !containsFatalErrors(tier2ValidationErrors))?"2"
                    :"3";
            
            Element qualityElement = doc.createElement("QualityScore");
            qualityElement.appendChild(doc.createTextNode(quality));
            rootElement.appendChild(qualityElement);
            
            //INVALIDMIRINGRESULT ELEMENTS
            if(tier1ValidationErrors != null)
            {
                for(int i = 0; i < tier1ValidationErrors.length; i++)
                {
                    rootElement.appendChild(generateValidationErrorNode(doc, tier1ValidationErrors[i]));
                }
            }
            if(tier2ValidationErrors != null)
            {
                for(int i = 0; i < tier2ValidationErrors.length; i++)
                {
                    rootElement.appendChild(generateValidationErrorNode(doc, tier2ValidationErrors[i]));
                }
            }
            //Tier 3
            
            return(getStringFromDoc(doc));
        }
        catch (ParserConfigurationException pce) 
        {
            pce.printStackTrace();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        
        //Oops, something went wrong.
        System.out.println("Error during Miring Validation Report Generation.");
        return null;
    }
    
    /**
     * Does this array contain any fatal errors?
     *
     * @param errors an array of ValidationError objects
     * @return does this array contain any fatal errors?
     */
    public static boolean containsFatalErrors(ValidationError[] errors)
    {
        //Does this list contain any fatal errors?
        for(int i = 0; i < errors.length; i++)
        {
            if(errors[i].isFatal())
            {
                return true;
            }
        }
        return false;
    }
    
    private static Element generateValidationErrorNode(Document doc, ValidationError validationError)
    {
        //Change a validation error into an XML Node to put in our report.
        Element invMiringElement = doc.createElement("InvalidMiringresult");
        
        //miringElementID
        Attr miringElementAttr = doc.createAttribute("miringElementID");
        miringElementAttr.setValue(validationError.getMiringElement());
        invMiringElement.setAttributeNode(miringElementAttr);
        
        //fatal
        Attr fatalAttr = doc.createAttribute("fatal");
        fatalAttr.setValue(validationError.isFatal()?"true":"false");
        invMiringElement.setAttributeNode(fatalAttr);
        
        //description
        Element invDescriptor = doc.createElement("description");
        invDescriptor.appendChild(doc.createTextNode(validationError.getErrorText()));
        invMiringElement.appendChild(invDescriptor);
        
        //solution
        Element invSolution = doc.createElement("solution");
        invSolution.appendChild(doc.createTextNode(validationError.getSolutionText()));
        invMiringElement.appendChild(invSolution);

        return invMiringElement;
    }
    
    private static String getStringFromDoc(Document doc)
    {
        //Generate an XML String from the Document object.
        String xmlString = null;
        try
        {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            //initialize StreamResult with File object to save to file
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
            xmlString = result.getWriter().toString();            
        }
        catch(Exception e)
        {
            System.out.println("Error generating XML String");
        }
        return xmlString;
    }
}
