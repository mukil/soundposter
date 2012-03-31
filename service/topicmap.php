<?php
// local proxy script to allow xhr to other domains
//
// author github.com/mukil
// proxy solution via http://jquery-howto.blogspot.com

// Set your return content type
header('Content-type: application/json');
$q = $_GET['t'];
$q = urlencode($q);

// Website url to open
$daurl = 'http://www.soundposter.com:8080/topicmap/'.$q;

// Get that website's content
$handle = fopen($daurl, "r");

// print $handle;

// If there is something, read and return
// if ($handle) {
while (!feof($handle)) {
    $buffer = fgets($handle, 4096);
    echo $buffer;
}
fclose($handle);
// }
?>
