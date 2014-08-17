
var site = new function () {

    var player_is_ready = false
    var now_playing = false
    var url_set = ""
    var playback_set = false
    var $player = undefined

    this.initialize_player = function () {

        $player = $("#soundsystem").jPlayer({
            ready: function () {
                player_is_ready = true
            },
            timeupdate: function(event) {
                // todo: write play-button and animate with event.jPlayer.status.currentPercentRelative
                // $('.timer').text($.jPlayer.convertTime(event.jPlayer.status.currentTime))
                $('.timer').text($.jPlayer.convertTime(event.jPlayer.status.duration))
            },
            play: function(event) {
                // keep state current
                now_playing = true
                var $play = $('#play-button')
                    $play.attr('src', '/com.soundposter.website/images/logos/SP_Logo_pause_71bbe2.png')
                // some gui manipulation
                // ..
            },
            loadstart: function(event) {
                // console.log("Loadstart, seekPercent = " + event.jPlayer.status.seekPercent)
            },
            loadeddata: function(event) {
                // do something smart with console.log(event.jPlayer.status)
            },
            pause: function(event) {
                var $play = $('#play-button')
                    $play.attr('src', '/com.soundposter.website/images/logos/SP_Logo_mit_dreieck_ffffff.png')
                // keep state current
                now_playing = false
                // fixme: jQuery('.map-info').html("♪ " + sp.selected_track.value)
            },
            ended: function(event) {
                // keep state current
                now_playing = false
                url_set = undefined
                // keep on playing
                // poster.play_next_track()
            },
            error: function(event) {
                // keep state current
                now_playing = false
                url_set = undefined
                console.log("jPlayer:error playing stream... ")
                // if (piwikTracker != undefined) piwikTracker.trackGoal(2)
                //
                if (event.jPlayer.error.type == "e_url_not_set") {
                    console.log("ERROR: Sound-Resource is not yet set => " + poster.url_set)
                } else if (event.jPlayer.error.type == "e_url") {
                    console.log('ERROR: An error occured during requesting media source url.')
                    /** poster.show_modal_notification("We cannot access the media anymore, probably the sound was moved. "
                        + "And if you're connected to the internet, this sound will be marked for review for the author"
                        + " of this soundposter.<br/>") **/
                    setTimeout(function() {
                        // if user did not react to error-message, we skip to the next track now
                        if (!now_playing) {
                            // poster.play_next_track()
                            // poster.show_notification("Skipping to next track")
                        }
                    }, 10000)
                } else if (event.jPlayer.error.type == "e_flash") {
                    console.log('ERROR: An error occured during flash-playback of media file.')
                } else if (event.jPlayer.error.type == "e_flash_disabled") {
                    poster.show_modal_notification("It looks like Adobe Flash is disabled. Please activate it.")
                } else if (event.jPlayer.error.type == "e_no_support") {
                    console.log('WARNING: Browser does not support any format supplied.')
                } else if (event.jPlayer.error.type == "e_no_solution") {
                    console.log('WARNING: Browser does not offer a solution for the compressed audio-format supplied.')
                } else if (event.jPlayer.error.type == "e_version") {
                    console.log('ERROR: JS/Flash Version Mismatch')
                } else {
                    console.log('ERROR: An unknown error occured with our jPlayer setup. Please mail the following ' +
                        'error code to "' + event.jPlayer.error.type + '" mail@soundposter.com. And thank you for '
                        + 'helping our software to get better!')
                }
                // some more gui related clean-up (e.g. hide timer)
                // $('.timer').hide()
            // backup, unselection of highlighted track
            },
            swfPath: "/com.soundposter.website/script/vendor/jquery.player/",
            supplied: "mp3",
            solution: "html, flash",
            errorAlerts: false
        });

    }

    this.set_resource = function (path) {
        url_set = path
    }

    this.interactive_frontpage = function () {
        //
        var $play = $('#play-button')
            $play.hover(function () {
                // $play.attr('src', '/com.soundposter.website/images/logos/SP_Logo_mit_dreieck_40b0e2.png')
                if (now_playing) {
                    $play.attr('src', '/com.soundposter.website/images/logos/SP_Logo_pause_71bbe2.png')
                } else {
                    $play.attr('src', '/com.soundposter.website/images/logos/SP_Logo_mit_dreieck_71bbe2.png')
                }
            }, function () {
                if (now_playing) {
                    $play.attr('src', '/com.soundposter.website/images/logos/SP_Logo_pause_71bbe2.png')
                } else {
                    if (playback_set) {
                        $play.attr('src', '/com.soundposter.website/images/logos/SP_Logo_mit_dreieck_71bbe2.png')
                    } else {
                        $play.attr('src', '/com.soundposter.website/images/logos/SP_Logo_mit_dreieck_ffffff.png')
                    }
                } // if (i could do good code :==))
            })
            $play.click(function () {
                if (!now_playing) {
                    if (!playback_set) {
                        if (url_set) {
                            $player.jPlayer("setMedia", {mp3: url_set})
                            $player.jPlayer("play")
                            playback_set = true
                        }
                    } else {
                        $player.jPlayer("play")
                    }
                } else {
                    $player.jPlayer("pause")
                }
            })
    }

     this.interactive_browse_page = function () {
        //
        var $home = $('.side-panel img.home')
            $home.hover(function () {
                $home.attr('src', '/com.soundposter.website/images/logos/SP_Logo_ohne_dreieck_555555.png')
            }, function () {
                $home.attr('src', '/com.soundposter.website/images/logos/SP_Logo_ohne_dreieck_ffffff.png')
            })

        var $dates = $('.poster-info .last-modified')
        //
        var $elements = $dates
        for (var $date_element in $elements) {
            var date_element = $elements[$date_element]
            var date_value = date_element.innerHTML
            if (typeof date_value !== "undefined") {
                var unix_timestamp = parseFloat(date_value.substr(14))
                date_element.innerHTML = 'Last modified ' + new Date(unix_timestamp)
            }
        }
    }

    this.interactive_page_title = function () {
        //
        var $home = $('.title img.home')
            $home.hover(function () {
                $home.attr('src', '/com.soundposter.website/images/logos/SP_Logo_ohne_dreieck_555555.png')
            }, function () {
                $home.attr('src', '/com.soundposter.website/images/logos/SP_Logo_ohne_dreieck_ffffff.png')
            })
    }

    this.add_soundcloud_track = function (trackId) {
        var added = request("GET", "/soundcloud/add/track/" + trackId)
        console.log(added)
        if (added == null) {
            var $span = $('<span>&nbsp;Track already known</span>')
            $('a#' + trackId).append($span)
            $span.fadeOut(1200)
        } else if (added.hasOwnProperty("type_uri")) {
            var $span = $('<span>&nbsp;Successfully added track</span>')
            $('a#' + trackId).append($span)
            $span.fadeOut(1200)
        }
    }

    this.add_soundcloud_set = function (setId) {
        var added = request("GET", "/soundcloud/add/set/" + setId)
        console.log(added)
        if (added == null) {
            var $span = $('<span>&nbsp;Set already known</span>')
            $('a#' + setId).append($span)
            $span.fadeOut(1200)
        } else if (added.hasOwnProperty("type_uri")) {
            var $span = $('s<pan>&nbsp;Successfully added set</span>')
            $('a#' + setId).append($span)
            $span.fadeOut(1200)
        }
    }

    this.show_band_discography = function (bandId) {
        var added = request("GET", "/bandcamp/view/discography/" + bandId)
        console.log(added)
        /** if (added == null) {
            var $span = $('<span>&nbsp;Track already known</span>')
            $('a#' + trackId).append($span)
            $span.fadeOut(1200)
        } else if (added.hasOwnProperty("type_uri")) {
            var $span = $('<span>&nbsp;Successfully added track</span>')
            $('a#' + trackId).append($span)
            $span.fadeOut(1200)
        } **/
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