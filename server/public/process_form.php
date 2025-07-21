<?php
// === VERBESSERTE FEHLERBEHANDLUNG ===
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
use setasign\Fpdi\Fpdi;

// Konfigurationen laden
$config = require __DIR__ . '/../config.php';
$db_config = require __DIR__ . '/../db_config.php';
$key_base_64 = require __DIR__ . '/../app_key.php';

// === DATENEMPFANG & VALIDIERUNG ===
$json_data = file_get_contents('php://input');
$data = json_decode($json_data);

// WICHTIG: Wir erwarten jetzt eine berater_id im JSON-Body
if (!$data || !isset($data->mainContact->email)) { // Validierung auf ein Pflichtfeld
    http_response_code(400);
    echo json_encode(['status' => 'error', 'message' => 'Ungültige oder unvollständige Daten empfangen.']);
    exit;
}

// HINWEIS: Hier kann Ihre detaillierte validate()-Funktion stehen, falls benötigt.
// Fürs Erste prüfen wir nur, ob überhaupt Daten da sind.

// === SCHRITT 5a: DATEN IN DER DATENBANK SPEICHERN ===
try {
    // DB-Passwort entschlüsseln
    $key = base64_decode(str_replace('base64:', '', $key_base_64));
    $iv = base64_decode(str_replace('base64:', '', $db_config['iv']));
    $tag = base64_decode(str_replace('base64:', '', $db_config['tag']));
    $encrypted_pass = base64_decode(str_replace('base64:', '', $db_config['pass_encrypted']));
    $decrypted_pass = openssl_decrypt($encrypted_pass, $db_config['cipher'], $key, OPENSSL_RAW_DATA, $iv, $tag);
    if ($decrypted_pass === false) {
        throw new Exception("Entschlüsselung des DB-Passworts fehlgeschlagen.");
    }

    // Mit DB verbinden
    $pdo = new PDO("mysql:host={$db_config['host']};dbname={$db_config['dbname']};charset=utf8", $db_config['user'], $decrypted_pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // WICHTIG: Die Berater-ID wird später durch ein sicheres Token ersetzt.
    // Für den Test nehmen wir vorerst eine feste ID an, z.B. 1
    $berater_id = $data->berater_id ?? 1;

    // Daten in die Tabelle einfügen
    $stmt = $pdo->prepare(
        "INSERT INTO formulare (berater_id, companyName, legalForm, formData) VALUES (?, ?, ?, ?)"
    );
    $stmt->execute([
        $berater_id,
        $data->companyName ?? '',
        $data->legalForm ?? '',
        $json_data // Wir speichern das komplette JSON
    ]);

} catch (Exception $e) {
    http_response_code(500);
    error_log("DB Error: " . $e->getMessage());
    echo json_encode(['status' => 'error', 'message' => 'Ein interner Serverfehler beim Speichern ist aufgetreten: ' . $e->getMessage()]);
    exit;
}

// === SCHRITT 5b: PDF-Erstellung ===
try {
    $pdf = new Fpdi('P', 'mm', 'A4');
    $pdf->AddPage();
    if (file_exists(__DIR__ . '/erfassungsbogen_bg.png')) {
        $pdf->Image(__DIR__ . '/erfassungsbogen_bg.png', 0, 0, 210, 297);
    } else {
        throw new Exception("Hintergrundbild erfassungsbogen_bg.png nicht gefunden.");
    }
    $pdf->SetFont('Helvetica', '', 9);
    $pdf->SetTextColor(0, 0, 0);

    function writeText($pdf, $x, $y, $text) {
        $text = iconv('UTF-8', 'windows-1252//IGNORE', $text ?? '');
        $pdf->SetXY($x, $y);
        $pdf->Cell(0, 0, $text);
    }
    function writeMultiLineText($pdf, $x, $y, $w, $text) {
        $text = iconv('UTF-8', 'windows-1252//IGNORE', $text ?? '');
        $pdf->SetXY($x, $y);
        $pdf->MultiCell($w, 4, $text);
    }

    // --- PLATZIERUNG DER DATEN ---
    writeText($pdf, 44, 47, $data->companyName ?? '');
    writeText($pdf, 139, 47, $data->legalForm ?? '');
    writeText($pdf, 44, 54, $data->streetAndNumber ?? '');
    writeText($pdf, 20, 61, $data->postalCode ?? '');
    writeText($pdf, 44, 61, $data->city ?? '');
    writeText($pdf, 139, 61, $data->foundationDate ?? '');
    if ($data->isVatDeductible ?? false) writeText($pdf, 114, 39.5, 'X'); else writeText($pdf, 125, 39.5, 'X');
    writeText($pdf, 44, 68, $data->mainContact->name ?? '');
    writeText($pdf, 77, 68, $data->mainContact->phone ?? '');
    writeText($pdf, 139, 68, $data->mainContact->email ?? '');
    if (isset($data->beneficialOwners) && is_array($data->beneficialOwners)) {
        $yPos = 81;
        foreach ($data->beneficialOwners as $owner) {
            writeText($pdf, 20, $yPos, $owner->lastName ?? '');
            writeText($pdf, 56, $yPos, $owner->firstName ?? '');
            writeText($pdf, 89, $yPos, $owner->birthDate ?? '');
            writeText($pdf, 139, $yPos, $owner->taxId ?? '');
            $yPos += 5.5;
        }
    }
    writeText($pdf, 20, 94, $data->industrySector ?? '');
    if ($data->hasWebsite ?? false) writeText($pdf, 120, 94, 'X'); else writeText($pdf, 130, 94, 'X');
    writeText($pdf, 139, 94, $data->websiteUrl ?? '');
    writeText($pdf, 44, 111, $data->bankDetails->institute ?? '');
    writeText($pdf, 69, 111, $data->bankDetails->iban ?? '');
    writeText($pdf, 139, 111, $data->bankDetails->taxId ?? '');
    if (isset($data->smeClassification)) {
        writeText($pdf, 81, 128, $data->smeClassification->penultimateYear->employees ?? '0');
        writeText($pdf, 81, 132.5, ($data->smeClassification->penultimateYear->turnover ?? '0') . ' €');
        writeText($pdf, 81, 137, ($data->smeClassification->penultimateYear->balanceSheetTotal ?? '0') . ' €');
        writeText($pdf, 122, 128, $data->smeClassification->lastYear->employees ?? '0');
        writeText($pdf, 122, 132.5, ($data->smeClassification->lastYear->turnover ?? '0') . ' €');
        writeText($pdf, 122, 137, ($data->smeClassification->lastYear->balanceSheetTotal ?? '0') . ' €');
    }
    if (isset($data->consultationDetails)) {
        writeText($pdf, 20, 148, $data->consultationDetails->focus ?? '');
        writeText($pdf, 81, 148, ($data->consultationDetails->scopeInDays ?? '') . ' Tage');
        writeText($pdf, 122, 148, ($data->consultationDetails->dailyRate ?? '') . ' €');
        writeText($pdf, 164, 148, $data->consultationDetails->endDate ?? '');
        writeMultiLineText($pdf, 20, 161.5, 172, $data->consultationDetails->initialSituation ?? '');
        writeMultiLineText($pdf, 20, 181, 172, $data->consultationDetails->consultationContent ?? '');
    }
    writeText($pdf, 23, 223, $data->consultingFirm ?? '');
    if (isset($data->consultants) && is_array($data->consultants)) {
        $yPos = 230;
        foreach ($data->consultants as $consultant) {
            writeText($pdf, 23, $yPos, $consultant->lastName ?? '');
            writeText($pdf, 56, $yPos, $consultant->firstName ?? '');
            writeText($pdf, 89, $yPos, $consultant->accreditationId ?? '');
            writeText($pdf, 139, $yPos, $consultant->email ?? '');
            $yPos += 5.5;
        }
    }
    if ($data->hasAcknowledgedPublicationObligations ?? false) {
        writeText($pdf, 181, 253, 'X');
    }

    $pdf_content = $pdf->Output('S');

} catch (Exception $e) {
    http_response_code(500);
    error_log("PDF Error: " . $e->getMessage());
    echo json_encode(['status' => 'error', 'message' => 'Ein interner Serverfehler bei der PDF-Erstellung ist aufgetreten.']);
    exit;
}

// === SCHRITT 5c: E-Mail-Versand ===
$mail = new PHPMailer(true);
try {
    $key_mail = base64_decode(str_replace('base64:', '', $key_base_64));
    $iv_mail = base64_decode(str_replace('base64:', '', $config['iv']));
    $tag_mail = base64_decode(str_replace('base64:', '', $config['tag']));
    $encrypted_password_mail = base64_decode(str_replace('base64:', '', $config['smtp_password_encrypted']));
    $decrypted_password_mail = openssl_decrypt($encrypted_password_mail, $config['cipher'], $key_mail, OPENSSL_RAW_DATA, $iv_mail, $tag_mail);
    if ($decrypted_password_mail === false) {
        throw new Exception("Passwort-Entschlüsselung für E-Mail fehlgeschlagen.");
    }
    
    $mail->isSMTP();
    $mail->Host       = $config['smtp_host'];
    $mail->SMTPAuth   = true;
    $mail->Username   = $config['smtp_username'];
    $mail->Password   = $decrypted_password_mail;
    $mail->SMTPSecure = ($config['smtp_secure'] === 'tls') ? PHPMailer::ENCRYPTION_STARTTLS : PHPMailer::ENCRYPTION_SMTPS;
    $mail->Port       = $config['smtp_port'];
    $mail->CharSet    = 'UTF-8';

    $mail->setFrom($config['smtp_username'], 'FormPilot RKW App');
    $mail->addAddress('pesch@rkw-thueringen.de', 'RKW Thüringen');
    $mail->addCC($data->mainContact->email);
    $mail->addStringAttachment($pdf_content, 'Erfassungsbogen-' . ($data->companyName ?? 'Antrag') . '.pdf');
    $mail->isHTML(true);
    $mail->Subject = 'Neue Beratungsanfrage via FormPilot: ' . ($data->companyName ?? 'Unbekannt');
    $mail->Body    = 'Im Anhang befindet sich der digital ausgefüllte Erfassungsbogen für eine neue Beratungsanfrage.';
    
    $mail->send();
    
    echo json_encode(['status' => 'success', 'message' => 'Anfrage erfolgreich gespeichert und versendet.']);

} catch (Exception $e) {
    http_response_code(500);
    error_log("PHPMailer Error: " . $e->getMessage());
    echo json_encode(['status' => 'error', 'message' => 'E-Mail konnte nicht gesendet werden: ' . $mail->ErrorInfo]);
}

?>