<?php
header('Content-Type: application/json');
echo json_encode([
    'status' => 'success',
    'message' => 'Verbindung zum Server erfolgreich hergestellt!'
]);
?>