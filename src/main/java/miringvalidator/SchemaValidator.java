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
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class SchemaValidator
{
    private static final Logger logger = LogManager.getLogger(SchemaValidator.class);
    public static List<ValidationError> validationErrors;
    
    /**
     * Validate xml against a schema
     *
     * @param xml a String containing the XML to validate
     * @param schemaFileName the file name of the schema to compare against
     * @return an array of ValidationError objects found during validation
     */
    public static ValidationError[] validate(String xml, String schemaFileName) 
    {
        logger.debug("Starting a schema validation");        
        validationErrors = new ArrayList<ValidationError>();

        try 
        {
            URL schemaURL = SchemaValidator.class.getResource(schemaFileName);
            logger.debug("Schema URL Resource Location = " + schemaURL);
            File schemaFile = new File(schemaURL.toURI());
            Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaFile);

            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setSchema(schema);
            
            final SAXParser parser = factory.newSAXParser();
            final MiringValidationContentHandler handler = new MiringValidationContentHandler();

            //parser.parse is what does the actual "validation."  It parses the sample xml referring to the schema.
            //Errors are thrown by the handler, and we'll turn those into validation errors that are human readable.
            parser.parse(new InputSource(new StringReader(xml)), handler);
        } 
        catch (SAXException e) 
        {
            //We shouldn't get exceptions out here.  They should have been handled by the MiringValidationContentHandler
            logger.error("SaxException Error: " + e.getLocalizedMessage());
        }
        catch (Exception e)
        {
            logger.error("Exception during schema validation: " + e.getLocalizedMessage());
        }
        
        if(validationErrors.size() > 0)
        {
            //List -> Array
            ValidationError[] array = validationErrors.toArray(new ValidationError[validationErrors.size()]);
            logger.debug(validationErrors.size() + " schema validation errors found");
            return array;
        }
        else
        {
            logger.debug("ZERO schema validation errors found");
            //Empty.  Not null.  No problems found.
            return new ValidationError[0];
        }
    }

    private static class MiringValidationContentHandler extends DefaultHandler 
    {    
        //private static String parentElement;
        
        private static String parentURI = "";
        private static String parentLocalName = "";
        private static String parentQName = "";
        private static Attributes parentAttributes;
        
        //triggered when parser starts the document.  Maybe this override can be deleted.
        //Maybe we want to detect an HMLID here.
        @Override
        public void startDocument() 
                throws SAXException 
        {
        }
        
        //startElement is a method that is triggered when the parser hits the start of an element
        //I'm using it to record information about parent nodes of what node I'm validating
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException 
        {
            /*logger.debug("Analyzing a node.");
            logger.debug("uri= " + uri);
            logger.debug("localName= " + localName);
            logger.debug("qName= " + qName);
            logger.debug("attributes= " + attributes.toString());*/
            parentURI = uri;
            parentLocalName = localName;
            parentQName = qName;
            parentAttributes = attributes;
            
            /*if(localName != null && !localName.isEmpty())
                parentElement = localName;
            else
                parentElement = qName;    */
        }
        
        //warning(), error(), and fatalError() are overrides which are triggered by 
        //parser warnings, errors and fatal errors.  
        //Here we provide simple handlers.
        @Override
        public void warning(SAXParseException exception) throws SAXException 
        {
            logger.debug("Sax Parser Warning: " + exception.getMessage());
            handleParserException(exception);
        }
    
        @Override
        public void error(SAXParseException exception) throws SAXException 
        {
            logger.debug("Sax Parser NonFatal Error: " + exception.getMessage());
            handleParserException(exception);
        }
    
        @Override
        public void fatalError(SAXParseException exception) throws SAXException 
        {
            logger.debug("Sax Parser Fatal Error: " + exception.getMessage());
            handleParserException(exception);
        }

        private static void handleParserException(SAXException exception)
        {
            //Mine the exception data for useful information.  We want to present anything we can.
            //Might be worthwhile to keep track of the place in the tree we are within the XML.
            //I'm calling this method when we get a legitimate SAX Parser exception, which are triggered
            //when the parser finds a problem with the xml
            
            //We take the SAX parser exception, tokenize it, and build ValidationError objects based on the errors.

            String errorMessage = "??????????";
            String solutionMessage = "??????????";
            String miringRuleID = "??????????";
            
            String[] exceptionTokens = tokenizeString(exception.getMessage());
            
            if(exceptionTokens[0].equals("cvc-complex-type.2.4.a:"))
            {
                // This cvc-complex-type is called if there is a node missing.  here's a few examples of what the exception.getMessage() can look like
                // cvc-complex-type.2.4.a: Invalid content was found starting with element 'sample'. One of '{"http://schemas.nmdp.org/spec/hml/1.0.1":property, "http://schemas.nmdp.org/spec/hml/1.0.1":hmlid}' is expected.
                // cvc-complex-type.2.4.a: Invalid content was found starting with element 'sample'. One of '{"http://schemas.nmdp.org/spec/hml/1.0.1":reporting-center}' is expected.
                // cvc-complex-type.2.4.a: Invalid content was found starting with element 'reporting-center'. One of '{"http://schemas.nmdp.org/spec/hml/1.0.1":property, "http://schemas.nmdp.org/spec/hml/1.0.1":hmlid}' is expected.

                // "hmlid" and "reporting-center" will be the last word between the '{' and '}'. 
                // Find their indices.
                int minInd = -1, maxInd = -1;
                for(int x = 0; x < exceptionTokens.length; x++)
                {
                    String token = exceptionTokens[x];
                    if(token.contains("{"))
                    {
                        minInd = x;
                    }
                    if(token.contains("}"))
                    {
                        maxInd = x;
                    }
                }
                
                if(minInd == -1 || maxInd == -1)
                {
                    logger.error("parsing a cvc-complex-type.2.4.a schema .  No Indices Found.");
                }
                // qualifiedNodeName should look like this:
                // "http://schemas.nmdp.org/spec/hml/1.0.1":hmlid}'
                String qualifiedNodeName = exceptionTokens[maxInd];
                int begIndex = 11 + qualifiedNodeName.indexOf("hml/1.0.1\":");
                int enDex = qualifiedNodeName.indexOf("}'");
                
                String missingNodeName = qualifiedNodeName.substring(begIndex, enDex);
                
                errorMessage = "There is a missing " + missingNodeName + " node underneath the " + parentLocalName + " node.";
                solutionMessage = "Please add exactly one " + missingNodeName + " node underneath the " + parentLocalName + " node.";
                
                if(missingNodeName.equals("hmlid"))
                {
                    miringRuleID = "1.1.a";
                }
                else if(missingNodeName.equals("reporting-center"))
                {
                    miringRuleID = "1.2.a";
                }
                else
                {                
                    logger.error("MissingNodeName Not Handled: " + missingNodeName);
                }
            }
            else if(exceptionTokens[0].equals("cvc-complex-type.4:"))
            {
                //This cvc-complex-type is called if there is an attribute missing from a node
                //It looks like this:
                // cvc-complex-type.4: Attribute 'quality-score' must appear on element 'variant'.
                
                String missingAttributeName = exceptionTokens[2].replace("'", "");
                String qualifiedNodeName = exceptionTokens[7];                
                String nodeName = qualifiedNodeName.substring(1, qualifiedNodeName.indexOf("'."));
                
                errorMessage = "The node " + nodeName + " is missing a " + missingAttributeName + " attribute.";
                solutionMessage = "Please add a " + missingAttributeName + " attribute to the " + nodeName + " node.";
                
                if(nodeName.equals("variant"))
                {
                    if(missingAttributeName.equals("id"))
                    {
                        miringRuleID = "5.3.a";
                    }
                    else if(missingAttributeName.equals("reference-bases"))
                    {
                        miringRuleID = "5.4.a";
                    }
                    else if(missingAttributeName.equals("alternate-bases"))
                    {
                        miringRuleID = "5.5.a";
                    }
                    else if(missingAttributeName.equals("quality-score"))
                    {
                        miringRuleID = "5.6.a";
                    }
                    else if(missingAttributeName.equals("filter"))
                    {
                        miringRuleID = "5.7.a";
                    }
                    else
                    {
                        logger.error("Missing attribute name not handled! : " + missingAttributeName);
                    }
                }
                else
                {
                    //more stuff to check in here.
                    logger.error("Node Name Not Handled! : " + nodeName);
                }
            }
            else
            {
                //There are more types of errors probably. 
                //What if I have too many of a thing?
                logger.error("This Sax Parser Exception isn't handled :" + exception.getMessage());
            }
            
            //Here we make a simple ValidationError object with the information we collected, and store it away.
            ValidationError ve =  new ValidationError(
                errorMessage
                ,true);
            
            ve.setSolutionText(solutionMessage);
            ve.setMiringRule(miringRuleID);
            
            Utilities.addValidationError(validationErrors, ve);
            //validationErrors.add(ve);
        }

        private static String[] tokenizeString(String exceptionMessage)
        {
            StringTokenizer st = new StringTokenizer(exceptionMessage);
            String[] messageTokens = new String[st.countTokens()];
            int counter = 0;
            while (st.hasMoreTokens()) 
            {
                messageTokens[counter] = st.nextToken();
                counter++;
            }
            return messageTokens;
        }
    }
}