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

document.getElementById("hmlInputFile").addEventListener("change", 
    function()
    {
        readSingleFile(document.getElementById("hmlInputFile"));
    }
);

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
}

function readSingleFile(fileElement) 
{
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
