
// var host = ""
var STATUS_INTERNAL = "Internal Server Error"
var STATUS_NOT_FOUND = "Not Found"
var STATUS_ACCESS_DENIED = "Unauthorized"

var poster = new function () {

    this.historyApiSupported = window.history.pushState
    this.mod = undefined
    this.data = undefined
    this.playlist = undefined
    this.sounds = undefined
    this.texts = undefined
    this.current = undefined // obsolete?
    this.selected_track = undefined
    this.selected_track_name = "No track selected"
    this.now_playing = false
    this.rendering = {'sounds': false, 'labels': false, 'events' :false}
    this.path = undefined

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

    this.initialize = function(meta, graphicUrl, hyperlink, setlist, trackId) {

        poster.data = meta
        poster.data.setlist = setlist
        //
        this.perform_audio_check()
        // jplayer (flash-fallback) integration
        this.initialize_player()
        // parse and render support-links
        this.initialize_outlinks(hyperlink)
        // initialize nodes
        this.initialize_nodes() // loads playlist and rendering_options

        // initialize poster graphic
        $container = $('div.postergraphic')
        $image = $('img.graphic')
        $image.attr('src', graphicUrl)
        $image.load(function(e) {
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
            poster.show_graphics()

            poster.path = "/posterview/" + username + "/" + webalias
            if (trackId != 0) {
                // dive deep into a soundposter
                poster.selected_track = poster.get_viz_by_id(trackId)
                // maybe we dont wanna start the audio immediately but render nice interactives
                var pos = poster.show_selected_track_by_id(trackId)
                // hint: differ better between set_selected_track and show_selected_track and play_selected_track
                poster.show_interactives(poster.play_selected_track, pos)
            } else {
                // start soundposter
                poster.show_interactives(poster.play_from_start)
            }
            return null
        })

        if (poster.mod.history) window.addEventListener("popstate", poster.handle_back)
        // $('a#postername').attr('href', poster.path) // fixme: go to start position

    }

    this.initialize_nodes = function () {

        // sort and initialize elements
        this.load_playlist(this.data) // fixme: do server side

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
                } else  if (rendering_options[i].uri === "com.soundposter.display_text_labels") {
                    poster.rendering.labels = true
                    this.show_texts()
                }
                if (debugLayout) console.log(rendering_options[i].uri)
            }
        }
    }

    this.initialize_outlinks = function (hyperlink) {

        if (hyperlink.composite.hasOwnProperty('dm4.webbrowser.url')) {
            poster.data.buylink = hyperlink.composite['dm4.webbrowser.url'].value;
            poster.data.buylabel = hyperlink.composite['dm4.webbrowser.web_resource_description'].value;
            poster.data.buylabel = poster.data.buylabel.toString() // .replaceAll("<p>", "").replaceAll("</p>", "")
        }
        $('a.support-link').text(poster.data.buylabel)
        $('a.support-link').attr("href", poster.data.buylink)
    }

    this.initialize_player = function () {

        $player = $("#soundsystem").jPlayer({
            ready: function () {
                if (debugControls) console.log("ready... ")
            },
            timeupdate: function(event) {
                // todo: animate with event.jPlayer.status.currentPercentRelative
                $('.timer').text($.jPlayer.convertTime(event.jPlayer.status.currentTime))
            },
            play: function(event) {
                // keep state current
                poster.now_playing = true
                // some gui manipulation
                $notifications.empty()
                $('.timer').show()
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
                // keep on playing
                poster.play_next_track()
            },
            error: function(event) {
                //
                if (debugControls) {
                    console.log("jPlayer:error playing stream... ")
                    console.log(event)
                }
                //
                if (event.jPlayer.error.type == "e_url_not_set") {
                    console.log("ERROR: Sound-Resource is not yet set.")
                } else if (event.jPlayer.error.type == "e_url") {
                    console.log('ERROR: An error occured during requesting media source url.')
                    poster.show_modal_notification("We cannot access the media anymore, probably the sound was moved."
                        + "<br/>The author of this soundposter will be notified.<br/>")
                } else if (event.jPlayer.error.type == "e_flash") {
                    console.log('ERROR: An error occured during flash-playback of media file.')
                } else if (event.jPlayer.error.type == "e_no_support") {
                    console.log('WARNING: Browser does not support any format supplied.')
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
            swfPath: "/com.soundposter.website/script/js",
            supplied: "mp3, m4a",
            solution: "html, flash",
            errorAlerts: false
        });

        $('.jp-next').click(poster.play_next_track)
        $('.jp-prev').click(poster.play_prev_track)

    }

    this.perform_audio_check = function() {

        // initialize modernizr
        poster.mod = Modernizr

        $notifications = $('div.notifications') // initialized globally
        $notifications.empty()
        for (var key in poster.mod.audio) {
            var value = poster.mod.audio[key]
            if (value === "") value = "Not supported"
            poster.show_notification(key + ': ' + value)
        }
    }

    this.show_notification = function (text) {
        // todo: if notification area is already visible, do not animate it
        var $message = $('<div>').text(text)
            $message.fadeIn('fast').delay(2000).fadeOut('slow')
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

    this.show_interactives = function(click_handler, pos) {
        /* var r = Raphael("interactives", 600, 600),
            R = 200,
            init = true,
            param = {stroke: "#fff", "stroke-width": 30},
            hash = document.location.hash,
            marksAttr = {fill: hash || "#444", stroke: "none"} **/
        // Get orientation
        var graphicRadius = 50
        var graphicX = $image.width() / 2 + (graphicRadius / 2)
        var graphicY = $image.height() / 2 + (graphicRadius / 2)
        poster.paper = Raphael("interactives", 6321, 6321)
        // poster.paper.image(graphicUrl, 1000, 1000, 2000, 2000)
        // and lets place our interactives not in the center of the postergraphic, but at the spot of the sound
        if (pos != undefined) {
            graphicX = pos.x + 55
            graphicY = pos.y - 70
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
            circle.animate({"r" : 55}, 25, "easeIn")
            circle.animate({"stroke-opacity" : .9}, 75, "easeIn")
            circle.animate({"stroke-width" : 8}, 100, "easeIn")
            arrow.transform("s1.1")
            circle.animate({"font-size" : 13}, 2000, "linear", function() { // dummy animation for timed fade-out
                circle.animate({"r" : 50}, 100, "easeOut")
                circle.animate({"stroke-opacity" : .7}, 100, "easeOut")
                circle.animate({"stroke-width" : 6}, 100, "easeOut")
                arrow.transform("");
            })
        }

        function ease_out() {
            // circle.animate({"r" : 40}, 200, "easeIn")
            // circle.glow( {color: "#333", width: 20} )
        }

    }

    this.show_texts = function() {
        if (this.texts != undefined) {
            //
            for (var item in this.texts) {
                var text = this.texts[item]
                var visualization = text['visualization']
                var itemX = visualization['dm4.topicmaps.x'].value
                var itemY = visualization['dm4.topicmaps.y'].value
                var element = "<div class=\"postertext\" style=\"position: absolute; top:" + itemY + "px; left: " + itemX
                + "px;\">" + text.value + "</div>";
                $(".postergraphic").append(element)
            }
        }
    }

    this.show_sounds = function() {
        if (this.playlist != undefined) {
            //
            if (debugModel) console.log(this.playlist)
            for (var item in this.playlist) {
                var song = this.playlist[item]
                var visualization = song['visualization']
                var itemX = visualization['dm4.topicmaps.x'].value
                var itemY = visualization['dm4.topicmaps.y'].value
                var $element = $("<div id=\"" + song.id + "\" class=\"posteritem\" style=\"position: absolute; top:"
                    + itemY + "px; left: " + itemX + "px;\">" + song.value + "</div>");
                    $element.click(function(e) {
                        // sound click handler
                        console.log("clicked... " + e.target.id)
                        if (poster.selected_track == undefined) {
                            console.log("Compelte Start (Visualize and then play): Settg sound id to => " + e.target.id)
                            poster.set_sound_visuals_by_id(e.target.id)
                            poster.play_selected_track()
                        } else if (poster.selected_track.id == e.target.id) {
                            if (poster.now_playing) {
                                poster.pause_selected_track()
                            } else {
                                console.log("not playing... " + e.target.id)
                                // continue with current track
                                $player.jPlayer("play")
                            }
                        } else {
                            console.log("Kickstart (Visualized): Settg sound id to => " + e.target.id)
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
        $('#jp_container_1').fadeIn({duration: 900});
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
                duration: 240, specialEasing: {width: 'linear', height: 'easeOutBounce'}
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

    this.show_selected_track_by_id = function (id) {
        var track = poster.get_viz_by_id(id)
        // animate soundposter
        var visualization = track['visualization']
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
        if (poster.sounds.length >= 1) {
            nextTrack = poster.sounds[0] // play first item in our (alredy sorted) sequence of sounds
        }
        if (nextTrack != undefined) {
            // #### dm4c.show_topic(dm4c.fetch_topic(nextTrack.id), "show", false, true)
            poster.selected_track = poster.get_viz_by_id(nextTrack.id)
            poster.play_selected_track()
            poster.toggle_interactives()
        }

    }

    // This is the main "play" method which every other method uses, after having set poster.selected_track
    this.play_selected_track = function() {

        // animate soundposter
        poster.show_selected_track_by_id(poster.selected_track.id)

        // get metadada and play stream
        var address = poster.get_audiofile_url(poster.selected_track)
        if (debugControls) console.log("Setting MediaSource to \"" + address+ "\"")
        if (address != undefined) {
            //
            poster.show_track_selection() // render title (if wanted)
            poster.show_buttons() // render controls
            //
            $player.jPlayer("setMedia", {mp3: address})
            $player.jPlayer("play")
            // just push track played into browser history
            if (poster.mod.history) {
                var state = {"id" : poster.selected_track.id}
                var title = poster.selected_track.value + " - " + poster.data.value+ " - soundposter.com/" + webalias
                history.pushState( state, title, poster.path + "/" + poster.selected_track.id)
            } else {
                console.log("WARNING: Your browser has no history support, so you cannot deep link into tracks.")
            }
            // some analytics rubbish
            // piwikTracker.trackGoal(1, sound_name);
            // some gui rubbish after each play
            poster.hide_interactives()
            $('#' + poster.selected_track.id).addClass('played')
            // $('.map-info').html("♪ " + this.selected_track.value)
            // $('.map-info').fadeIn(500).delay(2700).fadeOut(500)
            // $('.map-info').hide()
        } // fixme: what to do if server is down?
    }

    this.play_next_track = function () {
        var idx = poster.get_current_sound_idx()
        var track = poster.sounds[idx]
        if (track != undefined) {
            jQuery('#' + track.id).removeClass('played')
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
            jQuery('#' + track.id).removeClass('played')
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
            $('#' + played_item.id).removeClass('played')
        }
        for (var track in this.sounds) {
            var item = this.sounds[track]
            if (item.id == soundId) {
                // note: sp.selected_track must be the playlist-item (with a visualization-property)
                this.selected_track = this.get_viz_by_id(item.id)
                // animate picture..
                // fixme: update song title: document.title = sp.tape.value + " - soundposter/"
            }
        }
    }


    /** when topicmaps are changed, this must be called..needs some topicmap_loaded_hook **/
    this.load_playlist= function(topicmap) {
        // get selected topicmap
        poster.playlist = new Array()
        poster.sounds = new Array()
        //
        if (topicmap != undefined) {
            // iterate through topics
            for (var topic in topicmap.topics) { // (function(topic) {
                // access the topic's properties:
                if (topicmap.topics[topic].type_uri == "com.soundposter.sound") {
                    var topic = topicmap.topics[topic];
                    if (topic['visualization']['dm4.topicmaps.visibility'].value) {
                        // playlist [topic.id, topic['visualization']]
                        poster.playlist.push(topic); // the visual setlist
                        // load full composite of each sound-item
                        // fixme: fetch from our new setlist instead
                        // var sound = poster.get_topic_by_id(topic.id, true)
                        var sound = poster.load_sound_from_local_setlist(topic.id)
                        if (sound == null) throw new Error("Error while populating setlist.")
                        poster.sounds.push(sound); // the sound-stream meta data setli
                    }
                } else if (topicmap.topics[topic].type_uri == "com.soundposter.text") {
                    poster.texts.push(topicmap.topics[topic]);
                }
            }
            poster.playlist.sort(this.topic_sort); // alphabetical ascending
            poster.sounds.sort(this.track_sort); // ordinal number sorting ascending
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

        return loaded
    }

    this.initializeThis = function(profile, posterAlias) {

        var loaded = undefined

        try {
            loaded = request("GET", "/poster/" + profile + "/" + posterAlias)
        }catch (err) {
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

    /* this.noWay = function () {
        window.location.href = host
    } **/

    this.renderFullMessage = function (message) {
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
    this.get_audiofile_url = function (url_topic) {

        var audiofile = this.get_topic_by_id(url_topic.id, true)
        if (audiofile.composite["dm4.webbrowser.url"] != undefined) {
            if (debugModel) console.log("fetched url: " + audiofile.composite["dm4.webbrowser.url"].value)
            return audiofile.composite["dm4.webbrowser.url"].value
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
            poster.show_selected_track_by_id(e.state.id)
        }
    }

}
