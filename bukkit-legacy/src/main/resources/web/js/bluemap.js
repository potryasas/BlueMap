// BlueMap Legacy JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('BlueMap Legacy loaded');
    
    // Check if there are world maps defined
    const mapsList = document.getElementById('maps-list');
    
    // Add a simple animation when hovering over maps
    if (mapsList) {
        const mapLinks = mapsList.querySelectorAll('a');
        mapLinks.forEach(link => {
            link.addEventListener('mouseenter', function() {
                this.parentElement.style.backgroundColor = '#f0f7ff';
            });
            
            link.addEventListener('mouseleave', function() {
                this.parentElement.style.backgroundColor = '#f9f9f9';
            });
        });
    }
    
    // Try to load map list from server if available
    function refreshMapsList() {
        // Simple XHR request to fetch available maps
        const xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    try {
                        const mapsData = JSON.parse(xhr.responseText);
                        if (mapsData && mapsData.maps && mapsData.maps.length > 0) {
                            // Clear existing list
                            mapsList.innerHTML = '';
                            
                            // Add maps from server
                            mapsData.maps.forEach(map => {
                                const li = document.createElement('li');
                                const a = document.createElement('a');
                                a.href = `maps/${map.id}/index.html`;
                                a.textContent = map.name || map.id;
                                li.appendChild(a);
                                mapsList.appendChild(li);
                            });
                        }
                    } catch (e) {
                        console.error('Error parsing maps JSON:', e);
                    }
                }
            }
        };
        xhr.open('GET', 'maps.json', true);
        xhr.send();
    }
    
    // Try to refresh maps list
    refreshMapsList();
}); 