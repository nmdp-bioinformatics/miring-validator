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