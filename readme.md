Minecraft Marauder's Map
========================

Realtime Minecraft minimap, using:
* [Minecraft-Overviewer](https://github.com/brownan/Minecraft-Overviewer)
* [hey0's Server Mod](https://github.com/traitor/Minecraft-Server-Mod) (with a small plugin)

What's it look like?
![Example map](http://imgur.com/6cl1H.jpg)

Installation
----------------

There's a lot of assembly required; sorry. Hopefully you have some of it already. I've designed this for a Minecraft server that's running apache; if you aren't, you may have to ensure your server handles the json content-type header, instead of using the `.htaccess` file.
* Set up hey0's server mod
* copy MaraudersMap.jar to the plugins directory and enable it
* symlink `player-locations.json` to the root of your Minecraft-Overviewer folder (next to `index.html`)
* Append my `.htaccess` file to `.htaccess` in the Minecraft-Overviewer map root
* Set up Minecraft-Overviewer
* Edit `web\_assets/index.html`; add this line to the `<head>`:
 `<script type="text/javascript" src="regions.js"></script>`
 - Copy gps.js to web\_assets/

If you don't have your map and google maps server on the same machine, you can edit gps.js to fetch it from whatever web address you want.

