
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



    // --- The 4 AJAX Single Pages ---

    this.show_sign_up_view = function () {
        var $hello = $("#hello")
            $hello.hide()
        // 
        $('a.intro-btn').removeClass("selected")
        $('a.plans-btn').removeClass("selected")
        $('a.signup-btn').addClass("selected")

        var $body = $(".main-area")
            $body.empty()
            $body.append('<h3 class="title" >Sign up<h3>')
        
        var $main = $('<div class="content">')
            $main.append('<h3>Hi</h3>'
            + '<p>'
                + 'Since we do not want to leave anyone back disappointed we cannot really hand out user-accounts yet. This is also because the <a href="/legal">legal</a> issues we face if we open up our tool for the public.'
            + '</p>'
            + '<p>'
                + 'If you are interested to use this service on an experimental base, please leave us your contact-information so we can get in touc with you and support you to do so.'
            + '</p>'
            + '<form id="newsletter" action="/register" method="GET">'
                + '<input type="text" class="text type" name="name" placeholder="Your name"/><br/>'
                + '<input type="text" class="text type" name="mailbox" placeholder="Your e-mail"/><br/>'
                + '<textarea class="message type" name="message" rows="6" placeholder="Leave us a message"></textarea><br/>'
                + '<input type="submit" class="submit" name="Ok" value="OK"/>'
            + '</form>'
            + '<p>'
                + 'We surely will use your contact-information for no other purpose than informing you about the proceedings of soundposter.com.'
            + '</p>')
            /** $main.append('<p>'
                + 'To start bookmarking sound streams with soundposter.com we need to <br/>'
                + '- know your desired username and <br/>'
                + '- check your mailbox.'
                + '</p>') **/
            $body.append($main)
        
    }
    
    this.show_intro_view = function () {
        var $hello = $("#hello")
            $hello.hide()
        // 
        $('a.signup-btn').removeClass("selected")
        $('a.plans-btn').removeClass("selected")
        $('a.intro-btn').addClass("selected")
        
        var $body = $(".main-area")
            $body.empty()
            $body.append('<h2 class="title">Introduction</h2>')
        var $main = $('<div class="content">')
        var $list = $("<ol>")
            $list.append("<li>Sign up for your username - The simplest of all</li>")
            $list.append("<li>Bookmark streams you find on the web - Create a set of sounds</li>")
            $list.append("<li>Design and upload your soundposter graphic - The hardest of all</li>")
            $list.append("<li>Provide a name, some description and your license info - Typing in the details</li>")
            $list.append("<li>Publish your soundposter - Sharing it worldwide</li>")
            // 
            $main.append('<p>Soundposters have many more options as here described. For example, one can easily turn your soundposter '
            + ' into a fully-fledged audible &amp; mobile festival guide with interactive specials but let us leave that for sometime '
            + 'soon and go on with the basics. If we have the basics right nothing will stop you turning your soundposter into a super-huge '
            + 'audible website.</p>')
            $main.append('<h3>To create &amp; publish a soundposter this is what you need to do:</h3>')
            $main.append($list)
            $main.append('<p>All of this is easy if you got your own sounds and graphics already, or if you know some people '
                + 'who can provide you with those.</p>')
            
        var $footer = $('<div class="content">')
            $footer.append('<p>The two creative parts in here are</p>')
            $footer.append('<h3>Reference streams from the web</h3>')
            $footer.append('<p>You can re-use any of your bookmarks pointing to a SoundCloud or Bandcamp track. '
                + 'Additionally to using simple URLs to self-hosted sounds one can synchronize a soundposter setlist with a SoundCloud set.</p>')
            // 
            $footer.append('<h3>Create and upload your soundposter graphic</h3>')
            $footer.append('<p>To be compatible and working with mobile web-browsers your poster graphic should not exceed 5 MP in bitmap size, '
                + 'which for example, is the case for a bitmap of 2500px width and 1900px height. The file format of your poster-graphic needs '
                + 'to be PNG or JPG/JPEG.</p>'
                + '<p>If you do not need your soundposter accessible as a mobile web-app the only limit for the size of your poster graphic is the a moderate file size limit.</p>'
                + '<p>Additionally we suggest you to prepare a second bitmap graphic as a preview for your soundposter with the a width of 297px and a height of 420px.</p>')
            $footer.append('<p>The important part left then is<br/>')
            $footer.append('<h3>The legal stuff</h3>'
            + 'Please check the copyrights for your graphic and choose a license for it.<br/>'
            + 'Do choose a license for your soundposter, one to cover your personal arrangement of tracks and one for the poster-graphic.</p>')
            $body.append($main).append($footer)

        var $menu = $('<div class="content-links">')
            $menu.append(''
            + '<a href="/legal" title="Read more legal info about publishing on soundposter.com">Legal</a>'
            + '<a href="/philosophy" title="Read about our philosophy behind soundposter.com">Philosophy</a>'
            + '<a href="/about-us" title="Read more about the people soundposter.com">About us</a>')
            $body.append($menu)
    }
    
    this.show_plans_view = function () {
        var $hello = $("#hello")
            $hello.hide()

        $('a.signup-btn').removeClass("selected")
        $('a.intro-btn').removeClass("selected")
        $('a.plans-btn').addClass("selected")
        //
        var $body = $(".main-area")
            $body.empty()
            $body.append('<h2 class="title">Plans and Pricing</h2>')
        var $main = $('<div class="plans">')
            $main.append('<div class="offer">'
               + '<b>Standard</b><br/><br/>'
               + 'One soundposter for free with all standard features, incl. hosting and basic support for one year.'
               + '<p>&nbsp;</p>'
               + '<b class="available">Free</b>, like in "0$ per Year".'
            + '</div>')

            $main.append('<div class="offer unavailable">'
               + '<b>Publisher</b><br/><br/>'
               + 'Up to five soundposters with extended customization possibilities, hosting and direct support.'
               + '<p>&nbsp;</p>'
               + '<b class="unavailable">Officially unavailable</b>'
            + '</div>')

            $main.append('<div class="offer unavailable">'
                + '<b>Professional</b><br/><br/>'
                + 'Unlimited number of public or private soundposters with extended customization possibilities, hosting and direct support.'
                + '<p>&nbsp;</p>'
                + '<b class="unavailable">Officially unavailable</b>'
            + '</div>')

            $main.append('<div class="content-links"><a href="/sign-up" title="Try publishing with soundposter.com">Contact us</a>')

            $main.append('<p>&nbsp;</p>'
                + '<img class="fitzel" src="/com.soundposter.website/images/fitzel-rot-1200.png" title="This is a fitzel on soundposter.com"/>')
        
            $main.append('<div class="details">'
                + '<h3>Standard features</h3>'
                + '<p>You find information about the standard features of each soundposter in this paragraph <a href="/help#short-overview">on our Help</a> page.</p><br/><br/>'
                + '<h3>Terms of service</h3>'
                + 'As we find it important that many people can easily understand the contents of our "terms of service", we herewith try to make all the important details explicit. One definition per line.'
                + '<br/><br/>Please feel free to ask us directly if, after reading all the details, you are still unsure about what we have on offer and what we do not have on offer.<br/><br/>'
                + '<br/>'
                + '<i>hosting</i>; free of charge web service for your soundposter on soundposter.com up to many thousand visitors per day'
                + '<br/><br/>'
                + '<i>one year rule</i>; when a new soundposter was made public we guarantee that it will be there for at least 365 days (98% of the time).'
                + '<br/><br/>'
                + '<i>basic support</i>; honest, effective and goal-oriented support for you 24/7 via e-mail and a response at latest within one working week.'
                + '<br/><br/>'
                + '<i>sound</i>; a reference to a browser compatible audiostream served by some other web service but explicitly not from soundposter.com'
                + '<br/><br/>'
                + '<i>poster graphic</i>; a still graphic or image-file as the backdrop illustration for your sounds'
                + '<p><br/><br/></p></div>')
            $body.append($main)
    }

    this.show_imprint_view = function () {
        var $hello = $("#hello")
            $hello.hide()

        $('a.signup-btn').removeClass("selected")
        $('a.intro-btn').removeClass("selected")
        $('a.plans-btn').removeClass("selected")
        //
        var $body = $(".main-area")
            $body.empty()
            $body.append('<h2 class="title">Imprint</h2>')

        var $main = $('<div class="content imprint">')
            $main.append('<p>'
                + 'Hi, i am Malte Rei&szlig;ig and the creator of soundposter.com.</p>'
                + '<p>'
                    + '<img src="/com.soundposter.website/images/malte_foto.jpg" alt="Picture of me" class="picture-me"/><br/>'
                    + 'I currently design and develop this publishing platform in my spare time (Advertisement: <a href="http://mikromedia.de">mikromedia.de</a>) using a computer and some coffee. The concepts though, were distilled on a journey which started somewhat about 2 1/2 years ago and during which I talked with lots of <a href="/about-us">really cool people</a>.'
                + '</p>'
                + '<p>'
                    + 'Additionally, what I have to point out here: I am standing on the shoulders of giants of whom most are working hard to keep the web open and you can find out about all of them in the <a href="http://blog.soundposter.com/tagged/technical">technical section of our blog</a>.'
                + '</p>'
                + '<p>'
                    + '<i>soundposter.com</i> is an idea and service developed &amp; provided by<br/><br/>Malte Rei&szlig;ig<br/><br/>'
                    + 'Contact: malte|@|soundposter.com<br/>'
                    + 'Mobile: +49 175 340 3734<br/><br/>'
                    + 'Address:<br/>'
                    + 'M&uuml;llerstra&szlig;e 59a<br/>'
                    + '13349 Berlin, Germany<br/>'
                    + '<br/>'
                    + 'Hire me: <a href="http://www.mikromedia.de">www.mikromedia.de</a>'
                + '</p><br/>'
                + '<h3>Privacy &amp; Disclaimer</h3>'
                + '<p>'
                    + 'For analytical purposes we collect anonymized (e.g. 192.168.1.x) usage statistics for all requests to our site and store these in (a) the access and error logs of our webservers and (b) in a database.<br/><br/>'
                    + 'Irregularly we delete this usage data from our webservers and we do not share these anonymized access-data with anyone else, except the creators of a soundposter.com.<br/><br/>'
                    + 'We use the Google Fonts API service on this website, so Google Inc. also knows about your clicks around here. The soundposter section does not use Google Fonts API service.'
                + '</p>')
            $body.append($main)
        var $menu = $('<div class="content-links">')
            $menu.append(''
            + '<a href="/legal" title="Read more legal info about publishing on soundposter.com">Legal</a>'
            + '<a href="/philosophy" title="Read about our philosophy behind soundposter.com">Philosophy</a>'
            + '<a href="/about-us" title="Read more about the people soundposter.com">About us</a>')
            $body.append($menu)
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
                if (!now_playing) {
                    if (!playback_set) {
                        if (url_set) {
                            // ### move svg init 
                            site.setup_buffering_animation()
                            site.setup_playback_animation()
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



    // --- The playing controls for the frontpage

    this.setup_playback_animation = function () {
        playingArc = r.path().attr({
            "stroke": "#FFB900", "stroke-width": 3, "opacity": 1,
            "stroke-linecap": "square"
        })
        console.log("New Animation: Playback Ring was added...")
    }

    this.update_playback_animation = function (value, duration) {
        playingArc.animate({
            arc: [177, 143, value, duration, 126]
        }, 750, "linear")
    }

    this.setup_buffering_animation = function () {
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
                site.update_playback_animation(time, duration)
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
