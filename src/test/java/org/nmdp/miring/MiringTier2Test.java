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
import org.nmdp.miring.Utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

public class MiringTier2Test
{
    Logger logger = LoggerFactory.getLogger(MiringTier2Test.class);

    @Test
    public void testMiringElement1Tier2()
    {
        logger.debug("starting testMiringElement1Tier2");

        String xml;
        
        //1.1.c
        //Test if HMLID root is an OID
        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element1.hmlid.OID.xml");
        String oidHmlidErrorReport = new MiringValidator(xml).validate();
        
        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element1.hmlid.not.OID.xml");
        String notOidHmlidErrorReport = new MiringValidator(xml).validate();

        assertFalse(Utilities.containsErrorNode(oidHmlidErrorReport, "The hmlid root is not formatted like an OID."));
        assertTrue(Utilities.containsErrorNode(oidHmlidErrorReport, "The hmlid root is formatted like an OID."));
        
        assertTrue(Utilities.containsErrorNode(notOidHmlidErrorReport, "The hmlid root is not formatted like an OID."));
        assertFalse(Utilities.containsErrorNode(notOidHmlidErrorReport, "The hmlid root is formatted like an OID."));

        //1.3.b
        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element1.valid.testidsource.xml");
        String validTestIDErrorReport = new MiringValidator(xml).validate();
        
        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element1.invalid.testidsource.xml");
        String invalidTestIDErrorReport = new MiringValidator(xml).validate();

        assertFalse(Utilities.containsErrorNode(validTestIDErrorReport, "On a sbt-ngs node, test-id is not formatted like a GTR test ID."));
        assertFalse(Utilities.containsErrorNode(validTestIDErrorReport, "On a sbt-ngs node, the test-id-source is not explicitly 'NCBI-GTR'."));
        
        assertTrue(Utilities.containsErrorNode(invalidTestIDErrorReport, "On a sbt-ngs node, test-id is not formatted like a GTR test ID."));
        assertTrue(Utilities.containsErrorNode(invalidTestIDErrorReport, "On a sbt-ngs node, the test-id-source is not explicitly 'NCBI-GTR'."));
    }

    @Test
    public void testMiringElement2Tier2()
    {
        logger.debug("starting testMiringElement2Tier2");
        
        //2.2.c
        String xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element2.referencesequence.good.startend.xml");
        String results = new MiringValidator(xml).validate();
        assertFalse(Utilities.containsErrorNode(results, "On a reference sequence node, end attribute should be greater than or equal to the start attribute."));
        
        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element2.referencesequence.bad.startend.xml");
        results = new MiringValidator(xml).validate();
        assertTrue(Utilities.containsErrorNode(results, "On a reference sequence node, end attribute should be greater than or equal to the start attribute."));
        
        //2.2.1.c
        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element2.refsequence.nomatch.csb.xml");
        String badResults = new MiringValidator(xml).validate();

        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element2.refsequence.match.csb.xml");
        String goodResults = new MiringValidator(xml).validate();

        assertTrue(Utilities.containsErrorNode(badResults, "A reference-sequence node has an id attribute with no corresponding consensus-sequence-block id attribute."));
        assertFalse(Utilities.containsErrorNode(goodResults, "A reference-sequence node has an id attribute with no corresponding consensus-sequence-block id attribute."));
    }

    @Test
    public void testMiringElement3Tier2()
    {
        logger.debug("starting testMiringElement3Tier2");
        //3.2.a glstring node should have either text or a uri.
        String xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element3.glstring.empty.xml");
        String results = new MiringValidator(xml).validate();
        assertFalse(Utilities.containsErrorNode(results, "There is a missing glstring node underneath the allele-assignment node."));
        assertTrue(Utilities.containsErrorNode(results, "A glstring node should have one of either A) A uri attribute specifying the location of a valid glstring, or B) Text containing a valid glstring."));

        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element3.glstring.missing.xml");
        results = new MiringValidator(xml).validate();
        assertTrue(Utilities.containsErrorNode(results, "There is a missing glstring node underneath the allele-assignment node."));
        assertFalse(Utilities.containsErrorNode(results, "A glstring node should have one of either A) A uri attribute specifying the location of a valid glstring, or B) Text containing a valid glstring."));

        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element3.glstring.text.xml");
        results = new MiringValidator(xml).validate();
        assertFalse(Utilities.containsErrorNode(results, "There is a missing glstring node underneath the allele-assignment node."));
        assertFalse(Utilities.containsErrorNode(results, "A glstring node should have one of either A) A uri attribute specifying the location of a valid glstring, or B) Text containing a valid glstring."));

        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element3.glstring.uri.xml");
        results = new MiringValidator(xml).validate();
        assertFalse(Utilities.containsErrorNode(results, "There is a missing glstring node underneath the allele-assignment node."));
        assertFalse(Utilities.containsErrorNode(results, "A glstring node should have one of either A) A uri attribute specifying the location of a valid glstring, or B) Text containing a valid glstring."));

        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element3.glstring.textanduri.xml");
        results = new MiringValidator(xml).validate();
        assertFalse(Utilities.containsErrorNode(results, "There is a missing glstring node underneath the allele-assignment node."));
        assertTrue(Utilities.containsErrorNode(results, "A glstring node should have one of either A) A uri attribute specifying the location of a valid glstring, or B) Text containing a valid glstring."));
    }

