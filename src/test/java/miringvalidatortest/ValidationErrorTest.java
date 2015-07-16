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
package test.java.miringvalidatortest;

import static org.junit.Assert.*;
import main.java.miringvalidator.ValidationResult;
import main.java.miringvalidator.ValidationResult.Severity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

public class ValidationErrorTest
{
    Logger logger = LoggerFactory.getLogger(ValidationErrorTest.class);
    
    @Test
    public void testValidationErrors()
    {
        logger.debug("starting testValidationErrors");
        
        ValidationResult firstError= new ValidationResult("There is a problem with the HML",Severity.FATAL);
        firstError.setMiringRule("1.3.4.d");
        firstError.setSolutionText("Fix the data please.  It's obnoxious and obtrusive.");
        firstError.addXPath("Xpath of the ValidationError");
        
        ValidationResult secondError = new ValidationResult("There is a problem with the HML",Severity.FATAL);
        secondError.setMiringRule("1.3.4.d");
        secondError.setSolutionText("Fix the data please.  It's obnoxious and obtrusive.");
        secondError.addXPath("Xpath of the ValidationError");
        
        ValidationResult thirdError = new ValidationResult("A third, improved error description",Severity.FATAL);
        thirdError.setMiringRule("1.3.4.e");
        
        assertTrue(firstError.getMiringRule().equals("1.3.4.d"));
        
        //equals compares the description, solution, miring rule, severity, xpath, and moreinformation
        //compareTo compares only the MiringRule.  
        //This has implications when sorting and comparing ValidationErrors with eachother.
        assertTrue(firstError.equals(secondError));
        assertFalse(firstError.equals(thirdError));
        
        assertTrue("1.3.4.d".compareTo("1.3.4.d") == 0);
        assertTrue("1.3.4.e".compareTo("1.3.4.d") > 0);
        assertTrue("1.3.4.d".compareTo("1.3.4.e") < 0);
        
        assertTrue(firstError.compareTo(secondError) == 0);
        assertTrue( firstError.getMiringRule().compareTo(thirdError.getMiringRule())  < 0);
        assertTrue(firstError.compareTo(thirdError) < 0);
        
        assertFalse(thirdError.compareTo(firstError) < 0);
    }

}
