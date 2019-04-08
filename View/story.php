<!DOCTYPE html>
<html>
<head>
	<title>ALL IS A STORY</title>
</head>
<body>
	<h1>ALL IS A STORY</h1>

<br />

<div id="sample">
<br /><br />
<h4>Textarea Example</h4>
<div>
	<textarea style="width: 300px; height: 100px;" id="myArea2"></textarea>
	<br />
	<button onClick="addArea2();">Add Editor to TEXTAREA</button> <button onClick="removeArea2();">Remove Editor from TEXTAREA</button>	
</div>
<div style="clear: both;"></div>

<script src="../Assets/js/nicEdit/nicEdit.js" type="text/javascript"></script>
<script>
var area1, area2;

function toggleArea1() {
	if(!area1) {
		area1 = new nicEditor({fullPanel : true}).panelInstance('myArea1',{hasPanel : true});
	} else {
		area1.removeInstance('myArea1');
		area1 = null;
	}
}

function addArea2() {
	area2 = new nicEditor({fullPanel : true}).panelInstance('myArea2');
}
function removeArea2() {
	area2.removeInstance('myArea2');
}

bkLib.onDomLoaded(function() { toggleArea1(); });
</script>	
</div>
</body>
</html>