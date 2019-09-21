<html class="no-js" lang="en"><head>

	<script type="text/javascript">if (!window['YT']) {var YT = {loading: 0,loaded: 0};}if (!window['YTConfig']) {var YTConfig = {'host': 'http://www.youtube.com'};}if (!YT.loading) {YT.loading = 1;(function(){var l = [];YT.ready = function(f) {if (YT.loaded) {f();} else {l.push(f);}};window.onYTReady = function() {YT.loaded = 1;for (var i = 0; i < l.length; i++) {try {l[i]();} catch (e) {}}};YT.setConfig = function(c) {for (var k in c) {if (c.hasOwnProperty(k)) {YTConfig[k] = c[k];}}};var a = document.createElement('script');a.type = 'text/javascript';a.id = 'www-widgetapi-script';a.src = 'https://s.ytimg.com/yts/jsbin/www-widgetapi-vflgu2Ceb/www-widgetapi.js';a.async = true;var c = document.currentScript;if (c) {var n = c.nonce || c.getAttribute('nonce');if (n) {a.setAttribute('nonce', n);}}var b = document.getElementsByTagName('script')[0];b.parentNode.insertBefore(a, b);})();}</script>
  	
  	<title>Demo: Responsive YouTube Player with Scrolling Thumbnail Playlist</title> 
  	

  	<style type="text/css">

  		body {
  			margin: 30px;
  			padding: 0;
  			background: #ddd;
  			font-family: Arial, Helvetica, sans-serif;
  		}

  		.title {
  			width: 100%; 
  			max-width: 854px;
  			margin: 0 auto;
  		}

  		.caption {
  			width: 100%;
  			max-width: 854px;
  			margin: 0 auto;
  			padding: 20px 0;
  		}

  		.container {
  			width: 100%;
  			max-width: 854px;
  			min-width: 440px;
  			background: #fff;
  			margin: 0 auto;
  		}


  		/*  VIDEO PLAYER CONTAINER
 		############################### */
  		.vid-container {
		    position: relative;
		    padding-bottom: 52%;
		    padding-top: 30px; 
		    height: 0; 
		}
		 
		.vid-container iframe,
		.vid-container object,
		.vid-container embed {
		    position: absolute;
		    top: 0;
		    left: 0;
		    width: 100%;
		    height: 100%;
		}


		/*  VIDEOS PLAYLIST 
 		############################### */
		.vid-list-container {
			width: 92%;
			overflow: hidden;
			margin-top: 20px;
			margin-left:4%;
			padding-bottom: 20px;
		}

		.vid-list {
			width: 1344px;
			position: relative;
			top:0;
			left: 0;
		}

		.vid-item {
			display: block;
			width: 148px;
			height: 148px;
			float: left;
			margin: 0;
			padding: 10px;
		}

		.thumb {
			/*position: relative;*/
			overflow:hidden;
			height: 84px;
		}

		.thumb img {
			width: 100%;
			position: relative;
			top: -13px;
		}

		.vid-item .desc {
			color: #21A1D2;
			font-size: 15px;
			margin-top:5px;
		}

		.vid-item:hover {
			background: #eee;
			cursor: pointer;
		}

		.arrows {
			position:relative;
			width: 100%;
		}

		.arrow-left {
			color: #fff;
			position: absolute;
			background: #777;
			padding: 15px;
			left: -25px;
			top: -130px;
			z-index: 99;
			cursor: pointer;
		}

		.arrow-right {
			color: #fff;
			position: absolute;
			background: #777;
			padding: 15px;
			right: -25px;
			top: -130px;
			z-index:100;
			cursor: pointer;
		}

		.arrow-left:hover {
			background: #CC181E;
		}

		.arrow-right:hover {
			background: #CC181E;
		}


		@media (max-width: 624px) {
			body {
				margin: 15px;
			}
			.caption {
				margin-top: 40px;
			}
			.vid-list-container {
				padding-bottom: 20px;
			}

			/* reposition left/right arrows */
			.arrows {
				position:relative;
				margin: 0 auto;
				width:96px;
			}
			.arrow-left {
				left: 0;
				top: -17px;
			}

			.arrow-right {
				right: 0;
				top: -17px;
			}
		}

  	</style>

  	<script src="/Assets/js/jquery/jquery-3.3.1.min.js"></script>


  </head>

  <body>
  	<div class="title"><h2>Demo: Responsive YouTube Player with Scrolling Thumbnail Playlist</h2></div>

  	<div class="container">

  		<!-- THE YOUTUBE PLAYER -->
  		<div class="vid-container" id="player">
		    <!--<iframe id="vid_frame" src="https://www.youtube.com/embed/GbNhJ-7VxPM"></iframe>-->
		</div>

		<!-- THE PLAYLIST -->
		<div class="vid-list-container">
	        <div class="vid-list">
	         	
 	            <div class="vid-item" onclick="document.getElementById('vid_frame').src='http://youtube.com/embed/eg6kNoJmzkY?autoplay=1&amp;rel=0&amp;showinfo=0&amp;autohide=1'">
 	              <div class="thumb"><img src="http://img.youtube.com/vi/eg6kNoJmzkY/0.jpg"></div>
 	              <div class="desc">Jessica Hernandez &amp; the Deltas - Dead Brains</div>
 	            </div>
 	          
 	            <div class="vid-item" onclick="document.getElementById('vid_frame').src='http://youtube.com/embed/_Tz7KROhuAw?autoplay=1&amp;rel=0&amp;showinfo=0&amp;autohide=1'">
 	              <div class="thumb"><img src="http://img.youtube.com/vi/_Tz7KROhuAw/0.jpg"></div>
 	              <div class="desc">Barbatuques - CD Tum Pá - Sambalelê</div>
 	            </div>

 	            <div class="vid-item" onclick="document.getElementById('vid_frame').src='http://youtube.com/embed/F1f-gn_mG8M?autoplay=1&amp;rel=0&amp;showinfo=0&amp;autohide=1'">
 	              <div class="thumb"><img src="http://img.youtube.com/vi/F1f-gn_mG8M/0.jpg"></div>
 	              <div class="desc">Eleanor Turner plays Baroque Flamenco</div>
 	            </div>

 	            <div class="vid-item" onclick="document.getElementById('vid_frame').src='http://youtube.com/embed/fB8UTheTR7s?autoplay=1&amp;rel=0&amp;showinfo=0&amp;autohide=1'">
 	              <div class="thumb"><img src="http://img.youtube.com/vi/fB8UTheTR7s/0.jpg"></div>
 	              <div class="desc">Sleepy Man Banjo Boys: Bluegrass</div>
 	            </div>

 	            <div class="vid-item" onclick="document.getElementById('vid_frame').src='http://youtube.com/embed/0SNhAKyXtC8?autoplay=1&amp;rel=0&amp;showinfo=0&amp;autohide=1'">
 	              <div class="thumb"><img src="http://img.youtube.com/vi/0SNhAKyXtC8/0.jpg"></div>
 	              <div class="desc">Edmar Castaneda: NPR Music Tiny Desk Concert</div>
 	            </div>

 	            <div class="vid-item" onclick="document.getElementById('vid_frame').src='http://youtube.com/embed/RTHI_uGyfTM?autoplay=1&amp;rel=0&amp;showinfo=0&amp;autohide=1'">
 	              <div class="thumb"><img src="http://img.youtube.com/vi/RTHI_uGyfTM/0.jpg"></div>
 	              <div class="desc">Winter Harp performs Caravan</div>
 	            </div>
 	          
 	            <div class="vid-item" onclick="document.getElementById('vid_frame').src='http://youtube.com/embed/abQRt6p8T7g?autoplay=1&amp;rel=0&amp;showinfo=0&amp;autohide=1'">
 	              <div class="thumb"><img src="http://img.youtube.com/vi/abQRt6p8T7g/0.jpg"></div>
 	              <div class="desc">The Avett Brothers Tiny Desk Concert</div>
 	            </div>

 	            <div class="vid-item" onclick="document.getElementById('vid_frame').src='http://youtube.com/embed/fpmN9JorFew?autoplay=1&amp;rel=0&amp;showinfo=0&amp;autohide=1'">
 	              <div class="thumb"><img src="http://img.youtube.com/vi/fpmN9JorFew/0.jpg"></div>
 	              <div class="desc">Tracy Chapman - Give Me One Reason</div>
 	            </div>

	 	    </div>
        </div>

        <!-- LEFT AND RIGHT ARROWS -->
        <div class="arrows">
        	<div class="arrow-left"><i class="fa fa-chevron-left fa-lg"></i></div>
        	<div class="arrow-right"><i class="fa fa-chevron-right fa-lg"></i></div>
        </div>

  	</div>

  	<div class="caption"><a href="http://www.woosterwebdesign.com/responsive-youtube-player-with-playlist/">Related Blog Post</a></div>

  	<div class="caption">
  		<strong><a href="http://woosterwebdesign.com/mylist-video-player">Check out the MyList Video Player.</a></strong>
		<p>A fully responsive HTML5 video player featuring a scrolling thumbnail playlist. Uses latest YouTube API (V3) and dynamically loads YouTube playlist.</p>
  	</div>

  	<!-- JS FOR SCROLLING THE ROW OF THUMBNAILS -->
  	<script type="text/javascript">
  		$(document).ready(function () {
		    $(".arrow-right").bind("click", function (event) {
		        event.preventDefault();
		        $(".vid-list-container").stop().animate({
		            scrollLeft: "+=336"
		        }, 750);
		    });
		    $(".arrow-left").bind("click", function (event) {
		        event.preventDefault();
		        $(".vid-list-container").stop().animate({
		            scrollLeft: "-=336"
		        }, 750);
		    });
		});

		$(document).ready(function(){
	$("#vid_frame").on('ended', function(){
		alert('El video ha finalizado!!!');
	});
});


  	</script>

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
                alert('done');
            }
        }

    </script>

  	

  

</body></html>