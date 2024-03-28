(async function onLoad() {
  function trimmer(string){
      let trimmed = ''
      let letter = ''
      if(string.startsWith("ClientIp") || string.startsWith("HostName") || string.startsWith("AuthID")){
        for(letter in string){
          if(string[letter] === " "){
            break
          }
          else{
            trimmed += string[letter]
          }
        }
       } else if(string.startsWith("Preferred Locale") || string.startsWith("Server URL")){
        let space_counter = 0;
        letter = ''
        for(letter in string){
          if(space_counter === 2){
            space_counter = 0
            break
          }
          if(string[letter] === " "){
            trimmed += string[letter]
            space_counter += 1
          } else{
            trimmed += string[letter]
          }
        }
      }
      return trimmed;
    }
    let letter = ''
    let first_header = true
    let table = ''
    let headers_end = false

    let headers_table = ''; 
    let very_end_headers = false;

    let params_table = ''; 
    let first_param = true; 
    let very_end_params = false; 
    let key = '';
    //For single value tables 
     for (const val of document.querySelectorAll('div')) { 
 
       if(val.className === "card border-xs-0 border-sm d-flex fr-stretch-card"){ 
         val.style = "width: fit-content" 
       } 
 
       if(val.className === "min-vh-100 d-flex flex-column fr-fullscreen-mobile"){ 
         val.style = "margin: 0px" 
       }
       if(val.className === "fr-m-auto fr-center-card align-self-center"){
         val.style = "margin: initial"
       }
 
 
       let string = val.textContent.trimStart(" ").trimEnd(" ") 

       let trimmer_val; 
       //Create tables for values that will not change 
       if (string.startsWith("AuthID")) { 
         trimmer_val = trimmer(string);

         table += '<table style="width: 100%">'
         table += '<tr><td  style="overflow-wrap: anywhere; border: 1px solid black;"><code>' + trimmer_val + '</code></td><td style=" padding: 20px; border: 1px solid black; overflow-wrap: anywhere; word-break: break-all">' + string.replace("AuthID", "") + '</td></tr></table>'
         // style="border: 1px solid black; text-align: left" style="overflow-wrap: anywhere" 
         trimmer_val = '' 
         val.innerHTML = table 
         table = '' 
       } else if (string.startsWith("ClientIp")) { 
         val.style = "display: flex; justify-content: left" 
         trimmer_val = trimmer(string); 
         val.className = "display: block; margin-left: auto; margin-right: auto; width: 40%" 
         table += '<table style="width: 100%">'
         table += '<tr style="border: 1px solid black; text-align: left; padding: 20px;"><td style="border: 1px solid black; padding: 20px; text-align: left" style="overflow-wrap: anywhere"><code>' + trimmer_val + '</code></td><td style="border: 1px solid black; text-align: left; word-break: break-all" style="overflow-wrap: anywhere">' + string.replace("ClientIp", "") +  '</td></tr></table>'
         val.innerHTML = table 
         table = '' 
       } else if (string.startsWith("Server URL")) { 
         val.style = "display: flex; justify-content: left" 
         trimmer_val = trimmer(string) 
         table += '<table style="width: 100%">'
         table += '<tr style="border: 1px solid black;" text-align: left; padding: 20px;"><td style="border: 1px solid black; text-align: left; padding: 20px;" style="overflow-wrap: anywhere"><code>' + trimmer_val + '</code></td><td style="border: 1px solid black; text-align: left; word-break: break-all" style="overflow-wrap: anywhere">' + string.replace("Server URL", "") + '</td></tr></table>'
         val.innerHTML = table 
         table = '' 
       } else if (string.startsWith("Preferred Locale")) { 
         val.style = "display: flex; justify-content: left" 
         trimmer_val = trimmer(string) 
         table += '<table style="width: 100%">'
         table += '<tr style="""border: 1px solid black;" text-align: left; padding: 20px;"><td style="border: 1px solid black; text-align: left; padding: 20px;" style="overflow-wrap: anywhere"><code>' + trimmer_val + '</code></td><td style="border: 1px solid black; text-align: left; word-break: break-all" style="overflow-wrap: anywhere">' + string.replace("Preferred Locale", "") + '</td></tr></table>'
         val.innerHTML = table 
         table = '' 
       } else if (string.startsWith("HostName")) { 
         val.style = "display: flex; justify-content: left" 
         trimmer_val = trimmer(string) 
         table += '<table style="width: 100%">'
         table += '<tr style="border: 1px solid black;" text-align: left; padding: 20px;"><td style="border: 1px solid black; text-align: left; padding: 20px;" style="overflow-wrap: anywhere"><code>' + trimmer_val + '</code></td><td style="border: 1px solid black; text-align: left; word-break: break-all" style="overflow-wrap: anywhere">'+ string.replace("HostName", "")  + '</td></tr></table>'
         val.innerHTML = table 
         table = '' 
       } 
 
 
       //Create h3 tags for headers 
       if (string === "h3NODE STATE/h3" || 
               string === "h3AUTHID/h3" || 
               string === "h3HEADERS/h3" || 
               string === "h3CLIENT IP/h3" || 
               string === "h3COOKIES/h3" || 
               string === "h3HOSTNAME/h3" || 
               string === "h3LOCALE/h3" || 
               string === "h3PARAMETERS/h3" || 
               string === "h3SERVER URL/h3" || 
               string === "h3SHARED STATE/h3") { 
         val.outerHTML = '<h3 style="padding-top: 50px">' + val.outerHTML.replace("h3", "").replace("/h3", "") + '</h3>'
       } 
       //Create h4 tags for Key: Value pairs 
       if (string === "Key" || string === "Value") { 
         val.outerHTML = "<h4>" + val.outerHTML + "</h4>"
       } 

     }

  let string = "${parameters}"
  let parameters = '';
  letter = ''
  for(letter in string){

    if(string[letter].toUpperCase() !== string[letter].toLowerCase()){

      parameters += string[letter]
    }else if(string[letter] === " "){
      parameters += " "

    }
  }

  parameters = parameters.split(" ")
  let inner = 0;
  letter = ''
    //For Parameters 
    for (const val of document.querySelectorAll('div')) { 
      inner = inner + 1
      if (inner === 2) { 
        let string = val.textContent.trimStart(" ").trimEnd(" ") 
 
      let trimmed = '' 
        for (key in parameters) { 
          if (string.startsWith(parameters[key])) { 

            if(string.startsWith("realm:")){ 
              continue 
            }
            letter = ''
            for(letter in string){ 
              if(string[letter] === " "){ 
                break 
              } 
              else{ 
                trimmed += string[letter]
              } 
            } 
 
            if(first_param){
              params_table += '<table style="width: 100%"><tr style="border: 1px solid black; text-align: left; padding: 20px;"><td style="border: 1px solid black; text-align: left; padding: 20px;" style="overflow-wrap: anywhere"><code>'+ trimmed + '</code></td><td style="border: 1px solid black; text-align: left; word-break: break-all">' + string.replace(trimmed, "")  + '</td></tr>'
              first_param = false 
              val.innerHTML = "" 
            } 
            else if(string.startsWith(parameters[parameters.length-1])){

              params_table +='<tr style="border: 1px solid black; text-align: left; padding: 20px;"><td style="border: 1px solid black; text-align: left; padding: 20px;"style="overflow-wrap: anywhere"><code>' + trimmed + '</code></td><td style="border: 1px solid black; text-align: left" style="overflow-wrap: anywhere; word-break: break-all">' + string.replace(trimmed, "") +'</td></tr></table>'
              very_end_params = true 
              val.innerHTML = "" 
            } 
            else{ 
              params_table += '<tr style="border: 1px solid black; text-align: left; padding: 20px;"><td style="border: 1px solid black; text-align: left; padding: 20px;" style="overflow-wrap: anywhere"><code>' + trimmed  + '</code></td><td style="border: 1px solid black; text-align: left" style="overflow-wrap: anywhere; word-break: break-all"> ' +string.replace(trimmed, "") +'</td></tr>'
              val.innerHTML = "" 
            }
          } 
        }
    }else{ 
      continue; 
      } 
      inner = 0
    if(very_end_params){ 
      val.innerHTML = params_table
      very_end_params = false
    } 
 
 
 
 
 
    } 
 
    //For Cookies 
    let cookies_table = ''; 
    let first_cookie = true; 
    let very_end_cookie = false; 
    let trimmed_cookie;

  let java_cookies = "${cookies}";
  let cookies = '';
  letter = ''
  for(letter in java_cookies){
    //Trims escape characters from java_cookies
    if(java_cookies[letter].toUpperCase() !== java_cookies[letter].toLowerCase()){
      cookies += java_cookies[letter]
    }else if(java_cookies[letter] === " "){
      cookies += " "
    }else if (isNaN(java_cookies[letter]) === false){ //If it is a number
      cookies += java_cookies[letter]
    }
  }


  cookies = cookies.split(" ")
    for (const val of document.querySelectorAll('div')) {
      let string = val.textContent.trimStart(" ").trimEnd(" ") 
      if (very_end_cookie) { 
        break; 
      }
//iterates through keys in list to find place I want to make table 
      //console.log(string) 
      for (key in cookies) {
        if(string.startsWith(cookies[key])) {
//First time coming into loop so this is the beginning
            trimmed_cookie = '';
          letter = ''
            for (letter in string) { 
              if (string[letter] === " ") { 
                break; 
              } else { 
                trimmed_cookie += string[letter]
              } 
 
            }
          if (first_cookie) {
            cookies_table += '<table style="width: 100%"><tr style="border: 1px solid black; text-align: left; overflow-wrap: anywhere; padding: 20px;"><td style="border: 1px solid black; text-align: left; padding: 20px;"  style="overflow-wrap: anywhere"><code>' + trimmed_cookie + '</code></td><td style="border: 1px solid black; text-align: left; word-break: break-all" style="overflow-wrap: anywhere">' + string.replace(trimmed_cookie, "") + '</td></tr>';
 
            first_cookie = false; 
 
            //This is the last key 
 
            //Creates final row and closes table tag 
            val.innerHTML = "" 
 
            if (cookies.length === 1) { 
              cookies_table += "</table>"
              very_end_cookie = true 
              break 
            } 
          } 
        } else if (string.startsWith(cookies[key])) { 
          //If not first or last it creates the table row and removes that div 
          //If I don't remove the div it still has the original value with the old styling 
          trimmed_cookie = '';
          letter = ''
          for (letter in string) { 
            if (string[letter] === " ") { 
              break; 
            } else { 
              trimmed_cookie += string[letter]
            } 
          }
          cookies_table += '<tr style="border: 1px solid black; text-align: left; padding: 20px;"><td style="border: 1px solid black; text-align: left; padding: 20px;" style="overflow-wrap: anywhere"><code>' + trimmed_cookie + '</code></td><td style="border: 1px solid black; text-align: left; word-break: break-all" style="overflow-wrap: anywhere">' + string.replace(trimmed_cookie, "")  +'</td></tr>'
          val.innerHTML = ""
        } 
 
        else if (string.startsWith(cookies[cookies.length - 1])) { 
 
          trimmed_cookie = '';
          letter = ''
          for (letter in string) { 
            if (string[letter] === " ") { 
              break; 
            } else { 
              trimmed_cookie += string[letter]
            }
          } 
          //Creates final row and closes table tag 
          cookies_table +='<tr style="border: 1px solid black; text-align: left; padding: 20px;"><td style="overflow-wrap: anywhere; padding: 20px;" style="border: 1px solid black; text-align: left"><code>' + trimmed_cookie + '</code></td><td style="border: 1px solid black; text-align: left; padding: 20px; word-break: break-all" style="overflow-wrap: anywhere">' + string.replace(trimmed_cookie, "") + '</td></tr></table>'
          very_end_cookie = true; 
          val.innerHTML = "" 
          break 
 
        } 
      } 
      if (very_end_cookie) { 
        val.innerHTML = cookies_table;
        console.log("cookies_table: " + cookies_table)
        break
      } 
    }


  const java_headers = "${headers}";

  let headers = '';
  letter = ''
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

  let here = true
  //For Headers Table 
    for (const val of document.querySelectorAll('div')) { 
      inner = inner + 1
      if (inner === 2) { 
        let trimmed = '' 
        let string = val.textContent.trimStart(" ").trimEnd(" ")
        letter = ''
        for(letter in string){ 
          if(string[letter] === " " || string[letter] === "["){ 
            break 
          }else{ 
            trimmed += string[letter]
          } 
        } 
        let value = trimmed.trim() 
 
 
          for (key in headers) { 
 
            if (value === headers[key]) { 
              if (first_header) { 
                headers_table += '<table style = "border: 1px solid black; padding-top: 0px; width: 100%" >'
                headers_table += '<tr style="border: 1px solid black; text-align: left; padding: 20px;"><td style="border: 1px solid black; padding: 20px;"><code>' + value + '</code></td><td style="border: 1px solid black; text-align: left; word-break: break-all">' + string.replace(value, "") + '</td></tr>'
                first_header = false 
                val.innerHTML = "" 
              } else if (value === headers[headers.length - 1]) { 
                headers_table += '<tr style="border: 1px solid black; text-align: left; padding: 20px;"><td style="border: 1px solid black; text-align: left; padding: 20px;"><code>' + value + '</code></td><td style="border: 1px solid black; text-align: left; word-break: break-all">' + string.replace(value, "") + '</td></tr></table>'
                val.innerHTML = ""
                very_end_headers = true 
              } else { 
                headers_table += '<tr style="border: 1px solid black; text-align: left; padding: 20px;"><td style="border: 1px solid black; text-align: left; padding: 20px;"><code>' + value + '</code></td><td style="border: 1px solid black; text-align: left; word-break: break-all">' + string.replace(value, "") + '</td></tr>'
                val.remove() 
              } 
            } 
 
          }
        if(very_end_headers){ 
          if(here){ 
            val.innerHTML = headers_table
            headers_end = false
            here = false 
          } 
        } 
      }else{ 
        continue; 
      } 
 
 
      inner = 0 
    } 

  })();;