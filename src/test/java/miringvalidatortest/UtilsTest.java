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
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class UtilsTest
{
    private static final Logger logger = LogManager.getLogger(UtilsTest.class);

    @Test
    public void testLoadJarElements()
    {
        logger.debug("starting testLoadJarElements");
        try
        {
            URL jarURL = SchematronValidator.class.getResource("/jar/probatron.jar");
            assertNotNull(jarURL);
            URI jarURI = jarURL.toURI();
            URLClassLoader loadedProbatronClasses = Utilities.loadJarElements(new File(jarURI));
            assertNotNull(loadedProbatronClasses);
            
            //If it finds this class then we opened the jar successfully.
            Class sessionClass= loadedProbatronClasses.loadClass("org.probatron.Session");
            assertNotNull(sessionClass);
        }
        catch(Exception e)
        {
            fail("Exception: " + e);
        }        
    }
    
    @Test
    public void testContainsErrorNode()
    {
        logger.debug("starting testContainsErrorNode");
        String xml = "<MiringReport><hmlid extension=\"abcd\" root=\"1234\"/><QualityScore>3</QualityScore>" +
            "<InvalidMiringResult fatal=\"true\" miringRuleID=\"5.7.a\"><description>" +
            "The node variant is missing a filter attribute.</description></InvalidMiringResult></MiringReport>";
        
        assertTrue(Utilities.containsErrorNode(xml, "The node variant is missing a filter attribute."));
        //Only part of the text is required:
        assertTrue(Utilities.containsErrorNode(xml, "ode variant is missing a filter attribu"));
        assertFalse(Utilities.containsErrorNode(xml, "This text is not in the report.  Wooo."));
    }
    
    @Test
    public void testCallReflectedMethod()
    {
        logger.debug("starting testCallReflectedMethod");
        try
        {
            URL jarURL = SchematronValidator.class.getResource("/jar/probatron.jar");
            URI jarURI = jarURL.toURI();
            ClassLoader loadedProbatronClasses = Utilities.loadJarElements(new File(jarURI));
            
            Class sessionClass= loadedProbatronClasses.loadClass("org.probatron.Session");
            Object currentSession = sessionClass.newInstance();
            
            assertNotNull(currentSession);
            
            //Call org.probatron.Session.setSchemaDoc(String schemaDoc)
            Utilities.callReflectedMethod(currentSession,"setSchemaDoc", "NewSchemaFileName.sch", String.class);
        }
        catch(Exception e)
        {
            fail("Exception trying to call a reflected method: " + e);
        }
    }
    
    @Test
    public void  testXmlToDomObject()
    {
        logger.debug("starting testXmlToDomObject");

        String demoGoodXML = Utilities.readXmlResource("/hml/demogood.xml");
        Element xmlElement = Utilities.xmlToDomObject(demoGoodXML);
        assertNotNull(xmlElement);
        
        boolean sampleElementFound = false;
        NodeList childrenNodes = xmlElement.getChildNodes();
        for(int i = 0; i < childrenNodes.getLength(); i++)
        {
            String childsName = childrenNodes.item(i).getNodeName();
            if(childsName != null && childsName.equals("sample"))
            {
                //This xml has a sample node underneath, so we know it was succesfully loaded.
                sampleElementFound = true;
            }
        }
        assertTrue(sampleElementFound);
    }
    
    @Test
    public void testGetHMLID()
    {
        logger.debug("starting testGetHMLID");

        String demoGoodXML = Utilities.readXmlResource("/hml/demogood.xml");
        
        String root = Utilities.getHMLIDRoot(demoGoodXML);
        String extension = Utilities.getHMLIDExtension(demoGoodXML);
        
        assertTrue(root.equals("1234"));
        assertTrue(extension.equals("abcd"));        
    }
    
    @Test
    public void testReadXML()
    {
        logger.debug("starting testReadXML");

        String demoGoodXML = Utilities.readXmlResource("/hml/demogood.xml");
        assertNotNull(demoGoodXML);
        assertTrue(demoGoodXML.length() > 50);
    }
}
