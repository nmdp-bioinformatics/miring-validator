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

function printRuleTable()
{
    //alert("inside printrule");
    htmlText = "";
    
    getFileFromServer("rules/Rules.csv", function(text) 
    {
        //This is the callback function for the web request.
        //This function should really only be executed if the request was a success.
        if (text === null) 
        {
            alert("Problem getting Rules.csv from the server.  Text is null.");
        }
        else 
        {
            
            //alert("text: " + text);
            var lines = text.split(/\r|\r?\n/g);
            
            //alert("ruletext: " + ruleText);
            //alert("lineCount: " + lines.length);

            htmlText+="<table>";
            for(lineIndex = 0; lineIndex < lines.length; lineIndex++)
            {
                htmlText+="<tr>";
                
                line = lines[lineIndex];
                var tokens = line.split(",");
                
                htmlText+="<td>SMAMPLME</td>";
                for (tokenInd = 0; tokenInd < tokens.length; tokenInd++) 
                {
                    htmlText+="<td>" + tokens[tokenInd] + "</td>";
                }
                
                htmlText+="</tr>";
            }
            htmlText+="</table>";

            //alert("Setting Rule Table html to : " + htmlText);
            document.getElementById('ruleTable').contentWindow.document.write(htmlText);
            //return htmlText;
        }
    });
}

//AJAX to get a file from the server.
//http request status 200 = "OK"
//readyState of 4 = "The request is complete."
function getFileFromServer(url, doneCallback)
{
    alert("Inside getFileFromServer");
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = handleStateChange;
    xhr.open("GET", url, true);
    xhr.send();

    function handleStateChange() 
    {
        //State changed.  Is the request completed?
        if (xhr.readyState === 4) 
        {
            //Call the callback method, passing in response text
            doneCallback(xhr.status == 200 ? xhr.responseText : null);
        }
    }
}

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
            callValidatorService();
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
    getFileFromServer("hml/hml_1_0_1_example_miring.xml", function(text) 
    {
        if (text === null) 
        {
            alert("Problem getting hml_1_0_1_example_miring.xml from the server");
        }
        else 
        {
            sampleXML = text;
            
            document.getElementById("inputText").value = sampleXML;
            callValidatorService();
        }
    });
}

function callValidatorService() 
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

