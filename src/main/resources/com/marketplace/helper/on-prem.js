




for (const val of document.querySelectorAll('div')) {

    if (val.textContent === "h3NODE STATE/h3" ||
        val.textContent === "h3AUTHID/h3" ||
        val.textContent === "h3HEADERS/h3" ||
        val.textContent === "h3CLIENT IP/h3" ||
        val.textContent === "h3COOKIES/h3" ||
        val.textContent === "h3HOSTNAME/h3" ||
        val.textContent === "h3LOCALE/h3" ||
        val.textContent === "h3PARAMETERS/h3" ||
        val.textContent === "h3SERVER URL/h3" ||
        val.textContent === "h3SHARED STATE/h3") {

        val.outerHTML = "<h3 style='border-bottom: 2px solid black; padding-top: 5px'>" + val.outerHTML.replace("h3", "").replace("/h3", "") + "</h3>"
    }
    if (val.textContent === "Key" || val.textContent === "Value") {
        val.innerHTML = "<h4>" + val.outerHTML + "</h4>"
    }
}

// For Parameters
// const java_parameters_import = "${parameters}";
let string = "${parameters}"

// console.log("string: " + string)
// Make one big string like "authIndexType authIndexValue realm service"
//.split
let keys = '';
for(letter in string){

    if(string[letter].toUpperCase() !== string[letter].toLowerCase()){

        keys += string[letter]
    }else if(string[letter] === " "){
        keys += " "

    }
}

// console.log("Before split: " + keys)
// console.log("Type of keys before split: "+ typeof keys)
keys = keys.split(" ")

// console.log("After split: " + keys)
// console.log("Type of keys after split: "+ typeof keys)

let table = '';
let first = true;
let very_end = false;
let trimmed;
//Loops through divs
for (const val of document.querySelectorAll('div')) {
    //iterates through keys in list to find place I want to make table
    for (key in keys) {

        if (val.textContent.startsWith(keys[key])) {

            //First time coming into loop so this is the beginning
            if (first === true) {
                table += "<div class ='react-bootstrap-table'><table class='table table-bordered table-striped table-detail'>";
                first = false;
            }
            //This is the last key
            else if (val.textContent.startsWith(keys[keys.length - 1])) {
                const string = val.textContent;
                trimmed = '';
                for (letter in string) {
                    if (string[letter] === "[") {
                        break;
                    } else {
                        trimmed += string[letter]
                    }

                }
                //Creates final row and closes table tag
                table += "<tr><td><code>" + trimmed + "</code></td><td>" + val.textContent.replace(keys[key], "") + "</td></tr></table></div>"
                very_end = true;
            } else {
                //If not first or last it creates the table row and removes that div
                //If I don't remove the div it still has the original value with the old styling
                const string = val.textContent;
                trimmed = '';
                for (letter in string) {
                    if (string[letter] === "[") {
                        break;
                    } else {
                        trimmed += string[letter]
                    }
                }
                table += "<tr><td><code>" + trimmed + "</code></td><td>" + val.textContent.replace(keys[key], "") + "</td></tr>"
                //<table class="table table-hover"><thead><tr><th class="selection-cell-header" data-row-selection="true"><div class="checkbox"><input class="react-bs-select-all" id="checkboxHeader" name="checkboxHeader" type="checkbox"><label for="checkboxHeader"></label></div></th><th tabindex="0" aria-label="Username sortable" class="sortable">Username<span class="order"><span class="dropdown"><span class="caret"></span></span><span class="dropup"><span class="caret"></span></span></span></th><th tabindex="0">Full name</th><th tabindex="0">Email address</th><th tabindex="0">Status</th></tr></thead><tbody><tr><td class="selection-cell"><div class="checkbox"><input id="checkbox0" name="checkbox0" type="checkbox"><label for="checkbox0"></label></div></td><td title="demo"><span class="am-table-icon-cell"><span class="fa-stack fa-lg am-table-icon-cell-stack"><i class="fa fa-circle fa-stack-2x text-primary"></i><i class="fa fa-address-card fa-stack-1x fa-inverse"></i></span> <span><span>demo</span></span></span></td><td title="demo"><span>demo</span></td><td><span>demo@example.com</span></td><td><span class="text-success"><i class="fa fa-check-circle"></i> Active</span></td></tr><tr><td class="selection-cell"><div class="checkbox"><input id="checkbox1" name="checkbox1" type="checkbox"><label for="checkbox1"></label></div></td><td title="test_iproov"><span class="am-table-icon-cell"><span class="fa-stack fa-lg am-table-icon-cell-stack"><i class="fa fa-circle fa-stack-2x text-primary"></i><i class="fa fa-address-card fa-stack-1x fa-inverse"></i></span> <span><span>test_iproov</span></span></span></td><td title="test_iproov"><span>test_iproov</span></td><td><span>testiproov@mailinator.com</span></td><td><span class="text-success"><i class="fa fa-check-circle"></i> Active</span></td></tr></tbody></table></div>
                document.getElementById(val.id).remove()

            }
            if (very_end) {
                val.innerHTML = table;

            }
        }
    }
}

