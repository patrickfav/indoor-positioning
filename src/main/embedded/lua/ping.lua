#!/usr/bin/lua

function printCommand (commandString)
    local file = assert(io.popen(commandString,'r'))
    local output = file:read("*all")
    file:close()
    print(output)
end

printf = function(s,...)
    return io.write(s:format(...))
end -- function


local function main()
    math.randomseed(os.time())
    local colors = {'Gold','Tomato','YellowGreen','Aquamarine','Crimson','CornflowerBlue','DarkOrchid','LightSalmon','OliveDrab','Silver','PowderBlue','LimeGreen'}

    print("Content-Type: text/html; charset=utf-8")
    print("Cache-Control: no-cache, no-store, must-revalidate")
    print("Pragma: no-cache")
    print("Expires: 0")
    print("")
    print("<!DOCTYPE html>")
    print("<html lang='en'>")
    print("<head><meta charset='utf-8'><title>Ping Webserver</title></head>")
    printf("<body style='background-color: %s'>",colors[math.random(table.getn(colors))])
    print("<h1>CGI Ping Successful<h2>")
    print("<p>")
    print(os.date("%c"))
    print( "</p>")
    print("<pre>")
    printCommand("uptime")
    print("</pre>")
    print("</body></html>")

end

main()