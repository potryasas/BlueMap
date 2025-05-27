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
    
    // Add server info and version display
    const footer = document.querySelector('.footer');
    if (footer) {
        const versionElement = document.createElement('small');
        versionElement.textContent = 'BlueMap Legacy for Minecraft 1.5.2';
        versionElement.style.display = 'block';
        versionElement.style.marginTop = '5px';
        versionElement.style.fontSize = '11px';
        footer.appendChild(versionElement);
    }
    
    // Simple feature to auto-refresh the map list every 30 seconds
    // This would be useful in a real server environment where maps might be added
    function refreshMapsList() {
        console.log('Checking for map updates...');
        // In a real implementation, this would fetch the latest maps from the server
        // For now, we just log that we're checking
    }
    
    // Check for map updates every 30 seconds
    setInterval(refreshMapsList, 30000);
    
    // Detect if we're on a map page and add a back button
    if (window.location.href.includes('/maps/') && !document.querySelector('.back-button')) {
        const mapContainer = document.querySelector('.map-container');
        if (mapContainer) {
            const backButton = document.createElement('a');
            backButton.href = '../../index.html';
            backButton.textContent = '« Back to Map List';
            backButton.className = 'back-button';
            backButton.style.position = 'absolute';
            backButton.style.top = '10px';
            backButton.style.left = '10px';
            backButton.style.zIndex = '1000';
            backButton.style.backgroundColor = 'rgba(0,0,0,0.5)';
            backButton.style.color = 'white';
            backButton.style.padding = '8px 15px';
            backButton.style.borderRadius = '4px';
            backButton.style.textDecoration = 'none';
            
            mapContainer.appendChild(backButton);
        }
    }
}); 