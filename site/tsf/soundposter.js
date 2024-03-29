var sp = new function() {

    this.maptape = {} // empty topicmap object
    this.offsetX = 0
    this.offsetY = 0;
    this.centerX = 0;
    this.centerY = 0;
    this.all = []
    // 
    this.my_jPlayer = undefined
    // some play utility model
    this.selected_track = undefined
    this.latestTrack = undefined
    this.isPlaying = false
    this.playlist = new Array()
    this.texts = new Array()
    // this.images_uri = "http://localhost:8080/proxy/file:"
    // this.core_service_uri = "http://mikromedia.de/sp"
    this.core_service_uri = "http://soundposter.com/service"
      // this.core_service_uri = "http://localhost/sp/service"
    this.images_uri = "http://soundposter.com:8080/proxy/file:"
    // Notes for Android: Webkit-Settings needs to be changed;
    // I found out that at least the Option "Activate plugins" should be set to "Always", which is stupid, but works!
    // Also, zoom-level should be set to at least "Middle" or "Near" to show the correct style, which is also stupid!
    // Website Screen rotation on device rotation is also optional and need to be activated in the browser setting!
    // 
    this.load = function(sp_id) {
        // fixme: find another quick fix for one stupid cross domain policy.. issue:
        if (parent.window.location.href != undefined) { // catches crossdomain failure, when a sp is embedded
            if (parent.window.location.href.lastIndexOf("http://www.", 0) === 0) {
                var url = parent.window.location.href;
                var newUrl = url.substr(11);
                parent.window.location.href = 'http://'+newUrl;
            }
        }
        sp.render_sp_header()
        jQuery("#cp_container_1").remove()
        // jQuery("#soundposter").css("opacity", 0.6);
        //
        sp.handleResize()
        jQuery(window).resize(sp.handleResize)
        //
        var poster = sp.get_background_url(sp_id)
        if (poster != undefined) {
          var poster_full = sp.get_topic_by_id(poster.id, true)
          sp.render_sp_image(poster_full)
        }
        // 
        var sound = sp.get_topicmap_by_id(sp_id, false)
        var info = sp.maptape = sp.get_topic_by_id(sp_id, false)
        sp.load_playlist(sound)
        sp.render_sp_player()
        sp.render_map_info(info)
        sp.render_sp_footer(sp_id)
        sp.render_texts()
        // sp.render_tracks()
        // jQuery("#soundposter").css("opacity", 1)
    }

    this.load_all_poster = function(sp_id) {
        //
        // sp.render_sp_header()
        // jQuery("#cp_container_1").remove()
        // 
        var soundposter = sp.get_topics_by_type("dm4.topicmaps.topicmap", false)
        // TODO: sp.render_poster_listing(soundposter)
        // 
        sp.all = soundposter.items;
    }

    /** when topicmaps are changed, this must be called..needs some topicmap_loaded_hook ***/
    this.load_playlist = function(topicmap) {
      // get selected topicmap
      sp.playlist = new Array()
      // 
      if (topicmap != undefined) {
        // iterate through topics
        for (topic in topicmap.topics) { // (function(topic) {
          // access the topic's properties:
          if (topicmap.topics[topic].type_uri == "dm4.soundposter.track") {
            // var fullTrack = sp.get_topic_by_id(topicmap.topics[topic].id, true);
            // sp.playlist.push(fullTrack);
            sp.playlist.push(topicmap.topics[topic]);
            // console.log("trackNummer = > " + fullTrack.composite['com.soundposter.track_number'].value)
          } else if (topicmap.topics[topic].type_uri == "com.soundposter.text") {
            sp.texts.push(topicmap.topics[topic]);
          }
        }
        sp.playlist.sort(sp.topic_sort); // alphabetical ascending
        // console.log(sp.playlist)
      }
    }
    
    this.render_texts = function() {
        if (sp.texts != undefined) {
          //
          for (item in sp.texts) {
            var text = sp.texts[item]
            var visualization = text['visualization']
            var itemX = visualization['dm4.topicmaps.x'].value
            var itemY = visualization['dm4.topicmaps.y'].value
            var element = "<div class=\"postertext\" style=\"position: absolute; top:" + itemY + "px; left: " + itemX
              + "px;\">" + text.value + "</div>";
            jQuery(".postercontainer").append(element)
          }
        }
    }
    
    this.render_tracks = function() {
        if (sp.playlist != undefined) {
          //
          for (item in sp.playlist) {
            var song = sp.playlist[item]
            var visualization = song['visualization']
            var itemX = visualization['dm4.topicmaps.x'].value
            var itemY = visualization['dm4.topicmaps.y'].value
            var element = "<div id=\"" + song.id + "\" class=\"posteritem\" style=\"position: absolute; top:"
              + itemY + "px; left: " + itemX + "px;\">" + song.value + "</div>";
            jQuery(".postercontainer").append(element)
          }
        }
    }
    
    this.render_poster_list = function() {
        if (sp.all != undefined) {
          //
          var listing = "<ul class=\"all-poster-listing\">";
          for (item in sp.all) {
            var label = sp.all[item].value
            var topicId = sp.all[item].id
            var linkitem = "<li><a href=\"http://soundposter.com:8080/topicmap/" + topicId + "\">" + label + "</a></li>";
            listing += linkitem
          }
          listing += "</ul>";
          jQuery("#soundposter").append(listing)
        } else {
          alert("SP All undefined..")
        }
    }
    
    this.render_map_info = function (info) {
      document.title = info.value + " - soundposter/"
      jQuery("#mapName").html('<b id="lgn" title="soundposter/">sp/&nbsp;</b><b class="mapname">"' + info.value + '"</b>')
    }
    
    this.render_sp_image = function (image) {
      if (image != undefined) {
        var file_path = image['composite']['dm4.files.path'].value
        var file_resource = sp.images_uri + file_path
        // console.log(file_resource)
        jQuery(".postercontainer").append('<img src="'+ file_resource + '" class="postergraphic">')
        // 
        jQuery(".postergraphic").load(function() {
            // getting graphic size...
            var graphicWidth = jQuery("img.postergraphic").width()
            var graphicHeight = jQuery("img.postergraphic").height()
            // adjusting graphic-container size
            jQuery(".postercontainer").width(graphicWidth)
            jQuery(".postercontainer").height(graphicHeight)
            // focus the center of the graphic in the center of the windows viewport
            var moveX = sp.centerX - (graphicWidth / 2) - sp.offsetX
            var moveY = sp.centerY - (graphicHeight / 2) - sp.offsetY
            // 
            sp.movePosterAbout(moveX, moveY)
        })
        // 
        jQuery(".postercontainer").draggable({
          stop: function(event, ui) {
              // console.log("drag.offsetX:" + sp.offsetX + " drag.offsetY: " + sp.offsetY)
              sp.offsetX = ui.position.left;
              sp.offsetY = ui.position.top;
              // console.log("drag.newOffsetX:" + sp.offsetX + " drag.newOffsetY: " + sp.offsetY)
          }
        });
      }
    }
    
    this.movePosterAbout = function(aboutX, aboutY) {
        // console.log("move.aboutX:" + aboutX + " aboutY: " + aboutY)
        jQuery(".postercontainer").animate(
            {
                left: aboutX, top: aboutY
            }, {
                duration: 720, specialEasing: { width: 'linear', height: 'easeOutBounce' } 
            }
        )
        sp.offsetX = sp.offsetX + aboutX
        sp.offsetY = sp.offsetY + aboutY
        // console.log("initial.centerX:" + sp.offsetX + " centerY: " + sp.offsetY)
    }
    
    this.movePosterTo = function(toX, toY) {
        // console.log("move.toX:" + toX + " toY: " + toY)
        jQuery(".postercontainer").animate(
            {
                left: -(toX - sp.centerX), top: -(toY - sp.centerY)
            }, {
                duration: 720, specialEasing: { width: 'linear', height: 'easeOutBounce' } 
            }
        )
        // console.log("sp.offsetX:" + sp.offsetX + " offsetY: " + sp.offsetY)
        sp.offsetX = toX
        sp.offsetY = toY
        // console.log("=> sp.newOffsetX:" + sp.offsetX + " newOffsetY: " + sp.offsetY)
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
    this.track_sort = function (a, b) {
        // console.log(a)
        var trackA = a.composite['com.soundposter.track_number']
        var trackB = b.composite['com.soundposter.track_number']
        if (trackA == undefined) {
          return -1
        } else if (trackB == undefined) {
          return 1
        }
        // 
        var valueA = trackA.value
        var valueB = trackB.value
        if (valueA < valueB) // sort string ascending
            return -1
        if (valueA > valueB)
            return 1
        return 0 //default return value (no sorting)
    }
    
    this.render_sp_header = function () {
        // 
        var header = '<div id="sp-bar"></div>'
        var logo = '' // class="title shadowed"
        jQuery(header).insertAfter("#soundposter")
        var contact = '<div id="contact"><i>mail@soundposter.com</i></div>'
        jQuery(contact).insertAfter("#soundposter")
        // 
        // jQuery(logo).insertAfter(".jp-volume-max") // ### fIXME not present...
    }
    
    this.render_sp_footer = function (id) {
        var info = sp.get_related_topics(id, 'dm4.webbrowser.web_resource')
        if (info.items.length == 0) {
          return;
        }
        var supportLink = sp.get_topic_by_id(info.items[0].id, true)
        var label = "Support Artist"
        var url = 'http://www.soundposter.com'
        // url
        if (supportLink.composite.hasOwnProperty('dm4.webbrowser.url')) {
          url = supportLink.composite['dm4.webbrowser.url'].value
        }
        // label
        if (supportLink.composite.hasOwnProperty('dm4.webbrowser.web_resource_description')){
          label = supportLink.composite['dm4.webbrowser.web_resource_description'].value
        }
        // 
        var header = '<div id="support-link-2"><a href="' + url + '">' + label + '</a></div>'
        jQuery(header).insertAfter("#soundposter")
    }
    
    this.render_sp_player = function () {
      // 
      var html = '<div id="jquery_jplayer"></div>'
      var container = '<div id="jp_container" class="player">' + 
        '<ul class="info-area">' +
          '<li id="mapName"></li>' +
          '<li class="myTrackname">Select:</li>' +
          '<li class="jp-current-time"></li>' +
          '<li class="jp-duration"></li>' +
        '</ul>' + 
        '<div class="map-info">' +
          '<img id="start-mobile-play" src="images/playb.png" width="25">' +
          '<b id="lgn" title="soundposter/">sp/</b><b class="mapname">' + sp.maptape.value + '</b>' +
        '</div>' +
        '<ul class="control-area">' +
          '<li><a class="jp-prev" href="#"><img src="images/prevb.png" height="24" title="Previous"/></a></li>' +
          '<li><a class="jp-play" href="#"><img src="images/playb.png" height="24" title="Play"/></a></li>' +
          '<li><a class="jp-pause" href="#"><img src="images/pauseb.png" height="24" title="Pause"/></a></li>' +
          /** '<li><a class="jp-stop" href="#">Stop</a></li>' + **/
          '<li><a class="jp-next" href="#"><img src="images/nextb.png" height="24" title="Next"/></a></li>' +
          '<div class="jp-progress"><div class="jp-seek-bar"><div class="jp-play-bar"></div></div></div>' +
        // '</ul>' +
        // '<ul>' +
          /** '<li>Volume:</li>' + **/
          /** '<li><a class="jp-mute" href="#">Mute</a></li>' +
          '<li><a class="jp-unmute" href="#">Unmute</a></li>' + **/
          //'<li> <a class="jp-volume-bar" href="#">|&lt;----------&gt;|</a></li>' +
          /** '<li><a class="jp-volume-max" href="#">Max</a></li>' + **/
        '</ul>' +
      '</div>'
      // 
      jQuery(html).insertAfter("#soundposter")
      jQuery("#soundposter").append(container)
      // updateMapInfo()
      // maximizeCanvas()
      jQuery(".map-info").click(function(e) { sp.playFromStart() }) // used by mobile style..
      jQuery(".myTrackname").click(function(e) { sp.toggleTrackList() })
      jQuery(".jp-next").click(function(e) { sp.playNextTrack() })
      jQuery(".jp-prev").click(function(e) { sp.playPrevTrack() })
      // 
      // "global"-DOM attributes
      my_jPlayer = jQuery("#jquery_jplayer"),
      trackName = jQuery(".myTrackname"), 
      my_playState = jQuery("#jp_container .play-state"), 
      my_extraPlayInfo = jQuery("#jp_container .extra-play-info");

      opt_play_first = false,
      opt_auto_play = true, 
      opt_text_playing = "Now playing", // Text when playing
      opt_text_selected = "Track selected"; // Text when not playing
      // 
      jQuery.jPlayer.timeFormat.padMin = false
      jQuery.jPlayer.timeFormat.padSec = false
      jQuery.jPlayer.timeFormat.showHour = true
      // jQuery.jPlayer.timeFormat.sepHour = "h "
      // jQuery.jPlayer.timeFormat.sepMin = "min "
      // jQuery.jPlayer.timeFormat.sepSec = "sec"
      jQuery.jPlayer.timeFormat.sepHour = "."
      jQuery.jPlayer.timeFormat.sepMin = ":"
      jQuery.jPlayer.timeFormat.sepSec = ""
      jQuery.jPlayer.errorAlerts = true
      // 
      // Initialize the play state text
      my_playState.text(opt_text_selected);
      // Instance jPlayer
      my_jPlayer.jPlayer({
        ready: function () {
         // jQuery("#jp_container .track-default").click()
        },
        timeupdate: function(event) {
          if (my_jPlayer.jPlayer.event !== undefined) {
            my_extraPlayInfo.text(parseInt(my_jPlayer.jPlayer.event.jPlayer.status.currentPercentAbsolute, 10) + "%")
          }
          trackName.text(sp.selected_track.value)
        },
        play: function(event) {
          my_playState.text(opt_text_playing)
          sp.isPlaying = true
          document.title = "♪ " + document.title
          // jQuery('.map-info').fadeOut(500)
        },
        loadedmetadata: function(event) {
          // TODO: loaded some metadata... 
        },
        pause: function(event) {
          my_playState.text(opt_text_selected)
          sp.isPlaying = false
          document.title = sp.maptape.value + " - soundposter/"
          jQuery('.map-info').html("♪ " + sp.selected_track.value)
          /* *jQuery('.map-info').fadeIn(500) */
        },
        ended: function(event) {
          my_playState.text(opt_text_selected)
          sp.isPlaying = false
          document.title = sp.maptape.value + " - soundposter/"
          sp.playNextTrack()
        },
        error: function(event) {
          // if (event.jPlayer.error.type == "e_url_not_set") {
          if (event.jPlayer.error.type == "e_url_not_set") {
            sp.playNextTrack()
          } else if (event.jPlayer.error.type == "e_flash") {
            // ###
              alert('Sorry, an error occured during playback of media file. ErrorCode: \"' + 
                event.jPlayer.error.type + '\"')
          } else {
            // ####
            // alert('Sorry, an unknown error occured with our jPlayer setup. Please mail the following ' + 
             // 'error code to "' + event.jPlayer.error.type + '" mail@soundposter.com. And thank you for helping our software to get better!')
            // sp.playFromStart()
          }
          // backup, unselection of highlighted track
        },
        swfPath: "js",
        cssSelectorAncestor: "#jp_container",
        supplied: "mp3, m4a",
        wmode: "window",
        solution: "flash, html"
      }) // catch flash error warning
      initialized = true
    }
     
    this.play_selected_track = function() {
      sp.hideStartButton();
      // animate
      var visualization = sp.selected_track['visualization']
      var songX = visualization['dm4.topicmaps.x'].value// + 600
      var songY = visualization['dm4.topicmaps.y'].value// + 600
      // console.log("sp.offsetX: " + sp.offsetX + " offsetY: " + sp.offsetY)
      // console.log("  => songX: " + songX + " : songY: " + songY)
      // sp.movePosterAbout(songX + sp.offsetX, songY + sp.offsetY)
      sp.movePosterTo(songX, songY)
      var address = sp.get_audiofile_url(sp.selected_track)
      if (address != undefined) {
        my_jPlayer.jPlayer("setMedia", { mp3: address.value })
        my_jPlayer.jPlayer("play")
        latestTrack = sp.selected_track
        // if something was played, remove start button from the ui..
        jQuery('.map-info .start-button').remove()
        jQuery(".map-info").unbind('click');
        // 
        trackName.text(sp.selected_track.value)
        // 
        jQuery('#' + sp.selected_track.id).addClass('played')
        jQuery('.map-info').html("♪ " + sp.selected_track.value)
        // jQuery('.map-info').fadeIn(500).delay(2700).fadeOut(500)
        jQuery('.map-info').hide()
      }
    }
    
    this.playTrackById = function (topicId) {
      var idx = sp.getCurrentTrack()
      var track = sp.playlist[idx]
      if (track != undefined) {
        jQuery('#' + track.id).removeClass('played')
      }
      for (var track in sp.playlist) {
        var topic = sp.playlist[track]
        if (topic.id == topicId) {
          // ### select this topic.. /animate picture..
          sp.selected_track = topic
          sp.play_selected_track()
          jQuery("#trackMenu").remove()
          document.title = sp.maptape.value + " - soundposter/"
        }
      } 
    }
    
    // start "file:///home/malted/source/v5/deepamehta-audioposter/target/deepamehta-audioposter-1.0.jar"
    this.playNextTrack = function () {
      var idx = sp.getCurrentTrack()
      var track = sp.playlist[idx]
      if (track != undefined) {
        jQuery('#' + track.id).removeClass('played')
      }
      var nextTrack = undefined
      // 
      if (idx+1 == sp.playlist.length) {
        nextTrack = sp.playlist[0] // play first again..
      } else {
        nextTrack = sp.playlist[idx+1]
      }
      // 
      if (nextTrack != undefined) {
        // ### dm4c.show_topic(dm4c.fetch_topic(nextTrack.id), "show", false, true)
        sp.selected_track = nextTrack
        trackName.text(sp.selected_track.value)
        sp.play_selected_track()
        document.title = sp.maptape.value + " - soundposter/"
      }
    }
    
    this.playFromStart = function () {
      sp.hideStartButton();
      // start playing and hide start button..
      var nextTrack = undefined
      if (sp.playlist.length >= 1) {
        nextTrack = sp.playlist[0] // play first item in sequence..
        // console.log("playing " + nextTrack)
      }
      // 
      if (nextTrack != undefined) {
        // #### dm4c.show_topic(dm4c.fetch_topic(nextTrack.id), "show", false, true)
        sp.selected_track = nextTrack
        trackName.text(sp.selected_track.value)
        sp.play_selected_track()
        document.title = sp.maptape.value + " - soundposter/"
      }
    }
    
    this.playPrevTrack = function () {
      var idx = sp.getCurrentTrack()
      var track = sp.playlist[idx]
      if (track != undefined) {
        jQuery('#' + track.id).removeClass('played')
      }
      var nextTrack = undefined
      // 
      if (idx == 0) {
        nextTrack = sp.playlist[sp.playlist.length-1] // play last
      } else {
        nextTrack = sp.playlist[idx-1]
      }
      // 
      if (nextTrack != undefined) {
        // ### dm4c.show_topic(dm4c.fetch_topic(nextTrack.id), "show", false, true)
        sp.selected_track = nextTrack
        trackName.text(sp.selected_track.value)
        sp.play_selected_track()
        document.title = sp.maptape.value + " - soundposter/"
      }
    }
    
    this.getCurrentTrack = function () {
      // 
      var idx = 0
      for (var track in sp.playlist) {
        // 
        var topic = sp.playlist[track]
        if (sp.selected_track != undefined) {
          // 
          if (topic.id == sp.selected_track.id) {
            // 
            return idx
          }
          idx++
        }
      }
      return idx
    }
    
    this.toggleTrackList = function () {
      if (sp.playlist.length == 0) {
        // alert("playlist empty..")
        return ""
      }
      var tracklist = jQuery("#trackMenu")
      if (tracklist.length) {
        jQuery("#trackMenu").remove()
        jQuery(".selectedTrackname").attr("class", "myTrackname") // fixme: double class standards
      } else {
        var listing = '<div id="trackMenu"><ul>'
        for (var track in sp.playlist) {
          var topic = sp.playlist[track]
          if (topic.id != undefined && sp.selected_track == undefined) {
            // list all songs.. 
            // console.log(topic)
            listing += '<li id="' + topic.id + '" class="trackItem">' + topic.value + '</li>'
          } else {
            // if one is selected, list all but the current..
            if (topic.id != undefined && topic.id != sp.selected_track.id) {
              listing += '<li id="' + topic.id + '" class="trackItem">' + topic.value + '</li>'
            }
          }
          
        }
        listing += '</ul></div>'
        jQuery(listing).insertAfter("#jp_container")
        jQuery('.trackItem').click(sp.playListedTrack);
        jQuery(".myTrackname").attr("class", "selectedTrackname") // fixme: double class standards!
      }
    }
    
    this.playListedTrack = function (e) {
      // 
      sp.playTrackById(e.target.id);
    }
    
    this.hideStartButton = function () {
      jQuery(".map-start").hide();
    }
    
    // --
    // --- 4 REST Methods to query a complete soundposter from the dm4.webservice-module
    // --
    
    this.get_topicmap_by_id = function(topic_id, fetch_composite) {
      return request("GET", "/topicmap.php?t=" + topic_id + "&fetch_composite=" + fetch_composite) // + "?" + params.to_query_string())
    }
    
    this.get_topic_by_id = function(topic_id, fetch_composite) {
      return request("GET", "/topic.php?t=" + topic_id + "&fetch_composite=" + fetch_composite) // + "?" + params.to_query_string())
    }
    
    this.get_related_topics = function(topic_id, uri, fetch_composite) {
      return request("GET", "/related.php?t=" + topic_id + '&uri=' + uri + "&fetch_composite=" + fetch_composite)
    }
    
    this.get_topics_by_type = function(uri) {
      // dm4.topicmaps.topicmap
      return request("GET", "/topics.php?uri=" + uri)
    }
    
    /**
     * Retrieves and returns the URL topic for the given Song.
     * If there is no such URL topic undefined is returned.
     */
    this.get_audiofile_url = function (url_topic) {
      // 
      var audiofiles = sp.get_related_topics(url_topic.id, "dm4.webbrowser.url").items
      // ### assoc_type_uri: "dm4.core.composition", my_role_type_uri: "dm4.core.whole", 
      // ### others_role_type_uri: "dm4.core.part", others_topic_type_uri: "dm4.webbrowser.url"
      //
      if (audiofiles.length > 1) {
          alert("WARNING: data inconsistency\n\nThere are " + audiofiles.length + " audiofiles for URL \"" +
              url_topic.value + "\" (expected is one)")
      }
      //
      return audiofiles[0]
    }
    
    /**
     * Retrieves and returns the Background Image for a given soundposter.
     * If there is no such URL, undefined is returned.
     */
    this.get_background_url = function (topicmapId) {
      // 
      var imagefiles = sp.get_related_topics(topicmapId, "dm4.files.file").items
      // ### assoc_type_uri: "dm4.core.composition", my_role_type_uri: "dm4.core.whole", 
      // ### others_role_type_uri: "dm4.core.part", others_topic_type_uri: "dm4.webbrowser.url"
      //
      if (imagefiles != undefined && imagefiles.length == 1) {
          // OK
          return imagefiles[0]
      } else if (imagefiles == undefined && imagefiles.length > 1) {
          alert("WARNING: data inconsistency\n\nThere are " + imagefiles.length + " imagefiles for URL \"" +
              topicmapId + "\" (expected is one)")
          return imagefiles[0]
      }
      //
      return undefined
    }

    this.handleResize = function() {
        sp.centerX = (sp.windowWidth() / 2) - 55 // center label
        sp.centerY = (sp.windowHeight() / 2) - 30
    }

    this.windowHeight = function () {
      if (self.innerHeight) {
        return self.innerHeight
      }
      if (document.documentElement && document.documentElement.clientHeight) {
        return jQuery.clientHeight
      }
      if (document.body) {
        return document.body.clientHeight
      }
      return 0
    }

    this.windowWidth = function () {
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

}

// --------------------------------------------------------------------------------------------------- Private Functions

/**
 * Sends an AJAX request.
 *
 * @param   data                The data to be send as the request body. This argument depends on the
 *                              content_type argument. By default the data object (key/value pairs) is
 *                              serialized to JSON. Note: pairs with undefined values are not serialzed.
 * @param   content_type        Optional: the content type of the data. Default is "application/json".
 * @param   is_absolute_uri     If true, the URI is interpreted as relative to the DeepaMehta core service URI.
 *                              If false, the URI is interpreted as an absolute URI.
 * NOTE: this is a friendly copy of a method found in dm4.webclient.utils.. at github.com/jri/deepamehta
 */
function request(method, uri, data, content_type, is_absolute_uri) {
    var status                  // "success" if request was successful
    var responseCode            // HTTP response code, e.g. 304
    var responseMessage         // HTTP response message, e.g. "Not Modified"
    var responseData            // in case of successful request: the response data (response body)
    var exception               // in case of unsuccessful request: possibly an exception
    //
    // if (LOG_AJAX_REQUESTS) dm4c.log(method + " " + uri + "\n..... " + JSON.stringify(data))
    //
    content_type = content_type || "application/json"       // set default
    if (content_type == "application/json") {
        data = JSON.stringify(data)
    }
    //
    $.ajax({
        type: method,
        url: is_absolute_uri ? uri : sp.core_service_uri + uri,
        contentType: content_type,
        data: data,
        processData: false,
        async: false,
        success: function(data, textStatus, xhr) {
            // if (LOG_AJAX_REQUESTS) dm4c.log("..... " + xhr.status + " " + xhr.statusText +
               //  "\n..... " + JSON.stringify(data))
            responseData = data
        },
        error: function(xhr, textStatus, ex) {
            // if (LOG_AJAX_REQUESTS) dm4c.log("..... " + xhr.status + " " + xhr.statusText +
               //  "\n..... exception: " + JSON.stringify(ex))
            exception = ex
        },
        complete: function(xhr, textStatus) {
            status = textStatus
            responseCode = xhr.status
            responseMessage = xhr.statusText
        }
    })
    if (status == "success") {
        return responseData
    } else {
        throw "AJAX " + method + " request failed, server response: " + responseCode +
            " (" + responseMessage + "), exception: " + exception
    }
}

