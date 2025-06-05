const FAILED_TEXT = "&#10006; Invalid key";
const PASSED_TEXT = "&#10004; Valid key";

function delay(ms)
{
    return new Promise(resolve => setTimeout(resolve, ms));
}

document.addEventListener('DOMContentLoaded', async function() {
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

        await updateWifiStatus();

        if(await checkApiKey(data["floraeAccessKey"]))
        {
            apiKeyInfoPanel.innerHTML = PASSED_TEXT;
            apiKeyInfoPanel.className = "passed";
            apiKeyInfoPanel.style.display = "block";
        }

        else
        {
            apiKeyInfoPanel.innerHTML = FAILED_TEXT;
            apiKeyInfoPanel.className = "failed";
            apiKeyInfoPanel.style.display = "block";
        }

        setTimeout(()=>{
            apiKeyInfoPanel.textContent = "";
            apiKeyInfoPanel.className = "";
            apiKeyInfoPanel.style.display = "none";
        }, 3000)
    });
});

async function checkApiKey(apiKey) {
    try {
        const response = await fetch('/validate-api-key?apiKey='+apiKey, {
            method: 'GET',
        });
        if (response.ok) {
            const result = await response.json();
            return result.result;
        } else {
            console.error('Failed to validate API key:', response.statusText);
            return false;
        }
    } catch (err) {
        console.error('Error validating API key:', err);
        return false;
    }
}

async function updateWifiStatus() {
    const spinner = document.getElementById('wifi-spinner');
    if (spinner) spinner.style.display = 'block';
    try {
        let response, json;
        do {
            response = await fetch('/connection-status', {
                headers: {
                    'Content-Type': 'application/json',
                }
            });
            json = await response.json();
            if (!json.isConnecting || json.isConnected) break;
            await delay(500);
        } while (true);

        const responseIsConnecting = json.isConnecting;
        const responseIsConnected = json.isConnected;

        if(!responseIsConnecting && !responseIsConnected)
        {
            alert("Connecting failed");
            return;
        }

        if(responseIsConnected)
        {
            alert("Connected successfully, new IP: " + json.ip);
        }

        sessionStorage.setItem("isConnected", responseIsConnected);
    } finally {
        if (spinner) spinner.style.display = 'none';
    }
}