function singleItemTable(string) {

    let table = '';

    let very_end = false;

    for (const val of document.querySelectorAll('div')) {
        if (val.textContent.startsWith(string)) {
            table += "<div class ='react-bootstrap-table'><table class='table table-bordered table-striped table-detail'>";
            if (string === "HostName") {
                table += "<tr><td><code>" + "HostName" + "</code></td><td>" + val.textContent.replace("HostName", "") + "</td></tr></table></div>"
                very_end = true;
                if (very_end) {
                    val.innerHTML = table;
                    break;
                }
            } else if (string === "Server URL") {
                table += "<tr><td><code>" + "Server URL" + "</code></td><td>" + val.textContent.replace("Server URL", "") + "</td></tr></table></div>"
                very_end = true;
                if (very_end) {
                    val.innerHTML = table;
                    break;
                }
            } else if (string === "Preferred Locale") {
                table += "<tr><td><code>" + "Preferred Locale" + "</code></td><td>" + val.textContent.replace("Preferred Locale", "") + "</td></tr></table></div>"
                very_end = true;
                if (very_end) {
                    val.innerHTML = table;
                    break;
                }
            } else if (string === "ClientIp") {
                table += "<tr><td><code>" + "ClientIP" + "</code></td><td>" + val.textContent.replace("ClientIp", "") + "</td></tr></table></div>"
                very_end = true;
                if (very_end) {
                    val.innerHTML = table;
                    break;
                }
            } else if (string === "AuthID") {
                table += "<tr style=\"overflow-wrap: anywhere\"><td><code>" + "AuthID" + "</code></td><td style=\"overflow-wrap: anywhere\">" + val.textContent.replace("AuthID", "") + "</td></tr></table></div>"
                very_end = true;
                if (very_end) {
                    val.innerHTML = table;
                    break;
                }
            }
        }

    }
}

singleItemTable("HostName")
singleItemTable("Server URL")
singleItemTable("ClientIp")
singleItemTable("Preferred Locale")
singleItemTable("AuthID")



const java_headers = "${headers}";

let headers = '';
for(letter in java_headers){
    //if it is a letter - matches types
    if(java_headers[letter].toUpperCase() !== java_headers[letter].toLowerCase()){

        headers += java_headers[letter]
    }else if(java_headers[letter] === " "){
        headers += " "
    }
    else if(java_headers[letter]==="-"){
        headers += "-"
    }
}
headers = headers.split(" ")

//console.log("headers: " + headers)

let headers_table = '';
let first_headers = true;
let very_end_headers = false;
let array = []
let trimmed_headers;
//Loops through divs
for (const val of document.querySelectorAll('div')) {
    if (very_end_headers) {
        break;
    }
//iterates through keys in list to find place I want to make table
        trimmed = ''
        let string = val.textContent.trimStart(" ").trimEnd(" ")
        for(letter in string){
            if(string[letter] === " " || string[letter] === "["){
                break
            }else{
                trimmed += string[letter]
            }
        }
        let value = trimmed.trim()
    for (key in headers) {
        if (value === headers[key] && first_headers === true) {
            if(array.includes(value)){
                break
            }else{
                array.push(value)
            }
//First time coming into loop so this is the beginning
            if (first_headers === true) {
                headers_table += "<table class='table table-bordered table-striped table-detail'>";
                first_headers = false;
            }
            //This is the last key
            string = value;
            trimmed_headers = '';
            for (letter in string) {
                if (string[letter] === "[" || string[letter] === " ") {
                    break;
                } else {
                    trimmed_headers += string[letter]
                }

            }
            //Creates final row and closes table tag

            headers_table += "<tr style=\"overflow-wrap: anywhere\"><td style=\"overflow-wrap: anywhere\"><code>" + trimmed_headers + "</code></td><td style=\"overflow-wrap: anywhere\">" + val.textContent.replace(headers[key], "") + "</td></tr>"
            val.innerHTML = ""
        } else if (value === headers[key]) {
            if(array.includes(value)){
                break
            }else{

                array.push(value)
            }
            //If not first or last it creates the table row and removes that div
            //If I don't remove the div it still has the original value with the old styling
            string = val.textContent;
            trimmed_headers = '';
            for (letter in string) {
                if (string[letter] === "[") {
                    break;
                } else {
                    trimmed_headers += string[letter]
                }
            }
            headers_table += "<tr style=\"overflow-wrap: anywhere\"><td style=\"overflow-wrap: anywhere\"><code>" + trimmed_headers + "</code></td><td style=\"overflow-wrap: anywhere\">" + val.textContent.replace(headers[key], "") + "</td></tr>"

            val.innerHTML = ""

        } else if (value === headers[headers.length - 1]) {
            if(array.includes(value)){
                break
            }else{
                array.push(value)
            }
            string = value;
            trimmed_headers = '';
            for (letter in string) {
                if (string[letter] === "[") {
                    break;
                } else {
                    trimmed_headers += string[letter]
                }

            }
            //Creates final row and closes table tag
            //console.log("trimmed: "+ val.textContent.replace(headers[key], ""))
            headers_table += "<tr style=\"overflow-wrap: anywhere\"><td style=\"overflow-wrap: anywhere\"><code>" + trimmed_headers + "</code></td><td style=\"overflow-wrap: anywhere\">" + val.textContent.replace(trimmed_headers, "") + "</td></tr></table>"
            very_end_headers = true;
            val.innerHTML = ""
            break

        }
    }
    if (very_end_headers) {
        val.innerHTML = headers_table;
        //console.log("Inside very end headers\n" + headers_table)
        break
    }
}


