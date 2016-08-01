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
    These are the scripts used by index.html and MoreInfo.html to present a simple validation interface.
 */

function printRuleTable()
{
    htmlText = "";
    
    getFileFromServer("rules/Rules.csv", function(text) 
    {
        //This is the callback function for the web request.
        //This function should really only be executed if the request was a success.
        //Generate some table HTML from the rules csv and put it in the iframe element.
        if (text === null) 
        {
            alert("Problem getting Rules.csv from the server.  Text is null.");
        }
        else 
        {
            var lines = text.split(/\r|\r?\n/g);

            htmlText+="<table border=\"1\">";
            for(lineIndex = 0; lineIndex < lines.length; lineIndex++)
            {
                htmlText+="<tr>";
                
                line = lines[lineIndex];
                var tokens = line.split(",");
                
                for (tokenInd = 0; tokenInd < tokens.length; tokenInd++) 
                {
                    //If it's a table header
                    if(lineIndex==0)
                    {
                        htmlText+="<th>" + tokens[tokenInd] + "</th>";
                    }
                    else
                    {
                        htmlText+="<td>" + tokens[tokenInd] + "</td>";
                    }
                }
                htmlText+="</tr>";
            }
            htmlText+="</table>";

            document.getElementById('ruleTable').contentWindow.document.write(htmlText);
        }
    });
}

//AJAX to get a file from the server.
//http request status 200 = "OK"
//readyState of 4 = "The request is complete."
function getFileFromServer(url, doneCallback)
{
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
    //This is how users submit a file using the browse button.
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
    fileElement.files[0]=null;
    r=null;
    contents=null;
    f=null;
    
}

function loadSample()
{
    //load some sample HML and validate it.
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
    var version=document.getElementById("versionNumber").value;
    var xmlplusversion=[xmlText,version];
    if(version==0)
    {
        alert("Please select a version number");
    }
    else{
    //alert("xml = " + xmlText);
  
    var results = $.post(request,
        {xml:xmlplusversion},
        function(response)
        {
            //alert("This is called if there was a successful request.  Storing the response in the right text box.");
            var resultXml = new XMLSerializer().serializeToString(response);
            resultXml = decodeURIComponent(resultXml);
            document.getElementById("resultsText").value = resultXml;
            
            if(isMiringCompliant(resultXml)=="warnings")
            {
                document.getElementById("yellowCheck").style.display='block'
                         document.getElementById("greenCheck").style.display = 'none';
                         document.getElementById("redX").style.display = 'none';
                         document.getElementById("reject").style.display='none';
            }
            else if(isMiringCompliant(resultXml)=="true")
            {
                //alert("MIRING Compliant.");
                document.getElementById("greenCheck").style.display = 'block';
                document.getElementById("redX").style.display = 'none';
                         document.getElementById("reject").style.display='none';
                         document.getElementById("yellowCheck").style.display='block'
            }
            else
            {
                //alert("Not MIRING Compliant.");
                document.getElementById("greenCheck").style.display = 'none';
                document.getElementById("redX").style.display = 'block';
                         document.getElementById("reject").style.display='none';
                         document.getElementById("yellowCheck").style.display='block'
            }
            if(isHMLCompliant(resultXml)=="reject")
            {
                         document.getElementById("reject").style.display='block';
                         document.getElementById("greenCheck").style.display = 'none';
                         document.getElementById("redX").style.display = 'none';
                         document.getElementById("HMLcheck").style.display='none';
                         document.getElementById("HMLX").style.display='none';
           
            }
            else if (isHMLCompliant(resultXml)=="false")
            {
                //Not HML Compliant
                document.getElementById("HMLcheck").style.display='none';
                document.getElementById("HMLX").style.display='block';
                         document.getElementById("reject").style.display='none';
            }
                         else{
                         //HML Compliant
                         document.getElementById("HMLcheck").style.display='block';
                         document.getElementById("HMLX").style.display='none';
                         document.getElementById("reject").style.display='none';

                         }
            
        })
        .done(function() 
        {
            //alert( "Function was completed successfully." );
        })
        .fail(function(response)
        {
         //Alerts user about critical server errors due to the file
            alert( "Error.  Something wrong happened: Check in results text area ");
              clearText();
              document.getElementById("resultsText").value = "Please contact the System Admin: bioinformatics-web@nmdp.org in your message attach your hml and note your version number.             \n"+ response.responseText.replace(/{(.*?)}|<(.*?)>/g,"").replace(/^.*com.*$/gm,"").replace(/^.*org.*$/gm,"").replace(/\r?\n|\r/gm," ");
        })
        .always(function() 
        {
            //alert( "Finished Attempt.  This should always be called after success or failure." );
        }
    );
    }
}

function clearText()
{
    document.getElementById("resultsText").value = "";
    document.getElementById("inputText").value = "";
    document.getElementById("greenCheck").style.display = 'none';
    document.getElementById("redX").style.display = 'none';
    document.getElementById("HMLcheck").style.display='none';
    document.getElementById("HMLX").style.display='none';
    document.getElementById("reject").style.display='none';
    document.getElementById("yellowCheck").style.display='none';
   
}

function isHMLCompliant(xml)
{
    //Simple string operations to find true, false, or warn user if hml was rejected due to critical hml formatting error
    compliantBooleanBeginLocation = xml.indexOf("<hml-compliant>") + 15;
    compliantBooleanEndLocation = xml.indexOf("</hml-compliant>");
    compliantBoolean = xml.substring(compliantBooleanBeginLocation, compliantBooleanEndLocation);
    
    if(compliantBoolean == "true")
    {
        return "true";
    }
    else if (compliantBoolean == "false")
    {
        return "false";
    }
    else if(compliantBoolean == "reject")
    {
        alert("HML Rejected, Please Check Below For Reason");
        return "reject";
    }
    else
    {
        alert("Error determining HML Compliance.");
        return "false";
    }
    return "false";
}

function isMiringCompliant(xml)
{
    //Using simple string operations to pull a "true" or "false" from the report
    compliantBooleanBeginLocation = xml.indexOf("<miring-compliant>") + 18;
    compliantBooleanEndLocation = xml.indexOf("</miring-compliant>");
    compliantBoolean = xml.substring(compliantBooleanBeginLocation, compliantBooleanEndLocation);
    
    if(compliantBoolean == "true" )
    {
        return "true";
    }
    else if (compliantBoolean == "false")
    {
        return "false";
    }
    else if (compliantBoolean == "reject")
    {
        return "reject";
    }
    else if(compliantBoolean == "warnings")
    {
        return "warnings";
    }
    else
    {
        alert("Error determining MIRING Compliance.");
        return false;
    }
    return false;
}

