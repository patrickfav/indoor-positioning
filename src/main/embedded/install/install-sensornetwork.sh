#!/bin/sh

clear
echo "=== INSTALL SENSOR NODE SCRIPT v0.1 === "
echo ""
echo "== update opkg == "
echo ""
opkg update
echo ""
sleep 1
echo "== install uhttp webserver == "
echo ""
opkg install uhttpd
echo ""
sleep 1
echo "== install ssl == "
echo ""
opkg install uhttpd-mod-tls
echo ""
opkg install luci-ssl
echo ""
sleep 1
echo "== install iwinfo == "
echo ""
opkg install libiwinfo
echo ""
opkg install iwinfo
echo ""
sleep 1
echo "== restart uhttpd == "
echo ""
/etc/init.d/uhttpd restart
echo ""
sleep 1
echo "== reboot == "
echo "wait 30 seconds and then reconnect to the device"
reboot