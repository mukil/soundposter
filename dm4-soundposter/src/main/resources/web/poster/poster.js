
var host = "http://localhost:8080"
var STATUS_INTERNAL = "Internal Server Error"
var STATUS_NOT_FOUND = "Not Found"
var STATUS_ACCESS_DENIED = "Unauthorized"

var poster = new function () {

    this.historyApiSupported = window.history.pushState
    this.mod = undefined
    
    this.initializeByPath = function() {

        // handling deep links
        var url = window.location.href.substr()
        var path = url.split("/")

        var profileAlias = path[path.length - 2]
        var posterAlias = path[path.length - 1]
        var loaded = undefined

        try {
            loaded = request("GET", "/poster/" + profileAlias + "/" + posterAlias)
        } catch (err) {
            loaded = undefined
            if (err.code == STATUS_ACCESS_DENIED) {
                // console.log("Soundposter not published, access denied.")
                poster.renderFullMessage("The delicate soundposter you requested was not yet published world wide "
                    + "by its creator.")
            } else if (err.code == STATUS_NOT_FOUND) {
                // console.log("Soundposter for profile not found.")
                poster.renderFullMessage("It looks like something is wrong with the letters in the "
                    + "address bar of your browser. We don't know of any soundposter und this web-address.")
            } else if (err.code == STATUS_INTERNAL) {
                // console.log("Internal Server Error.")
                poster.renderFullMessage("Oops, upside your head, we say Ooops inside *our* head!<br/>"
                    + "Something went wrong, we' just send ourselves a report of how we broke things up and will"
                    + "fix this stage as soon as possible.<br/>")
            }
        }
        
        poster.mod = Modernizr
        console.log(poster.mod)
        
        return loaded
    }
    
    this.initialize = function(profile, posterAlias) {

        var loaded = undefined

        try {
            loaded = request("GET", "/poster/" + profile + "/" + posterAlias)
        } catch (err) {
            loaded = undefined
            if (err.code == STATUS_ACCESS_DENIED) {
                // console.log("Soundposter not published, access denied.")
                poster.renderFullMessage("The delicate soundposter you requested was not yet published world wide "
                    + "by its creator.")
            } else if (err.code == STATUS_NOT_FOUND) {
                // console.log("Soundposter for profile not found.")
                poster.renderFullMessage("It looks like something is wrong with the letters in the "
                    + "address bar of your browser. We don't know of any soundposter und this web-address.")
            } else if (err.code == STATUS_INTERNAL) {
                // console.log("Internal Server Error.")
                poster.renderFullMessage("Oops, upside your head, we say Ooops inside *our* head!<br/>"
                    + "Something went wrong, we' just send ourselves a report of how we broke things up and will"
                    + "fix this stage as soon as possible.<br/>")
            }
        }
        
        return loaded
    }

    this.noWay = function () {
        window.location.href = host
    }
    
    this.renderFullMessage = function (message) {
        $(".map-start").remove()
        // render these messages also in mobile style, where there's no sp-bar
        $("#sp-bar").html("<div class=\"error-message\"><br/>"+ message +"<br/><br/>"
            + "<span><a href=\""+ host +"\" class=\"btn ok\">Ok, no problem.</a></span>"
            + "<span><a href=\"javascript:poster.noWay()\" class=\"btn no-way\">Are you kidding me!?</a></span></div>")
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
