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
		if (!data) {
			return;
		}

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

	makeIcon: function(url) {
		var scale = 4;
		return new google.maps.MarkerImage(
				url,
				new google.maps.Size(8 * scale, 8 * scale),
				new google.maps.Point(8 * scale, 8 * scale),
				new google.maps.Point(4 * scale, 4 * scale),
				new google.maps.Size(64 * scale, 32 * scale)
				);
	},

	newPlayer: function(data) {
		var position = this.playerPosition(data);

		var iconUrl = 'http://s3.amazonaws.com/MinecraftSkins/' + \
                      data.name + '.png';
        this.players[data.name] = {
            marker: null,
            name: data.name,
            active: true
        };

		// first load the player's skin to see if they have a custom one
		// choose what URL to give the marker after loading
		var placeMarker = function(url) {
			var marker = new google.maps.Marker({
				map: map,
				position: position,
				title: data.name,
				icon: mcgps.makeIcon(url),
				visible: true
			});


            mcgps.players[data.name].marker = marker;
		};
		jQuery('<img src="' + iconUrl + '">').load(function() {
			placeMarker(iconUrl);
		}).error(function() {
			placeMarker('http://minecraft.net/img/char.png');
		});

	},

	updatePlayer: function(data) {
        if (!this.players[data.name].marker) {
            return;
        }
		this.players[data.name].marker.setPosition(
            this.playerPosition(data));
	}
};

mcgps.reloadGps();

