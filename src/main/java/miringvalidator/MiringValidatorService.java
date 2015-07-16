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

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import main.java.miringvalidator.ValidationResult.Severity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * This class provides a REST service to access the MIRING Validator.  
 * It expects a POST request with a single form parameter called xml.
*/
@Path("/ValidateMiring")
public class MiringValidatorService
{
    Logger logger = LoggerFactory.getLogger(MiringValidatorService.class);
    
    /**
     * This method provides a RESTFUL service for validating a MIRING compliant HML file
     *
     * @param xml this method accepts a Form Parameter "xml" containing the xml text
     * @return a String containing MIRING Results Report in XML format.
     */
    @POST
    @Produces("application/xml")
    public String validateMiring(@FormParam("xml") String xml)
    {
        System.out.println("Web Service Call Recieved.  ");
        //System.out.println("XML length=" + xml==null?"NULL":(xml.length() + " : " + xml.substring(0,20) + " ... " + xml.substring(xml.length()-20, xml.length())));
        logger.debug( "Received web service call.");
        //logger.debug("The exact text of the variable 'xml' is between the curly braces: \n{" + xml + "}\n");
        
        if(xml == null)
        {
            logger.error("XML is Null.");
            return ReportGenerator.generateReport(new ValidationResult[]{new ValidationResult("XML is null.",Severity.FATAL)}, null, null,null,null);
        }
        else if(xml.length() == 0)
        {
            logger.error("XML is Empty.");
            return ReportGenerator.generateReport(new ValidationResult[]{new ValidationResult("XML is length 0.",Severity.FATAL)}, null, null,null,null);
        }
        else
        {
            logger.debug("XML Length = " + xml.length());

            MiringValidator myValidator = new MiringValidator(xml);
            myValidator.validate();

            String report = myValidator.getReport();
            return report;
        }
    }
}
