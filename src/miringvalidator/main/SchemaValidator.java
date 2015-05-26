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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

import org.xml.sax.SAXException;

public class SchemaValidator
{
    public static ValidationError[] validate(String xml) 
    {
        List<ValidationError> validationErrors = new ArrayList<ValidationError>();
        Source xmlFile = null;
        try 
        {
            //URL schemaFile = new URL("http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd");
            //How do I load an xsd more dynamically?  Put it in the web.xml probably.
            //I need to add a config file for this.  This won't work anywhere else.
            File schemaFile = new File("/Users/bmatern/GitHub/MiringValidator/resources/schema/MiringTier1.xsd");
            
            //xmlFile = new StreamSource(new File("/Users/bmatern/GitHub/MiringValidator/xmlresources/test/missing-hmlid.xml"));
            xmlFile = new StreamSource(new java.io.StringReader(xml));
            
            SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            
            validator.validate(xmlFile);
            //System.out.println(xmlFile.getSystemId() + " is valid");
        } 
        catch (SAXException e) 
        {
            //So here we are, doing validation by catching exceptions.  This is sloppy but works for the prototype.  
            //I would very much like to be more specific with these errors.
            //The reason should be more descriptive.  And should correspond to the rule.  
            //How to do this?  Dan's Schema validator is a bit more robust than mine.  
            
            //System.out.println(xmlFile.getSystemId() + " is NOT valid");
            //System.out.println("Reason: " + e.getLocalizedMessage());
            
            validationErrors.add(new ValidationError(
                    e.getLocalizedMessage()
                    ,"Verify your HML has exactly one hmlid node."
                    ,"Miring Element 1.1"
                    ,true)
            );
        }
        catch (Exception e)
        {
            System.out.println("Exception during schema validation: " + e.getLocalizedMessage());
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

}
