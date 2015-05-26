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

//import java.util.Properties;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

//import org.apache.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;

@Path("/ValidateMiring")
public class MiringValidatorService
{
    /*static Logger logger = Logger.getLogger( MiringValidatorService.class );
    private final static String PROPERTY_LOGLVL = "property://probatron.org/log-level";
    private final static String DEFAULT_LOGLVL = "WARN";
    
    static
    {
        // set up log message format, etc.
        String logLvl = System.getProperty( PROPERTY_LOGLVL ) == null ? DEFAULT_LOGLVL : System
                .getProperty( PROPERTY_LOGLVL );
        Properties p = new Properties();
        p.setProperty( "log4j.rootCategory", logLvl + ",stderr" );
        p.setProperty( "log4j.appender.stderr", "org.apache.log4j.ConsoleAppender" );
        p.setProperty( "log4j.appender.stderr.layout", "org.apache.log4j.PatternLayout" );
        p.setProperty( "log4j.appender.stderr.target", "System.err" );
        p.setProperty( "log4j.appender.stderr.layout.ConversionPattern", "%p %m%n" );
        PropertyConfigurator.configure( p );
    }
*/
    
    @POST
    @Produces("application/xml")
    public String validateMiring(@FormParam("xml") String xml)
    {
        //logger.debug( "Received web service call with xml :\n" + xml);
        //logger.error("ERROR!!!!!");
        MiringValidator myValidator = new MiringValidator(xml);
        myValidator.validate();

        String report = myValidator.getReport();
        return report;
    }
}
