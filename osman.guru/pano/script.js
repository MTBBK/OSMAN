function tepe() {
  var x = document.getElementById("tepetepe");
  if (x.className === "topnav") {
    x.className += " responsive";
  } else {
    x.className = "topnav";
  }
}

document.getElementById('themeSwitcher').addEventListener('click', () => {
    const body = document.body;
    const currentTheme = body.getAttribute('data-color');
    let newTheme, newIcon, newImage;
		
    switch (currentTheme) {
        case 'light':
            newTheme = 'dark';
			newIcon = '/pano/logoOS.png';
			newImage = "url('./pano/logoB.png')";
            break;
        case 'dark':
            newTheme = 'fancy';
			newIcon = '/pano/logoOJ.png';
			newImage = "url('./pano/logoJ.png')";
            break;
        case 'fancy':
            newTheme = 'light';
			newIcon = '/pano/logoOB.png';
			newImage = "url('./pano/logoS.png')";
            break;
        default:
            newTheme = 'dark';
			newIcon = '/pano/logoOS.png';
			newImage = "url('./pano/logoB.png')";
    }

    applyTheme(newTheme, newIcon, newImage);
    localStorage.setItem('theme', newTheme);
    localStorage.setItem('icon', newIcon);
    localStorage.setItem('mainImage', newImage);
});

function applyTheme(theme, icon, image) {
    document.body.setAttribute('data-color', theme);
    const favicon = document.getElementById('favicon');
    const anaLogo = document.getElementById("anaLogo");
  	anaLogo.style.backgroundImage = image;
    favicon.href = icon;
}

window.addEventListener('DOMContentLoaded', () => {
    const savedTheme = localStorage.getItem('theme') || 'dark';
    const savedIcon = localStorage.getItem('icon') || '/pano/logoOS.png';
    const savedImage = localStorage.getItem('mainImage') || '/pano/logoB.png';
    applyTheme(savedTheme, savedIcon, savedImage);
});

