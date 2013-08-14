dm4c.add_multi_renderer('com.soundposter.sound_multi_renderer', {

    render_info: function (page_models, $parent) {

        var list = $('<ul class="sound-list">')
        for (var i = 0; i < page_models.length; i++) {
            var item = page_models[i].object
            if (item != undefined) {
                if (item.id != -1) {
                    var text = ""
                    if (item.hasOwnProperty('value')) {
                        text = item.value
                    }
                    // give info-item some behaviour
                    $listItem = $('<div id="' +item.id+ '">')
                    $listItem.click(function(e) {
                        var topicId = this.id
                        dm4c.do_reveal_related_topic(topicId)
                    })
                    $listItem.append('<i>' + text + '</i>')
                    list.append($('<li class="sound-item">').html($listItem))
                }
            }
        }
        $parent.append('<div class="field-label">Sounds</div>')
        $parent.append(list)

    },

    render_form: function (page_models, $parent) {

        // user cannot edit aggregated tweets of a twitter-search within page panel

        return function () {
            var values = []
            // returning (and referencing) all previously aggregated items back in our submit-function
            for (var item in page_models) {
                var topic_id = page_models[item].object.id
                if (topic_id != -1) {
                    values.push(dm4c.REF_PREFIX + topic_id)
                }
            }
            return values
        }
    }
})
