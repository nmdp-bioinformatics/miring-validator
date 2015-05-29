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

public class ValidationError
{
    String errorText;
    String solutionText;
    String miringRule;
    String xPath;
    boolean fatal;
    
    /**
     * Constructor for a ValidationError object. A ValidationError has getters and setters and doesn't do much else.
     *
     * @param errorText Text containing a description of the error.
     * @param fatal is the error considered fatal?  Should we reject the Miring HML?
     */
    public ValidationError(String errorText, boolean fatal)
    {
        this.errorText = errorText;
        this.fatal = fatal;
        this.solutionText = null;
        this.miringRule = null;
    }
    
    public String getErrorText()
    {
        return errorText;
    }

    public String getSolutionText()
    {
        return solutionText;
    }
    
    public String getMiringRule()
    {
        return miringRule;
    }

    public void setSolutionText(String solutionText)
    {
        this.solutionText = solutionText;
    }

    public boolean isFatal()
    {
        return fatal;
    }
    
    public String getxPath()
    {
        return xPath;
    }

    public void setxPath(String xPath)
    {
        this.xPath = xPath;
    }

    public void setErrorText(String errorText)
    {
        this.errorText = errorText;
    }

    public void setMiringRule(String miringRule)
    {
        this.miringRule = miringRule;
    }
}