    @Test
    public void testMiringElement4Tier2()
    {
        logger.debug("starting testMiringElement4Tier2");
        
        //4.a and 4.b
        String xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element4.SeqQual.good.xml");
        String results = new MiringValidator(xml).validate();
        assertFalse(Utilities.containsErrorNode(results, "On a sequence quality node, sequence-end must be greater than sequence-start."));
        assertFalse(Utilities.containsErrorNode(results, "On a sequence quality node, the sequence-start and sequence-end attributes must be between 0 and (consensus-sequence-block:end - consensus-sequence-block:start) inclusive."));

        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element4.SeqQual.bad.1.xml");
        results = new MiringValidator(xml).validate();
        assertFalse(Utilities.containsErrorNode(results, "On a sequence quality node, sequence-end must be greater than sequence-start."));
        assertTrue(Utilities.containsErrorNode(results, "On a sequence quality node, the sequence-start and sequence-end attributes must be between 0 and (consensus-sequence-block:end - consensus-sequence-block:start) inclusive."));

        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element4.SeqQual.bad.2.xml");
        results = new MiringValidator(xml).validate();
        assertFalse(Utilities.containsErrorNode(results, "On a sequence quality node, sequence-end must be greater than sequence-start."));
        assertTrue(Utilities.containsErrorNode(results, "On a sequence quality node, the sequence-start and sequence-end attributes must be between 0 and (consensus-sequence-block:end - consensus-sequence-block:start) inclusive."));

        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element4.SeqQual.bad.3.xml");
        results = new MiringValidator(xml).validate();
        assertTrue(Utilities.containsErrorNode(results, "On a sequence quality node, sequence-end must be greater than sequence-start."));
        assertFalse(Utilities.containsErrorNode(results, "On a sequence quality node, the sequence-start and sequence-end attributes must be between 0 and (consensus-sequence-block:end - consensus-sequence-block:start) inclusive."));

        //4.2.3.b
        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element4.CSB.good.startend.xml");
        results = new MiringValidator(xml).validate();
        assertFalse(Utilities.containsErrorNode(results, "On a consensus-sequence-block node, end attribute should be greater than or equal to the start attribute."));
        
        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element4.CSB.bad.startend.xml");
        results = new MiringValidator(xml).validate();
        assertTrue(Utilities.containsErrorNode(results, "On a consensus-sequence-block node, end attribute should be greater than or equal to the start attribute."));
        
        //4.2.3.d
        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element4.CSB.within.refseq.xml");
        results = new MiringValidator(xml).validate();        
        assertFalse(Utilities.containsErrorNode(results, "The start attribute on a consensus-sequence-block node should be greater than or equal to the start attribute on the corresponding reference-sequence node."));
        assertFalse(Utilities.containsErrorNode(results, "The end attribute on a consensus-sequence-block node should be less than or equal to the end attribute on the corresponding reference-sequence node."));
        
        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element4.CSB.outside.refseq.1.xml");
        results = new MiringValidator(xml).validate();
        assertFalse(Utilities.containsErrorNode(results, "The start attribute on a consensus-sequence-block node should be greater than or equal to the start attribute on the corresponding reference-sequence node."));
        assertTrue(Utilities.containsErrorNode(results, "The end attribute on a consensus-sequence-block node should be less than or equal to the end attribute on the corresponding reference-sequence node."));
        
        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element4.CSB.outside.refseq.2.xml");
        results = new MiringValidator(xml).validate();
        assertTrue(Utilities.containsErrorNode(results, "The start attribute on a consensus-sequence-block node should be greater than or equal to the start attribute on the corresponding reference-sequence node."));
        assertFalse(Utilities.containsErrorNode(results, "The end attribute on a consensus-sequence-block node should be less than or equal to the end attribute on the corresponding reference-sequence node."));
        
        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element4.CSB.outside.refseq.3.xml");
        results = new MiringValidator(xml).validate();
        assertTrue(Utilities.containsErrorNode(results, "The start attribute on a consensus-sequence-block node should be greater than or equal to the start attribute on the corresponding reference-sequence node."));
        assertTrue(Utilities.containsErrorNode(results, "The end attribute on a consensus-sequence-block node should be less than or equal to the end attribute on the corresponding reference-sequence node."));
        
        //4.2.3.e
        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element4.CSB.good.sequencelength.xml");
        results = new MiringValidator(xml).validate();
        assertFalse(Utilities.containsErrorNode(results, "For every consensus-sequence-block node, the child sequence node must have a length of (end - start)."));
        
        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element4.CSB.bad.sequencelength.xml");
        results = new MiringValidator(xml).validate();
        assertTrue(Utilities.containsErrorNode(results, "For every consensus-sequence-block node, the child sequence node must have a length of (end - start)."));

        //4.2.4.b
        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element4.phasinggroup.xml");
        results = new MiringValidator(xml).validate();
        assertTrue(Utilities.containsErrorNode(results, "On a consensus-sequence-block node, the phasing-group attribute is deprecated."));
        
        //4.2.7.b
        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element4.CSB.continuous.xml");
        results = new MiringValidator(xml).validate();
        assertFalse(Utilities.containsErrorNode(results, "A consensus-sequence-block with attribute continuity=true does not appear to be continuous with it's previous sibling consensus-sequence-block node"));
        
        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element4.CSB.not.continuous.xml");
        results = new MiringValidator(xml).validate();
        assertTrue(Utilities.containsErrorNode(results, "A consensus-sequence-block with attribute continuity=true does not appear to be continuous with it's previous sibling consensus-sequence-block node"));
    }

