FEATURED_IMAGE: "https://freepnglogo.com/images/all_img/gmail-icon-2013-2020-2edc.png"

POST_TITLE: "OSMAN Builder - Showcase of Features"
POST_DATE: "2026/06/27"
POST_AUTHOR: "Büyük Albert"

POST_TAGS:
- "Showcase"
- "Features"
- "Builder"

PAGE_SUMMARY: "MerhabaKedi Temalı Bir Sayfa"
OTOMATIC_SUMMARY_ENABLE: "False"

TABLE_OF_CONTENT_ENABLE: "True"
TABLE_OF_CONTENT_TITLE: "İçindekiler"

POST_CONTENT:
# Welcome to OSMAN!

The OSMAN Builder is a powerful, yet lightweight static site generator written in Java. It converts your Markdown files into a complete, ready-to-deploy static website.

This post will showcase the features provided by the OSMAN builder engine and its comprehensive Markdown Converter.

## Project Level Features

The `Builder.java` engine performs the entire site generation process, providing many built-in capabilities:

**Custom Configurations**: Centralized `config.osman` handling for simple site settings like Title, Description, Base URL, etc.
**Templates & Themes Support**: Easily switch templates and themes with settings like `TEMPLATE_NAME` and `THEME_NAME`.
**Auto-generated Tag Pages**: Generates dedicated tags pages based on posts' metadata (e.g. `tag_Showcase.html`).
**SEO & OpenGraph Tags**: Automatically handles OG tags (`og:title`, `og:description`, `og:image`) for optimal social sharing.
**Custom Assets Injection**: Any CSS or JS placed in the `Content/Assets` folder is automatically included in the `<head>` of your site.
**RSS & Sitemap generation**: Every build creates `rss.xml` and `sitemap.xml` for feed reading and search engines.
**Robots.txt generation**: Generates a standard `robots.txt` pointing to your sitemap.
**Estimated Reading Time**: Gives visitors a rough estimate of how long a post will take to read.
**Next & Previous Links**: Automatically generates page navigation between your blog posts.
**Analytics Integration**: Simple inclusion of analytics scripts via `ANALYTIC_SCRIPT`.
**Automatic Post Summarization**: Uses the first 20 words for excerpt/summary cards if `OTOMATIC_SUMMARY_ENABLE` is true, otherwise it uses `PAGE_SUMMARY`.
**Author Profile & Social Integration**: Built-in support for author avatars, names, and dynamic social media links via `SOCIAL_LINKS` and `SOCIAL_ICONS`.
**Dynamic Navigation Bars**: Configurable navigation bar links directly from your settings using the `NAV_BAR_LINKS` array.
**Automatic Static Asset Handling**: Any files placed in the `Content/` folder (outside of `Texts/`) are automatically copied directly to the output root.
**Comprehensive Build Logging**: Automatically captures detailed build logs and stack traces into the `ErrorLogs/` directory on every run.
**Configurable Site Index Settings**: Easily customize your landing page with variables like `WELCOME_TITLE`, `INTRO_ABOUT_ME`, `COPYRIGHT_YEAR`, and `FAVICON_URL`.

## Markdown Converter Features

Our custom `MarkdownConverter.java` provides rich support for Markdown syntax. Here are examples of everything you can do!

### Text Formatting
You can format your text heavily:
- **Bold**: **This text is bold.**
- *Italic*: *This text is italicized.*
- ~~Strikethrough~~: ~~This text has a line through it.~~
- Inline Code: You can use `System.out.println("Hello, World!");` in your sentences.

### Blockquotes
Want to quote someone? Easy!
> "Simplicity is the soul of efficiency." - Austin Freeman

### Headings
We support multiple levels of headings (H1 to H3), which also seamlessly integrate into an **Auto-Generated Table of Contents** (if `TABLE_OF_CONTENT_ENABLE` is true)!

### Code Blocks
For developers, we support syntax-highlighted code blocks with an integrated copy button:
```java
public static void main(String[] args) {
    Builder.log("main", "Begin.");
    buildSite();
}
```

### Lists
Unordered Lists:
- Apple
- Banana
- Orange

Task Lists:
- [x] Create OSMAN Engine
- [x] Write Markdown Parser
- [ ] Take over the world

### Tables
Tabular data is easily structured:

| Feature | Support | Version |
| ------- | ------- | ------- |
| Headings | Yes | 1.0 |
| Tables | Yes | 1.2 |
| Checkboxes | Yes | 1.3 |

### Links and Media
You can link to other sites: [Check out GitHub!](https://github.com)

Embed images directly:
![Placeholder Image](https://images.unsplash.com/photo-1542831371-29b0f74f9713?ixlib=rb-4.0.3&auto=format&fit=crop&w=1170&q=80)

Even embed YouTube videos using a shortcode!
[youtube:dQw4w9WgXcQ]

### Footnotes
Need to add a citation or a tiny explanation? You can use footnotes.[^1]

[^1]: This is an example footnote text that appears at the bottom of the content!

---
*End of Showcase*
