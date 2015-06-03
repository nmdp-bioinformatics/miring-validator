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
     * @return a String containing MIRING Results Report
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
        tier1ValidationErrors = SchemaValidator.validate(xml, "MiringTier1.xsd");
        
        //Tier 2
        //Skip it if we already know it is bad.  Maybe?  Do we want to schematron automatically?
        //Yeah actually don't skip it.  We want to give them the most information we can.
        //if(!ReportGenerator.containsFatalErrors(tier1ValidationErrors))
        {
            logger.debug("Attempting Tier 2 validation");
            //tier2ValidationErrors = SchematronValidator.validate(xml, new String[] {"demo.sch"});
            tier2ValidationErrors = SchematronValidator.validate(xml, new String[] 
                    {"MiringElement1.sch", "MiringElement2.sch", "MiringElement3.sch", "MiringElement4.sch"
                    , "MiringElement5.sch", "MiringElement6.sch", "MiringElement7.sch", "MiringElement8.sch"}
            );
            
            //Tier 3 is outside scope for now.  Okay.
            /*if(!containsFatalErrors(tier2ValidationErrors))
            {
                
            }*/
        }

        //Make a report.
        String hmlIdRoot = Utilities.getHMLIDRoot(xml);
        String hmlIdExt = Utilities.getHMLIDExtension(xml);        
        report = ReportGenerator.generateReport(tier1ValidationErrors, tier2ValidationErrors, hmlIdRoot, hmlIdExt);
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
