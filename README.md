# Customer Onboarding - KYC Admin Verification System

A secure microservice-based customer onboarding system with JWT-authenticated admin verification workflow.

## Features

### Core Functionality
- **Customer Registration & Authentication** - Secure user registration and login via authservice
- **KYC Document Management** - Upload, view, and delete KYC documents
- **Admin Verification Workflow** - JWT-secured admin-only KYC document verification
- **Audit Logging** - Complete audit trail for all user actions

### Security Features
- **JWT-based Authentication** - Secure token-based authentication between services
- **Role-based Authorization** - Admin-only access to verification endpoints
- **Custom Security Filters** - AdminJwtFilter for fine-grained access control
- **Oracle Database Integration** - Secure data persistence with proper schema design

## Architecture

### Microservices
1. **AuthService** (Port 8080) - User authentication and JWT token generation
2. **KYC Service** (Port 8090) - KYC document management and admin verification

### Database Schema
- **Users** - User credentials and roles
- **Customers** - Customer profile information
- **KYC Documents** - Document storage and metadata
- **KYC Verification** - Admin verification records with audit trail

## API Endpoints

### AuthService (Port 8080)
```
POST /api/auth/register - User registration
POST /api/auth/login    - User login (returns JWT)
GET  /api/auth/users    - Get all customers
```

### KYC Service (Port 8090)
```
POST   /api/kyc/upload/{customerId}           - Upload KYC document
GET    /api/kyc/documents/{customerId}        - View customer documents
DELETE /api/kyc/documents/{documentId}        - Delete document
PUT    /api/kyc/verify/{customerId}/{docId}   - Admin verification (JWT required)
```

## Technology Stack

- **Backend**: Spring Boot, Spring Security
- **Database**: Oracle Database
- **Authentication**: JWT (JSON Web Tokens)
- **Build Tool**: Maven
- **Java Version**: 17+

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Oracle Database
- Maven 3.6+

### Configuration

1. **Database Setup**
   - Create Oracle database schema
   - Update connection details in `application.properties`

2. **JWT Configuration**
   - Both services use the same JWT secret for token validation
   - Configure `jwt.secret` and `jwt.expiration` in application.properties

3. **Run Services**
   ```bash
   # Start AuthService (Port 8080)
   mvn spring-boot:run -Dspring-boot.run.main-class=com.onboarding.authservice.AuthserviceApplication
   
   # Start KYC Service (Port 8090)
   mvn spring-boot:run -Dspring-boot.run.main-class=com.onboarding.customer_onboarding.CustomerOnboardingApplication
   ```

## Admin Verification Workflow

1. **Admin Login** - Get JWT token from AuthService with ADMIN role
2. **JWT Validation** - AdminJwtFilter validates token and role
3. **Document Verification** - Admin updates KYC status and remarks
4. **Audit Trail** - Verification record stored with admin details

### Example Admin Verification Request
```http
PUT /api/kyc/verify/2/3
Authorization: Bearer <admin_jwt_token>
Content-Type: application/x-www-form-urlencoded

status=VERIFIED&remarks=Document verified successfully
```

## Security Implementation

### JWT Token Structure
```json
{
  "sub": "admin_username",
  "role": "ADMIN",
  "userId": 123,
  "iat": 1754046871,
  "exp": 1754133271
}
```

### Database Security
- No foreign key constraints on admin verification to avoid Oracle type conflicts
- Separate `admin_username` (VARCHAR2) and `verified_by` (NUMBER) fields
- Proper indexing and constraints for data integrity

## Development Notes

### Key Design Decisions
- **Microservice Architecture** - Separate auth and KYC services for scalability
- **Custom JWT Filter** - AdminJwtFilter for role-based endpoint protection
- **Oracle Schema Alignment** - Careful field mapping to avoid datatype conflicts
- **Audit Trail** - Complete tracking of admin verification actions

### Testing
- Use Postman or similar tool for API testing
- Ensure JWT tokens include required claims (role, userId)
- Test both success and failure scenarios for verification endpoints

## Contributors
- Sai Harshith - Lead Developer

## License
This project is part of a coding contest submission.
