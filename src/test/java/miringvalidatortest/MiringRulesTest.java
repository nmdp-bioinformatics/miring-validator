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
    public void testSchemaMiringElements()
    {
        //String xml = Utilities.readXmlResource("/hml/demogood.xml");
        //String xml = Utilities.readXmlResource("/hml/Element1.hmlid.OID.xml");
        String xml = Utilities.readXmlResource("/hml/Element1.rawreads.URI.xml");
        //String xml = Utilities.readXmlResource("/hml/demobad.xml");
        MiringValidator validator = new MiringValidator(xml);
        String results = validator.validate();
        System.out.println(results);
        
        
    }
    
    @Test
    public void testSchematronMiringElement1()
    {
        logger.debug("starting testSchematronMiringElement1");

        String xml;
        ValidationError[] errors; 
        
        //1.1.c
        //Test if HMLID root is an OID
        xml = Utilities.readXmlResource("/hml/Element1.hmlid.OID.xml");
        errors = SchematronValidator.validate(xml,new String[]{"/schematron/MiringElement1.sch"});
        String oidHmlidErrorReport = ReportGenerator.generateReport(errors,Utilities.getHMLIDRoot(xml), Utilities.getHMLIDExtension(xml));
        
        xml = Utilities.readXmlResource("/hml/Element1.hmlid.not.OID.xml");
        errors = SchematronValidator.validate(xml,new String[]{"/schematron/MiringElement1.sch"});
        String notOidHmlidErrorReport = ReportGenerator.generateReport(errors,Utilities.getHMLIDRoot(xml), Utilities.getHMLIDExtension(xml));

        assertFalse(Utilities.containsErrorNode(oidHmlidErrorReport, "The hmlid root is not formatted like an OID."));
        assertTrue(Utilities.containsErrorNode(oidHmlidErrorReport, "The hmlid root is formatted like an OID."));
        
        assertTrue(Utilities.containsErrorNode(notOidHmlidErrorReport, "The hmlid root is not formatted like an OID."));
        assertFalse(Utilities.containsErrorNode(notOidHmlidErrorReport, "The hmlid root is formatted like an OID."));

        
        
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

