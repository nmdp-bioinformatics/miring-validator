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
    public void testTemp()
    {
        //4.2.a and 4.2.3.a and 4.2.4.a and 4.2.5.a and 4.2.7.a
        String xml = Utilities.readXmlResource("/hml/Element4.CSB.bad.attributes.xml");
        MiringValidator validator = new MiringValidator(xml);
        String badResults = validator.validate();
        assertTrue(Utilities.containsErrorNode(badResults, "The node consensus-sequence-block is missing a description attribute."));
        assertTrue(Utilities.containsErrorNode(badResults, "The node consensus-sequence-block is missing a start attribute."));
        assertTrue(Utilities.containsErrorNode(badResults, "The node consensus-sequence-block is missing a end attribute."));
        assertTrue(Utilities.containsErrorNode(badResults, "The node consensus-sequence-block is missing a phase-set attribute."));
        assertTrue(Utilities.containsErrorNode(badResults, "The node consensus-sequence-block is missing a expected-copy-number attribute."));
        assertTrue(Utilities.containsErrorNode(badResults, "The node consensus-sequence-block is missing a continuity attribute."));
        
        xml = Utilities.readXmlResource("/hml/Element4.CSB.good.attributes.xml");
        validator = new MiringValidator(xml);
        String goodResults = validator.validate();
        assertFalse(Utilities.containsErrorNode(goodResults, "The node consensus-sequence-block is missing a description attribute."));
        assertFalse(Utilities.containsErrorNode(goodResults, "The node consensus-sequence-block is missing a start attribute."));
        assertFalse(Utilities.containsErrorNode(goodResults, "The node consensus-sequence-block is missing a end attribute."));
        assertFalse(Utilities.containsErrorNode(goodResults, "The node consensus-sequence-block is missing a phase-set attribute."));
        assertFalse(Utilities.containsErrorNode(goodResults, "The node consensus-sequence-block is missing a expected-copy-number attribute."));
        assertFalse(Utilities.containsErrorNode(goodResults, "The node consensus-sequence-block is missing a continuity attribute."));
        
        
    }
    
    @Test
    public void testMiringElement1Tier1()
    {
        logger.debug("starting testMiringElement1Tier1");
        //1.1.a
        String xml = Utilities.readXmlResource("/hml/Element1.no.hmlid.xml");
        MiringValidator validator = new MiringValidator(xml);
        String results = validator.validate();
        assertTrue(Utilities.containsErrorNode(results, "There is a missing hmlid node underneath the hml node."));
        
        //1.2.a
        xml = Utilities.readXmlResource("/hml/Element1.no.reportingcenter.xml");
        validator = new MiringValidator(xml);
        results = validator.validate();
        assertTrue(Utilities.containsErrorNode(results, "There is a missing reporting-center node underneath the hml node."));
        
        //1.3.a
        xml = Utilities.readXmlResource("/hml/Element1.sbtngs.missing.testid.xml");
        validator = new MiringValidator(xml);
        results = validator.validate();
        assertTrue(Utilities.containsErrorNode(results, "The node sbt-ngs is missing a test-id attribute."));
        assertTrue(Utilities.containsErrorNode(results, "The node sbt-ngs is missing a test-id-source attribute."));
        
        //1.5.a
        xml = Utilities.readXmlResource("/hml/Element1.missing.rawreads.xml");
        validator = new MiringValidator(xml);
        results = validator.validate();
        assertTrue(Utilities.containsErrorNode(results, "There is a missing raw-reads node underneath the sbt-ngs node."));
        
        //1.5.b
        xml = Utilities.readXmlResource("/hml/Element1.rawreads.no.availability.xml");
        validator = new MiringValidator(xml);
        String noAvailResults = validator.validate();
        assertTrue(Utilities.containsErrorNode(noAvailResults, "The node raw-reads is missing a availability attribute."));
        
        xml = Utilities.readXmlResource("/hml/Element1.rawreads.availability.xml");
        validator = new MiringValidator(xml);
        String availResults = validator.validate();
        assertFalse(Utilities.containsErrorNode(availResults, "The node raw-reads is missing a availability attribute."));
    }
    
    @Test
    public void testMiringElement1Tier2()
    {
        logger.debug("starting testMiringElement1Tier2");

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

        //1.3.b
        xml = Utilities.readXmlResource("/hml/Element1.valid.testidsource.xml");
        errors = SchematronValidator.validate(xml,new String[]{"/schematron/MiringElement1.sch"});
        String validTestIDErrorReport = ReportGenerator.generateReport(errors,Utilities.getHMLIDRoot(xml), Utilities.getHMLIDExtension(xml));
        
        xml = Utilities.readXmlResource("/hml/Element1.invalid.testidsource.xml");
        errors = SchematronValidator.validate(xml,new String[]{"/schematron/MiringElement1.sch"});
        String invalidTestIDErrorReport = ReportGenerator.generateReport(errors,Utilities.getHMLIDRoot(xml), Utilities.getHMLIDExtension(xml));

        assertFalse(Utilities.containsErrorNode(validTestIDErrorReport, "On a sbt-ngs node, test-id is not formatted like a GTR test ID."));
        assertFalse(Utilities.containsErrorNode(validTestIDErrorReport, "On a sbt-ngs node, the test-id-source is not explicitly 'NCBI-GTR'."));
        
        assertTrue(Utilities.containsErrorNode(invalidTestIDErrorReport, "On a sbt-ngs node, test-id is not formatted like a GTR test ID."));
        assertTrue(Utilities.containsErrorNode(invalidTestIDErrorReport, "On a sbt-ngs node, the test-id-source is not explicitly 'NCBI-GTR'."));
    }
    
    @Test
    public void testMiringElement2Tier1()
    {
        logger.debug("starting testMiringElement2Tier1");
        
        //2.1.a
        String xml = Utilities.readXmlResource("/hml/Element2.no.alleleassignment.xml");
        MiringValidator validator = new MiringValidator(xml);
        String results = validator.validate();
        assertTrue(Utilities.containsErrorNode(results, "There is a missing allele-assignment node underneath the typing node."));
        
        //2.1.b and 2.1.c
        xml = Utilities.readXmlResource("/hml/Element2.no.alleleassignment.xml");
        validator = new MiringValidator(xml);
        results = validator.validate();
        assertTrue(Utilities.containsErrorNode(results, "The node allele-assignment is missing a allele-db attribute."));
        assertTrue(Utilities.containsErrorNode(results, "The node allele-assignment is missing a allele-version attribute."));
        
        //2.2.b
        xml = Utilities.readXmlResource("/hml/Element2.referencesequence.missing.attributes.xml");
        validator = new MiringValidator(xml);
        results = validator.validate();
        assertTrue(Utilities.containsErrorNode(results, "The node reference-sequence is missing a name attribute."));
        assertTrue(Utilities.containsErrorNode(results, "The node reference-sequence is missing a start attribute."));
        assertTrue(Utilities.containsErrorNode(results, "The node reference-sequence is missing a end attribute."));
        assertTrue(Utilities.containsErrorNode(results, "The node reference-sequence is missing a accession attribute."));
        
        //2.3.b
        xml = Utilities.readXmlResource("/hml/Element2.referencesequence.missing.attributes.xml");
        validator = new MiringValidator(xml);
        results = validator.validate();
        assertTrue(Utilities.containsErrorNode(results, "The node reference-database is missing a curated attribute."));
    }
    
    @Test
    public void testMiringElement2Tier2()
    {
        logger.debug("starting testMiringElement2Tier2");
        
        //2.2.c
        String xml = Utilities.readXmlResource("/hml/Element2.referencesequence.good.startend.xml");
        MiringValidator validator = new MiringValidator(xml);
        String results = validator.validate();
        assertFalse(Utilities.containsErrorNode(results, "On a reference sequence node, end attribute should be greater than or equal to the start attribute."));
        
        xml = Utilities.readXmlResource("/hml/Element2.referencesequence.bad.startend.xml");
        validator = new MiringValidator(xml);
        results = validator.validate();
        assertTrue(Utilities.containsErrorNode(results, "On a reference sequence node, end attribute should be greater than or equal to the start attribute."));
        
        //2.2.1.c
        xml = Utilities.readXmlResource("/hml/Element2.refsequence.nomatch.csb.xml");
        String badResults = new MiringValidator(xml).validate();

        xml = Utilities.readXmlResource("/hml/Element2.refsequence.match.csb.xml");
        String goodResults = new MiringValidator(xml).validate();

        assertTrue(Utilities.containsErrorNode(badResults, "A reference-sequence node has an id attribute with no corresponding consensus-sequence-block id attribute."));
        assertFalse(Utilities.containsErrorNode(goodResults, "A reference-sequence node has an id attribute with no corresponding consensus-sequence-block id attribute."));

    }

    @Test
    public void testMiringElement3Tier1()
    {
        logger.debug("starting testMiringElement3Tier1");
        logger.debug("Nothing tested in Element 3 yet.");
    }
    
    @Test
    public void testMiringElement3Tier2()
    {
        logger.debug("starting testMiringElement3Tier2");
        logger.debug("Nothing tested in Element 3 yet.");
    }
    
    @Test
    public void testMiringElement4Tier1()
    {
        logger.debug("starting testMiringElement4Tier1");
        
        //4.2.a
        
       
        //4.2.3.a
        
        //4.2.4.a
        
        //4.2.5.a
        
        //4.2.7.a
    }
    
    @Test
    public void testMiringElement4Tier2()
    {
        logger.debug("starting testMiringElement4Tier2");
        
        //4.2.b
        
        //4.2.1.a
        
        //4.2.2.b
        
        //4.2.3.b
        
        //4.2.3.d
        
        //4.2.4.b
        
        //4.2.7.b
    }
}