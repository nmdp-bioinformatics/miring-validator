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

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import main.java.miringvalidator.MiringValidator;
import main.java.miringvalidator.SchematronValidator;
import main.java.miringvalidator.Utilities;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.helpers.AttributesImpl;

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
        String polishedXml = Utilities.cleanSequences(xml);
    }
    
    @Test
    public void testCleanNamespace()
    {
        logger.debug("starting testCleanNamespace");
        String xml = Utilities.readXmlResource("/hml/HMLWithNamespaces.hml");
        
        String polishedXml = Utilities.cleanNamespace(xml);
        
        String results = new MiringValidator(xml).validate();
        results = new MiringValidator(polishedXml).validate();
        
        assertTrue(results.length() > 1);
        fail();

    }

}
