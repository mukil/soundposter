
var host = "http://new.soundposter.com"

var site = new function () {
    
    var coverPoster = undefined

    this.historyApiSupported = window.history.pushState

    /** soundposter poster View application controler **/

    this.initView = function () {

        // handling deep links for our website
        var entryUrl = window.location.href
        var commandingUrl = entryUrl.substr(entryUrl.indexOf("#") + 1)
        var commands = commandingUrl.split("/")
        var website = commands[0]
        var websiteView = commands[1]

        console.log("requested " + website + " and website-view" + websiteView);

        site.getAndSetRandomSoundposter()

    }
    
    this.showSignupInterestDialog = function () {
        console.log($("#sign-up-dialog"))
        if ($("#sign-up-dialog").length == 0) {
            var dialog = "<div id=\"sign-up-dialog\"><form id=\"add-me\" action=\"javascript:site.signUpInterest()\">"
                + "<input type=\"text\" name=\"mailbox\" placeholder=\"Enter your e-mail address, "
                + "so we can get back to you..\" class=\"mailfield\"></input>"
                + "<input type=\"submit\" class=\"submit btn\" value=\"Ok, do so\"></input>"
                + "</form></div>"
            $("#header").append(dialog)
            $(".btn.sign-up").css("border-bottom", "3px solid #5b7b95;")
        } else {
            site.removeSignupInterestDialog()
        }
    }
    
    this.removeSignupInterestDialog = function () {
        $("#sign-up-dialog").remove()
        $(".btn.sign-up").css("", "")
    }

    this.signUpInterest = function () {
        var mailbox = $("[name=mailbox]").val()
        var name = "x"
        console.log("signing up " + mailbox)
        request("GET", "/create/signup/" + mailbox + "/" + name)
        
        site.removeSignupInterestDialog()
    }
    
    this.contactUs = function () {
        console.log("user wants to contact us..")
    }
    
    this.countPageView = function () {
        
    }
    
    this.checkIfMobileBrowser = function () {
        
    }
    
    this.getAndSetRandomSoundposter = function () {
        coverPoster = request("GET", "/poster/random")
        if (coverPoster != undefined) {
            var address = host + site.getFullSoundposterURL(coverPoster.id).url
            var cover = "<iframe src=\""+ address +"\" frameBorder=\"0\" style=\"border: 0px solid #333;\""
                +" width=\"480px\" height=\"582px\" id=\"posterframe\">Browser not compatible.</iframe>"
                +" <br/><br/>Watch the selected soundposter <a href=\""+ address +"\">in full size</a></iframe>."
           $("#cover").html(cover)
           $("#footer").html("<a href=\"http://twitter.com/soundposter\">subscribe @soundposter</a>")
        }
    }
    
    this.getFullSoundposterURL = function (posterId) {
        return request("GET", "/poster/url/" + posterId)
    }

    this.getAndSetMobilePreviewImage = function () {
        // 
    }
    
    /**
     * Sends an AJAX request.
     *
     * @param   method              The HTTP method: "GET", "POST", "PUT", "DELETE".
     * @patam   uri                 The request URI.
     * @param   data                Optional: the data to be sent to the server (an object). By default the data object
     *                              is serialized to JSON format. Note: key/value pairs with undefined values are not
     *                              serialized.
     *                              To use an alternate format set the Content-Type header (see "headers" parameter).
     * @param   callback            Optional: the function to be called if the request is successful. One argument is
     *                              passed: the data returned from the server.
     *                              If not specified, the request is send synchronously.
     * @param   headers             Optional: a map of additional header key/value pairs to send along with the request.
     * @param   response_data_type  Optional: affects the "Accept" header to be sent and controls the post-processing
     *                              of the response data. 2 possible values:
     *                                  "json" - the response data is parsed into a JavaScript object. The default.
     *                                  "text" - the response data is returned as is.
     * @param   is_absolute_uri     If true, the URI is interpreted as relative to the DeepaMehta core service URI.
     *                              If false, the URI is interpreted as an absolute URI.
     *
     * @return  For successful synchronous requests: the data returned from the server. Otherwise undefined.
     */
    function request(method, uri, data, callback, headers) {
        var async = callback != undefined
        var status          // used only for synchronous request: "success" if request was successful
        var response_data   // used only for synchronous successful request: the response data (response body)
        //
        // if (LOG_AJAX_REQUESTS) dm4c.log(method + " " + uri + "\n..... " + JSON.stringify(data))
        //
        var content_type = "application/json"
        data = JSON.stringify(data)
        //
        $.ajax({
            type: method,
            url: uri,
            contentType: content_type,
            headers: headers,
            data: data,
            dataType: "json",
            processData: false,
            async: async,
            success: function(data, text_status, jq_xhr) {
                if (callback) callback(data)
                response_data = data
            },
            error: function(jq_xhr, text_status, error_thrown) {
                throw {"status": text_status, "code": error_thrown}
            },
            complete: function(jq_xhr, text_status) {
                status = text_status
            }
        })
        if (!async && status == "success") {
            return response_data
        }
    }

}