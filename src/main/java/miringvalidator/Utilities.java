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
package main.java.miringvalidator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import main.java.miringvalidator.ValidationResult.Severity;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

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
        //check all nodes with name "description"
        NodeList childrenNodes = xmlToRootElement(validationErrorReport).getElementsByTagName("description");
        for(int i = 0; i < childrenNodes.getLength(); i++)
        {
            Node invMirResult = childrenNodes.item(i);
            String descriptionText = invMirResult.getTextContent();
            if(descriptionText!= null && descriptionText.contains(errNodeDescription))
            {
                return true;
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

            URL[] urls = { new URL("jar:file:" + jarFileLocation +"!/") };
            URLClassLoader cl = URLClassLoader.newInstance(urls);

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
    public static Object callReflectedMethod(Object callingObject, String methodName, Object singleParameter, Class<?> parameterClass)
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
     * Get the root element of an XML file from XML in string form.  They are more useful for parsing the XML.
     *
     * @param xml A String containing xml
     * @return an org.w3c.Dom.Element which is the root of the xml document.
     */
    public static Element xmlToRootElement(String xml)
    {
        try
        {
            Document document = xmlToDocumentObject(xml);
            Element rootElement = document.getDocumentElement();
            return rootElement;
        }
        catch(Exception e)
        {
            //If i was clever I'd handle these exceptions specifically
            //throws ParserConfigurationException, SAXException, IOException
            logger.error("Exception in Utilities.xmlToRootElement()" + e.toString());
            return null;
        }
    }
    
    /**
     * Convert XML in string form to a DOM Object.  
     *
     * @param xml A String containing xml
     * @return an org.w3c.Dom.Element which is the root of the xml document.
     */
    public static Document xmlToDocumentObject(String xml)
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));
            return document;
        }
        catch(Exception e)
        {
            //If i was clever I'd handle these exceptions specifically
            //throws ParserConfigurationException, SAXException, IOException
            logger.error("Exception in Utilities.xmlToDomObject()" + e.toString());
            return null;
        }
    }
    
    public static String getStringFromDoc(Document doc)
    {
        //Generate an XML String from the Document object.
        String xmlString = null;
        try
        {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
            xmlString = result.getWriter().toString();
        }
        catch(Exception e)
        {
            logger.error("Error generating XML String" + e);
        }
        return xmlString;
    }
    
    /**
     * Get the HMLID Root from an XML String.  
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
     * Get the HMLID Node from an XML String. The hmlid node is a child of the root node on
     * both an HML document, and a Validation Results report, so use this method on both
     * types of xml.
     *
     * @param xml A String containing HML
     * @return the HML document's HMLID node
     */
    private static Node getHMLIDNode(String xml)
    {
        //hmlid should be a child nodes of the root xml element.
        NodeList childrenNodes = xmlToRootElement(xml).getChildNodes();
        for(int i = 0; i < childrenNodes.getLength(); i++)
        {
            String childsFullName = childrenNodes.item(i).getNodeName();
            //Is it qualified with a namespace?
            String[] childsTokens = tokenizeString(childsFullName,":");
            String childsName = (childsTokens.length==1)?childsFullName:childsTokens[1];
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
    public static void addValidationError(List<ValidationResult> validationErrors, ValidationResult ve)
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
     * Concatenate and sort two arrays of Validation Error objects.  
     *
     * @param firstErrorArray An Array of ValidationError objects
     * @param secondErrorArray An Array of ValidationError objects
     * @return A combined and sorted Array of ValidationError objects
     */
    public static ValidationResult[] combineArrays(ValidationResult[] firstErrorArray, ValidationResult[] secondErrorArray)
    {
        //Super clever way of handling nulls.
        if(firstErrorArray == null)
        {
            return secondErrorArray;
        }
        if(secondErrorArray == null)
        {
            return firstErrorArray;
        }

        ValidationResult[] combinedErrors = new ValidationResult[firstErrorArray.length + secondErrorArray.length];
        
        for(int i = 0; i < firstErrorArray.length; i++)
        {
            combinedErrors[i] = firstErrorArray[i];
        }
        for(int j = 0; j < secondErrorArray.length; j++)
        {
            combinedErrors[j + firstErrorArray.length] = secondErrorArray[j];
        }
        
        //ValidationError objects are sorted by their Miring Rule IDs
        Arrays.sort(combinedErrors);

        return combinedErrors;
    }

    /**
     * Read an xml file from the Resources directory.  Returns a String containing the XML.
     *
     * @param xmlResourceName A String containing the name of the XML resource
     * @return a String containing the read XML
     */
    public static String readXmlResource(String xmlResourceName)
    {
        try
        {
            File schemaFileURL = new File(SchematronValidator.class.getResource(xmlResourceName).getFile());
            BufferedReader xmlReader = new BufferedReader(new FileReader(schemaFileURL));
            
            StringBuilder xmlBuffer = new StringBuilder();
            String line = xmlReader.readLine();
    
            while (line != null) 
            {
                xmlBuffer.append(line);
                xmlBuffer.append(System.lineSeparator());
                line = xmlReader.readLine();
            }
            xmlReader.close();
            
            String xmlText = xmlBuffer.toString();
            return xmlText;
        }
        catch(Exception e)
        {
            logger.error("Unable to open XML Resource:" + e);
            return null;
        }
    }

    /**
     * Convert an Attributes object into a String, for descriptive use in ValidationError objects.  The String will look like this:
     * {name1:value1}, {name2:value2}, ... {nameX:valueX}
     *
     * @param attributes XML attributes for a node
     * @return a String containing the attributes concatenated together.
     */
    public static String getAttributes(Attributes attributes)
    {
        String attributeString = "";
        if(attributes != null && attributes.getLength() > 0)
        {
            for(int i = 0; i < attributes.getLength(); i++)
            {
                attributeString = attributeString  + "{" + attributes.getLocalName(i) + ":" + attributes.getValue(i) + "}, ";
            }
        }
        // Trim the trailing comma and space        
        if(attributeString.contains(", "))
        {
            attributeString = attributeString.substring(0,attributeString.length()-2);
        }
        return attributeString;
    }

    /**
     * Does this array contain zero Miring errors?  Any ValidationErrors with Severity=FATAL OR Severity=MIRING will return false.
     *
     * @param errors an array of ValidationError objects
     * @return is this report miring compliant?
     */
    public static boolean isMiringCompliant(ValidationResult[] errors)
    {
        //Does this list contain any fatal errors?
        for(int i = 0; i < errors.length; i++)
        {
            if(errors[i].getSeverity()==Severity.FATAL  || errors[i].getSeverity()==Severity.MIRING)
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Does this array contain any fatal errors?  Any ValidationErrors with Severity=FATAL will return true.
     *
     * @param errors an array of ValidationError objects
     * @return true if this report has at least one fatal error
     */
    public static boolean hasFatalErrors(ValidationResult[] errors)
    {
        //Does this list contain any fatal errors?
        for(int i = 0; i < errors.length; i++)
        {
            if(errors[i] != null)
            {
                if(errors[i].getSeverity()==Severity.FATAL)
                {
                    return true;
                }
            }
        }
        return false;
    }    
    
    public static String[] tokenizeString(String text, String delimiter)
    {
        try
        {
            StringTokenizer st = new StringTokenizer(text, delimiter);
            String[] messageTokens = new String[st.countTokens()];
            int counter = 0;
            while (st.hasMoreTokens()) 
            {
                messageTokens[counter] = st.nextToken();
                counter++;
            }
            return messageTokens;
        }
        catch(Exception e)
        {
            logger.error("Exception in tokenizeString(): " + e);
            return null;
        }
    }


    //This method searches for <sequence> nodes, and cleans the text of any spaces or tabs.  
    //Pretty sure I"m not using htis for anything currently.
    /*public static String cleanSequences(String xml)
    {
        //Sequence objects often have lots of tabs and spaces.  Im gonna remove them.
        int indexBegin = xml.indexOf("<sequence>");
        int indexEnd = xml.indexOf("</sequence>");
        if(indexBegin != -1 && indexEnd != -1 && indexEnd > indexBegin)
        {
            //seqNode = "<sequence> AG GT </sequence>"
            String seqNode = xml.substring(indexBegin, indexEnd + 11);
            //rawSeq = " AG GT "
            String rawSeq = xml.substring(indexBegin +10 , indexEnd);
            
            if(!rawSeq.contains("<") && !rawSeq.contains(">"))
            {
                //sequenceRemainder = ".......<AnotherSequenceToClean?>....."
                String sequenceRemainder = cleanSequences(xml.substring(indexEnd + 11, xml.length()));
                //polishedSeqNode = "<sequence>AGGT</sequence>"
                String polishedSeqNode = seqNode.replace(" ", "").replace("\t", "").replace("\n", "");
                //return <clean sequence> + <clean remainder>.
                return xml.substring(0,indexEnd+11).replace(seqNode,polishedSeqNode) + sequenceRemainder;
            }
        }

        return xml;
    }*/
    
    
    //Will return null if hml is the root namespace.
    public static String getNamespaceName(String xml)
    {
        logger.debug("gettingNamespaceName");
            
        try
        {
            Document xmlDocument = xmlToDocumentObject(xml);
            Element rootNode = xmlDocument.getDocumentElement();
            
            String xmlns = rootNode.getAttribute("xmlns");
            
            if(xmlns != null && xmlns.equals("http://schemas.nmdp.org/spec/hml/1.0.1"))
            {
                logger.debug("HML 1.0.1 is the root namepace.");
                return null;
            }
            else
            {
                NamedNodeMap attributes = rootNode.getAttributes();
                if(attributes != null && attributes.getLength() > 0)
                {
                    for(int i = 0; i < attributes.getLength(); i++)
                    {
                        String name = attributes.item(i).getNodeName();
                        String value = attributes.item(i).getNodeValue();
                        if(name != null && value != null
                            && name.contains("xmlns:")
                            && value.equals("http://schemas.nmdp.org/spec/hml/1.0.1"))
                        {
                            String nameSpace = name.substring(name.indexOf("xmlns:") + 6, name.length());
                            logger.debug("Found the HML namespace: " + nameSpace);
                            
                            return nameSpace;
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            logger.error("Exception while cleaning namespaces:" + e);
        }
        return xml;
    }
    
    public static String stripNamespace(String nodeName, String namespaceName)
    {
        if(namespaceName==null)
        {
            return nodeName;
        }
        else if(!nodeName.contains(":"))
        {
            logger.error("Expected a namespace-qualified node name, with ns= " + namespaceName + ".  Instead we have= " + nodeName);
        }
        else
        {
            //ns2:reporting-center
            String[] tokens = Utilities.tokenizeString(nodeName,":");
            if(tokens != null && tokens.length == 2)
            {
                if(tokens[0].equals(namespaceName))
                {
                    return tokens[1];
                }
                else
                {
                    logger.error("Incorrect namespace found. expected=" + namespaceName + " nodeName = " +nodeName);
                    return nodeName;
                }
            }
            else
            {
                logger.error("Namespace expected and not found. expected=" + namespaceName + " nodeName = " +nodeName);
            }
        }
        logger.error("Something funny happened in stripNamespace(). Returning full node name: " + nodeName);
        return nodeName;
    }
    
    public static HashMap<String,String> getPropertiesFromRootHml(String xml)
    {
        try
        {
            Element rootElement = xmlToRootElement(xml);
            NodeList children = rootElement.getChildNodes();
            HashMap<String,String> results = new HashMap<String,String>();
            
            for(int i = 0; i < children.getLength(); i++)
            {
                if(children.item(i).getNodeName().contains("property"))
                {
                    String propertyName = children.item(i).getAttributes().getNamedItem("name").getNodeValue();
                    String propertyValue = children.item(i).getAttributes().getNamedItem("value").getNodeValue();
                    results.put(propertyName, propertyValue);
                }
            }
            return (results.size()==0)?null:results;
        }
        catch(Exception e)
        {
            logger.error("exception in getPropertiesFromRootXml" + xml);
            return null;
        }
    }
    
    public static String getSampleID(String xPath, Sample[] sampleIDs)
    {
        String sampleID;
        
        if(xPath==null || xPath.length()==0)
        {
           sampleID = null;
        }
        else
        {
            //Get the index of the sample, use it to lookup sample ID.
            // .../sample[index]/...
            int sampleLocation = xPath.indexOf("/sample[");
            if(sampleLocation != -1)
            {
                String tempSampleInd = xPath.substring(sampleLocation + 8, xPath.length());
                tempSampleInd = tempSampleInd.substring(0, tempSampleInd.indexOf("]"));
                int sampleIndex = Integer.parseInt(tempSampleInd);
                
                sampleID = sampleIDs[sampleIndex-1].id;
            }
            else
            {
                sampleID = null;
            }
        }

        return sampleID;
    }
    
    public static Node[] combineNodeLists(NodeList successfulReportList, NodeList failedAssertList)
    {
        Node[] newList = new Node[successfulReportList.getLength() + failedAssertList.getLength()];
        
        for(int i = 0; i < successfulReportList.getLength(); i++)
        {
            newList[i] = successfulReportList.item(i);
        }
        for(int i = 0; i < failedAssertList.getLength(); i++)
        {
            newList[i + successfulReportList.getLength()] = failedAssertList.item(i);
        }
        return newList;
    }

    public static String getAttribute(NamedNodeMap ruleAttributes, String attributeName)
    {
        Node miringRuleNode = ruleAttributes.getNamedItem(attributeName);
        String miringRule = miringRuleNode!=null?miringRuleNode.getNodeValue():null;
        return miringRule;
    }

    /*public static String trimPrologText(String xml)
    {
        try
        {
            Document doc = Utilities.xmlToDocumentObject(xml);
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPathExpression xpathExp = xpathFactory.newXPath().compile("//text()[normalize-space(.) = '']");  
            NodeList emptyTextNodes = (NodeList)xpathExp.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < emptyTextNodes.getLength(); i++) 
            {
                Node emptyTextNode = emptyTextNodes.item(i);
                emptyTextNode.getParentNode().removeChild(emptyTextNode);
            }

            String newXML = Utilities.getStringFromDoc(doc);
            return newXML;
        }
        catch(Exception e)
        {
            logger.error("Error during trimPrologText: " + e);
            return xml;
        }
    }*/
}
