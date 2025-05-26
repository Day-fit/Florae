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

async function checkApiKey() {
    
}