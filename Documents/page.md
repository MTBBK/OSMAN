# OSMAN Writing Page Documentation (page.html)

### Layout Configuration
- `{{ LAYOUT = "base.html" }}`: (Indicator) Tells the Java builder which base template to wrap this page with.

## Core Content Placeholders
- `{{ POST_TITLE }}`: The main heading of the post (H1).
- `{{ POST_CONTENT }}`: The Markdown-rendered body of the writing.
- `{{ POST_DATE }}`: The publication date.
- `{{ POST_AUTHOR }}`: The name of the author for this specific post.

## Reading Experience
- `{{ POST_READ_TIME }}`: Estimated time to read (e.g., "4 min read").
- `{{ POST_UPDATED_DATE }}`: Shows if and when the post was last modified.

## Taxonomy & Discovery
- `{{ POST_TAGS }}`: Injected list of links or text for categorical tags.
- `{{ PREVIOUS_POST }}`: Link to the chronologically preceding post.
- `{{ NEXT_POST }}`: Link to the chronologically following post.

## Visuals & SEO
- `{{ FEATURED_IMAGE_URL }}`: Placeholder for a hero image at the top of the article.
- `{{ POST_EXCERPT }}`: (Internal logic) Should be used by the builder to populate the `<meta name="description">` in the `base.html` head slot.

## Styling Guidelines
The `page.css` file targets the following semantic classes:
- `.post`: The main container for the article.
- `.post-header`: Contains title and metadata.
- `.post-content`: Styles the Markdown output (images, blockquotes, code).
- `.post-footer`: Contains tags and pagination.

---
*Note: The Java Builder handles the replacement of these placeholders based on the metadata found in `Content/` markdown files.*
