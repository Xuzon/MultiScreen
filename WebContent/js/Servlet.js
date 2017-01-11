
	setTimeout(function(){send(); }, 10000);
    //send();

    //Funcion que para el video durante x segundos.
    function pause(seg) {
    	if(seg!=0) {
    		console.log("Voy a parar: "+seg);
	        document.getElementById('video').pause();
	        setTimeout(function(){document.getElementById('video').play(); }, seg*1000);
    	}
    }

     function change(rate) {
         document.getElementById('video').playbackRate = rate;
     }
     
     //Función que prepara peticiones cada 2 seg.
    function send() {                           
        var url =  document.URL+"?timestamp=";
        setInterval(function () { 
        	console.log("Hago petición");
        	console.log("Video Time: "+document.getElementById('video').currentTime);
            httpGet(url+document.getElementById('video').currentTime);
        }, 2000);
    }

    //Función que envia peticiones y espera la respuesta y luego manda para el video durante x tiempo.
    function httpGet(url){
        var request = new XMLHttpRequest();
        request.onreadystatechange = function() {
	        if (request.readyState == 4 && request.status == 200) {
	            console.log("Hecha petición: "+request.responseText);
	            pause(parseFloat(request.responseText));
	        }
        }
        request.open("GET",url,true);
        request.send();

    }