# Job Ingestion & Source Strategy

## 1. Supported Ingestion Channels
1. **Manual Text Ingestion:** Direct copy-paste of raw Job Descriptions into the UI.
2. **Public URL Parser:** Direct retrieval of public career pages (Greenhouse, Lever, Workday) using clean HTTP fetching and DOM/Readability text extraction.
3. **Email Job Alert Parsing:** Extracting job postings directly from incoming LinkedIn/Indeed digest emails.

## 2. Anti-Bot & Anti-Scraping Compliance
- The application will **never** employ CAPTCHA-bypassing tools, proxy rotations, or aggressive headless browser crawling that violates target platform Terms of Service.
- If a job portal restricts programmatic HTTP scraping, the system provides a clean, 1-click manual paste modal.
