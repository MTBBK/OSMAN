# OSMAN Index Template Documentation (index.html)

This document explains the placeholders and design principles used in the `index.html` and `index.css` templates.

## Layout Configuration
- `{{ LAYOUT = "base.html" }}`: Tells the builder to use the base shell for the homepage.

## Placeholders Logic

The following placeholders should be detected and replaced by the Java Builder using data from `config.toml`:

### Personality Placeholders
- `{{ WELCOME_TITLE }}`: Main greeting title.
- `{{ INTRO_ABOUT_ME }}`: Short intro text about the author.
- `{{ AUTHOR_AVATAR_URL }}`: Placeholder for a profile image tag.

### Discovery & Socials
- `{{ SOCIAL_ICONS }}`: Slot for RSS and social media link icons.
- `{{ TAG_CLOUD }}`: A collection of tags used globaly to filter posts.
- `{{ TOTAL_POSTS_COUNT }}`: Total number of writings published.

### Post List Feature
- `{{ POST_LIST }}`: The builder will inject a series of "post-card" blocks here.

## Post Card Structure
When the builder generates the `POST_LIST`, each card should use these classes:
- `.post-card`: Container for the entry.
- `.post-card-title`: Link to the article.
- `.post-card-categories`: Tags specific to that article.
- `.post-card-excerpt`: Short summary/preview.
- `.post-card-meta`: Date, Read time, etc.
