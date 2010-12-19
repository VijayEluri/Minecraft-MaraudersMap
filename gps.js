window.mcgps = {
    running: true,
    /* Refresh positions this often */
    interval: 6000,
    dataFile: 'player-locations.json',
    players: {},

    reloadGps: function() {
        if (!this.running) {
            return;
        }

        var success = function(data, a, b, c) {
            mcgps.loadData(data);
        };
        var complete = function(xhr, textStatus) {
            var interval = mcgps.interval;
            if (textStatus == 'timeout') {
                interval = interval * 2;
            }

            setTimeout('mcgps.reloadGps()', interval);
        };
        jQuery.ajax({
            url: this.dataFile,
            cache: false,
            success: success,
            complete: complete,
            dataType: 'json'
        });
    },

    /* Update the local data (and markers) with values from the server */
    loadData: function(data) {
        var players = this.players;
        // mark for deletion so we can prune the list after
        for (var name in players) {
            players[name].active = false;
        }

        for (var i = 0; i < data.length; i++) {
            var playerData = data[i];
            if (players[playerData.name]) {
                this.updatePlayer(playerData);
            } else {
                this.newPlayer(playerData);
            }
            players[playerData.name].active = true;
        }
        for (var name in players) {
            if (!players[name].active) {
				players[name].marker.setMap(null);
                delete players[name];
            }
        }
    },

    playerPosition: function(coords) {
        return fromWorldToLatLng(coords.x, coords.y, coords.z);
    },

    newPlayer: function(data) {
        var position = this.playerPosition(data);


        var img_size = new google.maps.Size(8, 8);
        var half_img_size = new google.maps.Size(4, 4);
        var scaled_size = new google.maps.Size(32, 32);

        var Size = google.maps.Size;

        var scale = 4;
        var icon = new google.maps.MarkerImage(
                'http://www.minecraft.net/skin/' + data.name + '.png',
                new google.maps.Size(8 * scale, 8 * scale),
                new google.maps.Point(8 * scale, 8 * scale),
                new google.maps.Point(16, 16),
                new google.maps.Size(64 * scale, 32 * scale)
        );
        icon.origin = new Size(8, 8);


        var marker = new google.maps.Marker({
            map: map,
            position: position,
            title: data.name,
            icon: icon,
            visible: true
        });

        this.players[data.name] = {
            marker: marker,
            name: data.name,
            active: true
        };
    },

    updatePlayer: function(data) {
        this.players[data.name].marker.setPosition(
                this.playerPosition(data));
    }
};


mcgps.reloadGps();

