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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class SimpleXmlModel
{
    public String nodeName;
    public int nodeIndex;
    public List<SimpleXmlModel> childrenNodes = new ArrayList<SimpleXmlModel>();
    public SimpleXmlModel parentNode;
    public String attributes;
    private static final Logger logger = LogManager.getLogger(SimpleXmlModel.class);
    
    public SimpleXmlModel(String nodeName, int nodeIndex, String attributes)
    {
        this(nodeName);
        this.nodeIndex = nodeIndex;
        this.attributes = attributes;
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
            logger.error("Error in getHighestChildIndex: " + e);
        }            
        return highestIndex;
    }
    
    public String generateXpath()
    {
        try
        {
            //Recursively build a xPath for the current node.
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
            logger.error("Error during generateXpath(): " + e);
        }
        return null;
    }
}
