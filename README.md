# Transaction Starter Project

## Problem Understanding

This project implements a REST API for managing customer transactions using Java, Spring Boot, Spring Data JPA, and H2.

# The service supports four operations:

Create a transaction
Get a transaction by Transaction ID
Update transaction status
Get all transactions for a Customer ID

## Transaction Fields

Every transaction contains:

Transaction ID – unique identifier for the transaction
Customer ID – identifier of the customer
Amount – transaction amount
Currency  – three-letter currency code
Transaction Type – type of transaction
Transaction Status – current processing status

## Assumptions and Validation Rules

The following validation rules are applied:

1. Transaction ID is required and must not be blank.
2. Transaction ID must be unique.
3. Customer ID is required and must not be blank.
4. Amount is required and must be greater than zero.
5. Currency is required and must be a three-letter uppercase code.
6. Transaction Type is required and must be a supported transaction type.
7. A newly created transaction always starts with `PENDING`.
8. Transaction status is not accepted as part of the create request.
9. Duplicate Transaction IDs are treated as a business conflict and return 409 conflict.
10. Invalid request data returns 400 Bad Request.

## Status Transition Rules

A newly created transaction has the status `PENDING`.
transitions  allowed:

PENDING --> COMPLETED
PENDING --> FAILED
PENDING --> CANCELLED

Once a transaction reaches a final status, it cannot be changed:

COMPLETED --> not allowed
FAILED    --> not allowed
CANCELLED -->not allowed

Transaction not allowed:
PENDING  --> PENDING

An invalid status transition returns `409 Conflict`.

## API Endpoints

### 1. Create Transaction

```http
POST /api/transactions
```
Example request:

```json
{
  "transactionId": "TXN001",
  "customerId": "CUS001",
  "amount": 1000.50,
  "currency": "INR",
  "transactionType": "PAYMENT"
}
```

A successful request returns `201 Created`.

The initial status is automatically set to `PENDING`.

### 2. Get Transaction

```http
GET /api/transactions/{transactionId}
```

Example:

```http
GET /api/transactions/TXN001
```

Returns `200 OK` when the transaction exists.

If the transaction does not exist, the API returns `404 Not Found`.

### 3. Update Transaction Status

```http
PATCH /api/transactions/{transactionId}/status
```

Example request:

```json
{
  "status": "COMPLETED"
}
```

A successful update returns `200 OK`.

Only transactions currently in `PENDING` status can be updated.

### 4. Get Customer Transactions

```http
GET /api/transactions/customer/{customerId}
```

Example:

```http
GET /api/transactions/customer/CUS001
```

Returns all transactions belonging to the specified customer.

If the customer has no transactions, the API returns `200 OK` with an empty array:

```json
[]
```

## Error Handling

The API uses appropriate HTTP status codes:

Situation:                     HTTP Status:

Successful creation             `201 Created` 
Successful retrieval/update          `200 OK` 
Invalid input               `400 Bad Request` 
Transaction not found         `404 Not Found` 
Duplicate Transaction ID        `409 Conflict` 
Invalid status transition      `409 Conflict` 

A global exception handler is used to return clear error messages instead of exposing generic server errors.

## Testing

The project contains meaningful unit tests covering:

* Successful transaction creation
* Duplicate Transaction ID
* Successful transaction retrieval
* Transaction not found
* Successful status update
* Status update for a non-existing transaction
* Invalid status transition
* Retrieving customer transactions
* Customer with no transactions
* PENDING --> PENDING Rejection.


### The complete test suite is run using:

### Windows

mvnw.cmd clean test

### Linux / macOS

./mvnw clean test

## Design Approach

The application separates responsibilities into controller, service, repository, DTO, entity, enum, and exception layers.

* Controller handles HTTP requests and responses.
* Service contains business logic and validation rules.
* Repository handles database operations.
* DTOs represent API request data.
* Entity represents the transaction stored in the database.
* Exceptions and the global exception handler provide consistent error responses.


# Known Limitations
--> H2 is an embedded in-memory database, so data is not intended for long-term persistence.
--> Authentication and authorization are not implemented because they are outside the scope of the assignment.

# Improvements With More Time

- With additional time, I would consider:
- Adding integration tests for the REST endpoints.
- Adding more detailed structured logging.
- Using a persistent production database instead of the in-memory H2 database.

# AI  USAGE DISCLOSURE:
- AI was used for debugging and test-case design during the project.
- The generated suggestions were reviewed and implemented where appropriate.
- The final implementation was tested to verify that it works as expected.


# Test Run Output:
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.930 s -- in com.example.transactionstarter.TransactionStarterApplicationTests
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------

