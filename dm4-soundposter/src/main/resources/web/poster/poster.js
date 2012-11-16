
var host = "http://localhost:8080"

var poster = new function () {

    this.historyApiSupported = window.history.pushState

    this.initializeByPath = function() {

        // handling deep links
        var url = window.location.href.substr()
        var path = url.split("/")

        var profileAlias = path[path.length - 2]
        var posterAlias = path[path.length - 1]
        var loaded = undefined

        console.log("requested profile " + profileAlias + " and poster " + posterAlias);
        try {
            loaded = request("GET", "/poster/" + profileAlias + "/" + posterAlias)
        } catch (err) {
            loaded = undefined
            console.log(err)
        }
        return loaded
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
                // if (LOG_AJAX_REQUESTS) dm4c.log("..... " + jq_xhr.status + " " + jq_xhr.statusText +
                   //  "\n..... " + JSON.stringify(data))
                if (callback) {
                    callback(data)
                }
                response_data = data
            },
            error: function(jq_xhr, text_status, error_thrown) {
                // if (LOG_AJAX_REQUESTS) dm4c.log("..... " + jq_xhr.status + " " + jq_xhr.statusText +
                   //  "\n..... exception: " + JSON.stringify(error_thrown))
                throw "RESTClientError: " + method + " request failed (" + text_status + ": " + error_thrown + ")"
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
