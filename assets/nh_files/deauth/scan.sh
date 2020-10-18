#iwlist $1 scanning | grep 'Channel\|ESSID'
iw dev $1 scan | grep 'SSID\|(on\|primary channel'