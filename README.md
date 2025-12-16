# Banking System - Credit Card Management

A comprehensive banking system application implementing the Repository Pattern with support for both PostgreSQL and MongoDB databases.

## Project Structure

```
examine/
├── src/main/java/org/example/
│   ├── model/              # Entity classes (CardUser, Card)
│   ├── repository/         # Repository interfaces
│   │   ├── postgres/       # PostgreSQL implementations
│   │   └── mongo/          # MongoDB implementations
│   ├── database/           # Database connection utilities
│   └── Main.java           # Main application class
├── public/                  # Web frontend
│   ├── index.html
│   ├── styles.css
│   └── app.js
├── server.js               # Node.js REST API server
├── package.json            # Node.js dependencies
├── build.gradle.kts        # Java build configuration
└── README.md
```

## Features

### Java Application
- **Repository Pattern**: Abstract interfaces with PostgreSQL and MongoDB implementations
- **Database Switching**: Switch between databases without modifying business logic
- **Entity Models**: 
  - `CardUser`: name, surname, IIN
  - `Card`: inherits name/surname from CardUser, plus PAN, CVV, expiration date, currency, balance

### Web Application
- **REST API**: Full CRUD operations for CardUsers and Cards
- **Database Switching**: Switch between PostgreSQL and MongoDB via frontend
- **Money Operations**: Transfer, withdraw, and deposit money
- **Modern UI**: Clean, responsive interface with jQuery

## Prerequisites

- **Java 25 (Temurin 25)**: Required for the Java application
- **PostgreSQL**: Database server (user: postgres, no password)
- **MongoDB**: Database server (local or cluster)
- **Node.js**: Version 14+ for the web server
- **npm**: Package manager for Node.js

## Setup Instructions

### 1. PostgreSQL Setup

1. Install PostgreSQL if not already installed
2. Start PostgreSQL service
3. The application will automatically create:
   - Database: `banking_system`
   - Tables: `card_users`, `cards`

**Note**: PostgreSQL is configured to use:
- User: `postgres`
- Password: (empty)
- Database: `banking_system`
- Port: `5432`

### 2. MongoDB Setup

**Option A: Local MongoDB**
1. Install MongoDB locally
2. Start MongoDB service
3. The application will use: `mongodb://localhost:27017`

**Option B: MongoDB Atlas (Cloud)**
1. Create a MongoDB Atlas account
2. Create a cluster
3. Get your connection string
4. Update `server.js` and `MongoConnection.java` with your connection string

**Database and Collection Names:**
- Database: `banking_system`
- Collections: `card_users`, `cards`

### 3. Java Application Setup

1. Build the project:
```bash
./gradlew build
```

2. Run the application:
```bash
./gradlew run
```

Or compile and run manually:
```bash
./gradlew compileJava
java -cp build/classes/java/main:build/libs/* org.example.Main
```

### 4. Node.js Web Application Setup

1. Install dependencies:
```bash
npm install
```

2. Start the server:
```bash
npm start
```

The server will run on `http://localhost:3000`

3. Open your browser and navigate to:
```
http://localhost:3000
```

## API Endpoints

### Database Management
- `POST /api/switch-db` - Switch between PostgreSQL and MongoDB
- `GET /api/db-type` - Get current database type

### Card Users
- `GET /api/card-users` - Get all card users
- `GET /api/card-users/:iin` - Get card user by IIN
- `POST /api/card-users` - Create card user
- `PUT /api/card-users/:iin` - Update card user
- `DELETE /api/card-users/:iin` - Delete card user

### Cards
- `GET /api/cards` - Get all cards
- `GET /api/cards/:id` - Get card by ID
- `POST /api/cards` - Create card
- `PUT /api/cards/:id` - Update card
- `DELETE /api/cards/:id` - Delete card

### Money Operations
- `POST /api/cards/transfer` - Transfer money between cards
- `POST /api/cards/withdraw` - Withdraw money (requires CVV)
- `POST /api/cards/deposit` - Deposit money to card

## Data Model

### CardUser
- `name` (String): First name
- `surname` (String): Last name
- `iin` (String): Individual Identification Number (Primary Key)

### Card
- `cardId` (Integer): Internal card ID (Primary Key in PostgreSQL)
- `pan` (String): 16-digit unique card number (cannot duplicate)
- `cvv` (String): 3-digit security code (can duplicate)
- `dateOfExpire` (String): Expiration date in YY/MM format (auto-generated: current + 1 year)
- `name` (String): Inherited from CardUser
- `surname` (String): Inherited from CardUser
- `currency` (String): Currency type (e.g., KZT, USD)
- `balance` (Double): Card balance (supports decimals)

## Functionality

### Card Management
- Create multiple cards
- Create multiple card users
- Update card and user information
- Delete cards and users

### Money Operations
- **Transfer**: Transfer money by PAN or name/surname
- **Withdraw**: Withdraw money (requires CVV verification)
- **Deposit**: Deposit money by PAN or name/surname

### Display
- Cards list in format: `name surname = pan, cvv`
- Real-time updates when switching databases

## Repository Pattern Implementation

The application demonstrates the Repository Pattern with:
- **Interfaces**: `CardUserRepository`, `CardRepository`
- **PostgreSQL Implementation**: `PostgresCardUserRepository`, `PostgresCardRepository`
- **MongoDB Implementation**: `MongoCardUserRepository`, `MongoCardRepository`

Business logic remains unchanged when switching between database implementations.

## Configuration

### PostgreSQL Connection
Edit `DatabaseConnection.java`:
```java
private static final String POSTGRES_URL = "jdbc:postgresql://localhost:5432/banking_system";
private static final String POSTGRES_USER = "postgres";
private static final String POSTGRES_PASSWORD = "";
```

### MongoDB Connection
Edit `MongoConnection.java` or `server.js`:
```java
// For local MongoDB
private static final String DEFAULT_CONNECTION_STRING = "mongodb://localhost:27017";

// For MongoDB Atlas
private static final String DEFAULT_CONNECTION_STRING = "mongodb+srv://user:password@cluster.mongodb.net/";
```

## Troubleshooting

### MongoDB Dependency Warnings in IDE
If you see import errors for MongoDB classes in your IDE (e.g., "MongoClient cannot be resolved"):
- **This is normal** - these are IDE sync issues, not code errors
- The dependencies are correctly configured in `build.gradle.kts`
- Run `./gradlew build` to download dependencies
- Refresh your IDE's Gradle project/sync
- The code will compile and run correctly despite IDE warnings

### PostgreSQL Connection Issues
- Ensure PostgreSQL is running: `pg_isready`
- Check if database exists: `psql -U postgres -l`
- Verify user permissions

### MongoDB Connection Issues
- For local MongoDB: Ensure service is running
- For Atlas: Verify connection string and network access settings
- Check firewall rules

### Java Compilation Issues
- Ensure Java 25 (Temurin 25) is installed: `java -version`
- Verify Gradle wrapper: `./gradlew --version`
- If build fails, try: `./gradlew clean build`

### Node.js Server Issues
- Check if port 3000 is available
- Verify all dependencies are installed: `npm list`
- Check server logs for detailed error messages

## Development Notes

- The application uses Java 25 (Temurin 25) as specified
- PostgreSQL user is `postgres` with no password (as per requirements)
- MongoDB connection string can be configured for local or cloud instances
- Frontend uses simple, straightforward format for endpoints
- All database operations support both PostgreSQL and MongoDB seamlessly

## License

This project is created for educational purposes as part of the Information and Communication Technologies course.

