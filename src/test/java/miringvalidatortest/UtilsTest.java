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

import main.java.miringvalidator.SchematronValidator;
import main.java.miringvalidator.Utilities;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class UtilsTest
{
    private static final Logger logger = LogManager.getLogger(UtilsTest.class);

    @Test
    public void testLoadJarElements()
    {
        try
        {
            URL jarURL = SchematronValidator.class.getResource("/jar/probatron.jar");
            Assert.assertNotNull(jarURL);
            URI jarURI = jarURL.toURI();
            URLClassLoader loadedProbatronClasses = Utilities.loadJarElements(new File(jarURI));
            Assert.assertNotNull(loadedProbatronClasses);
            
            //If it finds this class then we opened the jar successfully.
            Class sessionClass= loadedProbatronClasses.loadClass("org.probatron.Session");
            Assert.assertTrue(sessionClass != null);
        }
        catch(Exception e)
        {
            fail("Exception: " + e);
        }        
    }
    
    @Test
    public void testContainsErrorNode()
    {
        String xml = "<MiringReport><hmlid extension=\"abcd\" root=\"1234\"/><QualityScore>3</QualityScore>" +
            "<InvalidMiringResult fatal=\"true\" miringRuleID=\"5.7.a\"><description>" +
            "The node variant is missing a filter attribute.</description></InvalidMiringResult></MiringReport>";
        
        Assert.assertTrue(Utilities.containsErrorNode(xml, "ode variant is missing a filter attribu"));        
        Assert.assertFalse(Utilities.containsErrorNode(xml, "This text is not in the report.  Wooo."));
    }
}
