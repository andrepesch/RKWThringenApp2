<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>RKW Erfassungsbogen vervollständigen</title>
    <link rel="stylesheet" href="https://cdn.simplecss.org/simple.min.css">
    <style>
        body { max-width: 800px; margin: auto; padding: 1rem; }
        header, main { background: var(--bg); padding: 2rem; border-radius: 10px; }
        .spinner { border: 4px solid #f3f3f3; border-top: 4px solid #3498db; border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; margin: 20px auto; }
        @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
        .hidden { display: none; }
        h3 { margin-top: 2rem; }
        #message-container h2 { color: var(--accent); }
    </style>
</head>
<body>
    <header>
        <h1>Erfassungsbogen vervollständigen</h1>
        <p>Bitte prüfen und ergänzen Sie die folgenden Daten.</p>
    </header>

    <main>
        <div id="loader" class="spinner"></div>
        <form id="customer-form" class="hidden">
            <h3 id="form-title"></h3>
            
            <h4>Unternehmensdaten</h4>
            <label for="companyName">Unternehmensname:</label>
            <input type="text" id="companyName" name="companyName" required>
            
            <h4>Ansprechpartner</h4>
            <label for="mainContactName">Name:</label>
            <input type="text" id="mainContactName" name="mainContactName" required>
            <label for="mainContactEmail">E-Mail:</label>
            <input type="email" id="mainContactEmail" name="mainContactEmail" required>
            <label for="mainContactPhone">Telefon:</label>
            <input type="tel" id="mainContactPhone" name="mainContactPhone">

            <!-- Fügen Sie hier bei Bedarf weitere Formularfelder nach demselben Muster hinzu -->

            <br><br>
            <button type="submit" id="submit-button">Änderungen speichern und zurücksenden</button>
        </form>
        <div id="message-container"></div>
    </main>

    <script>
        document.addEventListener('DOMContentLoaded', async () => {
            const form = document.getElementById('customer-form');
            const loader = document.getElementById('loader');
            const messageContainer = document.getElementById('message-container');
            const submitButton = document.getElementById('submit-button');
            
            let originalFormData = {};

            const urlParams = new URLSearchParams(window.location.search);
            const token = urlParams.get('token');

            if (!token) {
                loader.classList.add('hidden');
                messageContainer.innerHTML = '<p style="color: red;">Fehler: Kein gültiger Zugriffs-Token gefunden.</p>';
                return;
            }

            // 1. Daten vom Server laden
            try {
                const response = await fetch(`get_form_by_token.php?token=${token}`);
                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || 'Der Link ist möglicherweise ungültig oder abgelaufen.');
                }
                originalFormData = await response.json();
                
                // 2. Formularfelder befüllen
                document.getElementById('form-title').innerText = originalFormData.companyName;
                document.getElementById('companyName').value = originalFormData.companyName || '';
                document.getElementById('mainContactName').value = originalFormData.mainContact.name || '';
                document.getElementById('mainContactEmail').value = originalFormData.mainContact.email || '';
                document.getElementById('mainContactPhone').value = originalFormData.mainContact.phone || '';

                loader.classList.add('hidden');
                form.classList.remove('hidden');
            } catch (error) {
                loader.classList.add('hidden');
                messageContainer.innerHTML = `<p style="color: red;">${error.message}</p>`;
            }

            // 3. Voll funktionsfähige Speicherlogik
            form.addEventListener('submit', async (event) => {
                event.preventDefault();
                submitButton.disabled = true;
                submitButton.innerText = 'Speichert...';
                messageContainer.innerHTML = ''; // Alte Fehlermeldungen löschen

                // Geänderte Daten sammeln und mit den Originaldaten zusammenführen
                const updatedData = {
                    ...originalFormData,
                    companyName: document.getElementById('companyName').value,
                    mainContact: {
                        ...originalFormData.mainContact,
                        name: document.getElementById('mainContactName').value,
                        email: document.getElementById('mainContactEmail').value,
                        phone: document.getElementById('mainContactPhone').value
                    }
                    // Fügen Sie hier weitere Felder hinzu, die der Kunde ändern kann
                };

                // 4. Daten an den Server senden
                try {
                    const saveResponse = await fetch(`save_form.php?token=${token}`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(updatedData)
                    });
                    
                    const result = await saveResponse.json();

                    if (saveResponse.ok && result.status === 'success') {
                        form.classList.add('hidden');
                        messageContainer.innerHTML = '<h2>Vielen Dank!</h2><p>Ihre Änderungen wurden erfolgreich gespeichert und an Ihren Berater übermittelt. Sie können dieses Fenster nun schließen.</p>';
                    } else {
                        throw new Error(result.message || 'Ein unbekannter Fehler ist aufgetreten.');
                    }
                } catch (error) {
                    messageContainer.innerHTML = `<p style="color: red;">Ein Fehler ist aufgetreten: ${error.message}</p>`;
                    submitButton.disabled = false;
                    submitButton.innerText = 'Erneut versuchen';
                }
            });
        });
    </script>
</body>
</html>
