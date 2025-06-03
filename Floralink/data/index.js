document.addEventListener("DOMContentLoaded", () => 
{
    displayPlantData();
});

setInterval(displayPlantData, 500);

function displayPlantData()
{
    fetch('/sensors-status')
        .then(response => {
                if(response.ok)
                {
                    return response.json();
                }

                return null;
            })
        .then(data => {
            if(data == null)
            {
                alert('Server responded with an error');
            }

            data.forEach(sensorData => 
            {
                const elementToModdify = document.getElementById(sensorData.type);

                if(elementToModdify !== null)
                {
                    elementToModdify.innerHTML = sensorData.value + " " + sensorData.unit;
                }
            });
        })
}
