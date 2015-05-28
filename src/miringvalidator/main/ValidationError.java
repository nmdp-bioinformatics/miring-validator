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

public class ValidationError
{
    String errorText;
    String solutionText;
    String miringElement;
    boolean fatal;
    
    /**
     * Constructor for a ValidationError object. A ValidationError has getters and setters and doesn't do much else.
     *
     * @param errorText Text containing a description of the error.
     * @param solutionText Text containing suggestions of a solution   
     * @param miringElement Text specifying the specific Miring Rule broken
     * @param fatal is the error considered fatal?  Should we reject the Miring HML?
     */

    public ValidationError(String errorText, String solutionText, String miringElement, boolean fatal)
    {
        this.errorText = errorText;
        this.solutionText = solutionText;
        this.miringElement = miringElement;
        this.fatal = fatal;
    }
    
    public String getErrorText()
    {
        return errorText;
    }

    public String getSolutionText()
    {
        return solutionText;
    }
    
    public String getMiringElement()
    {
        return miringElement;
    }

    public void setSolutionText(String solutionText)
    {
        this.solutionText = solutionText;
    }

    public boolean isFatal()
    {
        return fatal;
    }
}
