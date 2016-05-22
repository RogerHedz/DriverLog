<?php
/* Much of this code from BigBrother GPS 
This is the public "show map" script
*/
$name = "Roger's Phone";
$logfile = "/home/pi/position.cur";

$lines = file($logfile);

if( count($lines) < 1){
print "Error! data file $logfile empty.";
die();
}

$p = $lines[0];

$p = explode(":", $p);

$time = $p[0];
$lat = $p[1];
$lon = $p[2];
$acc = $p[3];
$reg = $p[5];
$acc = (int)$acc;
$pos = "$lat,$lon";
$timelatest = strftime("%Y-%m-%d %H:%M:%S", $time);
$utime = urlencode($timelatest);
$uname = urlencode($name);

?>

<html>
<head>
<meta name="viewport" http-equiv="refresh" content="120" />
<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false"></script>
<script type="text/javascript">
  function initialize() {
    var latlng = new google.maps.LatLng(<?=$lat?>,<?=$lon?>);
    var myOptions = {
      zoom: 10,
      center: latlng,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
    var marker = new google.maps.Marker({
      position: latlng, 
      map: map, 
      title:"<?=$name?>"
  });
    var previousLocations = [
<?php
 /*Draw a polyline to each previous data point*/
 for($i=0; $i < count($lines); $i++) {
    $p = $lines[$i];
    $p = explode(":", $p);
    $time = $p[0];
    $lat = $p[1];
    $lon = $p[2];
    $acc = $p[3];
    $reg = $p[4];
    $acc = (int)$acc;
    $time = strftime("%Y-%m-%d %H:%M:%S", $time);
    echo "new google.maps.LatLng($lat,$lon),\n" ;
 }
?>
  ]; /* end array of previous positions */

 var prevPos = new google.maps.Polyline( {
   path: previousLocations,
   strokeColor: "#0000FF",
   strokeOpacity: 1.0,
   strokeWeight: 2,
   map: map
  });
  
    
  } // end Initialize

</script>
</head>
<body onload="initialize()">

<p>
  Latitude: <?=$lat?>  Longitude: <?=$lon?> <br />
  Accuracy: <?=$acc?> m  Car: <?=$reg?> <br />
  Showing: <?=$time?> to <?=$timelatest?> <br />
</p>

  <div id="map_canvas" style="width:90%; height:80%"></div>

</body>
</html>
