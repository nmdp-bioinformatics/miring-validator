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

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.*;

import org.nmdp.miring.ValidationResult.Severity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


/** 
 * SchemaValidator is a class used to validate an XML document against an XML Schema.
 * 
 * The validation is performed by an org.xml.sax parser.  The validation logic is handled by
 * the subclass, MiringValidationContentHandler.
*/
public class SchemaValidator
{
    static Logger logger = LoggerFactory.getLogger(SchemaValidator.class);
    public static List<ValidationResult> validationErrors;
    public static String hmlNamespace = null;
    public static List<Sample> samples;
    
    //missingNodeTemplates and missingAttributeTemplates are loaded from xml template files.
    //They define what information (rule id, and additional info, etc.) is included in error messages
    //Included info can be specified on a per-rule basis
    public static Document missingNodeTemplates = null;
    public static Document missingAttributeTemplates = null;
    
    /**
     * Validate xml against a schema
     *
     * @param xml a String containing the XML to validate
     * @param schemaFileName the file name of the schema to compare against
     * @return an array of ValidationError objects found during validation
     */
    public static ValidationResult[] validate(String xml, String schemaFileName) 
    {
        logger.debug("Starting a schema validation");
        validationErrors = new ArrayList<ValidationResult>();
        samples = new ArrayList<Sample>();

        try 
        {
            MiringValidationContentHandler.clearModel();
            //Switch the two
            if(schemaFileName.equals("/org/nmdp/miring/schema/MiringTier1.xsd"))
            {
                missingNodeTemplates = Utilities.xmlToDocumentObject(Utilities.readXmlResource("/org/nmdp/miring/ruletemplates/MissingNodeTemplate.xml"));
                missingAttributeTemplates = Utilities.xmlToDocumentObject(Utilities.readXmlResource("/org/nmdp/miring/ruletemplates/MissingAttributeTemplate.xml"));
                hmlNamespace = Utilities.getNamespaceName(xml);
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
                parser.parse(new InputSource(new StringReader(xml)), handler);//??????
                MiringValidationContentHandler.clearModel();//except here
            }

            else
            {
            	missingNodeTemplates = Utilities.xmlToDocumentObject(Utilities.readXmlResource("/org/nmdp/miring/ruletemplates/MissingNodeTemplate.xml"));
                missingAttributeTemplates = Utilities.xmlToDocumentObject(Utilities.readXmlResource("/org/nmdp/miring/ruletemplates/MissingAttributeTemplate.xml"));
            	hmlNamespace = Utilities.getNamespaceName(xml);
                URL schemaURL = SchemaValidator.class.getResource(schemaFileName);
                logger.debug("Schema URL Resource Location = " + schemaURL);
                File schemaFile = new File(schemaURL.toURI());
                Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaFile);

                final SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setSchema(schema);
                
                final SAXParser parser = factory.newSAXParser();
                final HMLValidationContentHandler handler = new HMLValidationContentHandler();
                //parser.parse is what does the actual "validation."  It parses the sample xml referring to the schema.
                //Errors are thrown by the handler, and we'll turn those into validation errors that are human readable.
                parser.parse(new InputSource(new StringReader(xml)), handler);//??????
                
            }
            
            
          
        }
        catch (Exception e)
        {
            logger.error("Exception during schema validation.", e);
        }
        
