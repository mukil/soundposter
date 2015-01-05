
var site = new function () {

    var player_is_ready = false
    var now_playing = false
    var url_set = ""
    var playback_set = false
    var $player = undefined
    
    // 
    
    var r
    
    var bufferingArc = undefined
    var playingArc = undefined
    
    var ringAnimation = undefined
    var bufferAnimation = undefined
    
    this.show_sign_up_view = function () {
        var $hello = $("#hello")
            $hello.hide()
        // 
        $('a.intro-btn').removeClass("selected")
        $('a.signup-btn').addClass("selected")

        var $body = $(".new-main")
            $body.empty()
            $body.append('<h3 class="title" >Sign up for your username</h3>')
        
        var $main = $('<div class="content">')
            $main.append('<p>'
                + 'To start bookmarking sound streams with soundposter.com we need to <br/>'
                + '- know your desired username and <br/>'
                + '- check your mailbox.'
                + '</p>')
            $body.append($main)
        
    }
    
    this.show_intro_view = function () {
        var $hello = $("#hello")
            $hello.hide()
        // 
        $('a.signup-btn').removeClass("selected")
        $('a.intro-btn').addClass("selected")
        
        var $body = $(".new-main")
            $body.empty()
            $body.append('<h2 class="title" >Intro to soundposter.com</h2>')
        var $main = $('<div class="content">')
        var $list = $("<ol>")
            $list.append("<li>Sign up for your username - The simplest of all</li>")
            $list.append("<li>Bookmark streams you find on the web - Create a set of sounds</li>")
            $list.append("<li>Design and upload your soundposter graphic - The hardest of all</li>")
            $list.append("<li>Provide a name, some description and your license info - Typing in the details</li>")
            $list.append("<li>Publish your soundposter - Sharing it worldwide</li>")
            // 
            $main.append('<p>Soundposters have many more options as here described. For example, one can easily turn your soundposter '
            + ' into a fully-fledged audible &amp; mobile festival guide with interactive specials! But let us leave that for sometime '
            + 'soon and go on with the basics, if we get those right, nothing will stop your soundposter from evolving into a super-huge '
            + 'audible graphic landscape.</p>')
            $main.append('<h3>To create &amp; publish your own soundposter you need to</h3>')
            $main.append($list)
            $main.append('<p>All of this is very easy if you got your own sounds and graphics already, or friends providing those.</p>')
            
        var $footer = $('<div class="content">')
            $footer.append("<p>The two more creative parts in here are</p>")
            $footer.append('<h3>Reference streams from the web</h3>')
            $footer.append("<p>....</p>")
            // 
            $footer.append('<h3>Create and upload your soundposter graphic</h3>')
            $footer.append("<p>...</p>")
            $footer.append('<p>The important part left then is<br/>')
            $footer.append('<h3>The legal stuff</h3>'
            + '- check the copyrights for your graphic and choose a license for it<br/>'
            + '- choose a license for your soundposter (your set of tracks and the graphic)</p>')
            $body.append($main).append($footer)
    }
    
    this.show_hello_view = function () {
        
    }
    
    this.render_player_controls = function () {
        
        r = Raphael("playback-control", 355, 290)
        r.customAttributes.arc = function (xloc, yloc, value, total, R) {
            var alpha = 360 / total * value,
                a = (90 - alpha) * Math.PI / 180,
                x = xloc + R * Math.cos(a),
                y = yloc - R * Math.sin(a),
                path;
            if (total === value) {
                path = [
                    ["M", xloc, yloc - R],
                    ["A", R, R, 0, 1, 1, xloc - 0.01, yloc - R]
                ];
            } else {
                path = [
                    ["M", xloc, yloc - R],
                    ["A", R, R, 0, +(alpha > 180), 1, x, y]
                ];
            }
            return {
                path: path
            };
        };
        
        var logo = r.image("/com.soundposter.website/images/logos/SP_Logo_backgrounded_ffffff_play_512.jpg", 50, 15, 256, 256);
            logo.attr("cursor", "pointer")
            
            logo.click(function () {
   
                console.log("Clicked main control .. ")
                
                if (!now_playing) {
                    if (!playback_set) {
                        if (url_set) {
                            // ### move svg init 
                            site.setupBufferingAnimation()
                            site.setupPlaybackAnimation()
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
            // 
            logo.hover(function() { // in
                if (now_playing) {
                    logo.attr("src", "/com.soundposter.website/images/logos/SP_Logo_backgrounded_71bbe2_pause_512.jpg");
                } else {
                    logo.attr("src", "/com.soundposter.website/images/logos/SP_Logo_backgrounded_71bbe2_play_512.jpg");
                }
            }, function () { // out
                if (now_playing) {
                    logo.attr("src", "/com.soundposter.website/images/logos/SP_Logo_backgrounded_ffffff_pause_512.jpg");
                } else {
                    logo.attr("src", "/com.soundposter.website/images/logos/SP_Logo_backgrounded_ffffff_play_512.jpg");
                }
            })
            
        /** var prev = r.image("/com.soundposter.website/images/prev-skip.png", 0, 123, 30, 35);
            prev.attr("cursor", "pointer")
            prev.attr("opacity", .1)
            prev.hover(function () { prev.attr("opacity", 1) }, function () { prev.attr("opacity", .1) })
            prev.click(function (e) {
                // load prev track
            })
            
        var fwd = r.image("/com.soundposter.website/images/fwd-skip.png", 325, 123, 30, 35);
            fwd.attr("cursor", "pointer")
            fwd.attr("opacity", .1)
            fwd.hover(function () { fwd.attr("opacity", 1) }, function () { fwd.attr("opacity", .11) })
            fwd.click(function(e) {
                // 
                // load next track
            })
            **/
            
    }
                
    this.setupPlaybackAnimation = function () {
        playingArc = r.path().attr({
            "stroke": "#FFB900", "stroke-width": 3, "opacity": 1,
            "stroke-linecap": "square"
        })
        console.log("New Animation: Playback Ring was added...")
    }
        
    this.updatePlaybackAnimation = function (value, duration) {
        playingArc.animate({
            arc: [177, 143, value, duration, 126]
        }, 750, "linear")
    }
    
    this.setupBufferingAnimation = function () {
        bufferingArc = r.path().attr({
            "stroke": "#fff", "stroke-width": 10, "opacity": .9,
            arc: [177, 143, 40, 100, 135]
        }).animate({
            arc: [177, 143, 100, 100, 135]
        }, 1200, "bounce")
        bufferingArc.click(function (e) {
            // 
            console.log("Clicked to seek ..", e)
        })
        console.log("New Animation: Seek Ring was added...")
    }

    this.initialize_player = function () {

        $player = $("#soundsystem").jPlayer({
            ready: function () {
                player_is_ready = true
            },
            timeupdate: function(event) {
                // todo: write play-button and animate with event.jPlayer.status.currentPercentRelative
                // $('.timer').text($.jPlayer.convertTime(event.jPlayer.status.currentTime))
                // $('.timer').text($.jPlayer.convertTime(event.jPlayer.status.duration))
                var duration = event.jPlayer.status.duration
                var time = event.jPlayer.status.currentTime
                site.updatePlaybackAnimation(time, duration)
            },
            play: function(event) {
                // keep state current
                now_playing = true
                // var $play = $('#play-button')
                   //  $play.attr('src', '/com.soundposter.website/images/logos/SP_Logo_pause_71bbe2.png')
                // some gui manipulation
                // ..
            },
            loadstart: function(event) {
                console.log("Started partial loading", event.jPlayer.status)
            },
            loadeddata: function(event) {
                // do something smart with console.log(event.jPlayer.status)
                console.log("Finished partial loading", event.jPlayer.status)
                // var duration = event.jPlayer.status.duration;
                
            },
            pause: function(event) {
                /// var $play = $('#play-button')
                    // $play.attr('src', '/com.soundposter.website/images/logos/SP_Logo_mit_dreieck_ffffff.png')
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
                // gui update
                // $('#play-button').attr('src', '/com.soundposter.website/images/logos/SP_Logo_mit_dreieck_ffffff.png')
                url_set = undefined
                console.log("jPlayer:error playing stream... ")
                // if (piwikTracker != undefined) piwikTracker.trackGoal(2)
                //
                if (event.jPlayer.error.type === "e_url_not_set") {
                    console.log("ERROR: Sound-Resource is not yet set => " + url_set)
                } else if (event.jPlayer.error.type === "e_url") {
                    console.log('ERROR: An error occured during requesting media source url.')
                    /** poster.show_modal_notification("We cannot access the media anymore, probably the sound was moved. "
                        + "And if you're connected to the internet, this sound will be marked for review for the author"
                        + " of this soundposter.<br/>")
                    setTimeout(function() {
                        // if user did not react to error-message, we skip to the next track now
                        if (!now_playing) {
                            // poster.play_next_track()
                            // poster.show_notification("Skipping to next track")
                        }
                    }, 10000) **/
                } else if (event.jPlayer.error.type === "e_flash") {
                    console.warn('ERROR: An error occured during flash-playback of media file.')
                } else if (event.jPlayer.error.type === "e_flash_disabled") {
                    console.warn("It looks like Adobe Flash is disabled. Please activate it.")
                } else if (event.jPlayer.error.type === "e_no_support") {
                    console.log('WARNING: Browser does not support any format supplied.')
                } else if (event.jPlayer.error.type === "e_no_solution") {
                    console.warn('WARNING: Browser does not offer a solution for the compressed audio-format supplied.')
                } else if (event.jPlayer.error.type === "e_version") {
                    console.log('ERROR: JS/Flash Version Mismatch')
                } else {
                    console.warn('ERROR: An unknown error occured with our jPlayer setup. Please mail the following ' +
                        'error code to "' + event.jPlayer.error.type + '" mail@soundposter.com. And thank you for '
                        + 'helping our software to get better!')
                }
                // some more gui related clean-up (e.g. hide timer)
                // $('.timer').hide()
            // backup, unselection of highlighted track
            },
            swfPath: "/com.soundposter.website/script/vendor/jquery.player/2.9.2/",
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
                    $play.attr('src', '/com.soundposter.website/images/logos/SP_Logo_backgrounded_71bbe2_pause_512.jpg')
                } else {
                    $play.attr('src', '/com.soundposter.website/images/logos/SP_Logo_backgrounded_71bbe2_play_512.jpg')
                }
            }, function () {
                if (now_playing) {
                    $play.attr('src', '/com.soundposter.website/images/logos/SP_Logo_backgrounded_71bbe2_pause_512.jpg')
                } else {
                    if (playback_set) {
                        $play.attr('src', '/com.soundposter.website/images/logos/SP_Logo_backgrounded_71bbe2_play_512.jpg')
                    } else {
                        $play.attr('src', '/com.soundposter.website/images/logos/SP_Logo_backgrounded_ffffff_play_512.jpg')
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
                $home.attr('src', '/com.soundposter.website/images/logos/SP_Logo_ohne_dreieck_555555_blank_grounded.jpg')
            }, function () {
                $home.attr('src', '/com.soundposter.website/images/logos/SP_Logo_ohne_dreieck_ffffff_blank_grounded.jpg')
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
                $home.attr('src', '/com.soundposter.website/images/logos/SP_Logo_ohne_dreieck_555555_blank_grounded.jpg')
            }, function () {
                $home.attr('src', '/com.soundposter.website/images/logos/SP_Logo_ohne_dreieck_ffffff_blank_grounded.jpg')
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