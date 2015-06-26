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
import main.java.miringvalidator.Utilities;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

public class MiscTest
{
    private static final Logger logger = LogManager.getLogger(MiscTest.class);

    @Test
    public void testTidyXml()
    {
        logger.debug("starting testTidy");
        String xml = Utilities.readXmlResource("/hml/HML.Bigger.Sample.xml");
        
        String results = new MiringValidator(xml).validate();
        assertTrue(results.length() > 1);
        //String polishedXml = Utilities.cleanSequences(xml);
    }

    @Test
    public void testXmlWithNamespace()
    {
        logger.debug("starting testXmlWithNamespace");
        String xml = Utilities.readXmlResource("/hml/HMLWithNamespaces.hml");

        String results;
        results = new MiringValidator(xml).validate();
        
        assertTrue(results.length() > 1);
    }
    
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
    }
}
