document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('settings-form');
    if (!form) return;
    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        const formData = new FormData(form);
        const data = {};
        formData.forEach((value, key) => {
            data[key] = value;
        });
        try {
            const response = await fetch('/save', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data),
            });
            if (response.ok) {
                alert('Configuration saved!');
            } else {
                alert('Error saving configuration.');
            }
        } catch (err) {
            alert('Network error.');
        }
    });
});

async function checkApiKey(apiKey) {
    try {
        const response = await fetch('/validate-api-key', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ apiKey }),
        });
        if (response.ok) {
            const result = await response.json();
            return result.isValid; // Assuming the server returns { isValid: true/false }
        } else {
            console.error('Failed to validate API key:', response.statusText);
            return false;
        }
    } catch (err) {
        console.error('Error validating API key:', err);
        return false;
    }
}