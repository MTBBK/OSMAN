# OSMAN Base Template Documentation

This document explains the placeholders and design principles used in the `base.html` and `base.css` templates.

## Placeholders Logic

The following placeholders should be detected and replaced by the Java Builder using data from `config.toml`:

### Identity & SEO
- `{{ SITE_TITLE }}`: The main title of the website.
- `{{ SITE_DESCRIPTION }}`: A brief description for SEO meta tags.
- `{{ AUTHOR_NAME }}`: The name of the site creator/author.
- `{{ FAVICON_URL }}`: Path to the site's shortcut icon.

### Layout & Navigation
- `{{ LOGO_AREA }}`: Injected HTML for the site logo (either an <img> tag or plain text).
- `{{ NAV_BAR_LINKS }}`: A collection of <a> tags for the top navigation menu.
- `{{ CONTENT }}`: The primary injection point for page-specific HTML content.

### Style & Customization
- `{{ HEAD_EXTRA }}`: A slot for injecting additional meta tags, scripts, or styles per page.

### Footer & Integration
- `{{ COPYRIGHT_YEAR }}`: The current year for the copyright notice.
- `{{ SOCIAL_LINKS }}`: A collection of <a> tags for social media profile links.

## Design Principles

### Simplicity & Performance
- **Minimalist Aesthetic**: The design avoids heavy effects like glassmorphism to ensure fast load times and a clean look.
- **System Fonts**: Uses Arial/Helvetica to avoid external dependencies and ensure consistent rendering across all devices.

### Responsiveness
- **Flexbox Layout**: Ensures that the Navbar and Footer adapt gracefully to different screen sizes.
- **Mobile Friendly**: On smaller screens, the layout shifts to a vertical stack for better usability.

### Flexibility
- **CSS Variables**: The colors are managed through `:root` variables, allowing for global theme changes by updating a single placeholder.
