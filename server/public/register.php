<?php
// === VERBESSERTE FEHLERBEHANDLUNG ===
ini_set('display_errors', 1);
error_reporting(E_ALL);
set_error_handler(function ($severity, $message, $file, $line) {
    http_response_code(500);
    echo json_encode([
        'status' => 'error',
        'message' => 'PHP Error: ' . $message,
        'file' => $file,
        'line' => $line
    ]);
    exit;
});

header('Content-Type: application/json');

// 1. Konfiguration und Schlüssel laden
$db_config = require __DIR__ . '/../db_config.php';
$key_base64 = require __DIR__ . '/../app_key.php';

// 2. Daten von der App empfangen
$json_data = file_get_contents('php://input');
$data = json_decode($json_data);

if (!$data || !isset($data->email) || !isset($data->password)) {
    http_response_code(400);
    echo json_encode(['status' => 'error', 'message' => 'Ungültige Eingabedaten.']);
    exit;
}
$email = $data->email;
$password = $data->password;

// 3. Eingaben validieren
if (!filter_var($email, FILTER_VALIDATE_EMAIL) || strlen($password) < 8) {
    http_response_code(400);
    echo json_encode(['status' => 'error', 'message' => 'E-Mail ungültig oder Passwort zu kurz.']);
    exit;
}

// 4. Passwort sicher verschlüsseln (für die Speicherung)
$password_hash = password_hash($password, PASSWORD_DEFAULT);

// 5. Datenbank-Passwort zur Laufzeit entschlüsseln
try {
    $key = base64_decode(str_replace('base64:', '', $key_base64));
    $iv = base64_decode(str_replace('base64:', '', $db_config['iv']));
    $tag = base64_decode(str_replace('base64:', '', $db_config['tag']));
    $encrypted_pass = base64_decode(str_replace('base64:', '', $db_config['pass_encrypted']));
    
    $decrypted_pass = openssl_decrypt($encrypted_pass, $db_config['cipher'], $key, OPENSSL_RAW_DATA, $iv, $tag);
    if ($decrypted_pass === false) {
        throw new Exception("Entschlüsselung des DB-Passworts fehlgeschlagen.");
    }
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['status' => 'error', 'message' => 'Server-Konfigurationsfehler: ' . $e->getMessage()]);
    exit;
}

// 6. Mit der Datenbank verbinden
try {
    $pdo = new PDO("mysql:host={$db_config['host']};dbname={$db_config['dbname']};charset=utf8", $db_config['user'], $decrypted_pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['status' => 'error', 'message' => 'Datenbankverbindung fehlgeschlagen: ' . $e->getMessage()]);
    exit;
}

// 7. Prüfen, ob die E-Mail bereits existiert
$stmt = $pdo->prepare("SELECT id FROM berater WHERE email = ?");
$stmt->execute([$email]);
if ($stmt->fetch()) {
    http_response_code(409);
    echo json_encode(['status' => 'error', 'message' => 'Diese E-Mail-Adresse ist bereits registriert.']);
    exit;
}

// 8. Neuen Berater in die Datenbank einfügen
try {
    $stmt = $pdo->prepare("INSERT INTO berater (email, password_hash) VALUES (?, ?)");
    $stmt->execute([$email, $password_hash]);
    echo json_encode(['status' => 'success', 'message' => 'Registrierung erfolgreich.']);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['status' => 'error', 'message' => 'Fehler beim Speichern der Daten.']);
}