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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * A simple class for storing a lightweight skeleton of an XML document.
 * 
 * It can also generate an Xpath for a given element in the document.
 * 
*/
public class SimpleXmlModel
{
    public String nodeName;
    public int nodeIndex;
    public List<SimpleXmlModel> childrenNodes = new ArrayList<SimpleXmlModel>();
    public SimpleXmlModel parentNode;
    Logger logger = LoggerFactory.getLogger(SimpleXmlModel.class);
    
    public SimpleXmlModel(String nodeName, int nodeIndex)
    {
        this(nodeName);
        this.nodeIndex = nodeIndex;
    }
    
    public SimpleXmlModel(String nodeName)
    {
        this();
        this.nodeName = nodeName;
    }
    
    public SimpleXmlModel()
    {
        this.nodeName = "";
        this.nodeIndex = 1;
        this.parentNode = null;
    }
    
    public void addChildNode(SimpleXmlModel childModel)
    {
        childModel.parentNode=this;
        this.childrenNodes.add(childModel);
    }
    
    /**
     * Of an element's children, get the highest index of a child with the name childName.
     * 
     * This is used for xpath construction
     *
     * @param childName a child element's name
     * @return a 1-based integer of an element's index, for used in xpath construction
     */
    public int getHighestChildIndex(String childName)
    {
        int highestIndex = 0;
        try
        {
            for(int i = 0; i < childrenNodes.size(); i++)
            {
                SimpleXmlModel childNode = childrenNodes.get(i);
                if(childNode.nodeName.equals(childName) && childNode.nodeIndex > highestIndex)
                {
                    highestIndex = childNode.nodeIndex;
                }
            }
        }
        catch(Exception e)
        {
            logger.error("Error in getHighestChildIndex",e);
        }            
        return highestIndex;
    }
    
    /**
     * Generate an Xpath for a specific element.
     * 
     * This method recurses back through it's ancestor elements to generate an xpath.
     *
     * @return an xPath specifying the location of this element
     */
    public String generateXpath()
    {
        try
        {
            //Recursively build an xPath for the current node.
            String currentXPath = "/" + nodeName + "[" + nodeIndex + "]";
            
            if(parentNode == null)
            {
                return currentXPath;
            }
            else
            {
                return parentNode.generateXpath() + currentXPath;
            }
        }
        catch(Exception e)
        {
            logger.error("Error during generateXpath()", e);
        }
        return null;
    }
    
    /**
     * Deallocate the xml model, as well as all of it's children recursively.
     *
     * @param recursionDepth how far we have recursed so far to generate this xpath.  For interest's sake only, this isn't useful.
     * @return an array of ValidationError objects found during validation
     */
    public void deAllocate(int recursionDepth)
    {
        //logger.debug("Recursion Depth in deAllocate" + recursionDepth);
        try
        {
            nodeName = null;
            nodeIndex = 0;
            parentNode = null;
            
            if(childrenNodes != null)
            {
                while(!childrenNodes.isEmpty())
                {
                    SimpleXmlModel temp = childrenNodes.remove(childrenNodes.size() - 1);
                    temp.deAllocate(recursionDepth + 1);
                    temp = null;
                }
                childrenNodes = null;
            }
        }
        catch(Exception e)
        {
            logger.error("Error during deAllocate()",e);
        }
    }
}
