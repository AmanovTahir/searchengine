Sure, here's the README_EN.md file with the ### headers for better formatting on GitHub:

```markdown
### SearchEngine

Search engine with a user-friendly web interface designed to provide the following key functionalities:

* Pre-indexing of websites: The engine crawls all pages of specified websites, as defined in the configuration file, and creates indexes based on lemmas found within the pages.
* Statistics display: Get an overview of the total number of websites, pages, and lemmas. Detailed information is available about the state of each site during or after indexing.
* Keyword search and relevance-based page retrieval: Search for keywords or phrases within indexed websites and retrieve pages based on relevance.

### Overview
#### Dashboard Tab
The dashboard displays detailed statistics for websites in the following statuses:
1. **INDEXING**: Indicates that indexing is in progress.
2. **INDEXED**: Indicates that indexing has been completed without errors.
3. **FAILED**: Indicates that an error occurred during indexing.

#### Management Tab
This section provides controls for managing the processing of websites:
* **START INDEXING**: Initiate indexing for all websites.
* **STOP INDEXING**: Stop ongoing indexing (available after starting).
* **Add/update page**: Add or update individual pages.

#### Search Tab
This is the search page where you can search for specific words or phrases within indexed websites.

### Technology Stack
* Java 17
* Maven
* Spring Boot
* MySQL 8
* Hibernate
* Lombok
* Jsoup
* Slf4j

### Local Project Setup
1. Install MySQL and create a database named *search_engine*.
2. In the *application.yml* configuration file, specify:
    * URLs and site names under *indexing-settings*
    * Database username and password
3. To access the lemma libraries, provide a token by creating a *settings.xml* file:
    * On Windows: C:/Users/<Your_Username>/.m2
    * On Linux: /home/<Your_Username>/.m2
    * On macOS: /Users/<Your_Username>/.m2

If the *settings.xml* file doesn't exist, create it and add the server configuration as described in the previous responses.

4. Access the web interface at http://localhost:8080/ once the application is running.

---

This project was developed as part of a student initiative and showcases skills in web scraping, data processing, and web application development. Feel free to explore the code, contribute, and use it to build your search-related projects. Your feedback is appreciated!

*Note: This README provides a brief overview. For more detailed information, consult the full documentation.*

For additional information or support, contact: [your.email@example.com](mailto:your.email@example.com)
```

You can copy and paste this markdown code into your `README_EN.md` file for a well-formatted version on GitHub.
