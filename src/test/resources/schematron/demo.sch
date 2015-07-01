<?xml version="1.0" encoding="utf-8"?>
<!-- 

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

 -->

<schema xmlns="http://purl.oclc.org/dsdl/schematron">
    <ns prefix="hml" uri="http://schemas.nmdp.org/spec/hml/1.0.1" />
    
    <pattern name="Reference Sequence Start and End">    
        <rule context="hml:reference-sequence">
        
            <assert test="@start">Reference Sequence node must include a @start attribute.</assert>
            
            <assert test="@end">Reference Sequence node must include a @end attribute.</assert>
            
            <assert test="number(@start) = '0'">start attribute on reference-sequence nodes should be 0.</assert>
            
            <assert test="number(@end) >= number(@start)">end attribute should be greater than or equal to the start attribute.</assert>

        </rule>        
    </pattern>
        
</schema>