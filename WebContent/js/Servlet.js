
    send();

    //Funcion que para el video durante x segundos.
    function pause(seg) {
        document.getElementById('video').pause();
    setTimeout(function(){document.getElementById('video').play(); }, seg);
    }

     function change(rate) {
         document.getElementById('video').playbackRate = rate;
     }
     
     //Función que prepara peticiones cada 2 seg.
    function send() {                           
        var url =  document.URL+"?timestamp=";
        setInterval(function () { 
            httpGet(url+document.getElementById('video').currentTime); 
        }, 2000);
    }

    //Función que envia peticiones y espera la respuesta y luego manda para el video durante x tiempo.
    function httpGet(url){
        var request = new XMLHttpRequest();
        request.open("GET",url,false);
        request.send();
        request.onreadystatechange = function() { 
        if (request.readyState == 4 && request.status == 200)
        	document.write("request.responseText");
            pause(parseInt(request.responseText));
        }
    }   