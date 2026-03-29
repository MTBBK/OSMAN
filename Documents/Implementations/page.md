# OSMAN Page Template Documentation (page.html)

This document explains the placeholders and design principles used in the `page.html` template.

## Placeholders Logic

The following placeholders should be detected and replaced by the Java Builder using data from `config.toml`:

### Core Content Placeholders
- `{{ POST_TITLE }}`: The main heading of the post (H1).
- `{{ POST_CONTENT }}`: The Markdown-rendered body of the writing.
- `{{ POST_DATE }}`: The publication date.
- `{{ POST_AUTHOR }}`: The name of the author for this specific post.

### Reading Experience
- `{{ POST_READ_TIME }}`: Estimated time to read (e.g., "4 min read").

### Taxonomy & Discovery
- `{{ POST_TAGS }}`: Injected list of links or text for categorical tags.
- `{{ PREVIOUS_POST }}`: Link to the chronologically preceding post.
- `{{ NEXT_POST }}`: Link to the chronologically following post.

### Visuals & SEO
- `{{ FEATURED_IMAGE_URL }}`: Placeholder for a hero image at the top of the article.

---
*Note: The Java Builder handles the replacement of these placeholders based on the metadata found in `Content` folders markdown files.*
