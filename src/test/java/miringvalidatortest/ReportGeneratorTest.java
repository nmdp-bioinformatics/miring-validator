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
        logger.debug("starting testReportGenerator");

        try
        {
            ValidationError error1 = new ValidationError("This is a big problem 1.", true);
            ValidationError error2 = new ValidationError("This is a big problem 2.", true);
            ValidationError error3 = new ValidationError("This is a big problem 3.", true);
            ValidationError error4 = new ValidationError("This is a big problem 4.", true);
            ValidationError error5 = new ValidationError("This is a big problem 5.", true);
            
            ValidationError[] tier1Errors = {error1, error2};
            ValidationError[] tier2Errors = {error3, error4, error5};
            
            String reportResults = ReportGenerator.generateReport(Utilities.combineArrays(tier1Errors, tier2Errors), "testRoot", "1.2.3.4");
            
            assertTrue(reportResults != null);
            assertTrue(reportResults.length() > 4);

            Element rootElement = Utilities.xmlToDomObject(reportResults);              
            NodeList list = rootElement.getElementsByTagName("InvalidMiringResult");
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
            ValidationError fatalError = new ValidationError("A fatal error", true);
            ValidationError nonFatalError = new ValidationError("A nonFatal error", false);
            
            //1 = Perfectly conforms to MIRING, no errors
            //2 = Some nonfatal warnings
            //3 = Rejected.
            String score1Report = ReportGenerator.generateReport(new ValidationError[]{}, "testRoot", "1.2.3.4");
            String score2Report = ReportGenerator.generateReport(new ValidationError[]{nonFatalError}, "testRoot", "1.2.3.4");
            String score3Report = ReportGenerator.generateReport(new ValidationError[]{fatalError,nonFatalError}, "testRoot", "1.2.3.4");
            
            NodeList score1QualityScoreNodes = Utilities.xmlToDomObject(score1Report).getElementsByTagName("QualityScore");
            NodeList score2QualityScoreNodes = Utilities.xmlToDomObject(score2Report).getElementsByTagName("QualityScore");
            NodeList score3QualityScoreNodes = Utilities.xmlToDomObject(score3Report).getElementsByTagName("QualityScore");
            
            assertEquals(score1QualityScoreNodes.getLength() ,1);
            assertEquals(score2QualityScoreNodes.getLength() ,1);
            assertEquals(score3QualityScoreNodes.getLength() ,1);
             
            assertEquals(score1QualityScoreNodes.item(0).getTextContent() ,"1");
            assertEquals(score2QualityScoreNodes.item(0).getTextContent() ,"2");
            assertEquals(score3QualityScoreNodes.item(0).getTextContent() ,"3");
        }
        catch(Exception e)
        {
            logger.error("Exception in testReportGenerator(): " + e);
            fail("Error testing Report Generator" + e);
        }
    }
}
