<?php
// local proxy script to allow xhr to other domains
//
// author github.com/mukil
// proxy solution via http://jquery-howto.blogspot.com

// Set your return content type
header('Content-type: application/json');
$uri = $_GET['uri'];
$uri = urlencode($uri);
// 
$fetch = $_GET['fetch_composite'];
$fetch = urlencode($fetch);

// Website url to open
// topic/by_type/dm4.topicmaps.topicmap
$daurl = 'http://www.soundposter.com:8080/core/topic/by_type/'.$uri.'?fetch_composite='.$fetch;

// Get that website's content
$handle = fopen($daurl, "r");

// If there is something, read and return
while (!feof($handle)) {
    $buffer = fgets($handle, 4096);
    echo $buffer;
}
fclose($handle);
?>
