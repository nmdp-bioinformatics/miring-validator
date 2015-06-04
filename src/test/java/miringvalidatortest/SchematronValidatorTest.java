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
import main.java.miringvalidator.ReportGenerator;
import main.java.miringvalidator.SchemaValidator;
import main.java.miringvalidator.SchematronValidator;
import main.java.miringvalidator.Utilities;
import main.java.miringvalidator.ValidationError;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

public class SchematronValidatorTest
{
    private static final Logger logger = LogManager.getLogger(SchematronValidatorTest.class);

    @Test
    public void schematronValidatorTest()
    {
        logger.debug("Starting a schematronValidatorTest");
        
        String demoGoodXML = Utilities.readXmlResource("/hml/demogood.xml");
        String demoBadXML = Utilities.readXmlResource("/hml/demobad.xml");

        ValidationError[] goodDemoErrors = SchematronValidator.validate(demoGoodXML,new String[]{"/schematron/demo.sch"});
        ValidationError[] badDemoErrors = SchematronValidator.validate(demoBadXML,new String[]{"/schematron/demo.sch"});
        
        assertTrue(goodDemoErrors.length == 0);
        
        //Schematron should find 2 errors in the demo files.  
        assertTrue(badDemoErrors.length == 2);
        
        //It doesn't really make sense to combine these two error sets like this in a report (should be schema errors + schematron errors), but lets do it anyways.
        String errorReport = ReportGenerator.generateReport(goodDemoErrors, badDemoErrors, "sampleRoot", "sampleExtension");
        
        assertTrue(Utilities.containsErrorNode( errorReport , "start attribute on reference-sequence nodes should be 0."));
        assertTrue(Utilities.containsErrorNode( errorReport , "end attribute should be greater than or equal to the start attribute."));
        assertFalse(Utilities.containsErrorNode( errorReport , "Definitely doesn't have this error text"));
    }
}
