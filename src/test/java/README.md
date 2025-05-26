# Test Coverage

This document provides an overview of the test coverage for the Javalin API Basic project.

## Test Files

### 1. RoutesTest.java
Tests the HTTP endpoints and their responses:
- `testGetUserById`: Tests the GET /users/{id} endpoint (404 response for non-existent user)
- `testGetAllUsers`: Tests the GET /users endpoint (200 response)
- `testAddUser`: Tests the POST /users endpoint (201 response)
- `testUpdateUser`: Tests the PUT /users/{id} endpoint (204 response)
- `testDeleteUser`: Tests the DELETE /users/{id} endpoint (204 response)
- `testGetUsersPaginated`: Tests the GET /users/paginated endpoint (200 response)

### 2. UserControllerTest.java
Tests the UserController class methods:
- `testGetUserById_UserExists`: Tests retrieving an existing user
- `testGetUserById_UserNotFound`: Tests handling of non-existent user (404 response)
- `testGetAllUsers`: Tests retrieving all users
- `testAddUser_Success`: Tests adding a new user successfully
- `testUpdateUser`: Tests updating an existing user
- `testDeleteUser`: Tests deleting a user
- `testGetUsersPaginated_WithParameters`: Tests pagination with custom parameters
- `testGetUsersPaginated_DefaultParameters`: Tests pagination with default parameters

### 3. UserServiceTest.java
Tests the UserService class methods:
- `testGetUserById_UserExists`: Tests retrieving an existing user
- `testGetUserById_UserNotFound`: Tests handling of non-existent user (exception thrown)
- `testGetAllUsers`: Tests retrieving all users
- `testAddUser`: Tests adding a new user
- `testUpdateUser`: Tests updating an existing user
- `testDeleteUser`: Tests deleting a user
- `testGetUsersPaginated`: Tests pagination functionality

### 4. RouteHandlersTest.java
Tests the RouteHandlers class methods:
- `testHandleRoot`: Tests the root endpoint handler
- `testHandleHello`: Tests the hello endpoint handler with path parameter
- `testHandleQueryParams`: Tests handling of query parameters
- `testHandleQueryParams_NullParams`: Tests handling of null query parameters

### 5. ExceptionHandlerTest.java
Tests the ExceptionHandler class:
- `testRegisterExceptionHandlers`: Tests registration of exception handlers
- `testUserNotFoundExceptionHandler`: Tests handling of UserNotFoundException
- `testHttpResponseExceptionHandler`: Tests handling of HttpResponseException
- `testGenericExceptionHandler`: Tests handling of generic exceptions
- `testErrorResponseClass`: Tests the ErrorResponse class

## Coverage Summary

The tests cover:
- Controller layer: All methods with success and error scenarios
- Service layer: All methods with success and error scenarios
- Route handlers: All handlers with various input scenarios
- Exception handling: All exception types and their handling
- Data validation: Input validation for various endpoints
- Edge cases: Null parameters, non-existent resources, etc.

## Running Tests

To run all tests:
```
mvn test
```

To run a specific test class:
```
mvn test -Dtest=ClassName
```

To run a specific test method:
```
mvn test -Dtest=ClassName#methodName
```