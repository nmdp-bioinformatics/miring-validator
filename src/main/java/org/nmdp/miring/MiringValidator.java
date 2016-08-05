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

import java.util.HashMap;
import java.util.*;
import java.util.Map.*;
import java.io.StringReader;

import org.nmdp.miring.ValidationResult.Severity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/** 
 * This class is used to validate an XML file against a MIRING checklist.  Both tier 1 and tier 2 are included in the validation.
*/
public class MiringValidator
{
    Logger logger = LoggerFactory.getLogger(MiringValidator.class);
    String xml;
    String report;
    ValidationResult[] tier1ValidationErrors;
    ValidationResult[] tier2ValidationErrors;
    ValidationResult[] hmlValidationErrors;
    Sample[] sampleIDs;
    public static Document xmlDom;
    
    /**
     * Constructor for a MiringValidator object
     *
     * @param xml a String containing the xml text
     */
    public MiringValidator(String xml)
    {
        this.xml = xml;
        this.report = null;
    }
    
    /**
     * Validate the xml text against MIRING checklist.  This method performs validation for both Tiers 1 and 2.
     *
     * @return a String containing MIRING Results Report
     */
    public String validate()
    {
        if(xml==null || xml.length() == 0)
        {
            logger.error("XML is null or length 0.");
            return ReportGenerator.generateReport(new ValidationResult[]{new ValidationResult("XML is null or length 0.",Severity.FATAL), new ValidationResult("XML is null or length 0.", Severity.HMLFATAL)}, null, null,null,null,0);
        }
        
        HashMap<String,String> properties = Utilities.getPropertiesFromRootHml(xml);
        logger.debug("Attempting HML Validation");
        String version = getVersion();
        System.out.println("Version Number = "+version);
        if(version==null)
        {
            report = ReportGenerator.generateReport(new ValidationResult[]{new ValidationResult("No Version Number Detected Please have a version number under then HML node",Severity.FATAL),new ValidationResult("No Version Number Detected Please have a version number under then HML node",Severity.HMLFATAL)}, null, null,null,null,0);
        }
        else
        {
        //Make method called version control
        hmlValidationErrors = SchemaValidator.validate(xml,"/org/nmdp/miring/schema/hml-"+version+".xsd");
        //If there are any fatal issues with HML do not continue
        if(!Utilities.hasHMLFatalErrors(hmlValidationErrors)&&!Utilities.hasRejects(hmlValidationErrors))
        {
        	//Tier 1
            logger.debug("Attempting Tier 1 Validation");
            tier1ValidationErrors = SchemaValidator.validate(xml, getMiring(version));
            sampleIDs = SchemaValidator.samples.toArray(new Sample[SchemaValidator.samples.size()]);
            //Tier 2
            //If tier 1 has fatal errors, we should not continue to tier 2.
            if(!Utilities.hasFatalErrors(tier1ValidationErrors))
            {
                logger.debug("Attempting Tier 2 validation");
                
                tier2ValidationErrors = SchematronValidator.validate(xml, new String[] {"/org/nmdp/miring/schematron/MiringAll.sch"});
                //Make a report.
                String hmlIdRoot = Utilities.getHMLIDRoot(xml);
                String hmlIdExt = Utilities.getHMLIDExtension(xml);
                report = ReportGenerator.generateReport(Utilities.combineArrays(tier1ValidationErrors, tier2ValidationErrors, hmlValidationErrors), hmlIdRoot, hmlIdExt, properties, sampleIDs,(tier1ValidationErrors.length + tier2ValidationErrors.length));

                
                //Tier 3 is outside scope for now.  Okay.
                /*if(!Utilities.hasFatalErrors(tier2ValidationErrors)))
                {
                //tier3();
                }*/
            }
            else
            {
                logger.error("Did not perform tier 2 validation, fatal errors in tier 1.");
                tier2ValidationErrors=new ValidationResult[0];
                
                //Make a report.
                String hmlIdRoot = Utilities.getHMLIDRoot(xml);
                String hmlIdExt = Utilities.getHMLIDExtension(xml);
                report = ReportGenerator.generateReport(Utilities.combineArrays(tier1ValidationErrors,tier2ValidationErrors,  hmlValidationErrors), hmlIdRoot, hmlIdExt, properties, sampleIDs,(tier1ValidationErrors.length+1));
            }

        }
        else
        {
            //Make a report.
            String hmlIdRoot = Utilities.getHMLIDRoot(xml);
            String hmlIdExt = Utilities.getHMLIDExtension(xml);
            report = ReportGenerator.generateReport(hmlValidationErrors, hmlIdRoot, hmlIdExt, properties, sampleIDs,0);

            logger.error("Did not perform Tier 1 validation, fatal errors in HML or malformed HML");
        }
        }
        
        
        return report;
    }

    public String getXml()
    {
        return xml;
    }

    public void setXml(String xml)
    {
        this.xml = xml;
    }

    
    public String getReport()
    {
        return report;
    }
    public String getVersion()
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document xmlDOM=null;
        try {
            builder = factory.newDocumentBuilder();
             xmlDOM = builder.parse(new InputSource(new StringReader(xml)));
            
        } catch (Exception e) {
            System.out.println("Error in handle Grabbing ");
            return "1.0.1";
        }
        NodeList xmlAttributes = xmlDOM.getElementsByTagName("*");
        NamedNodeMap xmlAttribute = xmlAttributes.item(0).getAttributes();
        for(int i = 0; i<xmlAttribute.getLength();i++){
            System.out.println("Custom Name Space Test "+xmlAttribute.item(i).getNodeName().toString());
            if(xmlAttribute.item(i).getNodeName().equals("version"))
            {
                return xmlAttribute.item(i).getNodeValue();
            }
        }
        return null;
        
        
        
    }
    public String getMiring(String version)
    {
        return (version.equals("1.0.1"))? "/org/nmdp/miring/schema/MiringTier1.xsd":"/org/nmdp/miring/schema/MiringTier1-1.0.xsd";
    }
}
