<?php

/* Much of this code from BigBrotherGPS 
 This is the receiver for a http POST
 Required data is latitude, longitude, accuracy and car registration number
 */

$logfile = "/home/pi/position.cur";
$maxDataPoints = "100";

/* Get the position data from the POST parameters */

$lat = $_POST["latitude"];
$lon = $_POST["longitude"];
$acc = $_POST["accuracy"];
$reg = $_POST["regnr"];

/* Load the original file contents. */
$lines = file($logfile);

/* Write the position data to a file for the map script */
 if ($lat && $lon && $acc) {
    $fcur = fopen($logfile, "w");

    $time = time();
    $out = "$time:$lat:$lon:$acc:$regnr\n";

    fputs($fcur, $out);

   /* write the old data under the new data */
   for($i=0; $i < count($lines); $i++){
     if ($i < $maxDataPoints) {
       $out = "$lines[$i]";
       fputs($fcur,$out);
     }
   }
    
    fclose($fcur);
    print ("location ok received!");
}

?>
