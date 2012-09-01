<?php
// local proxy script to allow xhr to other domains
//
// author github.com/mukil
// proxy solution via http://jquery-howto.blogspot.com

// Set your return content type
header('Content-type: application/json');
$q = $_GET['t'];
$q = urlencode($q);
$uri = $_GET['uri'];
$uri = urlencode($uri);

// Website url to open
$daurl = 'http://www.soundposter.com:8080/core/topic/'.$q.'/related_topics?others_topic_type_uri='.$uri;

$ch = curl_init($daurl);
// curl_setopt($ch, CURLOPT_HTTPHEADERS, array('Content-Type: application/json'));
curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
$output = curl_exec($ch);
// close curl resource to free up system resources
curl_close($ch);
echo $output;

/** 
// Get that website's content
$handle = fopen($daurl, "r");

// If there is something, read and return
while (!feof($handle)) {
    $buffer = fgets($handle, 4096);
    echo $buffer;
}
fclose($handle);
**/ 
?>
