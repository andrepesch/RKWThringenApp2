<?php
// share_form.php
// Generiert einen sicheren Token für ein Formular, speichert ihn in der Datenbank
// und sendet einen einmaligen Zugriffslink per E-Mail an den Kunden.

// === FEHLERBEHANDLUNG ===
ini_set('display_errors', 1);
error_reporting(E_ALL);
set_error_handler(function ($severity, $message, $file, $line) {
    http_response_code(500);
    echo json_encode(['status' => 'error', 'message' => "PHP Error: $message in $file on line $line"]);
    exit;
});

header('Content-Type: application/json');

// Composer Autoloader & Klassen importieren
require __DIR__ . '/vendor/autoload.php';
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

// Alle Konfigurationen laden
$config = require __DIR__ . '/../config.php';
$db_config = require __DIR__ . '/../db_config.php';
$key_base_64 = require __DIR__ . '/../app_key.php';

// === DATENEMPFANG VON DER APP ===
$form_id = $_GET['form_id'] ?? null;
if (!$form_id || !is_numeric($form_id)) {
    http_response_code(400);
    echo json_encode(['status' => 'error', 'message' => 'Gültige Formular-ID fehlt.']);
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

// === TOKEN GENERIEREN UND SPEICHERN ===
try {
    // 1. Sicheren, zufälligen Token generieren
    $token = bin2hex(random_bytes(32)); // Erzeugt einen 64-stelligen zufälligen String

    // 2. Token in der Datenbank für das spezifische Formular speichern
    $stmt = $pdo->prepare("UPDATE formulare SET share_token = ? WHERE id = ?");
    $stmt->execute([$token, $form_id]);
} catch (Exception $e) {
    http_response_code(500);
    error_log("Token Generation Error: " . $e->getMessage());
    echo json_encode(['status' => 'error', 'message' => 'Fehler beim Erstellen des sicheren Links.']);
    exit;
}

// === KUNDENDATEN LADEN UND E-MAIL SENDEN ===
try {
    // 3. E-Mail-Adresse des Kunden aus den Formulardaten holen
    $stmt = $pdo->prepare("SELECT formData FROM formulare WHERE id = ?");
    $stmt->execute([$form_id]);
    $form = $stmt->fetch();
    if (!$form) {
        throw new Exception("Formular nicht in der Datenbank gefunden.");
    }
    $formData = json_decode($form['formData']);
    $customer_email = $formData->mainContact->email ?? null;
    $customer_name = $formData->mainContact->name ?? 'Sehr geehrte/r Kunde/in';

    if (!$customer_email || !filter_var($customer_email, FILTER_VALIDATE_EMAIL)) {
        throw new Exception("Im Formular ist keine gültige E-Mail-Adresse des Ansprechpartners hinterlegt.");
    }

    // 4. E-Mail-Passwort entschlüsseln
    $iv_mail = base64_decode(str_replace('base64:', '', $config['iv']));
    $tag_mail = base64_decode(str_replace('base64:', '', $config['tag']));
    $encrypted_password_mail = base64_decode(str_replace('base64:', '', $config['smtp_password_encrypted']));
    $decrypted_password_mail = openssl_decrypt($encrypted_password_mail, $config['cipher'], $key, OPENSSL_RAW_DATA, $iv_mail, $tag_mail);
    if ($decrypted_password_mail === false) {
        throw new Exception("E-Mail Passwort Entschlüsselung fehlgeschlagen.");
    }

    // 5. E-Mail mit dem sicheren Link an den Kunden senden
    $sharing_link = "https://formpilot.eu/fill_form.php?token=" . $token;
    
    $mail = new PHPMailer(true);
    $mail->isSMTP();
    $mail->Host       = $config['smtp_host'];
    $mail->SMTPAuth   = true;
    $mail->Username   = $config['smtp_username'];
    $mail->Password   = $decrypted_password_mail;
    $mail->SMTPSecure = ($config['smtp_secure'] === 'tls') ? PHPMailer::ENCRYPTION_STARTTLS : PHPMailer::ENCRYPTION_SMTPS;
    $mail->Port       = $config['smtp_port'];
    $mail->CharSet    = 'UTF-8';

    $mail->setFrom($config['smtp_username'], 'Ihr RKW Berater (via FormPilot)');
    $mail->addAddress($customer_email, $customer_name);
    $mail->isHTML(true);
    $mail->Subject = 'Bitte um Vervollständigung Ihres RKW Erfassungsbogens';
    $mail->Body    = "Sehr geehrte/r $customer_name,<br><br>bitte klicken Sie auf den folgenden Link, um Ihren Erfassungsbogen zu überprüfen und zu vervollständigen. Dieser Link ist nur für Sie bestimmt und einmalig gültig.<br><br><a href=\"$sharing_link\">Zum Formular</a><br><br>Mit freundlichen Grüßen<br>Ihr RKW Thüringen Berater";
    $mail->AltBody = "Sehr geehrte/r $customer_name,\n\nbitte öffnen Sie den folgenden Link in Ihrem Browser, um Ihren Erfassungsbogen zu überprüfen und zu vervollständigen:\n$sharing_link\n\nMit freundlichen Grüßen\nIhr RKW Thüringen Berater";
    
    $mail->send();

    echo json_encode(['status' => 'success', 'message' => 'Einladungs-E-Mail wurde erfolgreich an ' . $customer_email . ' versendet.']);

} catch (Exception $e) {
    http_response_code(500);
    error_log("Mail Sending Error: " . $e->getMessage());
    echo json_encode(['status' => 'error', 'message' => 'Fehler: ' . $e->getMessage()]);
}

?>