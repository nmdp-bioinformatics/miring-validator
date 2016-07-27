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

import static org.junit.Assert.*;

import org.nmdp.miring.MiringValidator;
import org.nmdp.miring.ReportGenerator;
import org.nmdp.miring.SchemaValidator;
import org.nmdp.miring.Utilities;
import org.nmdp.miring.ValidationResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

public class MiscTest
{
    Logger logger = LoggerFactory.getLogger(MiscTest.class);

    @Test
    public void testReportSchema()
    {
        //Try against a report I have on file
        String xml = Utilities.readXmlResource("/org/nmdp/miring/hml/ExampleResultReport.xml");
        //System.out.println("Validating these results against the report schema:");
        //System.out.println(xml);
        assertTrue(xml.length() > 1);
        ValidationResult[] errors = SchemaValidator.validate(xml,"/schema/miringreport.xsd");
        assertTrue(errors.length == 0 );
        String errorReport = ReportGenerator.generateReport(errors, "sampleRoot", "sampleExtension", null, null,0);
        assertTrue(errorReport.length() > 0 );
        //System.out.println("Report Schema Results:");
        //System.out.println(errorReport);


        //Try against a generated report
        String hml = Utilities.readXmlResource("/org/nmdp/miring/hml/HMLWithCustomNamespace.xml");
        assertTrue(hml.length()>0);

        String results = new MiringValidator(hml).validate();

        //System.out.println("Validating these results against the report schema:");
        //System.out.println(results);

        xml = results;
        assertTrue(xml.length() > 1);
        errors = SchemaValidator.validate(xml,"/schema/miringreport.xsd");
        assertTrue(errors.length == 0 );
        errorReport = ReportGenerator.generateReport(errors, "sampleRoot", "sampleExtension", null, null,0);
        assertTrue(errorReport.length() > 0 );
        //System.out.println("Report Schema Results:");
        //System.out.println(errorReport);
    }

    @Test
    public void testInvalidProlog()
    {
        logger.debug("Starting a testInvalidProlog");

        String xml = Utilities.readXmlResource("/org/nmdp/miring/hml/invalid.prolog.xml.txt");
        MiringValidator validator = new MiringValidator(xml);
        String results = validator.validate();
        logger.debug("Results= "+ results);
        assertTrue(Utilities.containsErrorNode(results, "[1,1] Content is not allowed in prolog."));
    }


    @Test
    public void testXmlWithNamespace()
    {
        logger.debug("starting testXmlWithNamespace");
        String xml = Utilities.readXmlResource("/org/nmdp/miring/hml/HMLWithCustomNamespace.xml");
        assertTrue(xml.length()>0);

        String results;
        results = new MiringValidator(xml).validate();
        //System.out.println(results);

        assertTrue(results.length() > 1);
    }

    //TODO: Make a test for running my curl command.   Might not work because server is supposed to be running.  Only one way to find out.


    /*
     * TODO: Make a test for xml with text afterwards.

    @Test
    public void testXmlWithTextAfterwards()
    {
        logger.debug("starting testXmlWithTextAfterwards");

        String xml = Utilities.readXmlResource("/org/nmdp/miring/hml/HMLwithoutTextAfter.txt");
        String results = new MiringValidator(xml).validate();
        //System.out.println(results);
        assertTrue(results.length() > 1);

        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/HMLwithTextAfter.txt");
        results = new MiringValidator(xml).validate();
        //System.out.println(results);
        assertTrue(results.length() > 1);
    }*/
}
