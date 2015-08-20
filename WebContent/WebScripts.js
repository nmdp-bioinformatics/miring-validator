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

/*
    These are the scripts used by index.html to present a simple validation interface.
 */

function downloadResults()
{
    xml = document.getElementById("resultsText").value;
    download("MiringResultsReport.xml",xml);
}

function download(filename, text) 
{
    var element = document.createElement('a');
    element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(text));
    element.setAttribute('download', filename);

    element.style.display = 'none';
    document.body.appendChild(element);

    element.click();

    document.body.removeChild(element);
}

function click(el) 
{
    // Simulate click on the hidden file element.
    var evt = document.createEvent('Event');
    evt.initEvent('click', true, true);
    el.dispatchEvent(evt);
}

document.querySelector('#fileSelectButton').addEventListener('click', function(e) 
{
    //When we click the browse button, instead click the hidden file element.
    var fileInput = document.querySelector('#fileInput');
    fileInput.click(); 
}, false);

function readSingleFile(fileElement) 
{
    //Read the file from the hidden file element, put it's text in the input field.
    var f = fileElement.files[0]; 
    if (f)
    {
        var r = new FileReader();
        r.onload = function(e) 
        {
            var contents = e.target.result;
            document.getElementById("inputText").value = contents;
            callRestService();
        }
        r.readAsText(f);
    } 
    else 
    { 
        alert("Failed to load file");
    }
}

function loadSample()
{
    var request = window.location.href + "validator/ValidateMiring/";
    //alert("the request location is: " + request);
  
    var results = $.get(request,
        function(response)
        {
            //alert("This is called if there was a successful request.  Storing the response in the right text box.");
            var resultXml = new XMLSerializer().serializeToString(response);
            resultXml = decodeURIComponent(resultXml);
            //alert(String(resultXml));
            document.getElementById("inputText").value = resultXml;
            
            callRestService();
        })
        .done(function() 
        {
            //alert( "Function was completed successfully." );
        })
        .fail(function() 
        {
            alert( "Error.  Something wrong happened.");
            alert("request = " + request);
        })
        .always(function() 
        {
            //alert( "Finished Attempt.  This should always be called after success or failure." );
        }
    );
}

function callRestService() 
{
    var request = window.location.href + "validator/ValidateMiring/";
    //alert("the request location is: " + request);
    var xmlText = document.getElementById("inputText").value;
    //alert("xml = " + xmlText);
  
    var results = $.post(request,
        {xml:xmlText},
        function(response)
        {
            //alert("This is called if there was a successful request.  Storing the response in the right text box.");
            var resultXml = new XMLSerializer().serializeToString(response);
            resultXml = decodeURIComponent(resultXml);
            //alert(String(resultXml));
            document.getElementById("resultsText").value = resultXml;
            
            if(isMiringCompliant(resultXml))
            {
                //alert("MIRING Compliant.");
                document.getElementById("greenCheck").style.display = 'block';
                document.getElementById("redX").style.display = 'none';
            }
            else
            {
                //alert("Not MIRING Compliant.");
                document.getElementById("greenCheck").style.display = 'none';
                document.getElementById("redX").style.display = 'block';
            }
            
        })
        .done(function() 
        {
            //alert( "Function was completed successfully." );
        })
        .fail(function() 
        {
            alert( "Error.  Something wrong happened.");
            alert("request = " + request);
        })
        .always(function() 
        {
            //alert( "Finished Attempt.  This should always be called after success or failure." );
        }
    );
}

function clearText()
{
    document.getElementById("resultsText").value = "";
    document.getElementById("inputText").value = "";
    document.getElementById("greenCheck").style.display = 'none';
    document.getElementById("redX").style.display = 'none';
}

function isMiringCompliant(xml)
{
    //Using simple string operations to pull a "true" or "false" from the report
    compliantBooleanBeginLocation = xml.indexOf("<miring-compliant>") + 18;
    compliantBooleanEndLocation = xml.indexOf("</miring-compliant>");
    compliantBoolean = xml.substring(compliantBooleanBeginLocation, compliantBooleanEndLocation);
    //alert(compliantBoolean);
    
    if(compliantBoolean == "true" )
    {
        return true;
    }
    else if (compliantBoolean == "false")
    {
        return false;
    }
    else
    {
        alert("Error determining MIRING Compliance.");
        return false;
    }
}

