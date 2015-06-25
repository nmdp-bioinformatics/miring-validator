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

import java.util.HashMap;

import main.java.miringvalidator.MiringValidator;
import main.java.miringvalidator.MiringValidatorService;
import main.java.miringvalidator.ReportGenerator;
import main.java.miringvalidator.Utilities;
import main.java.miringvalidator.ValidationResult;
import main.java.miringvalidator.ValidationResult.Severity;

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
        logger.debug("starting testReportGenerator");

        try
        {
            ValidationResult error1 = new ValidationResult("This is a big problem 1.", Severity.MIRING);
            ValidationResult error2 = new ValidationResult("This is a big problem 2.", Severity.MIRING);
            ValidationResult error3 = new ValidationResult("This is a big problem 3.", Severity.MIRING);
            ValidationResult error4 = new ValidationResult("This is a big problem 4.", Severity.MIRING);
            ValidationResult error5 = new ValidationResult("This is a big problem 5.", Severity.MIRING);
            
            ValidationResult[] tier1Errors = {error1, error2};
            ValidationResult[] tier2Errors = {error3, error4, error5};
            
            String reportResults = ReportGenerator.generateReport(Utilities.combineArrays(tier1Errors, tier2Errors), "testRoot", "1.2.3.4", null, null);
            
            assertTrue(reportResults != null);
            assertTrue(reportResults.length() > 4);

            Element rootElement = Utilities.xmlToRootElement(reportResults);              
            NodeList list = rootElement.getElementsByTagName("MiringResult");
            assertTrue(list.getLength() == 5);
            
            assertTrue(Utilities.containsErrorNode(reportResults, "This is a big problem 1."));
            assertTrue(Utilities.containsErrorNode(reportResults, "This is a big problem 3."));
            assertTrue(Utilities.containsErrorNode(reportResults, "This is a big problem 5."));
            assertFalse(Utilities.containsErrorNode(reportResults, "This error text is not in the report."));
            
            String hmlIDRoot = Utilities.getHMLIDRoot(reportResults);
            String hmlIDExtension = Utilities.getHMLIDExtension(reportResults);            
            assertTrue(hmlIDRoot.equals("testRoot"));
            assertTrue(hmlIDExtension.equals("1.2.3.4"));
            
            //Test the qualityscore of a report.              
            ValidationResult fatalError = new ValidationResult("A fatal error", Severity.FATAL);
            ValidationResult nonFatalError = new ValidationResult("A nonFatal error", Severity.WARNING);
            
            String score1Report = ReportGenerator.generateReport(new ValidationResult[]{}, "testRoot", "1.2.3.4", null, null);
            String score2Report = ReportGenerator.generateReport(new ValidationResult[]{nonFatalError}, "testRoot", "1.2.3.4", null, null);
            String score3Report = ReportGenerator.generateReport(new ValidationResult[]{fatalError,nonFatalError}, "testRoot", "1.2.3.4", null, null);
            
            String score1Compliance = Utilities.xmlToRootElement(score1Report).getAttributes().getNamedItem("miringCompliant").getNodeValue();
            String score2Compliance = Utilities.xmlToRootElement(score2Report).getAttributes().getNamedItem("miringCompliant").getNodeValue();
            String score3Compliance = Utilities.xmlToRootElement(score3Report).getAttributes().getNamedItem("miringCompliant").getNodeValue();
            
            assertEquals(score1Compliance ,"true");
            assertEquals(score2Compliance ,"true");
            assertEquals(score3Compliance ,"false");
        }
        catch(Exception e)
        {
            logger.error("Exception in testReportGenerator(): " + e);
            fail("Error testing Report Generator" + e);
        }
    }
    
    @Test
    public void testGetPropertiesFromRootHml()
    {
        logger.debug("starting testXmlWithNamespace");
        String xml = Utilities.readXmlResource("/hml/HMLWithNamespaces.hml");
        
        HashMap properties = Utilities.getPropertiesFromRootHml(xml);

        assertTrue(properties.size() > 0);
        assertTrue(properties.get("MessageReceived") != null 
                && properties.get("MessageReceived").toString().length() > 1); 
    }
}
