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
package miringvalidator.main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class SchematronValidator
{
    static ClassLoader loadedProbatronClasses;
    static String tempXmlLocation = "/Users/bmatern/GitHub/MiringValidator/temp/temp.xml";
    static String schemaPath = "/Users/bmatern/GitHub/MiringValidator/resources/schematron/";
    static String jarFileLocation = "/Users/bmatern/GitHub/MiringValidator/resources/jar/probatron.jar";

    
    
    
    //So here's the mystery.  validate() works but validate2() doesnt.
    //I'm trying to latch onto SchematronSchema to use it's methods.  
    //Just because I think it's dumb to write the XML to a file in order to use it.    
    //It seems to work to make a session., but not to do it from SchematronSchema.
    //Maybe something special happens in session somewhere, or one of those variables is required
    //Maybe Session loads in the supporting stylesheets or something.
    //I think if i get logger to work, then schematron logs can be employed.
    public static ValidationError[] validate(String xml, String schemaFileName)
    {
        //This method sort of mimics the main method in the probatron jar.  
        //I really don't want to have the jar run on it's own, because then I need to spawn other threads etc        
        try
        {
            loadJarElements();

            writeXml(xml, tempXmlLocation);

            String candidate = (tempXmlLocation);

            //Here we're loading up a "Session" object thru reflection
            //Session is kind of the starting point for probatron.
            //Luckily it has a default constructor so I can call it using newInstance().
            Class c= loadedProbatronClasses.loadClass("org.probatron.Session");
            Object sessionObject = c.newInstance();
            
            //set the SchemaDoc for our Session.
            String schemaFileNameAndPath = "file:" + schemaPath + schemaFileName;
            callReflectedMethod(sessionObject, "setSchemaDoc", schemaFileNameAndPath, schemaFileNameAndPath.getClass());

            Object validationReportObject = callReflectedMethod(sessionObject, "doValidation", "file:" + candidate, candidate.getClass());
            //Object validationReportObject = doValidation(xml, schemaPath + schemaFileName);
            
            removeTempXml(tempXmlLocation);
            
            ByteArrayOutputStream myBaos = new ByteArrayOutputStream();

            callReflectedMethod(validationReportObject, "streamOut", myBaos, Class.forName("java.io.OutputStream"));
            String resultString = myBaos.toString();

            ValidationError[] resultingErrors = getValidationErrorsFromSchematronReport(resultString);

            return resultingErrors;
        }
        catch(Exception e )
        {
            System.out.println("Error during schematron validation: " + e);
            //logger.fatal( e );
        }

        //logger.info( "Done. Elapsed time (ms):" + ( System.currentTimeMillis() - t ) );        
        
        return new ValidationError[0];
    }
    
    public static ValidationError[] validate2(String xml, String schemaFileName)
    {
        //This method sort of mimics the main method in the probatron jar.  
        //I really don't want to have the jar run on it's own, because then I need to spawn other threads etc        
        try
        {
            loadJarElements();

            //writeXml(xml, tempXmlLocation);

            //String candidate = (tempXmlLocation);

            //Here we're loading up a "Session" object thru reflection
            //Session is kind of the starting point for probatron.
            //Luckily it has a default constructor so I can call it using newInstance().
            //Class c= loadedProbatronClasses.loadClass("org.probatron.Session");
            //Object sessionObject = c.newInstance();
            
            //set the SchemaDoc for our Session.
            //String schemaFileNameAndPath = schemaPath + schemaFileName;
            //callReflectedMethod(sessionObject, "setSchemaDoc", schemaFileNameAndPath, schemaFileNameAndPath.getClass());

            //Object validationReportObject = callReflectedMethod(sessionObject, "doValidation", "file:" + candidate, candidate.getClass());
            Object validationReportObject = doValidation(xml, schemaPath + schemaFileName);
            
            //removeTempXml(tempXmlLocation);
            
            ByteArrayOutputStream myBaos = new ByteArrayOutputStream();

            callReflectedMethod(validationReportObject, "streamOut", myBaos, Class.forName("java.io.OutputStream"));
            String resultString = myBaos.toString();

            ValidationError[] resultingErrors = getValidationErrorsFromSchematronReport(resultString);

            return resultingErrors;
        }
        catch(Exception e )
        {
            System.out.println("Error during schematron validation: " + e);
            //logger.fatal( e );
        }

        //logger.info( "Done. Elapsed time (ms):" + ( System.currentTimeMillis() - t ) );        
        
        return new ValidationError[0];
    }
    
    //This method mimics Probatron's Session.doValidation.
    //It returns a probatron ValidationReport object.
    //I can't inherit from ValidationReport though, since it's reflected.
    //Since I'm sort of overriding Session's methods, I think I don't even need
    //To make a session object anymore.
    static Object doValidation(String xml, String schemaLocation) 
    {
        //ValidationReport vr = null;
        Object vr = null;
        //theSchema is a SchematronSchema object.
        Object theSchema = null;
        try 
        {
            InputStream schemaInputStream = new FileInputStream(schemaLocation);
            InputStream xmlInputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            //InputStream xmlInputStream = new FileI
        
            //theSchema = new SchematronSchema( this, new URL( this.schemaDoc ) );

            Class c= loadedProbatronClasses.loadClass("org.probatron.SchematronSchema");
            //Using the SchematronSchema constructor that takes an InputStream
            Constructor ctor = c.getDeclaredConstructor(InputStream.class);
            //Already public, lets not mess with accessibility
            //ctor.setAccessible(true);
            theSchema = ctor.newInstance(schemaInputStream);


            // production code should handle these exceptions more gracefully

            //I can call the SchematronSchema.validateCandidate method with an inputstream param
            //, instead of the URL param.  Bueno.
            //vr = theSchema.validateCandidate( new URL( candidate ) );            
            vr = callReflectedMethod(theSchema,"validateCandidate", xmlInputStream, Class.forName("java.io.InputStream"));
            //vr = callReflectedMethod(theSchema,"validateCandidate", new URL("file:" + tempXmlLocation)  , Class.forName("java.net.URL"));

       
        } 
        catch(Exception e)
        {
            System.out.println("Exception in doValidation: " + e);
        }

        /*if( physicalLocators )
        {
            vr.annotateWithLocators( this, new URL( candidate ) );
        }

        if( getReportFormat() == ValidationReport.REPORT_SVRL_MERGED )
        {
            vr.mergeSvrlIntoCandidate( this, new URL( candidate ) );
        }*/

        return vr;

    }
    
    
    
    private static Object callReflectedMethod(Object callingObject, String methodName, Object singleParameter, Class parameterClass)
    {
        //Only works for methods that take a single parameter.
        //We must pass in the Class of the parameter,
        //because "getMethod" is very specific about the class of the parameter.  
        //Using inherited classes (ByteArrayOutputStream vs. OutputStream) will break getMethod().
        Method method = null;
        try 
        {
            //method = callingObject.getClass().getMethod(methodName, parameterClass);
            method = callingObject.getClass().getDeclaredMethod(methodName, parameterClass);
            method.setAccessible(true);
            return method.invoke(callingObject, singleParameter);
        } 
        catch (SecurityException e) 
        {
            System.out.println("Security exception while calling reflected method: " + e);
        } 
        catch (NoSuchMethodException e) 
        {
            System.out.println("NoSuchMethod exception while calling reflected method: " + e);
        }
        catch (IllegalArgumentException e) 
        {
            System.out.println("IllegalArgument exception while calling reflected method: " + e);
        } 
        catch (IllegalAccessException e) 
        {
            System.out.println("IllegalAccess exception while calling reflected method: " + e);
        } 
        catch (InvocationTargetException e) 
        {
            System.out.println("InvocationTarget exception while calling reflected method: " + e);
        }
        catch (Exception e)
        {
            System.out.println("Exception while calling reflected method: " + e);
        }

        return null;
    }
        
 
    private static void loadJarElements()
    {
        //This method will crack open the probatron jar.
        //I store them in a static ClassLoader object loadedProbatronClasses.  
        //I think they're only available from this object
        //not from the general JVM.
        //I don't need to load any classes right now.  They are available in loadedProbatronClasses.
        //I will load the classes as I need them.
        //I reckon that this method should be called in a "setup" method somewhere, rather than 
        //each time we validate a schematron.  Or maybe not.  Not sure the overhead of this.
        try
        {
            JarFile jarFile = new JarFile(jarFileLocation);
            Enumeration e = jarFile.entries();
    
            URL[] urls = { new URL("jar:file:" + jarFileLocation +"!/") };
            URLClassLoader cl = URLClassLoader.newInstance(urls);
            loadedProbatronClasses = cl;
    
/*
            
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
                {//Having problems with log4j classes.  I think it's being loaded twice, so it's getting an exception here.  
                    //Not really sure, but log4j is being loaded already so here we are.
                    //Could be that I only need to load the org.probatron.* classes
                    System.out.println(className + " will not be loaded.");
                }
                else
                {
                    System.out.println(className + " loading...");
                    Class c = cl.loadClass(className);
                }
            }
            */
            jarFile.close();
        }
        catch(Exception e)
        {
            System.out.println("Error during schematron validation:" + e);
        }
    }
    
    private static ValidationError[] getValidationErrorsFromSchematronReport(String xml)
    {
        List<ValidationError> validationErrors = new ArrayList<ValidationError>();

        try
        {
            Element rootElement = xmlToDomObject(xml);  
            NodeList list = rootElement.getElementsByTagName("svrl:text");

            if (list != null && list.getLength() > 0) 
            {
                for(int i = 0; i < list.getLength(); i++)
                {
                    Node n = list.item(i);

                        Node childNode = n.getFirstChild();

                        String errorMessage = childNode.getNodeValue();
                        
                        ValidationError ve = new ValidationError(
                                errorMessage
                                ,"Please verify the start and end attributes on your reference-sequence node."
                                ,"Miring Element 4.2.3"
                                ,true);
                        validationErrors.add(ve);
                }
            }
        }
        catch(Exception e)
        {
            System.out.println("Error forming DOM from schematron results: " + e);
        }

        if(validationErrors.size() > 0)
        {
            //List -> Array
            ValidationError[] array = validationErrors.toArray(new ValidationError[validationErrors.size()]);
            return array;
        }
        else
        {
            //Empty.  Not null.  No problems found.
            return new ValidationError[0];
        }
    }

    private static Element xmlToDomObject(String xml)
            throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));
        Element rootElement = document.getDocumentElement();
        return rootElement;
    }
/*
    static String fixArg( String arg )
    {
        // user concession, if no URL scheme assume these are files
        return arg.indexOf( ":" ) == - 1 ? "file:" + arg : arg;
    }
*/
    private static void removeTempXml(String path)
    {
        try 
        {
            File myFile = new File(path);
            myFile.delete();
        } 
        catch (Exception e) 
        {
            System.out.println("Exception when removing temp file " + path + " : " + e.toString());
        } 
        
    }

    private static void writeXml(String xmlText, String fileName)
    {
        try
        {
            PrintWriter writer = new PrintWriter(fileName, "UTF-8");
            writer.println(xmlText);
            writer.close();
        }
        catch(Exception e)
        {
            System.out.println("Error writing XML to file: " + e);
        }
    }

}
