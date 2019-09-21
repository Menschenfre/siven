 <script type="text/javascript">if (!window['YT']) {var YT = {loading: 0,loaded: 0};}if (!window['YTConfig']) {var YTConfig = {'host': 'http://www.youtube.com'};}if (!YT.loading) {YT.loading = 1;(function(){var l = [];YT.ready = function(f) {if (YT.loaded) {f();} else {l.push(f);}};window.onYTReady = function() {YT.loaded = 1;for (var i = 0; i < l.length; i++) {try {l[i]();} catch (e) {}}};YT.setConfig = function(c) {for (var k in c) {if (c.hasOwnProperty(k)) {YTConfig[k] = c[k];}}};var a = document.createElement('script');a.type = 'text/javascript';a.id = 'www-widgetapi-script';a.src = 'https://s.ytimg.com/yts/jsbin/www-widgetapi-vflgu2Ceb/www-widgetapi.js';a.async = true;var c = document.currentScript;if (c) {var n = c.nonce || c.getAttribute('nonce');if (n) {a.setAttribute('nonce', n);}}var b = document.getElementsByTagName('script')[0];b.parentNode.insertBefore(a, b);})();}</script>
 <div id="player"></div>

    

    <script>

        // create youtube player
        var player;
        function onYouTubePlayerAPIReady() {
            player = new YT.Player('player', {
              width: '640',
              height: '390',
              videoId: '0Bmhjf0rKe8',
              events: {
                onReady: onPlayerReady,
                onStateChange: onPlayerStateChange
              }
            });
        }

        // autoplay video
        function onPlayerReady(event) {
            event.target.playVideo();
        }

        // when video ends
        function onPlayerStateChange(event) {        
            if(event.data === 0) {          
                //alert('done');
                player.loadVideoById('GbNhJ-7VxPM');
            }
        }


        

    </script>

    <?php $content = file_get_contents("http://youtube.com/get_video_info?video_id=GbNhJ-7VxPM");
parse_str($content, $ytarr);
$jsondec = json_decode($ytarr['player_response'],true); echo $jsondec['videoDetails'][title];
//echo $ytarr['title'];