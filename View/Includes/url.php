<?php
// base directory
$base_dir = __DIR__;

// server protocol
$protocol = empty($_SERVER['HTTPS']) ? 'http' : 'https';

// domain name
$domain = $_SERVER['SERVER_NAME'];

// base url
$base_url = preg_replace("!^${doc_root}!", '', $base_dir);

// server port
$port = $_SERVER['SERVER_PORT'];
$disp_port = ($protocol == 'http' && $port == 80 || $protocol == 'https' && $port == 443) ? '' : ":$port";

// put em all together to get the complete base URL
$url = "${protocol}://${domain}${disp_port}${base_url}";

// Includes base URL

$includes_url = "${url}/View/Includes/css.php";

echo $base_dir."<br>"; // = http://example.com/path/directory
echo $protocol."<br>";
echo $domain."<br>";
echo $base_url."<br>";
echo $port."<br>";
echo $disp_port."<br>";
echo $url."<br>";



$domain
?>