    @Test
    public void testMiringElement5Tier2()
    {
        logger.debug("starting testMiringElement5Tier2");
        
        //5.2.b and 5.2.d
        String xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element5.variant.bad.attributes.xml");
        String badResults = new MiringValidator(xml).validate();
        assertFalse(Utilities.containsErrorNode(badResults, "The start attribute on a variant node should be greater than or equal to the start attribute on the corresponding reference-sequence node."));
        assertTrue(Utilities.containsErrorNode(badResults, "The end attribute on a variant node should be less than or equal to the end attribute on the corresponding reference-sequence node."));
        assertTrue(Utilities.containsErrorNode(badResults, "On a variant node, end attribute should be greater than or equal to the start attribute."));

        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element5.variant.good.attributes.xml");
        String goodResults = new MiringValidator(xml).validate();
        assertFalse(Utilities.containsErrorNode(goodResults, "The start attribute on a variant node should be greater than or equal to the start attribute on the corresponding reference-sequence node."));
        assertFalse(Utilities.containsErrorNode(goodResults, "The end attribute on a variant node should be less than or equal to the end attribute on the corresponding reference-sequence node."));
        assertFalse(Utilities.containsErrorNode(goodResults, "On a variant node, end attribute should be greater than or equal to the start attribute."));

        //5.3.b and 5.3.c
        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element5.variant.good.ids.xml");
        String results = new MiringValidator(xml).validate();
        assertFalse(Utilities.containsErrorNode(results, "The variant nodes under a single consensus-sequence-block must have id attributes that are integers ranging from 0:n-1, where n is the number of variants"));
        
        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element5.variant.bad.ids.1.xml");
        results = new MiringValidator(xml).validate();
        assertTrue(Utilities.containsErrorNode(results, "The variant nodes under a single consensus-sequence-block must have id attributes that are integers ranging from 0:n-1, where n is the number of variants"));

        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element5.variant.bad.ids.2.xml");
        results = new MiringValidator(xml).validate();
        assertTrue(Utilities.containsErrorNode(results, "The variant nodes under a single consensus-sequence-block must have id attributes that are integers ranging from 0:n-1, where n is the number of variants"));

        xml = Utilities.readXmlResource("/org/nmdp/miring/hml/Element5.variant.bad.ids.3.xml");
        results = new MiringValidator(xml).validate();
        assertTrue(Utilities.containsErrorNode(results, "The variant nodes under a single consensus-sequence-block must have id attributes that are integers ranging from 0:n-1, where n is the number of variants"));
    }
}