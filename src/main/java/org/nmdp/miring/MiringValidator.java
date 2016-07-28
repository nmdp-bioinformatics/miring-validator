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

import org.nmdp.miring.ValidationResult.Severity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
//import org.w3c.dom.DocumentBuilder;
//import org.w3c.dom.DocumentBuilderFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

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
    String version;
    
    /**
     * Constructor for a MiringValidator object
     *
     * @param xml a String containing the xml text
     */
    public MiringValidator(String xml,String version)
    {
        this.xml = xml;
        this.report = null;
        this.version=version;
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
        //Retrieve version number submitted by user
        //worry in space and optimization
        /**
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(new File(xml));
        NodeList nodeList = document.getElementsByTagName("hml");
        String versionNumber = nodeList.item(x).getAttributes().getNamedItem("version").getNodeValue();
         
        logger.debug("Version Number: "+ cd.getData());*/
        
        HashMap<String,String> properties = Utilities.getPropertiesFromRootHml(xml);
        logger.debug("Attempting HML Validation");
        //Make method called version control
        hmlValidationErrors = SchemaValidator.validate(xml,"/org/nmdp/miring/schema/hml-"+version+".xsd");
        //If there are any fatal issues with HML do not continue
        if(!Utilities.hasHMLFatalErrors(hmlValidationErrors)&&!Utilities.hasRejects(hmlValidationErrors))
        {
        	//Tier 1
            logger.debug("Attempting Tier 1 Validation");
            tier1ValidationErrors = SchemaValidator.validate(xml, "/org/nmdp/miring/schema/MiringTier1.xsd");
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
}
