<?php
// get_form_details.php
// Liefert die kompletten JSON-Daten für ein einzelnes Formular, angereichert mit der korrekten ID.

ini_set('display_errors', 1);
error_reporting(E_ALL);
header('Content-Type: application/json');

$db_config = require __DIR__ . '/../db_config.php';
$key_base_64 = require __DIR__ . '/../app_key.php';

if (!isset($_GET['form_id'])) {
    http_response_code(400);
    echo json_encode(['status' => 'error', 'message' => 'Formular-ID fehlt.']);
    exit;
}
$form_id = $_GET['form_id'];

// DB-Passwort entschlüsseln und verbinden...
try {
    $key = base64_decode(str_replace('base64:', '', $key_base_64));
    $iv = base64_decode(str_replace('base64:', '', $db_config['iv']));
    $tag = base64_decode(str_replace('base64:', '', $db_config['tag']));
    $encrypted_pass = base64_decode(str_replace('base64:', '', $db_config['pass_encrypted']));
    $decrypted_pass = openssl_decrypt($encrypted_pass, $db_config['cipher'], $key, OPENSSL_RAW_DATA, $iv, $tag);
    
    $pdo = new PDO("mysql:host={$db_config['host']};dbname={$db_config['dbname']};charset=utf8", $db_config['user'], $decrypted_pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['status' => 'error', 'message' => 'Server-Fehler: ' . $e->getMessage()]);
    exit;
}

// Formulardaten abrufen
$stmt = $pdo->prepare("SELECT id, formData FROM formulare WHERE id = ?");
$stmt->execute([$form_id]);
$result = $stmt->fetch(PDO::FETCH_ASSOC);

if ($result && !empty($result['formData'])) {
    // Den gespeicherten JSON-String dekodieren
    $formDataObject = json_decode($result['formData']);
    
    // Sicherstellen, dass das Objekt gültig ist und die ID aus der DB-Zeile setzen
    if (is_object($formDataObject)) {
        $formDataObject->id = (int)$result['id'];
        
        // Das korrigierte Objekt wieder als JSON ausgeben
        echo json_encode($formDataObject);
    } else {
        http_response_code(500);
        echo json_encode(['status' => 'error', 'message' => 'In der Datenbank gespeicherte Formulardaten sind ungültig.']);
    }
} else {
    http_response_code(404);
    echo json_encode(['status' => 'error', 'message' => 'Formular nicht gefunden oder leer.']);
}