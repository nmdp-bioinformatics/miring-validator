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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class MiringValidator
{
    private static final Logger logger = LogManager.getLogger(MiringValidator.class);
    String xml;
    String report;
    ValidationError[] tier1ValidationErrors;
    ValidationError[] tier2ValidationErrors;
    
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
     * Validate the xml text against MIRING checklist
     *
     * @return a String containing MIRING Results Report
     */
    public String validate()
    {
        //Tier 1
        logger.debug("Attempting Tier 1 Validation");
        tier1ValidationErrors = SchemaValidator.validate(xml, "/schema/MiringTier1.xsd");
        
        //Tier 2
        //Skip it if we already know it is bad.  Maybe?  Do we want to schematron automatically?
        //Yeah actually don't skip it.  We want to give them the most information we can.
        //if(!ReportGenerator.containsFatalErrors(tier1ValidationErrors))
        
        //We're just gonna skip tier 2 if there are fatal schema validation errors.
        //I mean fatal as in it's a bad XML file, not a bad MIRING file.
        //Schematron can't get errors if it can't parse the XML validly.
        //For now I only know about the "Content is not allowed in prolog." error
        String tier1Report = ReportGenerator.generateReport(tier1ValidationErrors, null, null);
        if(!Utilities.containsErrorNode(tier1Report,"Content is not allowed in prolog."))
        {
            //It's probably a big resource overhead to do a schematron for each schema file.  
            //If time is a problem, we should proably combine all the schematron schema into one file.
            //For each schematron validation, several XSLT transforms are required, and it's kinda time consuming.
            //Consider combining all the schema.
            //It might be possible to have one schema load several other ones, I'll have to look into that.
            //And by that i mean embedding a schematron in another schematron.  Possible?  Maybe?
            logger.debug("Attempting Tier 2 validation");
            
            //tier2ValidationErrors = SchematronValidator.validate(xml, new String[] {"demo.sch"});
            tier2ValidationErrors = SchematronValidator.validate(xml, new String[] 
                    {"/schematron/MiringElement1.sch", "/schematron/MiringElement2.sch", "/schematron/MiringElement3.sch", "/schematron/MiringElement4.sch"
                    , "/schematron/MiringElement5.sch", "/schematron/MiringElement6.sch", "/schematron/MiringElement7.sch", "/schematron/MiringElement8.sch"}
            );
            
            //Tier 3 is outside scope for now.  Okay.
            /*if(!containsFatalErrors(tier2ValidationErrors))
            {
            //tier3();
            }*/
        }

        //Make a report.
        String hmlIdRoot = Utilities.getHMLIDRoot(xml);
        String hmlIdExt = Utilities.getHMLIDExtension(xml);        
        report = ReportGenerator.generateReport(Utilities.combineArrays(tier1ValidationErrors, tier2ValidationErrors), hmlIdRoot, hmlIdExt);
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