        if(validationErrors.size() > 0)
        {
            //List -> Array
            ValidationResult[] array = validationErrors.toArray(new ValidationResult[validationErrors.size()]);
            logger.debug(validationErrors.size() + " schema validation errors found");
            return array;
        }
        else
        {
            logger.debug("ZERO schema validation errors found");
            //Empty.  Not null.  No problems found.
            return new ValidationResult[0];
        }
    }

    /** 
     * MiringValidationContentHandler is a subclass of SchemaValidator, which is responsible for handling 
     * parse exceptions, and performing Miring Specific logic for determining Miring Results.
     * 
     * The methods in this class are overrides of DefaultHandler, which I extend to provide validation logic.
     * 
     * The startElement and endElement methods are used to construct the SimpleXmlModel for the document.
     * 
     * Parser exceptions are interpreted and translated into MIRING ValidationResults.
    */
    private static class MiringValidationContentHandler extends DefaultHandler 
    {    
        //xmlRootNode represents the root node of the xml document, which is a
        //skeleton representation of the document, built recursively during the sax parse
        //This SimpleXmlModel is used to generate an xpath on the report
        public static SimpleXmlModel xmlRootNode;
        public static SimpleXmlModel xmlCurrentNode;
        public static int nodeCount = 0;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException 
        {
            nodeCount++;
            //logger.debug("NODE COUNT: " + nodeCount + " NAME: " + localName + " ATTRIBUTES: " + Utilities.getAttributes(attributes));
            
            try
            {
                if(localName.equals("sample"))
                {
                    //Every time we start analyzing a sample, store it's ID, for reporting purposes
                    String sampleID = attributes.getValue("id");
                    String centerCode = attributes.getValue("center-code");
                    samples.add(new Sample(sampleID,centerCode));
                }
                
                if(xmlRootNode==null)
                {
                    //This is the new root node.
                    xmlRootNode = new SimpleXmlModel(localName,1);
                    xmlCurrentNode = xmlRootNode;
                }
                else
                {
                    //There is a root node already.  This must be a child node.
                    int highestChildIndex = xmlCurrentNode.getHighestChildIndex(localName);
                    SimpleXmlModel newCurrentNode = new SimpleXmlModel(localName , highestChildIndex + 1);
                    xmlCurrentNode.addChildNode(newCurrentNode);
                    xmlCurrentNode = newCurrentNode;
                }
            }
            catch(Exception e)
            {
                logger.error("Exception in startElement",e);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException 
        {
            try
            {
                if(xmlCurrentNode.parentNode != null)
                {
                    //If the parent node *IS* null, that means we're closing out the root HML element.  All done.  
                    //Otherwise zoom out the parser to the parent
                    xmlCurrentNode = xmlCurrentNode.parentNode;
                }
            }
            catch(Exception e)
            {
                logger.error("Exception in endElement",e);
            }
        }
        
        //warning(), error(), and fatalError() are overrides which are triggered by 
        //parser warnings, errors and fatal errors.  
        //Here we provide simple handlers for the three.
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
        //Put More Detaill in these

        
        /**
         * Translate a SaxException object into a useful MiringResult
         * 
         * I'm calling this method when we get a legitimate SAX Parser exception, which are triggered
         * When the parser finds a problem with the xml.
         * Take the SAX parser exception, tokenize it, and build a Miring-specific MiringResult object based on the errors.
         *
         * @param exception a SaxException containing schema validation information
         */
        private static void handleParserException(SAXParseException exception)
        {
            ValidationResult ve = null;
            
            String errorMessage = exception.getMessage();
            String[] exceptionTokens = Utilities.tokenizeString(errorMessage, " ");
            String error="["+Integer.toString(exception.getLineNumber())+","+Integer.toString(exception.getColumnNumber())+"]";
            
                       //MISSING NODE
           if(exceptionTokens[0].equals("cvc-complex-type.2.4.a:") || exceptionTokens[0].equals("cvc-complex-type.2.4.b:"))
            {
                // This cvc-complex-type is called if there is a node missing.  here's a few examples of what the exception.getMessage() can look like
                // cvc-complex-type.2.4.a: Invalid content was found starting with element 'sample'. One of '{"http://schemas.nmdp.org/spec/hml/1.0.1":property, "http://schemas.nmdp.org/spec/hml/1.0.1":hmlid}' is expected.
                // cvc-complex-type.2.4.a: Invalid content was found starting with element 'sample'. One of '{"http://schemas.nmdp.org/spec/hml/1.0.1":reporting-center}' is expected.
                // cvc-complex-type.2.4.a: Invalid content was found starting with element 'reporting-center'. One of '{"http://schemas.nmdp.org/spec/hml/1.0.1":property, "http://schemas.nmdp.org/spec/hml/1.0.1":hmlid}' is expected.
                // cvc-complex-type.2.4.b: The content of element 'sbt-ngs' is not complete. One of '{"http://schemas.nmdp.org/spec/hml/1.0.1":property, "http://schemas.nmdp.org/spec/hml/1.0.1":raw-reads}' is expected.
                // for 2.4.b, it's interesting that it says the content of element 'sbt-ngs'.  That's the real parent of the node where it's missing.  Perhaps I can use that info, maybe it's useless.
                // Format seems better defined here:  https://wiki.xmldation.com/Support/Validator/cvc-complex-type-2-4-a
                // If I'm getting wrong node names, better look into the format of this error.
                
                // The missing node name ("hmlid", "reporting-center", etc.) will be the last word between the '{' and '}'. 
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
                ve = handleMissingNode(error,missingNodeName);
            }
            //MISSING ATTRIBUTE
            else if(exceptionTokens[0].equals("cvc-complex-type.4:"))
            {
                //This cvc-complex-type is called if there is an attribute missing from a node
                //It looks like this:
                // cvc-complex-type.4: Attribute 'quality-score' must appear on element 'variant'.
                // https://wiki.xmldation.com/Support/Validator/cvc-complex-type-4
                
                String missingAttributeName = exceptionTokens[2].replace("'", "");
                String untrimmedNodeName = exceptionTokens[7];
                String nodeName = untrimmedNodeName.substring(1, untrimmedNodeName.indexOf("'."));//What does this do?
                
                ve = handleMissingAttribute(error,missingAttributeName, Utilities.stripNamespace(nodeName, hmlNamespace));
            }
            //If there is an error with formatting of the hml consider it fatal and reject the file
            else
            {

                for(int i =cvcOrNumberCheck(exceptionTokens[0]); i<exceptionTokens.length;i++)
                {
                    error+=" "+exceptionTokens[i];
                }
                    
                ve=new ValidationResult(error,Severity.FATAL);
                
                ve.setSolutionText("Verify that your HML file is well formed, and conforms to the chosen hml version");
                ve.setMiringRule("reject");
            }

            Utilities.addValidationError(validationErrors, ve);
        }

        /**
         * Create a ValidationResult object based on a missing attribute.
         *
         * @param missingAttributeName the name of the missing attribute
         * @param nodeName the name of the element missing the attribute.
         * 
         * @return a ValidationResult object for this missing attribute
         */
        private static ValidationResult handleMissingAttribute(String error, String missingAttributeName, String nodeName)
        {
            String errorMessage = error+" The node " + nodeName + " is missing a " + missingAttributeName + " attribute.";
            String solutionText = "Please add a " + missingAttributeName + " attribute to the " + nodeName + " node.";
            ValidationResult ve = new ValidationResult(errorMessage,Severity.FATAL);
            
            //Specific logic for various MIRING errors
            try
            {
                boolean matchFound = false;
                NodeList ruleNodes = missingAttributeTemplates.getElementsByTagName("rule");
                for(int i = 0; i < ruleNodes.getLength(); i++)
                {
                    NamedNodeMap ruleAttributes = ruleNodes.item(i).getAttributes();

                    String templateNodeName = Utilities.getAttribute(ruleAttributes, "node-name");
                    String templateAttributeName = Utilities.getAttribute(ruleAttributes, "attribute-name");
                    
                    if(missingAttributeName.equals(templateAttributeName)
                        && nodeName.equals(templateNodeName))
                    {
                        matchFound = true;
                        String miringRule = Utilities.getAttribute(ruleAttributes, "miring-rule-id");
                        String templateSeverity = Utilities.getAttribute(ruleAttributes, "severity");
                        String templateSolution = Utilities.getAttribute(ruleAttributes, "solution-text");
                        
                        Severity severity = 
                            templateSeverity.equals("fatal")?Severity.FATAL:
                            templateSeverity.equals("miring")?Severity.MIRING:
                            templateSeverity.equals("warning")?Severity.WARNING:
                            templateSeverity.equals("info")?Severity.INFO:
                            Severity.FATAL;
                        
                        ve =  new ValidationResult(errorMessage,severity);
                        ve.setSolutionText(templateSolution==null ? solutionText : solutionText + " " + templateSolution);
                        ve.setMiringRule(miringRule);
                        
                        break;
                    }
                }
                if(!matchFound)
                {
                    throw new Exception("Missing attribute name not handled!: " + nodeName + ":" + missingAttributeName);
                }
            }
            catch(Exception e)
            {
                logger.error("Exception during handleMissingAttribute" , e);
            }
            
            String xPath = xmlCurrentNode.generateXpath();
            //For some reason missing attribute exceptions are thrown BEFORE the parser hits the startElement method for the parent node.  
            //startElement is hit AFTER the attributes for the node are checked
            //So right now, the parent node isn't contained in the xmlCurrentNode structure.  We need to add the most recent parent to this xpath.
            int nodeIndex = 1;
            for(int i = 0; i < xmlCurrentNode.childrenNodes.size(); i++)
            {
                SimpleXmlModel currentNode = xmlCurrentNode.childrenNodes.get(i);
                if(currentNode.nodeName.equals(nodeName))
                {
                    //If the parent has other children, then this one's not the first.
                    nodeIndex++;
                }
            }
            
            xPath = xPath + "/" + nodeName + "[" + nodeIndex + "]";
            ve.addXPath(xPath);
            
            return ve;
        }

        /**
         * Create a ValidationResult object based on a missing node
         *
         * @param missingnodeName the name of the missing node
         * 
         * @return a ValidationResult object for this missing node
         */
        private static ValidationResult handleMissingNode(String error,String missingNodeName)
        {
            String parentNodeName = "Unhandled ParentNodeName";

            parentNodeName = xmlCurrentNode.nodeName;
            if(parentNodeName.isEmpty())
            {
                logger.error("No parent node found for missingNodeName=" + missingNodeName);
            }

            String errorMessage = error+" There is a missing " + missingNodeName + " node underneath the " + parentNodeName + " node.";
            String solutionText = "Please add one " + missingNodeName + " node underneath the " + parentNodeName + " node. If it exists, please make sure the placement is in accordance of HML 1.0.1.";
            ValidationResult ve = new ValidationResult(errorMessage,Severity.FATAL);
            
            //Specific logic for various MIRING errors
            try
            {
                boolean matchFound = false;
                NodeList ruleNodes = missingNodeTemplates.getElementsByTagName("rule");
                for(int i = 0; i < ruleNodes.getLength(); i++)
                {
                    NamedNodeMap ruleAttributes = ruleNodes.item(i).getAttributes();
                    
                    String templateNodeName = Utilities.getAttribute(ruleAttributes, "node-name");
                    if(missingNodeName.equals(templateNodeName))
                    {
                        matchFound = true;
                        
                        String miringRule = Utilities.getAttribute(ruleAttributes, "miring-rule-id");
                        String templateSolution = Utilities.getAttribute(ruleAttributes, "solution-text");
                        
                        String templateSeverity = Utilities.getAttribute(ruleAttributes, "severity");
                        Severity severity = 
                            templateSeverity.equals("fatal")?Severity.FATAL:
                            templateSeverity.equals("miring")?Severity.MIRING:
                            templateSeverity.equals("warning")?Severity.WARNING:
                            templateSeverity.equals("info")?Severity.INFO:
                            Severity.FATAL;
                        
                        ve =  new ValidationResult(errorMessage,severity);
                        ve.setSolutionText(templateSolution==null ? solutionText : solutionText + " " + templateSolution);
                        ve.setMiringRule(miringRule);
                        
                        break;
                    }
                }
                if(!matchFound)
                {
                    throw new Exception("Missing node name not handled!: " + missingNodeName);
                }
            }
            catch(Exception e)
            {
                logger.error("Exception during handleMissingNode.",e);
            }
            
            String xPath = xmlCurrentNode.generateXpath();
            ve.addXPath(xPath);

            return ve;
        }

        /**
         * Clean up the SimpleXmlModels 
         * 
         * I'm not sure if this is necessary.  Since SimpleXmlModel objects refer to both a parent and children,
         * garbage collector might not flag them for destruction when the SchemaValidator object is de-referenced.
         * Just want to make sure we don't have extra objects hanging around.
         */
        private static void clearModel()
        {
            if(xmlRootNode != null)
            {
                xmlRootNode.deAllocate(1);
                xmlRootNode = null;
            }
            
            if(xmlCurrentNode != null)
            {
                xmlCurrentNode.deAllocate(1);
                xmlCurrentNode = null;
            }
            nodeCount = 0;
        }
        private static int cvcOrNumberCheck(String exceptionName)
        {
            logger.debug("ExceptionName= "+exceptionName.substring(0,3));
            if(exceptionName.substring(0,3).equals("cvc")){
                return 1;
            }
            else
            {
                return 0;
            }
        }

    }
    //Is there any methods I need to make it HML specfic or is this okay with pretty much just a vairable name change and deletions here and there
    private static class HMLValidationContentHandler extends DefaultHandler
      {
      	//xmlRootNode represents the root node of the xml document, which is a
        //skeleton representation of the document, built recursively during the sax parse
        //This SimpleXmlModel is used to generate an xpath on the report
        public static SimpleXmlModel xmlRootNode;
        public static SimpleXmlModel xmlCurrentNode;
        public static int nodeCount = 0;
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException 
        {
            nodeCount++;
            //logger.debug("NODE COUNT: " + nodeCount + " NAME: " + localName + " ATTRIBUTES: " + Utilities.getAttributes(attributes));
            
            try
            {
                if(localName.equals("sample"))//??
                {
                    //Every time we start analyzing a sample, store it's ID, for reporting purposes
                    String sampleID = attributes.getValue("id");
                    String centerCode = attributes.getValue("center-code");
                    samples.add(new Sample(sampleID,centerCode));
                }
                
                if(xmlRootNode==null)
                {//Can stay the same-Anu
                    //This is the new root node.
                    xmlRootNode = new SimpleXmlModel(localName,1);
                    xmlCurrentNode = xmlRootNode;
                }
                else//Stay same?-Anu Needs explaining, why do we care about the child and parent nodes
                {
                    //There is a root node already.  This must be a child node.
                    int highestChildIndex = xmlCurrentNode.getHighestChildIndex(localName);
                    SimpleXmlModel newCurrentNode = new SimpleXmlModel(localName , highestChildIndex + 1);
                    xmlCurrentNode.addChildNode(newCurrentNode);
                    xmlCurrentNode = newCurrentNode;
                }
            }
            catch(Exception e)
            {
                logger.error("Exception in startElement",e);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException 
        {
            try
            {
                if(xmlCurrentNode.parentNode != null)
                {
                    //If the parent node *IS* null, that means we're closing out the root HML element.  All done.  
                    //Otherwise zoom out the parser to the parent
                    xmlCurrentNode = xmlCurrentNode.parentNode;
                }
            }
            catch(Exception e)
            {
                logger.error("Exception in endElement",e);
            }
        }
        //warning(), error(), and fatalError() are overrides which are triggered by 
        //parser warnings, errors and fatal errors.  
        //Here we provide simple handlers for the three.
        @Override
        public void warning(SAXParseException exception) throws SAXException 
        {
            logger.debug("Sax Parser Warning: " + exception.getMessage());
            handleParserException(exception, Severity.WARNING);
        }
    
        @Override
        public void error(SAXParseException exception) throws SAXException 
        {
            logger.debug("Sax Parser NonFatal Error: " + exception.getMessage());
            handleParserException(exception, Severity.INFO);
        }
    
        @Override
        public void fatalError(SAXParseException exception) throws SAXException 
        {
            logger.debug("Sax Parser Fatal Error: " + exception.getMessage());
            handleParserException(exception,Severity.FATAL);
        }
          
          

    
        /**
         * Translate a SaxException object into a useful MiringResult
         * 
         * I'm calling this method when we get a legitimate SAX Parser exception, which are triggered
         * When the parser finds a problem with the xml.
         * Take the SAX parser exception, tokenize it, and build a Miring-specific MiringResult object based on the errors.
         *
         * @param exception a SaxException containing schema validation information
         */
        private static void handleParserException(SAXParseException exception, Severity severity)
        {
 
            ValidationResult ve = new ValidationResult(exception.getMessage(),severity);
            
            String errorMessage = exception.getMessage();
            String[] exceptionTokens = Utilities.tokenizeString(errorMessage, " ");
            String error="["+Integer.toString(exception.getLineNumber())+","+Integer.toString(exception.getColumnNumber())+"]";
            
            if(errorMessage.equals("Content is not allowed in prolog."))
            {
                ve = new ValidationResult("Content is not allowed in prolog.",Severity.FATAL);
                ve.setSolutionText("This most likely means that there is some text before the initial xml node begins.  Get rid of it and try again." );
            }
            //MISSING NODE
            else if(exceptionTokens[0].equals("cvc-complex-type.2.4.a:") || exceptionTokens[0].equals("cvc-complex-type.2.4.b:"))
            {
                // This cvc-complex-type is called if there is a node missing.  here's a few examples of what the exception.getMessage() can look like
                // cvc-complex-type.2.4.a: Invalid content was found starting with element 'sample'. One of '{"http://schemas.nmdp.org/spec/hml/1.0.1":property, "http://schemas.nmdp.org/spec/hml/1.0.1":hmlid}' is expected.
                // cvc-complex-type.2.4.a: Invalid content was found starting with element 'sample'. One of '{"http://schemas.nmdp.org/spec/hml/1.0.1":reporting-center}' is expected.
                // cvc-complex-type.2.4.a: Invalid content was found starting with element 'reporting-center'. One of '{"http://schemas.nmdp.org/spec/hml/1.0.1":property, "http://schemas.nmdp.org/spec/hml/1.0.1":hmlid}' is expected.
                // cvc-complex-type.2.4.b: The content of element 'sbt-ngs' is not complete. One of '{"http://schemas.nmdp.org/spec/hml/1.0.1":property, "http://schemas.nmdp.org/spec/hml/1.0.1":raw-reads}' is expected.
                // for 2.4.b, it's interesting that it says the content of element 'sbt-ngs'.  That's the real parent of the node where it's missing.  Perhaps I can use that info, maybe it's useless.
                // Format seems better defined here:  https://wiki.xmldation.com/Support/Validator/cvc-complex-type-2-4-a
                // If I'm getting wrong node names, better look into the format of this error.
                
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
                ve = handleMissingNode(error,missingNodeName);
            }
            //MISSING ATTRIBUTE
            else if(exceptionTokens[0].equals("cvc-complex-type.4:"))
            {
                //This cvc-complex-type is called if there is an attribute missing from a node
                //It looks like this:
                // cvc-complex-type.4: Attribute 'quality-score' must appear on element 'variant'.
                // https://wiki.xmldation.com/Support/Validator/cvc-complex-type-4
                
                String missingAttributeName = exceptionTokens[2].replace("'", "");
                String untrimmedNodeName = exceptionTokens[7];
                String nodeName = untrimmedNodeName.substring(1, untrimmedNodeName.indexOf("'."));
                
                ve = handleMissingAttribute(error,missingAttributeName, Utilities.stripNamespace(nodeName, hmlNamespace));
            }
            //If there HML is not well formed reject the file and return the error and how to fix it
            else
            {
                for(int i =cvcOrNumberCheck(exceptionTokens[0]); i<exceptionTokens.length;i++)
                {
                    error+=" "+exceptionTokens[i];
                }
                
                ve=new ValidationResult(error,Severity.HMLFATAL);
                ve.setSolutionText("Verify that your HML file is well formed, and conforms to the chosen hml version");
                ve.setMiringRule("reject");
            }
           
            Utilities.addValidationError(validationErrors, ve);
            
            
        }

        /**
         * Create a ValidationResult object based on a missing attribute.
         *
         * @param missingAttributeName the name of the missing attribute
         * @param nodeName the name of the element missing the attribute.
         * 
         * @return a ValidationResult object for this missing attribute
         */
        private static ValidationResult handleMissingAttribute(String error,String missingAttributeName, String nodeName)
        {
            String errorMessage = error+" The node " + nodeName + " is missing a " + missingAttributeName + " attribute.";
            String solutionText = "Please add a " + missingAttributeName + " attribute to the " + nodeName + " node.";
            ValidationResult ve = new ValidationResult(errorMessage,Severity.HMLFATAL);
            
            //Specific logic for various HML errors
            try
            {
                boolean matchFound = false;
                NodeList ruleNodes = missingAttributeTemplates.getElementsByTagName("hml-rule");
                for(int i = 0; i < ruleNodes.getLength(); i++)
                {
                    NamedNodeMap ruleAttributes = ruleNodes.item(i).getAttributes();

                    String templateNodeName = Utilities.getAttribute(ruleAttributes, "node-name");
                    String templateAttributeName = Utilities.getAttribute(ruleAttributes, "attribute");
                    
                    if(missingAttributeName.equals(templateAttributeName)
                        && nodeName.equals(templateNodeName))
                    {
                        matchFound = true;
                        String templateSeverity = Utilities.getAttribute(ruleAttributes, "severity");
                        String templateSolution = Utilities.getAttribute(ruleAttributes, "solution-text");
                        
                        Severity severity = 
                            templateSeverity.equals("fatal")?Severity.HMLFATAL:
                            templateSeverity.equals("hml")?Severity.HML:
                            templateSeverity.equals("warning")?Severity.HMLWARNING:
                            templateSeverity.equals("info")?Severity.INFO:
                            Severity.FATAL;
                        
                        ve =  new ValidationResult(errorMessage,severity);
                        ve.setSolutionText(templateSolution==null ? solutionText : solutionText + " " + templateSolution);
                        ve.setMiringRule("HML");
                        
                        break;
                    }
                }
                if(!matchFound)
                {
                    throw new Exception("Missing attribute name not handled!: " + nodeName + ":" + missingAttributeName+" Check Miring Validation Node");
                }
            }
            catch(Exception e)
            {
                logger.error("Exception during handleMissingAttribute" , e);
            }
            
            String xPath = xmlCurrentNode.generateXpath();
            //For some reason missing attribute exceptions are thrown BEFORE the parser hits the startElement method for the parent node.  
            //startElement is hit AFTER the attributes for the node are checked
            //So right now, the parent node isn't contained in the xmlCurrentNode structure.  We need to add the most recent parent to this xpath.
            int nodeIndex = 1;
            for(int i = 0; i < xmlCurrentNode.childrenNodes.size(); i++)
            {
                SimpleXmlModel currentNode = xmlCurrentNode.childrenNodes.get(i);
                if(currentNode.nodeName.equals(nodeName))
                {
                    //If the parent has other children, then this one's not the first.
                    nodeIndex++;
                }
            }
            
            xPath = xPath + "/" + nodeName + "[" + nodeIndex + "]";
            ve.addXPath(xPath);
            
            return ve;
        }

        /**
         * Create a ValidationResult object based on a missing node
         *
         * @param missingnodeName the name of the missing node
         * 
         * @return a ValidationResult object for this missing node
         */
        private static ValidationResult handleMissingNode(String error,String missingNodeName)
        {

            String parentNodeName = "Unhandled ParentNodeName";
            
            parentNodeName = xmlCurrentNode.nodeName;
            if(parentNodeName.isEmpty())
            {
                logger.error("No parent node found for missingNodeName=" + missingNodeName);
            }
            
            String errorMessage = error+" There is a missing " + missingNodeName + " node underneath the " + parentNodeName + " node.";
            String solutionText = "Please add one " + missingNodeName + " node underneath the " + parentNodeName + " node. If it exists, please make sure the placement is in accordance of HML 1.0.1.";
            ValidationResult ve = new ValidationResult(errorMessage,Severity.HMLFATAL);
            boolean matchFound = false;
            //Specific logic for various HML errors
            try
            {
                
                NodeList ruleNodes = missingNodeTemplates.getElementsByTagName("hml-rule");
                
                for(int i = 0; i < ruleNodes.getLength(); i++)
                {
                    NamedNodeMap ruleAttributes = ruleNodes.item(i).getAttributes();
                    
                    String templateNodeName = Utilities.getAttribute(ruleAttributes, "node-name");
                    if(missingNodeName.equals(templateNodeName))
                    {
                        matchFound = true;

                        String templateSolution = Utilities.getAttribute(ruleAttributes, "solution-text");
                        
                        String templateSeverity = Utilities.getAttribute(ruleAttributes, "severity");
    
                        Severity severity = 
                            templateSeverity.equals("fatal")?Severity.HMLFATAL:
                            templateSeverity.equals("hml")?Severity.HML:
                            templateSeverity.equals("warning")?Severity.HMLWARNING:
                            templateSeverity.equals("info")?Severity.INFO:
                            Severity.HMLFATAL;
                        
                        ve =  new ValidationResult(errorMessage,severity);
                        ve.setSolutionText(templateSolution==null ? solutionText : solutionText + " " + templateSolution);
                        ve.setMiringRule("HML");
                        
                        break;
                    }
                }
                if(!matchFound)
                {
                    throw new Exception("Missing node name not handled!: " + missingNodeName+"May be node Miring Tier 1 Validation");
                }
            }
            catch(Exception e)
            {
                logger.error("Exception during handleMissingNode.",e);
            }
            if(!matchFound)
            {
                ve.setMiringRule("Node");
            }
            String xPath = xmlCurrentNode.generateXpath();
            ve.addXPath(xPath);

            return ve;
        }

        /**
         * Clean up the SimpleXmlModels 
         * 
         * I'm not sure if this is necessary.  Since SimpleXmlModel objects refer to both a parent and children,
         * garbage collector might not flag them for destruction when the SchemaValidator object is de-referenced.
         * Just want to make sure we don't have extra objects hanging around.
         */
        private static void clearModel()
        {
            if(xmlRootNode != null)
            {
                xmlRootNode.deAllocate(1);
                xmlRootNode = null;
            }
            
            if(xmlCurrentNode != null)
            {
                xmlCurrentNode.deAllocate(1);
                xmlCurrentNode = null;
            }
            nodeCount = 0;
        }
          private static int cvcOrNumberCheck(String exceptionName)
          {
              logger.debug("ExceptionName= "+exceptionName.substring(0,3));
              if(exceptionName.substring(0,3).equals("cvc")){
                  return 1;
              }
              else
              {
                  return 0;
              }
          }
        }
     
}