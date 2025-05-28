const FAILED_TEXT = "&#10006; Invalid key";
const PASSED_TEXT = "&#10004; Valid key";
const BASE_URL = "https://florae.dayfit.pl";

document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('settings-form');
    const apiKeyInfoPanel = document.getElementById('checkresult');

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

        if(checkApiKey(data["floraeAccessKey"]))
        {
            apiKeyInfoPanel.textContent = PASSED_TEXT;
            apiKeyInfoPanel.className = "passed";
            apiKeyInfoPanel.style.display = "block";

        }

        else
        {
            apiKeyInfoPanel.textContent = FAILED_TEXT;
            apiKeyInfoPanel.className = "failed";
            apiKeyInfoPanel.style.display = "block";
        }

        setTimeout(()=>{
            apiKeyInfoPanel.textContent = "";
            apiKeyInfoPanel.className = "";
            apiKeyInfoPanel.style.display = "none";
        }, 1000)
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
            return result.isValid;
        } else {
            console.error('Failed to validate API key:', response.statusText);
            return false;
        }
    } catch (err) {
        console.error('Error validating API key:', err);
        return false;
    }
}