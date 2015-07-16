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
package test.java.miringvalidatortest;

import static org.junit.Assert.*;
import main.java.miringvalidator.MiringValidator;
import main.java.miringvalidator.ReportGenerator;
import main.java.miringvalidator.SchemaValidator;
import main.java.miringvalidator.Utilities;
import main.java.miringvalidator.ValidationResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

public class SchemaValidatorTest
{
    Logger logger = LoggerFactory.getLogger(SchemaValidatorTest.class);

    @Test
    public void schemaValidatorTest()
    {
        logger.debug("Starting a schemaValidatorTest");
        
        String demoGoodXML = Utilities.readXmlResource("/hml/demogood.xml");
        String demoBadXML = Utilities.readXmlResource("/hml/demobad.xml");

        ValidationResult[] goodDemoErrors = SchemaValidator.validate(demoGoodXML,"/schema/demo.xsd");
        ValidationResult[] badDemoErrors = SchemaValidator.validate(demoBadXML,"/schema/demo.xsd");
        
        assertTrue(goodDemoErrors.length == 0);
        
        //There are actually 2 errors in demobad.xml: missing hmlid and Missing reporting-center
        //I think there should be 2 errors in this, but the SAX xml parser stops parsing the current node when it encounters the first error. 
        //Not awful because once the missing hmlid is replaced, the validator will flag the missing reporting-center
        //Just demonstrating a weakness of the schema parser.   Fix in the future if it makes sense to.
        assertTrue(badDemoErrors.length == 1);
        
        String errorReport = ReportGenerator.generateReport(badDemoErrors, "sampleRoot", "sampleExtension", null, null);
        assertTrue(Utilities.containsErrorNode( errorReport , "There is a missing hmlid node underneath the hml node." ));
        assertFalse(Utilities.containsErrorNode( errorReport , "There is a missing reporting-center node underneath the hml node." ));
    }

}
