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
package main.java.miringvalidator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Utilities
{
    private static final Logger logger = LogManager.getLogger(Utilities.class);
    
    /**
     * Does XML contain an error node with errNodeDescription in the text?
     *
     * @param validationErrorReport A String containing an XML Validation Error Report
     * @param errNodeDescription The text to search for
     * @return True if the Validation Error Report contains at least one InvalidMiringResult with an errorText containing errNodeDescription
     */
    public static boolean containsErrorNode(String validationErrorReport, String errNodeDescription)
    {
        //I planned to use this for unit testing.  A quick way to check if i found an error report.  
        //  It's not implemented yet but probably easy.
        //  See what im doing to pull out hmlid        
        NodeList childrenNodes = xmlToDomObject(validationErrorReport).getChildNodes();
        for(int i = 0; i < childrenNodes.getLength(); i++)
        {
            String childsName = childrenNodes.item(i).getNodeName();
            if(childsName != null && childsName.equals("InvalidMiringResult"))
            {
                System.out.println("COOL WE FOUND IT:" + childsName);
                Node invMirResult = childrenNodes.item(i);
                //String description = invMirResult.getAttributes().getNamedItem("description");
                ///check stuff
            }
        }
        return false;
    }
    
    /**
     * Load Probatron Classes
     *
     * @param jarFileLocation The relative location of the jar file resource
     * @return a URLClassLoader object, which you can use to call probatron methods reflectively.
     */
    public static URLClassLoader loadJarElements(File jarFileLocation)
    {
        logger.debug("Loading jar elements from " + jarFileLocation.toString());
        try
        {
            JarFile jarFile = new JarFile(jarFileLocation);
            Enumeration e = jarFile.entries();

            URL[] urls = { new URL("jar:file:" + jarFileLocation +"!/") };
            URLClassLoader cl = URLClassLoader.newInstance(urls);
            
            /*
            //I don't need to load any classes right now.  They are available in loadedProbatronClasses.
            //I will load the classes as I need them.
            while (e.hasMoreElements()) 
            {
                JarEntry je = (JarEntry) e.nextElement();
                if(je.isDirectory() || !je.getName().endsWith(".class"))
                {
                    continue;
                }
                // -6 because of .class
                String className = je.getName().substring(0,je.getName().length()-6);
                className = className.replace('/', '.');

                if(className.contains("org.apache.log4j"))
                {
                    //Having problems with log4j classes.  I think it's being loaded twice, so it's getting an exception here.  
                    //Not really sure, but log4j is being loaded already so here we are.
                    //Could be that I only need to load the org.probatron.* classes
                    System.out.println(className + " will not be loaded.");
                }
                else
                {
                    System.out.println(className + " loading...");
                    Class c = cl.loadClass(className);
                }
            }*/
            
            jarFile.close();
            return cl;
        }
        catch(Exception e)
        {
            logger.error("Error during schematron validation:" + e);
        }
        return null;
    }

    /**
     * Call a reflected method within a class.  This method must have a single parameter
     *
     * @param callingObject The object which calls the method
     * @param methodName a String with the name of the reflected method
     * @param singleParameter The object parameter to pass into the reflected method.
     * @param parameterClass The class of the object expected by the reflected method.  It must be the correct class, and not an inherited class.
     * @return an Object which is the result of the reflected method.
     */
    public static Object callReflectedMethod(Object callingObject, String methodName, Object singleParameter, Class parameterClass)
    {
        Method method = null;
        try 
        {
            method = callingObject.getClass().getDeclaredMethod(methodName, parameterClass);
            method.setAccessible(true);
            return method.invoke(callingObject, singleParameter);
        } 
        catch (SecurityException e) 
        {
            logger.error("Security exception while calling reflected method: " + e);
        } 
        catch (NoSuchMethodException e) 
        {
            logger.error("NoSuchMethod exception while calling reflected method: " + e);
        }
        catch (IllegalArgumentException e) 
        {
            logger.error("IllegalArgument exception while calling reflected method: " + e);
        } 
        catch (IllegalAccessException e) 
        {
            logger.error("IllegalAccess exception while calling reflected method: " + e);
        } 
        catch (InvocationTargetException e) 
        {
            logger.error("InvocationTarget exception while calling reflected method: " + e);
        }
        catch (RuntimeException e)
        {
            logger.error("Runtime exception while calling reflected method: " + e);
        }
        catch (Exception e)
        {
            logger.error("Exception while calling reflected method: " + e);
        }

        logger.error("callReflectedMethod() returned a null reflected method object");
        return null;
    }

    /**
     * Convert XML in string form to a DOM Object.  They are more useful for parsing the XML.
     *
     * @param xml A String containing xml
     * @return an org.w3c.Dom.Element which is the root of the xml document.
     */
    public static Element xmlToDomObject(String xml)
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));
            Element rootElement = document.getDocumentElement();
            return rootElement;
        }
        catch(Exception e)
        {
            //If i was clever I'd handle these exceptions specifically
            //throws ParserConfigurationException, SAXException, IOException
            logger.error("Exception in Utilities.xmlToDomObject()" + e.toString());
            return null;
        }
    }
    
    /**
     * Get the HMLID Root from an XML String
     *
     * @param xml A String containing xml
     * @return A String containing the HMLID root
     */
    public static String getHMLIDRoot(String xml)
    {
        try
        {
            return getHMLIDNode(xml).getAttributes().getNamedItem("root").getNodeValue();
        }
        catch(Exception e)
        {
            //Probably should be specific.  I'm catching NullPointerException when the getHMLIDNode() can't getAttributes()
            logger.debug("Unable to find an HMLID Root: " + e.toString());
            return null;
        }
    }
    
    /**
     * Get the HMLID Extension from an XML String
     *
     * @param xml A String containing xml
     * @return A String containing the HMLID extension
     */
    public static String getHMLIDExtension(String xml)
    {
        try
        {
            return getHMLIDNode(xml).getAttributes().getNamedItem("extension").getNodeValue();
        }
        catch(Exception e)
        {
            logger.debug("Unable to find an HMLID Extension: " + e.toString());
            return null;
        }
    }
    
    /**
     * Get the HMLID Node from an XML String
     *
     * @param xml A String containing HML
     * @return the HML document's HMLID node
     */
    private static Node getHMLIDNode(String xml)
    {
        //hmlid should be a child nodes of the root xml element.
        NodeList childrenNodes = xmlToDomObject(xml).getChildNodes();
        for(int i = 0; i < childrenNodes.getLength(); i++)
        {
            String childsName = childrenNodes.item(i).getNodeName();
            if(childsName != null && childsName.equals("hmlid"))
            {
                return childrenNodes.item(i);
            }
        }
        return null;
    }
    
    /**
     * Add a ValidationError to a list of ValidationError objects.  This method disallows duplicates.
     *
     * @param validationErrors A list of ValidationError objects that you would like to add to.
     * @param ve a ValidationError to add to the list.
     */
    public static void addValidationError(List<ValidationError> validationErrors, ValidationError ve)
    {
        //Don't add duplicate errors, they don't help.
        if(!validationErrors.contains(ve))
        {
            validationErrors.add(ve);
        }
        else
        {
            logger.debug("This validation error is a duplicate, not adding it to the list.");
        }
    }

    /**
     * Concatenate and sort two arrays of Validation Error objects
     *
     * @param tier1ValidationErrors An Array of ValidationError objects
     * @param tier2ValidationErrors An Array of ValidationError objects
     * @return A combined and sorted Array of ValidationError objects
     */
    public static ValidationError[] combineArrays(ValidationError[] tier1ValidationErrors, ValidationError[] tier2ValidationErrors)
    {
        ValidationError[] combinedErrors = new ValidationError[tier1ValidationErrors.length + tier2ValidationErrors.length];
        
        for(int i = 0; i < tier1ValidationErrors.length; i++)
        {
            combinedErrors[i] = tier1ValidationErrors[i];
        }
        for(int j = 0; j < tier2ValidationErrors.length; j++)
        {
            combinedErrors[j + tier1ValidationErrors.length] = tier2ValidationErrors[j];
        }
        
        //ValidationError objects are sorted by their Miring Rule IDs
        Arrays.sort(combinedErrors);

        return combinedErrors;
    }

    //I shouldn't need to write and delete XML files from the hard drive.
    //By default, probatron reads xml files from the hard drive, so I needed 
    //to write to hard drive in order to use it.  Calling probatron reflectively allows us to 
    //use streams instead of writing to file.
    //Code is kept here for convenience.    
    /*public static void removeTempXml(String path)
    {
        logger.debug("Removing XML from " + path);
        try 
        {
            File myFile = new File(path);
            myFile.delete();
        } 
        catch (Exception e) 
        {
            logger.error("Exception when removing temp file " + path + " : " + e.toString());
        } 
        
    }

    public static void writeXml(String xmlText, String fileName)
    {
        logger.debug("Writing XML to " + fileName);
        try
        {
            PrintWriter writer = new PrintWriter(fileName, "UTF-8");
            writer.println(xmlText);
            writer.close();
        }
        catch(Exception e)
        {
            logger.error("Error writing XML to file: " + e);
        }
    }*/
}
