# Keycloak Manual Configuration Completion Guide

### Step 1: Create the Client Application

1. **Access Keycloak Admin Console**
   - Go to http://localhost:8180/admin
   - Login with: admin / admin123

2. **Switch to Automotive Realm**
   - Click the hamburger menu (☰) in top left
   - Click on "master" dropdown at the top
   - Select "automotive" realm

3. **Create Client**
   - Click "Clients" in the left navigation
   - Click "Create client" button
   - Fill in the form:
     - **Client type**: OpenID Connect (default)
     - **Client ID**: `automotive-sales-app`
     - **Name**: `Automotive Sales Application`
     - **Description**: `Spring Boot application for automotive sales management`
   - Click "Next"

4. **Configure Client Settings**
   - **Client authentication**: ON (for confidential client)
   - **Authorization**: OFF (not needed for this setup)
   - **Standard flow**: ON (Authorization Code Flow)
   - **Direct access grants**: ON (Resource Owner Password Credentials)
   - **Implicit flow**: OFF (deprecated)
   - **Service accounts roles**: OFF (not needed)
   - Click "Next"

5. **Configure Login Settings**
   - **Root URL**: `http://localhost:8080`
   - **Home URL**: `http://localhost:8080`
   - **Valid redirect URIs**: `http://localhost:8080/*`
   - **Valid post logout redirect URIs**: `http://localhost:8080/*`
   - **Web origins**: `http://localhost:8080`
   - Click "Save"

### Step 2: Get Client Secret

1. **Navigate to Credentials Tab**
   - In the client settings, click the "Credentials" tab
   - Copy the "Client secret" value
   - Update your `application.yml` if needed (currently configured for JWT validation)

### Step 3: Create Realm Roles

1. **Navigate to Realm Roles**
   - Click "Realm roles" in the left navigation
   - Click "Create role" button

2. **Create ADMIN Role**
   - **Role name**: `ADMIN`
   - **Description**: `Administrator with full system access`
   - Click "Save"

3. **Create MANAGER Role**
   - **Role name**: `MANAGER`
   - **Description**: `Manager with elevated privileges`
   - Click "Save"

4. **Create SALESPERSON Role**
   - **Role name**: `SALESPERSON`
   - **Description**: `Sales staff with basic access`
   - Click "Save"

### Step 4: Create Test Users

1. **Navigate to Users**
   - Click "Users" in the left navigation
   - Click "Add user" button

2. **Create Admin User**
   - **Username**: `admin.user`
   - **Email**: `admin@automotive.com`
   - **First name**: `Admin`
   - **Last name**: `User`
   - **Email verified**: ON
   - **Enabled**: ON
   - Click "Create"

3. **Set Admin Password**
   - Go to "Credentials" tab
   - Click "Set password"
   - **Password**: `admin123`
   - **Temporary**: OFF
   - Click "Save"

4. **Assign Admin Role**
   - Go to "Role mapping" tab
   - Click "Assign role"
   - Select "ADMIN" role
   - Click "Assign"

5. **Create Manager User**
   - Repeat steps 2-4 with:
     - **Username**: `manager.user`
     - **Email**: `manager@automotive.com`
     - **First name**: `Manager`
     - **Last name**: `User`
     - **Password**: `manager123`
     - **Role**: `MANAGER`

6. **Create Salesperson User**
   - Repeat steps 2-4 with:
     - **Username**: `sales.user`
     - **Email**: `sales@automotive.com`
     - **First name**: `Sales`
     - **Last name**: `User`
     - **Password**: `sales123`
     - **Role**: `SALESPERSON`

### Step 5: Test the Configuration

1. **Test Token Endpoint**
   ```bash
   curl -X POST "http://localhost:8180/realms/automotive/protocol/openid-connect/token" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "username=admin@automotive.com&password=admin123&grant_type=password&client_id=automotive-sales-app&client_secret=YOUR_CLIENT_SECRET"
   ```

2. **Verify OpenID Configuration**
   ```bash
   curl http://localhost:8180/realms/automotive/.well-known/openid_connect/configuration
   ```

3. **Test JWT Validation**
   - Start your Spring Boot application
   - Make authenticated requests using the JWT tokens

### Step 6: Update Application Configuration (if needed)

If you need to use client credentials instead of JWT validation, update `application.yml`:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: automotive-sales-app
            client-secret: YOUR_CLIENT_SECRET
            scope: openid,profile,email
            authorization-grant-type: authorization_code
            redirect-uri: http://localhost:8080/login/oauth2/code/keycloak
        provider:
          keycloak:
            issuer-uri: http://localhost:8180/realms/automotive
            user-name-attribute: preferred_username
```

## Troubleshooting

### Common Issues

1. **"Protocol not found" Error**
   - This occurs when the client hasn't been created yet
   - Complete Step 1 to create the client application

2. **Invalid Client Error**
   - Verify the client ID matches exactly: `automotive-sales-app`
   - Check that the client is enabled

3. **Invalid Redirect URI**
   - Ensure redirect URIs include `http://localhost:8080/*`
   - Check that web origins include `http://localhost:8080`

4. **Role Mapping Issues**
   - Verify roles are created at the realm level
   - Check that users have the correct role assignments
   - Ensure the JWT contains the expected role claims

### Verification Commands

```bash
# Test realm accessibility
curl http://localhost:8180/realms/automotive

# Test OpenID configuration
curl http://localhost:8180/realms/automotive/.well-known/openid_connect/configuration

# Test token endpoint (after client creation)
curl -X POST "http://localhost:8180/realms/automotive/protocol/openid_connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin.user&password=admin123&grant_type=password&client_id=automotive-sales-app"
```

## Next Steps After Completion

1. **Test Spring Boot Integration**
   - Start the application with `docker-compose up`
   - Test authenticated endpoints
   - Verify role-based access control

2. **Production Considerations**
   - Change default passwords
   - Configure HTTPS
   - Set up proper database persistence
   - Configure email settings for user management
   - Set up proper logging and monitoring

## Summary

Once these steps are completed:
- Keycloak will be fully configured
- OpenID Connect endpoints will be available
- JWT authentication will work with your Spring Boot application
- Role-based access control will be functional
- Test users will be available for development and testing
