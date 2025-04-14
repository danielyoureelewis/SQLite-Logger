# SQLite-Logger

A Burp Suite extension that logs all HTTP requests and responses from all Burp tools to a SQLite database for later analysis.

## Overview

SQLite-Logger captures HTTP traffic from all Burp Suite tools including Proxy, Repeater, Intruder, Scanner, and more. The extension stores complete request and response data in a local SQLite database, making it easy to:

- Maintain a comprehensive history of all your testing activities
- Export data for external analysis or reporting
- Search and filter past requests outside of Burp Suite
- Create audit trails for penetration tests

## Features

- Captures requests from all Burp tools
- Logs full request and response content
- Stores metadata including host, path, method, and status code
- Automatically creates database in user home directory
- Minimal performance impact on Burp Suite

## Installation

### Prerequisites

- Burp Suite Professional or Community
- Java Runtime Environment (JRE) 11 or higher
- SQLite JDBC driver (included in the extension JAR)

### Steps

1. Download the latest `SQLite-Logger.jar` from the releases page
2. In Burp Suite, go to the Extensions tab
3. Click "Add" in the Extensions section
4. Set Extension Type to "Java"
5. Select the downloaded JAR file
6. Click "Next" to load the extension

## Usage

The extension works automatically after installation with no configuration required. All HTTP traffic handled by Burp Suite will be logged to a SQLite database located at:

```
~/burp_requests.db
```

### Database Schema

The extension creates a `requests` table with the following structure:

| Column    | Type                | Description                                  |
|-----------|---------------------|----------------------------------------------|
| tool      | TEXT                | Name of the Burp tool that sent the request  |
| status    | INT                 | HTTP status code of the response             |
| host      | TEXT                | Target hostname                              |
| method    | TEXT                | HTTP method (GET, POST, etc.)                |
| path      | TEXT                | Request path                                 |
| req       | TEXT                | Full HTTP request                            |
| res       | TEXT                | Full HTTP response                           |
| timestamp | DATETIME            | When the request was logged                  |

### Accessing the Data

You can use any SQLite client to query the database, such as:

- [DB Browser for SQLite](https://sqlitebrowser.org/) (cross-platform GUI)
- [SQLite command line tools](https://sqlite.org/cli.html)
- Programming languages with SQLite support (Python, Java, etc.)

Example query to get all requests to a specific host:

```sql
SELECT * FROM requests WHERE host = 'example.com' ORDER BY timestamp DESC;
```

## Development

### Building from Source

1. Clone the repository
2. Ensure you have JDK 11+ installed
3. Build using Gradle:

```
./gradlew build
```

The JAR file will be created in the `build/libs` directory.

### Project Structure

- `BurpExtender.java` - Main extension class implementing the Burp Montoya API

## Troubleshooting

### Common Issues

1. **Extension doesn't load**: Ensure you have JRE 11+ installed and that the SQLite JDBC driver is properly bundled in the JAR.

2. **Missing requests**: The extension should capture all requests from all tools. If requests are missing, check Burp's extension output for any error messages.

3. **Database errors**: Ensure you have write permissions to your home directory where the database is created.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
