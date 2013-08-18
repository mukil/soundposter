
// var host = ""
var STATUS_INTERNAL = "Internal Server Error"
var STATUS_NOT_FOUND = "Not Found"
var STATUS_ACCESS_DENIED = "Unauthorized"
var HOST_URL = "http://www.soundposter.com"

var poster = new function () {

    this.historyApiSupported = window.history.pushState
    this.mod = undefined
    this.data = undefined
    this.playlist = undefined
    this.sounds = undefined
    this.events = undefined
    this.texts = undefined
    this.current = undefined // obsolete?
    this.selected_track = undefined
    this.selected_track_name = "No track selected"
    this.now_playing = false
    this.rendering = {'sounds': false, 'labels': false, 'events' :false}
    this.path = undefined
    this.player_is_ready = false
    this.url_set = undefined

    var $container = undefined
    var $image = undefined
    var $player = undefined
    var $notifications = undefined

    var debugLayout = false
    var debugModel = false
    var debugControls = false

    this.offsetX = 0
    this.offsetY = 0
    this.centerX = 0
    this.centerY = 0

    this.initialize = function(meta, graphicUrl, hyperlink, setlist, trackId, username, webalias) {

        // fixme: if one of these is not set, we may get a syntax error during initialization?

        poster.data = meta
        poster.data.setlist = setlist
        poster.path = "/" + username + "/" + webalias
        //
        this.perform_browser_check()
        // jplayer (flash-fallback) integration
        this.initialize_player()
        // hide play-controls for now
        this.hide_buttons()
        // parse and render support-links
        this.initialize_outlinks(hyperlink)
        // initialize nodes
        this.initialize_nodes() // loads playlist and rendering_options
        //
        setup_base_page()

        // this.perform_flash_check_call()

        // initialize poster graphic first, and when loaded, the whole soundposter player
        $container = $('div.postergraphic')
        $image = $('img.graphic')
        var posterHasGraphic = false
        //
        if (graphicUrl) {
            $image.attr('src', graphicUrl)
            $image.load( function(e) {
                //
                poster.show_loading_arc()
                posterHasGraphic = true
                //
                $container.width($image.width())
                $container.height($image.height())

                $container.draggable( {cursor: "move", scroll: false,
                    stop: function (event, ui) {
                        if (debugLayout) console.log("debug.Position => ")
                        if (debugLayout) console.log(ui.position)
                        if (debugLayout) console.log("debug.Offset => ")
                        if (debugLayout) console.log(ui.offset)
                    }, zIndex:  0
                })

                // focus the center of the graphic in the center of the windows viewport
                var moveX = poster.centerX - ($image.width() / 2) - poster.offsetX
                var moveY = poster.centerY - ($image.height() / 2) - poster.offsetY
                //
                poster.move_poster_about(moveX, moveY)
                // test:
                poster.show_graphics()
                setup_interactives()
                //
                // fixme: handle hiding of loading-animation better
                poster.hide_loading_arc()
                //
                return null
            })
        } else {
            setup_interactives()
            poster.show_notification("This soundposter has no postergraphic set yet.")
            $('body.poster').append('<span class="warning">This soundposter has no postergraphic set yet.</span>')
        }
        //
        if (poster.mod.history) window.addEventListener("popstate", poster.handle_back)
        // $('a#postername').attr('href', poster.path) // fixme: go to start position

            function setup_base_page () {
                // in any case initialize setlist dialog
                if (poster.rendering.event_list) {
                    poster.initialize_setlist_event_dialog()
                } else {
                    poster.initialize_setlist_sound_dialog()
                }
            }

            function setup_interactives () {
                // todo: another svg-element earlier, for displying e.g. image load percentage..
                if (trackId != 0) {
                    // dive deep into a soundposter
                    poster.selected_track = poster.get_viz_by_id(trackId)
                    // maybe we dont wanna start the audio immediately but render nice interactives
                    var pos = poster.show_selected_track()
                        pos.y = pos.y - 55
                        pos.x = pos.x
                    // hint: differ better between set_selected_track and show_selected_track and play_selected_track
                    poster.show_interactives(poster.play_selected_track, pos, posterHasGraphic)
                } else {
                    // start soundposter
                    poster.show_interactives(poster.play_from_start, undefined, posterHasGraphic)
                    // poster.show_setlist_dialog()
                }
            }

    }

    this.initialize_nodes = function () {

        // sort and initialize elements
        this.load_lists(this.data) // fixme: do server side

        // var soundposter_name = this.data.info.value
        // var soundposter_tags = this.data.info.composite["com.soundposter.tag"]
        // var soundposter_license = this.data.info.composite["com.soundposter.license_info"]
        // var soundoster_description = this.data.info.composite["dm4.topicmaps.description"]
        var rendering_options = this.data.info.composite["com.soundposter.display_options"]
        // var last_modified = this.data.info.composite["com.soundposter.last_modified"]

        for (var i=0; i <= rendering_options.length; i++) {
            // value is either empty (==default rendering options) or
            if (rendering_options[i] != undefined) {
                if (rendering_options[i].uri === "com.soundposter.display_song_labels") {
                    poster.rendering.sounds = true
                    this.show_sounds()
                } else if (rendering_options[i].uri === "com.soundposter.display_event_labels") {
                    poster.rendering.events = true
                    this.show_events()
                } else  if (rendering_options[i].uri === "com.soundposter.display_text_labels") {
                    poster.rendering.labels = true
                    this.show_texts()
                } else  if (rendering_options[i].uri === "com.soundposter.display_event_listing") {
                    poster.rendering.event_list = true
                } else if (rendering_options[i].uri === "com.soundposter.display_sound_times") {
                    poster.rendering.sound_times = true
                }
                if (debugLayout) console.log(rendering_options[i].uri)
            }
        }
    }

    this.initialize_outlinks = function (hyperlink) {

        if (hyperlink.composite != undefined) {
            // legacy version
            if (hyperlink.composite.hasOwnProperty('dm4.webbrowser.url')) {
                poster.data.buylink = hyperlink.composite['dm4.webbrowser.url'].value;
                poster.data.buylabel = hyperlink.composite['dm4.webbrowser.web_resource_description'].value;
                poster.data.buylabel = poster.data.buylabel.toString() // .replaceAll("<p>", "").replaceAll("</p>", "")
                $('a.support-link').text("Artist Link")
                $('a.support-link').attr("href", poster.data.buylink)
                return null
            }
        }
        // overriden by new version
        if (buylink_href.indexOf("http://") != -1) {
            $('a.support-link').text(buylink_label)
            $('a.support-link').attr("href", buylink_href)
            return null
        }
        // in case none of it is set
        $('a.support-link').remove()
    }

    this.perform_flash_check_call = function () {
        // if on android and khtml, let's recommend the following browser aggressively:
        // https://play.google.com/store/apps/details?id=org.mozilla.firefox_beta
        var flash_object = document.getElementById('dummy_movie')
        try {
            // console.log(flash_object)
            flash_object.callFlash("Hi Flash!")
        } catch (e) {
            console.log(e)
            // throw new Error("Flash not active")
        }
    }

    this.initialize_setlist_sound_dialog = function () {
        // todo: if (tracklist is active), seekbar should be within tracklist-item
        var $listing = $('ul.listing')
            $listing.empty()
        for (var item in poster.sounds) {
            var sound = poster.sounds[item]
            // prepare data for each list-item
            var sound_name = (sound.composite.hasOwnProperty('com.soundposter.sound_name')) ? sound.composite['com.soundposter.sound_name'].value  : ""
            var album_name = (sound.composite.hasOwnProperty('com.soundposter.album_name')) ? sound.composite['com.soundposter.album_name'][0].value  : "" // just render 1st album at hand
            var artists_name = (sound.composite.hasOwnProperty('com.soundposter.artist_name')) ? sound.composite['com.soundposter.artist_name'].value  : [""]
            var artist_name = ""
            var idx = 0
            for (var name in artists_name) {
                if (artists_name.hasOwnProperty('value')) {
                    artist_name += artists_name[name].value
                    if (artists_name.length > 1 && idx !== artists_name.length) artist_name += ", "
                    idx++
                }
            }
            //
            var sound_description = (sound.composite.hasOwnProperty('com.soundposter.sound_description')) ? sound.composite['com.soundposter.sound_description'].value  : ""
            var gig_start_time = (sound.composite.hasOwnProperty('com.soundposter.gig_start_time')) ? sound.composite['com.soundposter.gig_start_time'].value  : ""
            var publisher_homepage = (sound.composite.hasOwnProperty('com.soundposter.publisher_info')) ? sound.composite['com.soundposter.publisher_info'].value  : ""
            var publisher_name = (sound.composite.hasOwnProperty('com.soundposter.publisher_name')) ? sound.composite['com.soundposter.publisher_name'].value  : ""
            var license_info = (sound.composite.hasOwnProperty('com.soundposter.license_info')) ? sound.composite['com.soundposter.license_info'].value  : ""
            var track_position = (sound.composite.hasOwnProperty('com.soundposter.ordinal_number')) ? parseInt(sound.composite['com.soundposter.ordinal_number'].value) : 0
            if (poster.rendering.sound_times) {
                // overriding track positio with gig_times value
                track_position = gig_start_time
            }
            var source_page = (sound.composite.hasOwnProperty('com.soundposter.source_page')) ? sound.composite['com.soundposter.source_page'].value : "com.soundposter.unspecified_source"
            var stream_info = (sound.composite.hasOwnProperty('dm4.webbrowser.url')) ? sound.composite['dm4.webbrowser.url'].value : ""
            var stream_unavailable = (sound.composite.hasOwnProperty('com.soundposter.stream_unavailable')) ? sound.composite['com.soundposter.stream_unavailable'].value  : ""
            //
            var stream_provider_class = "unspecified-stream"
            var stream_provider_placeholder = ""
            // fixme: currently calculating the streaming provider based on stream-location-uri
            if (stream_info.indexOf("soundcloud") != -1) {
                stream_provider_class = "soundcloud-stream"
                stream_provider_placeholder = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
            } else if (stream_info.indexOf("poppler") != -1) {
                stream_provider_class = "bandcamp-stream"
                stream_provider_placeholder = '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
                    + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
            }
            // construct ui-elements
            var $playnow = $('<a href="javascript:;" id="play-' + sound.id + '" class="playnow">')
                $playnow.append('<span id="posi-' + sound.id + '" class="track-position">' + track_position + '</span>')
                $playnow.append('<span id="name-' + sound.id + '" class="track-name">' + sound_name + '</span>')
                $playnow.append('<span id="arts-' + sound.id + '" class="artist-name">' + artist_name + '</span>')
                $playnow.append('<span id="albu-' + sound.id + '" class="album-name">' + album_name + '</span>')
                $playnow.click(function(e) {
                    poster.set_sound_visuals_by_id(parseInt(e.target.id.substr(5)))
                    poster.play_selected_track()
                })
            /** var $share = $('<a href="javascript:;" id="share-' + sound.id + '" class="share">Share</a>')
                $share.click(function(e) {
                    poster.share(parseInt(e.target.id.substr(5)))
                })**/
            var $entry = $('<div class="element">')
            var $source_btn = $('<span class="source-info">')
                $entry.append($playnow)
                $entry.append($source_btn)
                if (source_page !== "") {
                    $source_btn.append('<a class="' +stream_provider_class+ '" href="' +source_page+ '"'
                    + 'title="' +source_page+ '" ' + 'target="_blank">' +stream_provider_placeholder+ '</a>')
                } else {
                    $source_btn.append('<a class="' +stream_provider_class+ ' unavailable" href="javascript:;"'
                    + 'title="' +source_page+ '">' +stream_provider_placeholder+ '</a>')
                }
            var $listitem = $('<li id="entry-' + sound.id + '">')
                $listitem.append($entry)
            //
            $listing.append($listitem)
        }
        // list-divider: $listing.append('<li data-role="list-divider" data-divider-theme="a">Spot X</a>')
    }

    this.initialize_setlist_event_dialog = function () {
        // todo: if (tracklist is active), seekbar should be within tracklist-item
        var $listing = $('ul.listing')
            $listing.empty()
        for (var item in poster.events) {
            var event = poster.get_topic_by_id(poster.events[item].id, true)
                // sort sounds by gig time
                event.composite['com.soundposter.sound'].sort(poster.gig_sort)
            // append event-header
            $listing.append('<li data-role="list-divider" data-divider-theme="a">' +event.value+ '</a>')
            // construct sound-items
            if (event.composite.hasOwnProperty('com.soundposter.sound')) {
                for (var key in event.composite['com.soundposter.sound']) {
                    var sound = event.composite['com.soundposter.sound'][key]
                    var sound_name = (sound.composite.hasOwnProperty('com.soundposter.sound_name')) ? sound.composite['com.soundposter.sound_name'].value  : ""
                    var gig_start_time = (sound.composite.hasOwnProperty('com.soundposter.gig_start_time')) ? sound.composite['com.soundposter.gig_start_time'].value  : ""
                    var track_position = (gig_start_time != undefined) ? gig_start_time : ""
                    var source_page = (sound.composite.hasOwnProperty('com.soundposter.source_page')) ? sound.composite['com.soundposter.source_page'].value : "com.soundposter.unspecified_source"
                    var stream_info = (sound.composite.hasOwnProperty('dm4.webbrowser.url')) ? sound.composite['dm4.webbrowser.url'].value : ""
                    var stream_provider_class = "unspecified-stream"
                    var stream_provider_placeholder = ""
                    // fixme: currently calculating the streaming provider based on stream-location-uri
                    if (stream_info.indexOf("soundcloud") != -1) {
                        stream_provider_class = "soundcloud-stream"
                        stream_provider_placeholder = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
                    } else if (stream_info.indexOf("poppler") != -1) {
                        stream_provider_class = "bandcamp-stream"
                        stream_provider_placeholder = '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
                            + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
                    }
                    // construct ui-elements
                    var $playnow = $('<a href="javascript:;" id="play-' + sound.id + '" class="playnow">')
                        $playnow.append('<span id="posi-' + sound.id + '" class="track-position">' + track_position + '</span>')
                        $playnow.append('<span id="name-' + sound.id + '" class="track-name">' + sound_name + '</span>')
                        // $playnow.append('<span id="arts-' + sound.id + '" class="artist-name">' + artist_name + '</span>')
                        // $playnow.append('<span id="albu-' + sound.id + '" class="album-name">' + album_name + '</span>')
                        $playnow.click(function(e) {
                            poster.set_sound_visuals_by_id(parseInt(e.target.id.substr(5)))
                            poster.play_selected_track()
                        })
                    var $entry = $('<div class="element">')
                    var $source_btn = $('<span class="source-info">')
                        $entry.append($playnow)
                        $entry.append($source_btn)
                        if (source_page !== "") {
                            $source_btn.append('<a class="' +stream_provider_class+ '" href="' +source_page+ '"'
                            + 'title="' +source_page+ '" ' + 'target="_blank">' +stream_provider_placeholder+ '</a>')
                        } else {
                            $source_btn.append('<a class="' +stream_provider_class+ ' unavailable" href="javascript:;"'
                            + 'title="' +source_page+ '">' +stream_provider_placeholder+ '</a>')
                        }
                    var $listitem = $('<li id="entry-' + sound.id + '">')
                        $listitem.append($entry)
                    //
                    $listing.append($listitem)
                }
            }
        }
    }

    this.share = function () {

        var trackId = 0
        var text = "", url = "", hashtags = "np, soundposter"
        if (poster.selected_track == undefined) {
            // share poster
            text = poster.data.info.value + " "
            url = HOST_URL + poster.path
        } else {
            // share track
            trackId = poster.selected_track.id
            url = HOST_URL + poster.path + "/" + trackId
            text = poster.selected_track.value + " - " + poster.data.info.value + " "
        }
        var intentAddress = 'https://twitter.com/intent/tweet?url='+url+'&text='+text+'&hashtags='+ hashtags
        // open blank window with intent address
        window.open(encodeURI(intentAddress), 'Share this sound via Twitter.com', 'width=300,height=420')
        // URL-Schema:
        // https://twitter.com/intent/tweet?url=http://www.soundposter.com/..&text=Track 1 - Soundposter X&hashtags=np,soundposter
    }

    this.embed = function () {

        var $container = $('.top-menu .embed-area')
            console.log($container)

        if ($container.length > 0) {
            $('.controls #jp_container_1').css("top", "65px")
            $('.top-menu #sign-up').removeClass('active')
            // hide, remove
            $container.remove()
        } else {
            // initiate this
            $container = $('<div class="embed-area">')
            var address = HOST_URL + poster.path
            if (poster.selected_track != undefined) address = address + "/" + poster.selected_track.id
            render_input_field()
            $('.top-menu').append($container)
            $('.controls #jp_container_1').css("top", "95px")
            $('.top-menu #sign-up').addClass('active')
        }

        function render_input_field () {
            var iframe_string = '<iframe src=' +address+ ' frameBorder=0 '
                + 'width=297 height=440></iframe>'
                + '<br/>Watch this soundposter in <a href=' +address+ '>full size</a>. '
            var $input_field = $('<input type="text" name="embed-tag" value="' +iframe_string+ '">')
                $container.append($input_field)
        }

    }

    this.go_to_source = function () {
        var address = poster.get_source_info(poster.selected_track)
        if (address != undefined && address.indexOf("://") != -1) {
            window.open(address, "Soundposter Redirect to Source", null)
            console.log("Fine.")
        } else {
            console.log("WARNING: Source information not set/avaialble.")
        }
    }

    this.stop_propagation = function (e) {
        console.log("trying to stop event propagation in general and on registered elements...")
        // stop event propagation, like quirksmode proposed it
        if (!e) e = window.event;
        e.cancelBubble = true;
        if (e.stopPropagation) e.stopPropagation();
    }

    this.toggle_tracklist_view = function () {
        // toggle-button
        var $tracklist_button = $('a.tracklist-toogle')
        if ($tracklist_button.hasClass('active')) {
            $tracklist_button.removeClass('active')
        } else {
            $tracklist_button.addClass('active')
        }
        // list-view
        $('ul.listing').slideToggle('fast')
        $('ul.listing').listview('refresh')
    }

    this.show_setlist_dialog = function () {v
        $('ul.listing').show('fast')
        $('ul.listing').listview('refresh')
        // $('div.setlist').show('fast')
    }

    this.hide_setlist_dialog = function () {
        // $('div.setlist').hide('fast')
        $('ul.listing').hide('fast')
    }

    this.initialize_player = function () {

        $player = $("#soundsystem").jPlayer({
            ready: function () {
                if (debugControls) console.log("ready... ")
                poster.player_is_ready = true
            },
            timeupdate: function(event) {
                // todo: write play-button and animate with event.jPlayer.status.currentPercentRelative
                // $('.timer').text($.jPlayer.convertTime(event.jPlayer.status.currentTime))
                $('.timer').text($.jPlayer.convertTime(event.jPlayer.status.duration))
            },
            play: function(event) {
                // keep state current
                poster.now_playing = true
                // some gui manipulation
                $notifications.empty()
                $('.timer').show()
                $('.lower-menu .source').show()
                $('.lower-menu .share').show()
            },
            loadstart: function(event) {
                // console.log("Loadstart, seekPercent = " + event.jPlayer.status.seekPercent)
            },
            loadeddata: function(event) {
                // do something smart with console.log(event.jPlayer.status)
            },
            loadedmetadata: function(event) {
                // TODO: loaded some metadata...
                if (debugControls) {
                    console.log("jPlayer:loadedmetada... ") // not really useful
                    console.log(event)
                }
            },
            pause: function(event) {
                // keep state current
                poster.now_playing = false
                // fixme: jQuery('.map-info').html("♪ " + sp.selected_track.value)
            },
            ended: function(event) {
                // keep state current
                poster.now_playing = false
                poster.url_set = undefined
                // keep on playing
                poster.play_next_track()
            },
            error: function(event) {
                // keep state current
                poster.now_playing = false
                poster.url_set = undefined
                $('.lower-menu .source').hide()
                //
                if (debugControls) {
                    console.log("jPlayer:error playing stream... ")
                    console.log(event)
                }
                //
                if (event.jPlayer.error.type == "e_url_not_set") {
                    console.log("ERROR: Sound-Resource is not yet set => " + poster.url_set)
                } else if (event.jPlayer.error.type == "e_url") {
                    console.log('ERROR: An error occured during requesting media source url.')
                    poster.show_modal_notification("We cannot access the media anymore, probably the sound was moved. "
                        + "And if you're connected to the internet, this sound will be marked for review for the author"
                        + " of this soundposter.<br/>")
                    setTimeout(function() {
                        // if user did not react to error-message, we skip to the next track now
                        if (!poster.now_playing) {
                            poster.play_next_track()
                            poster.show_notification("Skipping to next track")
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
                $('.timer').hide()
            // backup, unselection of highlighted track
            },
            swfPath: "/com.soundposter.website/script/vendor/jquery.player/",
            supplied: "mp3",
            solution: "html, flash",
            errorAlerts: false
        });

        $('.jp-next').click(poster.play_next_track)
        $('.jp-prev').click(poster.play_prev_track)
        //
        if (menu_label !== "") $('a.tracklist-toogle').text(menu_label)

    }

    this.perform_browser_check = function() {

        // initialize modernizr
        poster.mod = Modernizr

        $notifications = $('div.notifications') // initialized globally
        $notifications.empty()
        for (var key in poster.mod.audio) {
            var value = poster.mod.audio[key]
            if (value === "") value = "Not supported"
            poster.show_notification(key + ': ' + value)
        }
        //
        poster.show_notification((poster.mod.svg == false) ? "svg: Not supported" : "svg: OK")
        // poster.show_notification((poster.mod.inlinesvg == false) ? "inlinesvg: Not supported" : "inlinesvg: OK")
        poster.show_notification((poster.mod.audiodata == false) ? "audiodata: Not supported" : "audiodata: OK")
        poster.show_notification((poster.mod.webaudio == false) ? "webaudio: Not supported" : "webaudio: OK")
        // poster.show_notification((poster.mod.indexeddb == false) ? "indexeddb: Not supported" : "indexeddb: OK")
        // poster.show_notification((poster.mod.boxshado == false) ? "box shadow: Not supported" : "box shadow: OK")
        // poster.show_notification("user-agent: " + navigator.userAgent)
    }

    this.show_notification = function (text) {
        // todo: if notification area is already visible, do not animate it
        var $message = $('<div>').text(text)
            $message.css('display', 'inline-block')
            $message.fadeIn('fast').delay(3500).fadeOut('slow')
        $notifications.append($message)
        /** .show('fast', function() {
            $notifications.delay(6000, function(){
                $notifications.hide('slow')
                // $notifications.unbind('delay')
            })
        }) */
    }

    this.show_modal_notification = function (message) {
        var $message, $option, $close = undefined
        if ($('.notifications .text').length > 0) {
            $message = $('.notifications .text')
            $message.html(message)
            $option = $('<br/><a href="javascript:;">Go to next</a>').click(function(e) {
                poster.play_next_track()
                $notifications.empty()
            })
            $close = $('<a href="javascript:;">Close</a>').click(function(e) {
                $notifications.empty()
            })
            $message.append($option).append('&nbsp;&nbsp;&nbsp;').append($close)

        } else {
            $message = $('<div class="text">').html(message)
            $option = $('<br/><a href="javascript:;">Go to next</a>').click(function(e) {
                    poster.play_next_track()
                    $notifications.empty()
                })
            $close = $('<a href="javascript:;">Close</a>').click(function(e) {
                    $notifications.empty()
                })
            $message.append($option).append('&nbsp;&nbsp;&nbsp;').append($close)
            $notifications.append($message)
        }
    }

    this.show_graphics = function () {
        $image.fadeIn()
        $('div.posteritem').fadeIn({duration: 900})
    }

    this.show_loading_arc = function(pos) {
        // get Orientation
        var sizeX = poster.viewport_width()
        var sizeY = poster.viewport_height()
        var graphicX = sizeX / 2 - 150
        var graphicY = sizeY / 2 - 150

        $("#loading-area").css("left", graphicX - 5)
        $("#loading-area").css("top", graphicY + 20)
        $('#loading-area').show()
        // create Paper
        poster.paper = Raphael("loading-area", 300, 300)
        var R = 70
        // Custom Attribute
        poster.paper.customAttributes.arc = function (value, total, R) {
            var alpha = 360 / total * value,
                a = (90 - alpha) * Math.PI / 180,
                x = 150 + R * Math.cos(a),
                y = 150 - R * Math.sin(a),
                color = "hsb(".concat(143, ",", value / total, ", .75)"),
                path
            if (total == value) {
                path = [["M", 150, 150 - R], ["A", R, R, 0, 1, 1, 149.99, 150 - R]]
            } else {
                path = [["M", 150, 150 - R], ["A", R, R, 0, +(alpha > 180), 1, x, y]]
            }
            return {path: path, stroke: color}
        }

        var sec = poster.paper.path()
            .attr({"stroke": "#ffffff", "stroke-opacity" : 0.8, "stroke-width": 30})
            .attr({arc: [1, 60, R]})
            // "stroke-linecap": "round"
            sec.animate({arc: [60, 60, R]}, 7000)
        if (pos != undefined) {
            graphicX = pos.x + 55
            graphicY = pos.y - 65
        }

    }

    this.hide_loading_arc = function () {
        $('#loading-area').remove()
    }

    /** fixme: still depends in postergraphic being loaded .. **/
    this.show_interactives = function(click_handler, pos, posterHasGraphic) {
        /* var r = Raphael("interactives", 600, 600),
            R = 200,
            init = true,
            param = {stroke: "#fff", "stroke-width": 30},
            hash = document.location.hash,
            marksAttr = {fill: hash || "#444", stroke: "none"} **/
        // Get orientation
        var graphicRadius = 40
        var graphicX = poster.viewport_width() / 2
        var graphicY = poster.viewport_height() / 2
        if (posterHasGraphic) {
            graphicX = $image.width() / 2 + graphicRadius
            graphicY = $image.height() / 2 + graphicRadius
        }
        poster.paper = Raphael("interactives", 6321, 6321) // todo: dont set max raphael-canvas-size
        // poster.paper.image(graphicUrl, 1000, 1000, 2000, 2000)
        // and lets place our interactives not in the center of the postergraphic, but at the spot of the sound
        if (pos != undefined) {
            graphicX = pos.x + 55
            graphicY = pos.y - 65
        }
        // Creates circle
        var circle = poster.paper.circle(graphicX, graphicY, graphicRadius)
            // circle.attr("fill", "0-#333-#a9a9a9:5-#333") // ‹angle›-‹colour›[-‹colour›[:‹offset›]]*-‹colour›
            circle.attr("fill", "r[0.7,1]#2d2d2d-#131313") // r[(‹fx›, ‹fy›)]‹colour›[-‹colour›[:‹offset›]]*-‹colour›
            // circle.attr("fill", "#fff") // r[(‹fx›, ‹fy›)]‹colour›[-‹colour›[:‹offset›]]*-‹colour›
            circle.attr("fill-opacity", "1") // r[(‹fx›, ‹fy›)]‹colour›[-‹colour›[:‹offset›]]*-‹colour›
            circle.attr("stroke", "#fff")
            circle.attr("stroke-width", 6)
            circle.attr("stroke-opacity", .7)
            circle.attr("cursor", "pointer")
            // circle.transform("t" +centerX+ "," +centerY+ "")

        var arrow = poster.paper.path("M" +(parseInt(graphicX)-10)+ "," +parseInt(graphicY)+ ", l0,25, l35,-25 l-35,-25, l0,25,z")
            arrow.attr("fill", "#fff")
            // arrow.attr("stroke", "#a9a9a9")
            // arrow.attr("stroke-width", 2)
            arrow.attr("cursor", "pointer")

        // var info = poster.paper.tag(graphicX, graphicY, 'Click to play', 45)

        /** circle.mousedown(function(e) {
            console.log("click, calling animation")
        }) **/

        circle.hover(ease_in, ease_out)
        circle.click(click_handler)
        arrow.click(click_handler)

        function ease_in() {
            circle.animate({"r" : 45}, 25, "easeIn")
            circle.animate({"stroke-opacity" : .9}, 75, "easeIn")
            circle.animate({"stroke-width" : 8}, 100, "easeIn")
            arrow.transform("s1.1")
            circle.animate({"font-size" : 13}, 2000, "linear", function() { // dummy animation for timed fade-out
                circle.animate({"r" : 40}, 100, "easeOut")
                circle.animate({"stroke-opacity" : .7}, 100, "easeOut")
                circle.animate({"stroke-width" : 6}, 100, "easeOut")
                arrow.transform("");
            })
        }

        function ease_out() {
            // circle.animate({"r" : 40}, 200, "easeIn")
            // circle.glow( {color: "#333", width: 20} )
        }

        $('#interactives').fadeIn(200)

    }

    this.show_texts = function() {
        if (poster.texts != undefined) {
            //
            for (var item in poster.texts) {
                var text = this.texts[item]
                var visualization = text['visualization']
                var itemX = visualization['dm4.topicmaps.x'].value - 25
                var itemY = visualization['dm4.topicmaps.y'].value - 25
                var element = "<div class=\"postertext\" style=\"position: absolute; top:" + itemY + "px; left: " + itemX
                + "px;\">" + text.value + "</div>";
                $(".postergraphic").append(element)
            }
        }
    }

    this.show_events = function() {
        if (poster.events != undefined) {
            //
            for (var item in poster.events) {
                var text = poster.events[item]
                var visualization = text['visualization']
                var itemX = visualization['dm4.topicmaps.x'].value - 25
                var itemY = visualization['dm4.topicmaps.y'].value - 25
                var element = "<div class=\"posterevent\" style=\"position: absolute; top:" + itemY + "px; left: " + itemX
                + "px;\">" + text.value + "</div>";
                $(".postergraphic").append(element)
            }
        }
    }

    this.show_sounds = function() {
        if (poster.playlist != undefined) {
            //
            if (debugModel) console.log(poster.playlist)
            for (var item in this.playlist) {
                var song = this.playlist[item]
                var visualization = song['visualization']
                var itemX = visualization['dm4.topicmaps.x'].value
                var itemY = visualization['dm4.topicmaps.y'].value
                var sound_name = song.value
                if (poster.rendering.sound_times) {
                    // pre-pending start_gig_time-value to sound_name
                    var start_time = poster.get_gig_time(song)
                    sound_name = start_time + " " + sound_name
                }
                var $element = $("<div id=\"" + song.id + "\" class=\"posteritem\" style=\"position: absolute; top:"
                    + itemY + "px; left: " + itemX + "px;\">" + sound_name + "</div>");
                    $element.click(function(e) {
                        // sound click handler
                        if (poster.selected_track == undefined) {
                            //
                            if (debugControls) console.log("Compelte Start: Settg sound id to => " + e.target.id)
                            poster.set_sound_visuals_by_id(e.target.id)
                            poster.play_selected_track()
                        } else if (poster.selected_track.id == e.target.id) {
                            //
                            if (poster.now_playing) {
                                poster.pause_selected_track()
                            } else {
                                var trackId = e.target.id
                                console.log(poster.url_set)
                                if (poster.url_set == undefined) {
                                    // start playing this element
                                    // if not set, set it to element with id: trackId
                                    poster.set_sound_visuals_by_id(trackId)
                                    poster.play_selected_track()
                                } else {
                                    if (debugControls) console.log("DEBUG: continuing with playing current sound... ")
                                    // continue with current track
                                    $player.jPlayer("play")
                                }
                            }
                        } else {
                            if (debugControls) console.log("Kickstart (Visualized): Settg sound id to => " +e.target.id)
                            poster.set_sound_visuals_by_id(e.target.id)
                            poster.play_selected_track()
                        }
                    })
                $("div.postergraphic").append($element)
            }
        }
    }

    this.show_track_selection = function () {
        if (!poster.rendering.sounds) { // if sound-labels are not rendered, we display the title of selected_track
            var sound_name = this.selected_track.value // .composite["com.soundposter.sound_name"].value
            poster.selected_track_name = sound_name
            $('#selection').text("Title: " + sound_name)
            $('#selection').fadeIn({duration: 900});
        }
    }

    this.show_buttons = function () {
        // $('a.jp-play').fadeIn({duration: 900})
        // $('a.jp-pause').fadeIn({duration: 900})
        // $('a.jp-prev').fadeIn({duration: 900})
        // $('a.jp-next').fadeIn({duration: 900})
        //
        $('a.jp-play').show()
        $('a.jp-pause').show()
        $('a.jp-prev').show()
        $('a.jp-next').show()
    }

    this.hide_buttons = function () {
        $('a.jp-play').hide()
        $('a.jp-pause').hide()
        $('a.jp-prev').hide()
        $('a.jp-next').hide()
    }

    this.remove_style_from_currently_played_items = function (itemId) {
        $('#' + itemId).removeClass('playing')
        $('ul.listing li#entry-' + itemId).removeClass('playing')
    }

    this.add_style_to_currently_played_items = function (itemId) {
        $('#' + itemId).addClass('playing')
        $('ul.listing li#entry-' + itemId).addClass('playing')
    }

    this.toggle_interactives = function () {
        $('#interactives').toggle({duration: 900});
    }

    this.hide_interactives = function () {
        $('#interactives').hide()
    }

    this.move_poster = function(toX, toY) {
        if (poster.debugLayout) console.log("move.toX:" + toX + " toY: " + toY)
        $("div.postergraphic").animate(
            {
                left: -(toX - poster.centerX), top: -(toY - poster.centerY)
            }, {
                duration: 600, specialEasing: {width: 'linear', height: 'easeOutBounce'}
            }
        )
        if (poster.debugLayout) console.log("sp.offsetX:" + poster.offsetX + " offsetY: " + poster.offsetY)
        poster.offsetX = toX
        poster.offsetY = toY
        if (poster.debugLayout) console.log("=> sp.newOffsetX:" + poster.offsetX + " newOffsetY: " + poster.offsetY)
    }

    this.move_poster_about = function(aboutX, aboutY) {
        if (poster.debugLayout) console.log("move.aboutX:" + aboutX + " aboutY: " + aboutY)
        $("div.postergraphic").animate({left: aboutX, top: aboutY}, {duration: 1, easing: 'swing'})
        poster.offsetX = poster.offsetX + aboutX
        poster.offsetY = poster.offsetY + aboutY
    }

    this.show_track_by_id = function (id) {
        var track = poster.get_viz_by_id(id)
        // animate soundposter
        var visualization = track['visualization']
        var songX = visualization['dm4.topicmaps.x'].value// + 600
        var songY = visualization['dm4.topicmaps.y'].value// + 600
        if (debugLayout) console.log("should move to position => X/Y" + songX + ":" + songY)
        poster.move_poster(songX, songY)
        return {x: songX, y: songY}
    }

    this.show_selected_track = function () {
        // var track = poster.get_viz_by_id(poster.selected_track.id)
        // animate soundposter
        var visualization = poster.selected_track['visualization']
        var songX = visualization['dm4.topicmaps.x'].value// + 600
        var songY = visualization['dm4.topicmaps.y'].value// + 600
        if (debugLayout) console.log("should move to position => X/Y" + songX + ":" + songY)
        poster.move_poster(songX, songY)
        return {x: songX, y: songY}
    }

    this.pause_selected_track = function() {
        $player.jPlayer("pause")
    }

    this.play_from_start = function () {

        // start playing and hide start button..
        var nextTrack = undefined
        if (poster.sounds.length > 0) {
            nextTrack = poster.sounds[0] // play first item in our (already sorted) sequence of sounds
        }
        if (nextTrack != undefined) {
            // #### dm4c.show_topic(dm4c.fetch_topic(nextTrack.id), "show", false, true)
            poster.selected_track = poster.get_viz_by_id(nextTrack.id)
            poster.play_selected_track()
            poster.hide_interactives()
        } else {
            poster.show_notification("Sorry, we could not start playing, press any other key...")
            // console.log("could not play_from_start, since poster.sounds are not initialized?")
            // console.log(poster.sounds)
        }

    }

    // This is the main "play" method which every other method uses, after having set poster.selected_track
    this.play_selected_track = function() {
        var track_id = poster.selected_track.id
        var track_name = poster.selected_track.value
        // animate soundposter
        poster.show_selected_track()

        // get metadada and play stream
        var address = poster.get_audiofile_url(poster.selected_track)
        if (debugControls) console.log("Setting MediaSource to \"" + address+ "\"")
        if (address != undefined) {
            //
            poster.show_track_selection() // render title (if wanted)
            poster.show_buttons() // render controls
            //
            poster.url_set = address
            $player.jPlayer("setMedia", {mp3: address})
            $player.jPlayer("play")
            // just push track played into browser history
            if (poster.mod.history) {
                var state = {"id" : track_id}
                var title = track_name + " - " + poster.data.value
                history.pushState(state, title, poster.path + "/" + track_id)
            } else {
                console.log("WARNING: Your browser has no history support, so you cannot deep link into tracks.")
            }
            // some analytics rubbish
            piwikTracker.trackGoal(1, track_name);
            // some gui rubbish after kickstart via the big button play
            poster.hide_interactives()
            // some more
            poster.add_style_to_currently_played_items(poster.selected_track.id)
            // $('.map-info').html("♪ " + this.selected_track.value)
            // $('.map-info').fadeIn(500).delay(2700).fadeOut(500)
            // $('.map-info').hide()
            if (!poster.player_is_ready) {
                // occurs where flash browser was blocked first, is now active => jPlayer not yet ready
                setTimeout(function (e) {
                    $player.jPlayer("setMedia", {mp3: address})
                    $player.jPlayer("play")
                }, 1500)
            }
        } // fixme: what to do if server is down?
    }

    this.play_next_track = function () {
        var idx = poster.get_current_sound_idx()
        var track = poster.sounds[idx]
        if (track != undefined) {
            poster.remove_style_from_currently_played_items(track.id)
        }
        var nextTrack = undefined
        //
        if (idx+1 == poster.sounds.length) {
            nextTrack = poster.sounds[0] // play first again..
        } else {
            nextTrack = poster.sounds[idx+1]
        }
        //
        if (nextTrack != undefined) {
            poster.selected_track = poster.get_viz_by_id(nextTrack.id)
            poster.play_selected_track()
        }
    }

    this.play_prev_track = function () {
      var idx = poster.get_current_sound_idx()
        var track = poster.sounds[idx]
        if (track != undefined) {
            poster.remove_style_from_currently_played_items(track.id)
        }
        var nextTrack = undefined
        //
        if (idx+1 == poster.sounds.length) {
            nextTrack = poster.sounds[0] // play first again..
        } else {
            nextTrack = poster.sounds[idx-1]
        }
        //
        if (nextTrack != undefined) {
            poster.selected_track = poster.get_viz_by_id(nextTrack.id)
            poster.play_selected_track()
        }
    }

    this.get_viz_by_id = function (item_id) {
        //
        for (var track in this.playlist) {
            //
            var item = this.playlist[track]
            if (item_id != undefined) {
                //
                if (item.id == item_id) {
                    //
                    return item
                }
            }
        }
        return null
    }

    this.get_current_sound_idx = function () {
        //
        var idx = 0
        for (var track in this.sounds) {
            //
            var topic = this.sounds[track]
            if (this.selected_track != undefined) {
                //
                if (topic.id == this.selected_track.id) {
                    // index of track in sp.sounds-array
                    return idx
                }
                idx++
            }
        }
        return idx
    }

     this.set_sound_visuals_by_id = function (soundId) {
        var idx = this.get_current_sound_idx()
        var played_item = this.sounds[idx]
        if (played_item != undefined) {
            poster.remove_style_from_currently_played_items(played_item.id)
        }
        for (var track in this.sounds) {
            var item = this.sounds[track]
            if (item.id == soundId) {
                // note: sp.selected_track must be the playlist-item (with a visualization-property)
                poster.selected_track = this.get_viz_by_id(item.id)
                // animate picture..
                // fixme: update song title: document.title = sp.tape.value + " - soundposter/"
            }
        }
    }

    /** when topicmaps are changed, this must be called..needs some topicmap_loaded_hook **/
    this.load_lists = function(topicmap) {
        // get selected topicmap
        poster.playlist = new Array()
        poster.sounds = new Array()
        poster.texts = new Array()
        poster.events = new Array()
        //
        if (topicmap != undefined) {
            // iterate through topics
            for (var topic in topicmap.topics) { // (function(topic) {
                // access the topic's properties:
                if (topicmap.topics[topic].type_uri == "com.soundposter.sound") {
                    var item = topicmap.topics[topic]
                    if (item['visualization']['dm4.topicmaps.visibility'].value) {
                        // playlist [topic.id, topic['visualization']]
                        poster.playlist.push(item) // the visual setlist
                        // load full composite of each sound-item
                        // fixme: fetch from our new setlist instead
                        // var sound = poster.get_topic_by_id(topic.id, true)
                        var sound = poster.load_sound_from_local_setlist(item.id)
                        if (sound == null) throw new Error("Error while populating setlist.")
                        poster.sounds.push(sound) // the sound-stream meta data setli
                    }
                } else if (topicmap.topics[topic].type_uri == "com.soundposter.text") {
                    poster.texts.push(topicmap.topics[topic])
                } else if (topicmap.topics[topic].type_uri == "com.soundposter.event") {
                    poster.events.push(topicmap.topics[topic])
                }
            }
            poster.events.sort(this.topic_sort) // alphabetical ascending
            poster.playlist.sort(this.topic_sort) // alphabetical ascending
            poster.sounds.sort(this.track_sort) // ordinal number sorting ascending
            if(debugModel) console.log(poster.sounds)
            if(debugModel) console.log(poster.playlist)
        }
    }

    this.load_sound_from_local_setlist= function (topicId) {
        for (var item in poster.data.setlist) {
            var sound = poster.data.setlist[item]
            if (sound.id === topicId) return sound
        }
        return null
    }

    // compare "a" and "b" in some fashion, and return -1, 0, or 1
    this.gig_sort = function (a, b) {
        var gigA = (a.composite.hasOwnProperty('com.soundposter.gig_start_time')) ? a.composite['com.soundposter.gig_start_time'].value  : 0
        var gigB = (b.composite.hasOwnProperty('com.soundposter.gig_start_time')) ? b.composite['com.soundposter.gig_start_time'].value  : 0
        if (gigA < gigB) // sort string ascending
          return -1
        if (gigA > gigB)
          return 1
        return 0 //default return value (no sorting) **/
    }

    // compare "a" and "b" in some fashion, and return -1, 0, or 1
    this.topic_sort = function (a, b) {
        // console.log(a)
        var nameA = a.value
        var nameB = b.value
        if (nameA < nameB) // sort string ascending
          return -1
        if (nameA > nameB)
          return 1
        return 0 //default return value (no sorting)
    }

    // compare "a" and "b" in some fashion, and return -1, 0, or 1
    this.track_sort = function (compositeA, compositeB) {
        var trackA = compositeA.composite['com.soundposter.ordinal_number']
        var trackB = compositeB.composite['com.soundposter.ordinal_number']
        if (trackA == undefined) {
          if (debugControls) console.log("trackA undefined ordinal number value")
          return -1
        } else if (trackB == undefined) {
          if (debugControls) console.log("trackB undefined ordinal number value")
          return 1
        }
        //
        var valueA = trackA.value
        var valueB = trackB.value
        if (valueA < valueB) // sort numeric ascending?
            return -1
        if (valueA > valueB)
            return 1
        return 0 //default return value (no sorting)
    }

    this.check_this = function(profile, webalias) {

        var loaded = undefined
        try {
            loaded = request("GET", "/poster/" + profile + "/" + webalias)
        } catch (err) {
            loaded = undefined
            if (err.code == STATUS_ACCESS_DENIED) {
                // console.log("Soundposter not published, access denied.")
                poster.render_full_message("The delicate soundposter you requested was not yet published world wide "
                    + "by its creator.")
            } else if (err.code == STATUS_NOT_FOUND) {
                // console.log("Soundposter for profile not found.")
                poster.render_full_message("It looks like something is wrong with the letters in the "
                    + "address bar of your browser. We don't know of any soundposter und this web-address.")
            } else if (err.code == STATUS_INTERNAL) {
                // console.log("Internal Server Error.")
                poster.render_full_message("Oops, upside your head, we say Ooops inside *our* head!<br/>"
                    + "Something went wrong, we' just send ourselves a report of how we broke things up and will"
                    + "fix this stage as soon as possible.<br/>")
            }
        }

        return loaded
    }

    /* this.noWay = function () {
        window.location.href = host
    } **/

    this.render_full_message = function (message) {
        $(".map-start").remove()
        // render these messages also in mobile style, where there's no sp-bar
        $("#sp-bar").html("<div class=\"error-message\"><br/>"+ message +"<br/><br/>"
            + "<span><a href=\""+ host +"\" class=\"btn ok\">Ok, no problem.</a></span>"
            + "<span><a href=\"javascript:poster.noWay()\" class=\"btn no-way\">Are you kidding me!?</a></span></div>")
    }

    this.get_topic_by_id = function(topic_id, fetch_composite) {
        return request("GET", "/core/topic/" + topic_id + "?fetch_composite=" + fetch_composite)
    }

    /**
     * Retrieves and returns the URL topic for the given Sound.
     * If there is no such URL topic undefined is returned.
     */
    this.get_audiofile_url = function (sound) {

        // var audiofile = this.get_topic_by_id(url_topic.id, true)
        var audiofile = this.load_sound_from_local_setlist(sound.id)
        if (audiofile.composite["dm4.webbrowser.url"] != undefined) {
            if (debugModel) console.log("fetched url: " + audiofile.composite["dm4.webbrowser.url"].value)
            return audiofile.composite["dm4.webbrowser.url"].value
        }
        return undefined
    }

    /**
     * Retrieves and returns the URL topic for the given Sound.
     * If there is no such URL topic undefined is returned.
     */
    this.get_gig_time = function (sound) {

        // var audiofile = this.get_topic_by_id(url_topic.id, true)
        var gig_start_time = this.load_sound_from_local_setlist(sound.id)
        if (gig_start_time.composite["com.soundposter.gig_start_time"] != undefined) {
            if (debugModel) console.log("fetched gig_time: " + gig_start_time.composite["com.soundposter.gig_start_time"].value)
            return gig_start_time.composite["com.soundposter.gig_start_time"].value
        }
        return undefined
    }

    /**
     * Retrieves and returns the URL-Source Information for the given Sound.
     * If there is no such URL topic undefined is returned.
     */
    this.get_source_info = function (sound) {

        var audiofile = this.load_sound_from_local_setlist(sound.id)
        if (audiofile.composite["com.soundposter.source_page"] != undefined) {
            if (debugModel) console.log("fetched source_page: " + audiofile.composite["com.soundposter.source_page"].value)
            console.log(audiofile.composite)
            return audiofile.composite["com.soundposter.source_page"].value
        }
        return undefined
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

    this.handle_resize = function() {
        poster.centerX = (poster.viewport_width() / 2) - 55 // center label
        poster.centerY = (poster.viewport_height() / 2) - 30
        if (debugLayout) console.log("posterCenterX: " + poster.centerX + " posterCenterY: " + poster.centerY)
    }

    this.viewport_height = function () {
        if (self.innerHeight) {
            return self.innerHeight;
        }
        if (document.documentElement && document.documentElement.clientHeight) {
            return jQuery.clientHeight;
        }
        if (document.body) {
            return document.body.clientHeight;
        }
        return 0;
    }

    this.viewport_width = function() {
        if (self.innerWidth) {
            return self.innerWidth
        }
        if (document.documentElement && document.documentElement.clientWidth) {
            return jQuery.clientWidth
        }
        if (document.body) {
            return document.body.clientWidth
        }
        return 0
    }

    this.handle_back = function (e) {
        if (e.state != null) {
            if (e.state.id != undefined) poster.show_track_by_id(e.state.id)
        }
    }

}
