<?php
// get_forms.php (Korrigierte Version für das neue Kartendesign)

ini_set('display_errors', 1);
error_reporting(E_ALL);
header('Content-Type: application/json');

$db_config = require __DIR__ . '/../db_config.php';
$key_base_64 = require __DIR__ . '/../app_key.php';

if (!isset($_GET['berater_id'])) {
    http_response_code(400);
    echo json_encode(['status' => 'error', 'message' => 'Berater-ID fehlt.']);
    exit;
}
$berater_id = $_GET['berater_id'];

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
    echo json_encode(['status' => 'error', 'message' => 'Server-Fehler.']);
    exit;
}

// Alle relevanten Daten für das Dashboard abrufen
$stmt = $pdo->prepare("SELECT id, status, companyName, formData, updated_at FROM formulare WHERE berater_id = ? ORDER BY updated_at DESC");
$stmt->execute([$berater_id]);
$forms = $stmt->fetchAll(PDO::FETCH_ASSOC);

$results = [];
foreach ($forms as $form) {
    $formData = json_decode($form['formData']);
    
    // Alle benötigten Felder sicher aus dem JSON extrahieren
    $dailyRate = $formData->consultationDetails->dailyRate ?? 0;
    $scopeInDays = $formData->consultationDetails->scopeInDays ?? 0;
    $address = trim(($formData->streetAndNumber ?? '') . ', ' . ($formData->postalCode ?? '') . ' ' . ($formData->city ?? ''), ', ');
    $mainContact = $formData->mainContact->name ?? 'Kein Ansprechpartner';

    $results[] = [
        'id' => $form['id'],
        'status' => $form['status'],
        'companyName' => $form['companyName'],
        'address' => $address,
        'mainContact' => $mainContact,
        'scopeInDays' => $scopeInDays,
        'dailyRate' => (int)$dailyRate,
        'updated_at' => $form['updated_at']
    ];
}

echo json_encode($results);