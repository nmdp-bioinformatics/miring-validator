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
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Utilities
{
    public static boolean containsErrorNode(Element xmlDomObject, String errNodeDescription)
    {
        return false;
    }
    
    public static URLClassLoader loadJarElements(String jarFileLocation)
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
            return cl;
        }
        catch(Exception e)
        {
            System.out.println("Error during schematron validation:" + e);
        }
        return null;
    }

    public static Object callReflectedMethod(Object callingObject, String methodName, Object singleParameter, Class parameterClass)
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

    public static Element xmlToDomObject(String xml)
            throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));
        Element rootElement = document.getDocumentElement();
        return rootElement;
    }

    public static void removeTempXml(String path)
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

    public static void writeXml(String xmlText, String fileName)
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
