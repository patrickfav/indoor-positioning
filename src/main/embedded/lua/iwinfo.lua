#!/usr/bin/lua

function printCommand (commandString)
    local file = assert(io.popen(commandString,'r'))
    local output = file:read("*all")
    file:close()
    print(output)
end

local string = require('string')
local find = string.find
local match = string.match
local gmatch = string.gmatch

-- parse querystring into table. urldecode tokens
function querystringParse(str, sep, eq)
    if not sep then sep = '&' end
    if not eq then eq = '=' end
    local vars = {}
    for pair in gmatch(str,'[^' .. sep .. ']+') do
        if not find(pair, eq) then
            vars[pair] = ''
        else
            local key, value = match(pair, '([^' .. eq .. ']*)' .. eq .. '(.*)')
            if key then
                vars[key] = value
            end
        end
    end
    return vars
end


local function main()
    local queryString = os.getenv("QUERY_STRING");
    local queryMap = querystringParse(queryString,"&","=")

    print("Content-Type: application/xml; charset=utf-8")
    print("Cache-Control: no-cache, no-store, must-revalidate")
    print("Pragma: no-cache")
    print("Expires: 0")
    print("")

    print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    print("<router>")

    if queryMap['adapter'] ~= nil then
        local adapter = queryMap['adapter']
        print("<adapter name=\"".. adapter .."\">")
        print("   <info><![CDATA[")
        printCommand("iwinfo ".. adapter .." info")
        print("     ]]></info>")
        if queryMap['scan'] == 'true' then
            print("   <scan><![CDATA[")
            printCommand("iwinfo " .. adapter .. " scan")
            print("     ]]></scan>")
        end
        print("   <txpowerlist><![CDATA[")
        printCommand("iwinfo ".. adapter .." txpowerlist")
        print("     ]]></txpowerlist>")
        print("   <assoclist><![CDATA[")
        printCommand("iwinfo ".. adapter .." assoclist")
        print("     ]]></assoclist>")
        print("</adapter>")
    else

        print("<adapter-list>")
        print("<![CDATA[")
        printCommand("iwinfo")
        print("]]>")
        print("</adapter-list>")
        print("<date>")
        print("  <![CDATA[")
        printCommand("date")
        print("]]>")
        print("</date>")
        print("<uptime>")
        print("  <![CDATA[")
        printCommand("uptime")
        print("]]>")
        print("</uptime>")
        print("<ifconfig>")
        print("  <![CDATA[")
        printCommand("ifconfig")
        print("]]>")
        print("</ifconfig>")
        print("<cpuinfo>")
        print("  <![CDATA[")
        printCommand("cat /proc/cpuinfo")
        print("]]>")
        print("</cpuinfo>")
        print("<meminfo>")
        print("  <![CDATA[")
        printCommand("cat /proc/meminfo")
        print("]]>")
        print("</meminfo>")
        print("<version>")
        print("  <![CDATA[")
        printCommand("cat /proc/version")
        print("]]>")
        print("</version>")

    end

    print("</router>")
end

main()