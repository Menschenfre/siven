 <!-- script de youtube api, pendiente meterlo a una clase-->
 <script type="text/javascript">if (!window['YT']) {var YT = {loading: 0,loaded: 0};}if (!window['YTConfig']) {var YTConfig = {'host': 'http://www.youtube.com'};}if (!YT.loading) {YT.loading = 1;(function(){var l = [];YT.ready = function(f) {if (YT.loaded) {f();} else {l.push(f);}};window.onYTReady = function() {YT.loaded = 1;for (var i = 0; i < l.length; i++) {try {l[i]();} catch (e) {}}};YT.setConfig = function(c) {for (var k in c) {if (c.hasOwnProperty(k)) {YTConfig[k] = c[k];}}};var a = document.createElement('script');a.type = 'text/javascript';a.id = 'www-widgetapi-script';a.src = 'https://s.ytimg.com/yts/jsbin/www-widgetapi-vflgu2Ceb/www-widgetapi.js';a.async = true;var c = document.currentScript;if (c) {var n = c.nonce || c.getAttribute('nonce');if (n) {a.setAttribute('nonce', n);}}var b = document.getElementsByTagName('script')[0];b.parentNode.insertBefore(a, b);})();}</script>

<div class="col-md-7 grid-margin stretch-card">
  <div class="card">
    <div class="card-body">
      <h4 class="card-title">Música registrada</h4>
      <p class="card-description">
        Música registrada.
      </p>
       
       <div id="player"></div>
     

      
      
    </div>
  </div>
</div> 


    <script>

        // create youtube player
        var player;
        function onYouTubePlayerAPIReady() {
            player = new YT.Player('player', {
              width: '856',
              height: '482',
              videoId: 'avLxcVkPgug',
              events: {
                onStateChange: onPlayerStateChange
              }
            });
        }

        // when video ends
        function onPlayerStateChange(event) {        
            if(event.data === 0) {          
                //alert('done');
                player.loadVideoById('1wYNFfgrXTI');
            }
        }


        

    </script>