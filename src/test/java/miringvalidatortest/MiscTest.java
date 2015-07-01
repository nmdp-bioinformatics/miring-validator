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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

public class MiscTest
{
    private static final Logger logger = LogManager.getLogger(MiscTest.class);

    @Test
    public void testReportSchema()
    {
        String xml = Utilities.readXmlResource("/hml/ExampleResultReport.xml");
        assertTrue(xml.length() > 1);
        ValidationResult[] errors = SchemaValidator.validate(xml,"/schema/miringreport.xsd");
        assertTrue(errors.length == 0 );
        String errorReport = ReportGenerator.generateReport(errors, "sampleRoot", "sampleExtension", null, null);
        assertTrue(errorReport.length() > 0 );
        System.out.println(errorReport);

    }

    @Test
    public void testInvalidProlog()
    {
        logger.debug("Starting a testInvalidProlog");
        
        String xml = Utilities.readXmlResource("/hml/invalid.prolog.xml.txt");
        MiringValidator validator = new MiringValidator(xml);
        String results = validator.validate();
        assertTrue(Utilities.containsErrorNode(results, "Content is not allowed in prolog."));
    }

    /*
     * TODO: Get a test for XML Custom Namespace.  Need to craft an HML file.
    @Test
    public void testXmlWithNamespace()
    {
        logger.debug("starting testXmlWithNamespace");
        String xml = Utilities.readXmlResource("/hml/HMLWithNamespaces.hml");
        assertTrue(xml.length()>0);

        String results;
        results = new MiringValidator(xml).validate();
        
        assertTrue(results.length() > 1);
    }*/
    
    
    /*
     * TODO: Make a test for xml with text afterwards.
     
    @Test
    public void testXmlWithTextAfterwards()
    {
        logger.debug("starting testXmlWithTextAfterwards");
        
        String xml = Utilities.readXmlResource("/hml/HMLwithoutTextAfter.txt");
        String results = new MiringValidator(xml).validate();
        //System.out.println(results);
        assertTrue(results.length() > 1);

        xml = Utilities.readXmlResource("/hml/HMLwithTextAfter.txt");
        results = new MiringValidator(xml).validate();
        //System.out.println(results);
        assertTrue(results.length() > 1);
    }*/
}
