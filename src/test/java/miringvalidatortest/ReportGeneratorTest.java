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
package test.java.miringvalidatortest;

import static org.junit.Assert.*;
import main.java.miringvalidator.MiringValidatorService;
import main.java.miringvalidator.ReportGenerator;
import main.java.miringvalidator.Utilities;
import main.java.miringvalidator.ValidationError;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ReportGeneratorTest
{
    private static final Logger logger = LogManager.getLogger(ReportGeneratorTest.class);

    @Test
    public void testReportGenerator()
    {
        try
        {
            ValidationError error1 = new ValidationError("This is a big problem 1.", true);
            ValidationError error2 = new ValidationError("This is a big problem 2.", true);
            ValidationError error3 = new ValidationError("This is a big problem 3.", true);
            ValidationError error4 = new ValidationError("This is a big problem 4.", true);
            ValidationError error5 = new ValidationError("This is a big problem 5.", true);
            
            ValidationError[] tier1Errors = {error1, error2};
            ValidationError[] tier2Errors = {error3, error4, error5};
            
            String reportResults = ReportGenerator.generateReport(tier1Errors, tier2Errors, "testRoot", "1.2.3.4");
            
            assert(reportResults != null);
            assert(reportResults.length() > 4);

            Element rootElement = Utilities.xmlToDomObject(reportResults);  
            
            NodeList list = rootElement.getElementsByTagName("InvalidMiringResult");
            logger.debug("Report Generator Test");
            logger.debug("listCount = " + list.getLength());


            if (list != null && list.getLength() > 0) 
            {
                for(int i = 1; i < list.getLength(); i++)
                {
                    logger.debug("Local name:" + list.item(i).getLocalName());
                    //logger.debug(list.toString());
                }
                //I want to test that there is a node for a couple of those errors i made up.  

            }
        }
        catch(Exception e)
        {
            logger.error("Exception in testReportGenerator(): " + e);
            fail("Error testing Report Generator" + e);
        }
        
    }

}