let java_cookies = "${cookies}";
//console.log("javacookies: "+java_cookies)
let cookies = '';
for(letter in java_cookies){

    if(java_cookies[letter].toUpperCase() !== java_cookies[letter].toLowerCase()){

        cookies += java_cookies[letter]
    }else if(java_cookies[letter] === " "){
        cookies += " "

    }
}


cookies = cookies.split(" ")

let cookies_table = '';
let first_cookie = true;
let very_end_cookie = false;
let trimmed_cookie;
string = ''

//Loops through divs
for (const val of document.querySelectorAll('div')) {
    if (very_end_cookie) {
        break;
    }

//iterates through keys in list to find place I want to make table
    for (key in cookies) {

        if (val.textContent.startsWith(cookies[key]) && first_cookie === true) {

//First time coming into loop so this is the beginning
            if (first_cookie === true) {
                cookies_table += "<div class ='react-bootstrap-table'><table class='table table-bordered table-striped table-detail'>";
                first_cookie = false;
            }
            //This is the key
            string = val.textContent;
            trimmed_cookie = '';

            for (letter in string) {
                if (string[letter] === "[" || string[letter] === " ") {
                    break;
                } else {
                    trimmed_cookie += string[letter]
                }

            }

            cookies_table += "<tr style=\"overflow-wrap: anywhere\"><td style=\"overflow-wrap: anywhere\"><code>" + trimmed_cookie + "</code></td><td style=\"overflow-wrap: anywhere\">" + val.textContent.replace(cookies[key], "") + "</td></tr>"
            val.innerHTML = ""

            if(cookies.length === 1) {
                cookies_table += "</table></div>"
                very_end_cookie = true
                break
            }
        } else if (val.textContent.startsWith(cookies[key])) {
            //If not first or last it creates the table row and removes that div
            //If I don't remove the div it still has the original value with the old styling
            string = val.textContent;
            trimmed_cookie = '';
            for (letter in string) {
                if (string[letter] === "[" || string[letter] === " ") {
                    break;
                } else {
                    trimmed_cookie += string[letter]
                }
            }

            cookies_table += "<tr><td><code>" + trimmed_cookie + "</code></td><td>" + val.textContent.replace(cookies[key], "") + "</td></tr>"
            val.innerHTML = ""

        }

        else if (val.textContent.startsWith(cookies[cookies.length - 1])) {
             string = val.textContent;
            trimmed_cookie = '';
            for (letter in string) {
                if (string[letter] === "[" || string[letter] === " ") {
                    break;
                } else {
                    trimmed_cookie += string[letter]
                }

            }
            //Creates final row and closes table tag
            cookies_table += "<tr style=\"overflow-wrap: anywhere\"><td style=\"overflow-wrap: anywhere\"><code>" + trimmed_cookie + "</code></td><td style=\"overflow-wrap: anywhere\">" + val.textContent.replace(trimmed_cookie, "") + "</td></tr></table></div>"
            very_end_cookie = true;
            val.innerHTML = ""
            break

        }
    }
    if (very_end_cookie) {
        val.innerHTML = cookies_table;
        break
    }
}


