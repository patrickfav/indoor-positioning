
# Devices

## TP-LINK TL-WR710N v1

### Info

* [OpenWrt TL-WR710N ](http://wiki.openwrt.org/toh/tp-link/tl-wr710n)
* Type: ar71xx, CPU: Atheros AR7240, Freq 400Mhz, Flash: 8MB, RAM: 32MB
* Latest working Version: 14.07 Barrier Braker with Super-Wrt

### First Install
* Connect Router through Wifi (default pw on the device)
* Access Router through Browser with ip (default `192.168.0.254` - `admin/admin`)
* Go to System Tools -> Firmware Upgrade
* Choose file 01_first_time_superwrt-wr710n-factory.bin (or similiar for first time upgrade, see [super-wrt-site](http://www.superwrt.eu/en/firmware/tp-link-wr710n/) )
* WAIT AND DO NOT PREMETIFVLY CUT POWER
* Connect with Ethernet and connect to OpenWrt (default `192.168.1.1` - root/admin)
* Change password to 'admin' (to activate ssh): System -> Administration
* Optional: Change theme to 'Bootstrap' if available in System -> System -> System Properties -> Tab: Language and Styles
* Connect to existing Wlan: Network -> Wifi -> Scan
    * Uncheck "An additional network will be created if you leave this unchecked."
    * Select LAN Firewall Zone
* Submit + Save & Apply
* Network -> Wifi -> Edit Master AP: Change Access Point SSID Name to e.g. "Node xxx.xxx.3.1 - TPLink" and actiate WPA Security with password "admin1234" -> Save & Apply
* Optional: If used in LAN with multiple other nodes change IP to something different (correlate with ssid name "xxx.xxx.3.1") in Network -> Interfaces -> Edit "LAN" -> Tab: General Setup -> IPv4 address (wait for reboot)
* Copy service files:
    * Connect with SCP (use winscp in windows)
    * copy lua or shell scripts to `www/cgi-bin/` and set cmod to 755 (to make it execute) - make sure linetermination is `LF` not `CRLF`
    * delete file extensions if present (the `.lua` for example)
    * try out: `http://xxx.xxx.3.1/cgi-bin/iwinfo`
    * known issue: `Unable to launch the requested CGI program` scripts usually do not run straight away - it helps sometimes to delete all content except the most basic and reexecute (should only happend in `CRLF` line mode)

## TP-LINK TL-MR3020 v1.9

### Info

* [OpenWrt TL-MR3020](http://wiki.openwrt.org/toh/tp-link/tl-mr3020)
* Type: ar71xx, CPU: Atheros AR7240, Freq 400Mhz, Flash: 4MB, RAM: 32MB
* Latest working Version: [12.09 Attitude Adjustment](http://downloads.openwrt.org/attitude_adjustment/12.09/ar71xx/generic/) (14.07 BB does not seem to work ok)

# General How-To

## Install Necessary Plugins

* NOTE: The Router needs internet connection for this to work
* upload `install-sensornetwork.sh` to root folder (e.g. with winscp)
* make sure file is in `LF` (for linux) not `CRLF` (for windows) for linebreaks (so `\n`)
* connect to device and open bash (with e.g. putty in win) and execute with
    * `cd ..`
    * `sh ./install-sensornetwork.sh`
* wait for the router to reboot

## Upgrade OpenWrt from OpenWrt

* Access Web Ui in browser
* System -> Backup/Flash Images
* In Tab "Configuration" or "Custom Files" add `/etc/sysupgrade.conf`
* use xxx- **sysupgrade**.bin file in "Flash new firmware image" and uncheck "Keep settings" and press flash button
* on the next page the md5 sum will be presented, this can be compared to the ones in the [build folder](http://downloads.openwrt.org/attitude_adjustment/12.09/ar71xx/generic/) if ok, press "proceed"

## Misc Links

* [How to set Interface Metric in Windows 8 to change the priority of network adapters (Ethernet vs. Wifi)](https://restingsysadmin.wordpress.com/2013/03/28/setting-network-interface-priority-on-windows-8-with-powershell/)
