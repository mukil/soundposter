dm4c.add_plugin("com.soundposter.webapp", function() {

    var plugin_path = "/com.soundposter.webapp"

    dm4c.load_script(plugin_path + "/script/js/jquery.jplayer.min.js")
    //
    var my_jPlayer = undefined
    // some play utility model
    var latestTrack = undefined
    var isPlaying = false
    var playlist = new Array()
    var initialized = false
    var playlistLoaded = false
    var editingMode = false
    var maptape = {} // topicmap model object, once playlist was loaded..
    // 
    // define type specific commands and register them
    dm4c.add_listener('init', function (e) {
        console.log("Soundposter-Plugin.initializing(0.9) ... ")
        showSoundposterHeader()
        if (!initialized) {
            show_soundposter_player()
        }
        if (!playlistLoaded) {
            load_playlist()
        }
        // dm4c.ui.menu(undefined, "Help") 
        // dm4c.toolbar.special_menu 
    })


    // ***********************************************************
    // *** Webclient Hooks (triggered by deepamehta-webclient) ***
    // ***********************************************************


    /**
     * @param topic a CanvasTopic object
     */
    dm4c.add_listener('topic_doubleclicked', function(topic) {
      //
      if (topic.type_uri == "com.soundposter.sound") {
        playSelectedTopic()
      }
    })
    
    dm4c.add_listener('post_select_topic', function(topic) {
      //
      /** if (topic.type_uri == "com.soundposter.track") {
        // dm4 gui hacks
        maximizeCanvas()
        // showPlayer()
        // 
      } else if (topic.type_uri == "dm4.notes.note") {
        showDetailPanel()
        // hidePlayer()
      }**/
      
    })
    
    // TODO: find post_page_renderer hook to place..
    // jQuery(".field-value .topic-list").attr("style", "display: none;")
    // dm4 gui hack to hide what's related navigation 

    // ----------------------------------------------------soundposter------------------------------- Private Functions
    // called before plugin_init..
    function playSelectedTopic() {
      // 
      var address = get_audiofile_url(dm4c.selected_object)
      if (address != undefined) {
        my_jPlayer.jPlayer("setMedia", { mp3: address.value })
        my_jPlayer.jPlayer("play")
        latestTrack = dm4c.selected_object
        trackName.text(dm4c.selected_object.value)
        // var trackList = latestTrack.label
        // jQuery("#tracklist").html(trackList)
      }
    }
    
    function playTrackById(topicId) {
      for (var track in playlist) {
        var topic = playlist[track]
        if (topic.id == topicId) {
          // select this topic..
          dm4c.show_topic(dm4c.fetch_topic(topic.id), "show", false, true)
          // playSelectedTopic()
          jQuery("#trackMenu").remove()
          document.title = maptape.value + " - soundposter/"
        }
      } 
    }
    
    function playNextTrack() {
      var idx = getCurrentTrack()
      var nextTrack = undefined
      // 
      if (idx+1 == playlist.length) {
        nextTrack = playlist[0] // play first again..
      } else {
        nextTrack = playlist[idx+1]
      }
      // 
      if (nextTrack != undefined) {
        dm4c.show_topic(dm4c.fetch_topic(nextTrack.id), "show", false, true)
        latestTrack = nextTrack
        trackName.text(nextTrack.label)
        document.title = maptape.value + " - soundposter/"
      }
    }
    
    function playFromStart() {
      // 
      var nextTrack = undefined
      load_playlist() // makes updateMap info... 
      if (playlist.length >= 1) {
        nextTrack = playlist[0] // play first item in sequence..
      }
      // 
      if (nextTrack != undefined) {
        dm4c.show_topic(dm4c.fetch_topic(nextTrack.id), "show", false, true)
        latestTrack = nextTrack
        trackName.text(nextTrack.label)
        document.title = maptape.value + " - soundposter/"
      }
    }
    
    function playPrevTrack() {
      var idx = getCurrentTrack()
      var nextTrack = undefined
      // 
      if (idx == 0) {
        nextTrack = playlist[playlist.length-1] // play last
      } else {
        nextTrack = playlist[idx-1]
      }
      // 
      if (nextTrack != undefined) {
        dm4c.show_topic(dm4c.fetch_topic(nextTrack.id), "show", false, true)
        latestTrack = nextTrack
        trackName.text(nextTrack.label)
        document.title = maptape.value + " - soundposter/"
      }
    }
    
    function getCurrentTrack() {
      // 
      var idx = 0
      for (var track in playlist) {
        // 
        // if (playlist.hasOwnProperty(playlist[track])) {
        var topic = playlist[track]
        if (latestTrack != undefined) {
          // 
          if (topic.id == latestTrack.id) {
            // 
            return idx
          }
          idx++
        }
        // }
      }
      return idx
    }
    
    function showSoundposterHeader() {
      var header = '<div id="sp-bar"></div>'
      var logo = '' // class="title shadowed"
      jQuery(header).insertAfter("#split-panel")
      var contact = '<div id="contact"><i>mail@soundposter.com</i></div>'
      jQuery(contact).insertAfter("#sp-bar")
      // jQuery("#main-toolbar").attr("style", "display:none;")
      // jQuery(logo).insertAfter(".jp-volume-max") // ### fIXME not present...
    }
    
    function hideDetailPanel() {
      // jQuery("#page-content").css("width", "0px")
      jQuery("#page-content").css("display", "none")
      jQuery("#page-toolbar").css("display", "none")
      jQuery(".ui-resizable-handle").css("display", "none")
      // jQuery("#split-panel td").css("display", "none")
    }
    
    function hidePlayer() {
      jQuery(".player").hide()
      jQuery("#main-toolbar").attr("style", "display:block;")
    }
    
    function showPlayer() {
      jQuery(".player").show()
      jQuery("#main-toolbar").attr("style", "display:none;")
    }
    
    function showDetailPanel() {
      // jQuery("#page-content").css("width", "0px")
      dm4c.split_panel.set_slider_position(700)
      jQuery("#page-content").css("display", "block")
      jQuery("#page-toolbar").css("display", "block")
      jQuery(".ui-resizable-handle").css("display", "block")
    }
    
    function maximizeCanvas() {
      // dm4c.split_panel.set_slider_position(window.innerWidth)
      // dm4c.canvas.resize({"width": window.innerWidth, "height": window.innerHeight})
      // jQuery("#canvas").attr("height", window.innerHeight - 50)
    }
    
    function authenticate_user() {
      var given = jQuery("#pwd").val()
      if (given == "lms9000") {
        // jQuery("#sp-bar").hide()
        jQuery("#sp-bar").css("left", -window.innerWidth + 30)
        jQuery("#sp-bar").css("height", 32)
        showDetailPanel()
        editingMode = true
        jQuery("#my-dialog").dialog("close")
        hidePlayer()
      } else {
        var dialog ='<div id="anotherdialog" title=""><br>' + 
          '<p>Just <a href="mailto:mail@soundposter.com">ask us!</a><br/>' +
          '<small>We are currently in private beta, which means, if you want to create your own ' + 
          'soundposters we scream "yappieqzert" when we hear from you!</small></p></div>'
        // 
        dm4c.ui.dialog("anotherdialog", "", dialog, 300, undefined, function() {
          jQuery("#my-dialog").dialog("close")
          jQuery("#anotherdialog").dialog("close")
        })
        jQuery("#anotherdialog").dialog("open")
      }
    }
    
    function showDialog(message) {
      var dialog ='<div id="a-dialog" title="Something unexpected happened"><br/><br/>' + message + '</div>'
      // 
      dm4c.ui.dialog("a-dialog", "Something unexpected happened", dialog, 340)
      jQuery("#a-dialog").dialog("open")
    }
    
    function soundposterDialog() {
      if (editingMode == false) {
        // ask for editing credentials....
        var dialog ='<div id="myDialog" title="kmlkqmdASDSJf..."><br>öklsfjölqklkasps:<br/>' + 
          '<form action="#"><input type="password" id="pwd"></form></div>'
        // 
        dm4c.ui.dialog("my-dialog", "../run.sh dmc", dialog, 280, "(x!)", authenticate_user)
        jQuery("#my-dialog").dialog("open")
      } else {
        // go back to presentation mode..
        jQuery("#sp-bar").css("left", 0)
        jQuery("#sp-bar").css("height", 38)
        editingMode = false
        hideDetailPanel()
        showPlayer()
        maximizeCanvas()
      }
    }
    
    function toggleTrackList() {
      if (playlist.length == 0) {
        load_playlist()
      }
      var tracklist = jQuery("#trackMenu")
      if (tracklist.length) {
        jQuery("#trackMenu").remove()
        jQuery(".selectedTrackname").attr("class", "myTrackname")
      } else {
        var listing = '<div id="trackMenu"><ul>'
        for (var track in playlist) {
          var topic = playlist[track]
          if (topic.id != undefined && dm4c.selected_object == undefined) {
            // list all songs.. 
            listing += '<li id="' + topic.id + '" class="trackItem">' + topic.label + '</li>'
          } else {
            // if one is selected, list all but the current..
            if (topic.id != undefined && topic.id != dm4c.selected_object.id) {
              listing += '<li id="' + topic.id + '" class="trackItem">' + topic.label + '</li>'
            }
          }
          
        }
        listing += '</ul></div>'
        jQuery(listing).insertAfter("#jp_container")
        jQuery('.trackItem').click(playListedTrack);
        jQuery(".myTrackname").attr("class", "selectedTrackname")
      }
    }
    
    function playListedTrack (e) {
      // 
      playTrackById(e.target.id);
    }

    /**
     * Retrieves and returns the URL topic for the given Song.
     * If there is no such URL topic undefined is returned.
     */
    function get_audiofile_url(url_topic) {
      // 
      var audiofiles = dm4c.restc.get_topic_related_topics(url_topic.id, {
          assoc_type_uri: "dm4.core.composition",
          my_role_type_uri: "dm4.core.parent",
          others_role_type_uri: "dm4.core.child",
          others_topic_type_uri: "dm4.webbrowser.url"
      }).items
      //
      if (audiofiles.length > 1) {
          alert("WARNING: data inconsistency\n\nThere are " + audiofiles.length + " audiofiles for URL \"" +
              url_topic.value + "\" (expected is one)")
      }
      //
      return audiofiles[0]
    }
    
    function show_soundposter_player() {
      // 
      var html = '<div id="jquery_jplayer"></div>'
      var container = '<div id="jp_container" class="player">' + 
        '<ul>' +
          '<li><a class="jp-play" href="#"><img src="'+ plugin_path +'/images/playb.png" height="24" title="Play"/></a></li>' +
          '<li><a class="jp-pause" href="#"><img src="'+ plugin_path +'/images/pauseb.png" height="24" title="Pause"/></a></li>' +
          /** '<li><a class="jp-stop" href="#">Stop</a></li>' + **/
          '<li><a class="jp-prev" href="#"><img src="'+ plugin_path +'/images/prevb.png" height="24" title="Previous"/></a></li>' +
          '<li><a class="jp-next" href="#"><img src="'+ plugin_path +'/images/nextb.png" height="24" title="Next"/></a></li>' +
          '<li class="playFromStart">Start</li>' +
          '<li class="myTrackname">Select:</li>' +
          '<li class="jp-current-time"></li>' +
          '<li class="jp-duration"></li>' +
        // '</ul>' +
        // '<ul>' +
          /** '<li>Volume:</li>' + id="lgn" title="edit soundposter/">**/
          /** '<li><a class="jp-mute" href="#">Mute</a></li>' +
          '<li><a class="jp-unmute" href="#">Unmute</a></li>' + **/
          /* '<li> <a class="jp-volume-bar" href="#">|&lt;----------&gt;|</a></li>' + **/
          /** '<li><a class="jp-volume-max" href="#">Max</a></li>' + **/
          '<li id="mapName"></li>' +
        '</ul>' +
      '</div>'
      // dm4 gui hacks
      jQuery(html).insertBefore("#main-toolbar")
      jQuery("#canvas-panel").append(container)
      // updateMapInfo()
      // hideDetailPanel()
      maximizeCanvas()
      // jQuery("#main-toolbar").attr("style", "display:none;")
      // jQuery("#workspace-form button").attr("disabled", "true")
      // jQuery("#search-widget .ui-button").attr("disabled", "true")
      // jQuery("#topicmap-form .ui-button").attr("disabled", "true")
      // jQuery("#workspace-form .ui-button").attr("disabled", "true")
      jQuery(".playFromStart").click(function(e) { playFromStart() })
      jQuery(".myTrackname").click(function(e) { toggleTrackList() })
      jQuery(".jp-next").click(function(e) { playNextTrack() })
      jQuery(".jp-prev").click(function(e) { playPrevTrack() })
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
      jQuery.jPlayer.timeFormat.showHour = true;
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
          // 
          trackName.text(dm4c.selected_object.value)
        },
        play: function(event) {
          my_playState.text(opt_text_playing)
          isPlaying = true
          document.title = "♪ " + document.title
        },
        loadedmetadata: function(event) {
          // TODO: loaded some metadata... 
        },
        pause: function(event) {
          my_playState.text(opt_text_selected)
          isPlaying = false
          document.title = maptape.value + " - soundposter/"
        },
        ended: function(event) {
          my_playState.text(opt_text_selected)
          isPlaying = false
          document.title = maptape.value + " - soundposter/"
          playNextTrack()
        },
        error: function(event) {
          // if (event.jPlayer.error.type == "e_url_not_set") {
          if (event.jPlayer.error.type == "e_url_not_set") {
            playNextTrack()
          } else if (event.jPlayer.error.type == "e_flash") {
            showDialog('A problem with your browsers flash plugin occured. Please make sure flash works or use ' + 
              'a webbrowser which supports the HTML Audio-Element.')
          } else {
            showDialog('Sorry, an unknown error occured with our jPlayer setup. Please mail the following ' + 
             'error code to "' + event.jPlayer.error.type + '" mail@soundposter.com. And thank you for helping our software to get better!')
            // playFromStart()
          }
        },
        swfPath: plugin_path +"/script/js",
        cssSelectorAncestor: "#jp_container",
        supplied: "mp3, m4a",
        wmode: "window",
        solution: "flash, html"
      }) // catch flash error warning
      initialized = true
    }
    
    function updateMapInfo() {
      var topicmap = dm4c.get_plugin("de.deepamehta.topicmaps").get_topicmap()
      if (topicmap != undefined) {
        var id = topicmap.get_id()
        if (id != undefined) {
          var map = dm4c.fetch_topic(id)
          document.title = map.value + " - soundposter/"
          jQuery("#mapName").html('<b>sp/</b><b class="mapname">' + map.value + '</b>')
          jQuery("#mapName").append('<b id="lgn" title="create & edit soundposter/">Login</a>')
          jQuery("#lgn").click(soundposterDialog)
          maptape = map
        }
      }
    }
    
    /** when topicmaps are changed, this must be called..needs some topicmap_loaded_hook ***/
    function load_playlist() {
      // get selected topicmap
      playlist = new Array()
      var topicmap = dm4c.get_plugin("de.deepamehta.topicmaps").get_topicmap()
      // 
      /** if (!playlistLoaded) { // just load once.. **/
        if (topicmap != undefined) {
          // iterate through topics
          topicmap.iterate_topics(function(topic) {
            // access the topic's properties:
            if (topic.type_uri == "com.soundposter.track") {
              playlist.push(topic);
            }
          })
          /** playlistLoaded = true**/
          updateMapInfo()
          playlist.sort(topicSort); // alphabetical ascending
        }
      // }
    }
    
    // compare "a" and "b" in some fashion, and return -1, 0, or 1
    function topicSort(a, b) {
      var nameA = a.label
      var nameB = b.label
      if (nameA < nameB) // sort string ascending
        return -1
      if (nameA > nameB)
        return 1
      return 0 //default return value (no sorting)
    }
})
