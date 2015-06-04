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
import main.java.miringvalidator.MiringValidator;
import main.java.miringvalidator.ReportGenerator;
import main.java.miringvalidator.SchemaValidator;
import main.java.miringvalidator.SchematronValidator;
import main.java.miringvalidator.Utilities;
import main.java.miringvalidator.ValidationError;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

public class MiringRulesTest
{
    private static final Logger logger = LogManager.getLogger(MiringRulesTest.class);

    @Test
    public void testSchematronMiringElement1()
    {
        logger.debug("starting testSchematronMiringElement1");

        String xml = Utilities.readXmlResource("/hml/Element1.good.1.xml");
        ValidationError[] errors = SchematronValidator.validate(xml,new String[]{"/schematron/MiringElement1.sch"});
        String goodXmlErrorReport = ReportGenerator.generateReport(errors,Utilities.getHMLIDRoot(xml), Utilities.getHMLIDExtension(xml));
        
        xml = Utilities.readXmlResource("/hml/Element1.bad.1.xml");
        errors = SchematronValidator.validate(xml,new String[]{"/schematron/MiringElement1.sch"});
        String badXmlErrorReport = ReportGenerator.generateReport(errors,Utilities.getHMLIDRoot(xml), Utilities.getHMLIDExtension(xml));
        
        assertFalse(Utilities.containsErrorNode(goodXmlErrorReport, "The root attribute for an HMLID should be 1234."));
        assertTrue(Utilities.containsErrorNode(badXmlErrorReport, "The root attribute for an HMLID should be 1234."));
        
        fail("this is not complete.");
    }
    
    @Test
    public void testSchematronMiringElement2()
    {
        logger.debug("starting testSchematronMiringElement2");
        fail("not yet implemented");
    }
    
    @Test
    public void testSchematronMiringElement3()
    {
        logger.debug("starting testSchematronMiringElement3");
        fail("not yet implemented");
    }


}
