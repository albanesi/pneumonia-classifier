fetch('/history/all')
    .then(response => response.json())
    .then(data => {
        console.log("✅ Verlauf geladen:", data); // NEU
        const tbody = document.getElementById("verlaufTableBody");
        tbody.innerHTML = ""; // leeren (wichtig bei Refresh)
        data.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
        data.forEach(entry => {
            const row = document.createElement("tr");
            row.innerHTML = `
                <td>${new Date(entry.timestamp).toLocaleString()}</td>
                <td>${entry.filename}</td>
                <td><strong>${entry.diagnosis.replaceAll("_", " ")}</strong></td>
                <td><img src="data:image/jpeg;base64,${entry.imageBase64}" class="img-thumbnail"/></td>
            `;
            tbody.appendChild(row);
        });
    })
    .catch(error => {
        console.error("❌ Fehler beim Laden des Verlaufs:", error);
    });
