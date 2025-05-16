function checkFiles(files) {
    if (files.length !== 1) {
        alert("Bitte genau eine Datei hochladen.");
        return;
    }

    const file = files[0];
    if (file.size / 1024 / 1024 > 10) {
        alert("Datei zu gross (max. 10MB)");
        return;
    }

    console.log("📤 Bildauswahl:", file.name);

    answerPart.style.visibility = "visible";
    preview.src = URL.createObjectURL(file);

    const formData = new FormData();
    formData.append("image", file);

    console.log("📨 Sende Bild an /analyze...");

    fetch('/analyze', {
        method: 'POST',
        body: formData
    })
        .then(res => res.text())
        .then(text => {
            const predictions = JSON.parse(text);
            console.log("✅ Prediction empfangen:", predictions);

            const top = predictions.reduce((a, b) => a.probability > b.probability ? a : b);

            let resultHtml = `<p><strong>➡️ Vorhersage: <span class="highlight">${top.className.replaceAll("_", " ")}</span></strong></p>`;
            resultHtml += "<ul class='list-group'>";
            predictions.forEach(p => {
                const label = p.className.replaceAll("_", " ");
                const percent = (p.probability * 100).toFixed(1);
                resultHtml += `<li class="list-group-item d-flex justify-content-between">
                                <span>${label}</span>
                                <span class="badge badge-secondary">${percent}%</span>
                               </li>`;
            });
            resultHtml += "</ul>";
            answer.innerHTML = resultHtml;

            // Spinner anzeigen
            const spinner = document.createElement('div');
            spinner.id = "loading-spinner";
            spinner.innerHTML = `<div class="mt-4 text-center text-muted">Gemini-Analyse wird geladen…<br><div class="spinner-border text-light mt-2" role="status"><span class="sr-only">Loading...</span></div></div>`;
            answer.appendChild(spinner);

            const promptText = `
Du erhältst die Textdiagnose: <strong>${top.className.replaceAll("_", " ")}</strong>.

Erstelle einen HTML-Text in zwei gut strukturierten Abschnitten:

1. <strong>Bildmerkmale:</strong> Beschreibe in einem <p>-Element typische visuelle Röntgenmerkmale für diese Diagnose – z. B. Infiltrate, Verschattungen, Flüssigkeit. Maximal 3 Sätze.

2. <strong>Ärztliche Empfehlungen:</strong> Gib 3–5 umsetzbare medizinische Empfehlungen in einem <ul><li>…</li></ul>-Block. Keine Laienerklärung, nur relevante Maßnahmen für medizinisches Fachpersonal.

⚠️ Verwende ausschließlich reines HTML. Keine Markdown-Syntax, keine Backticks (\`), keine Codeblocks!
`;

            console.log("🧠 Sende Prompt an Gemini...");

            fetch('/explain', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ prompt: promptText })
            })
                .then(res => res.text())
                .then(text => {
                    console.log("✅ Gemini-Antwort erhalten");
                    document.getElementById("loading-spinner")?.remove();
                    const explainDiv = document.createElement('div');
                    explainDiv.className = 'mt-4';
                    explainDiv.innerHTML = `<h6>Analyse durch Gemini</h6>${text}`;
                    answer.appendChild(explainDiv);

                    // Diagnose in MongoDB speichern
                    const uploadForm = new FormData();
                    uploadForm.append("image", file);
                    uploadForm.append("diagnosis", top.className);

                    console.log("🗃️ Sende Diagnose an /history/store...");

                    fetch('/history/store', {
                        method: 'POST',
                        body: uploadForm
                    })
                        .then(() => {
                            console.log("✅ Diagnose gespeichert");
                        })
                        .catch(err => {
                            console.error("❌ Fehler beim Speichern:", err);
                        });
                })
                .catch(err => {
                    console.error("❌ Fehler bei Gemini-Antwort:", err);
                });
        })
        .catch(error => {
            console.error("❌ Fehler beim Upload oder /analyze:", error);
        });
}
