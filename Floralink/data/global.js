let isConnected;

async function updateWifiStatus() {
    const response = await fetch('/connection-status', {
        headers: {
            'Content-Type': 'application/json',
        }
    });
    const json = await response.json();
    const responseIsConnecting = json.isConnecting;
    const responseIsConnected = json.isConnected;

    while(!responseIsConnected && responseIsConnecting)
    {
        await delay(500);
    }

    if(!responseIsConnecting && !responseIsConnected)
    {
        alert("Connecting failed");
        return;
    }

    if(responseIsConnected)
    {
        alert("Connected successfully, new IP: " + json.ip);
    }

    sessionStorage.setItem("isConnected", isConnected);
}