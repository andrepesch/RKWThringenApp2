<?php
// get_form_by_token.php
// Sucht in der Datenbank nach einem Formular, das zu einem sicheren Token passt,
// und gibt die Formulardaten als JSON zurück. Dieses Skript wird vom Web-Formular des Kunden aufgerufen.

// === FEHLERBEHANDLUNG ===
ini_set('display_errors', 1);
error_reporting(E_ALL);
set_error_handler(function ($severity, $message, $file, $line) {
    http_response_code(500);
    echo json_encode(['status' => 'error', 'message' => "PHP Error: $message in $file on line $line"]);
    exit;
});

header('Content-Type: application/json');

// Alle Konfigurationen laden
$db_config = require __DIR__ . '/../db_config.php';
$key_base_64 = require __DIR__ . '/../app_key.php';

// === TOKEN AUS DER URL EMPFANGEN ===
$token = $_GET['token'] ?? null;
if (!$token || strlen($token) < 64) { // Prüft, ob ein Token vorhanden und lang genug ist
    http_response_code(400);
    echo json_encode(['status' => 'error', 'message' => 'Ungültiger oder fehlender Zugriffs-Token.']);
    exit;
}

// === DATENBANK-VERBINDUNG AUFBAUEN ===
try {
    // Geheimen Schlüssel für die Entschlüsselung vorbereiten
    $key = base64_decode(str_replace('base64:', '', $key_base_64));

    // DB-Passwort entschlüsseln
    $iv_db = base64_decode(str_replace('base64:', '', $db_config['iv']));
    $tag_db = base64_decode(str_replace('base64:', '', $db_config['tag']));
    $encrypted_pass_db = base64_decode(str_replace('base64:', '', $db_config['pass_encrypted']));
    $decrypted_pass = openssl_decrypt($encrypted_pass_db, $db_config['cipher'], $key, OPENSSL_RAW_DATA, $iv_db, $tag_db);
    if ($decrypted_pass === false) throw new Exception("DB Passwort Entschlüsselung fehlgeschlagen.");

    // Mit der Datenbank verbinden
    $pdo = new PDO("mysql:host={$db_config['host']};dbname={$db_config['dbname']};charset=utf8", $db_config['user'], $decrypted_pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (Exception $e) {
    http_response_code(500);
    error_log("DB Connection Error: " . $e->getMessage());
    echo json_encode(['status' => 'error', 'message' => 'Server-Fehler bei der Datenbankverbindung.']);
    exit;
}

// === FORMULARDATEN ANHAND DES TOKENS SUCHEN ===
try {
    $stmt = $pdo->prepare("SELECT formData FROM formulare WHERE share_token = ?");
    $stmt->execute([$token]);
    $result = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($result && !empty($result['formData'])) {
        // Erfolg! Wir geben den rohen JSON-String der Formulardaten direkt aus.
        // Das JavaScript im Web-Formular kann dies direkt verarbeiten.
        echo $result['formData'];
    } else {
        // Wenn kein Formular mit diesem Token gefunden wurde
        http_response_code(404); // "Not Found" ist der korrekte Statuscode
        echo json_encode(['status' => 'error', 'message' => 'Kein Formular für diesen Link gefunden. Er ist möglicherweise abgelaufen oder ungültig.']);
    }
} catch (Exception $e) {
    http_response_code(500);
    error_log("Form Fetch Error: " . $e->getMessage());
    echo json_encode(['status' => 'error', 'message' => 'Fehler beim Abrufen der Formulardaten.']);
}

?>