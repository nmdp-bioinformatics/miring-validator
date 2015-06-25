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

//import java.util.Properties;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import main.java.miringvalidator.ValidationResult.Severity;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

@Path("/ValidateMiring")
public class MiringValidatorService
{
    private static final Logger logger = LogManager.getLogger(MiringValidatorService.class);
    
    /**
     * This method provides a RESTFUL service for validating a MIRING compliant HML file
     *
     * @param xml this method accepts a Form Parameter xml containing the xml text
     * @return a String containing MIRING Results Report
     */
    @POST
    @Produces("application/xml")
    public String validateMiring(@FormParam("xml") String xml)
    {
        logger.debug( "Received web service call.");
        
        if(xml == null || xml.length() == 0)
        {
            logger.error("XML is Null or Empty.");
            return ReportGenerator.generateReport(new ValidationResult[]{new ValidationResult("XML is null or length 0.",Severity.FATAL)}, null, null,null);
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
