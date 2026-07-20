# Microsoft Exchange Email Service

A secure, high-performance, and environment-agnostic microservice designed for dispatching automated correspondence through the Microsoft Graph API using application-level permissions.

## Architecture & Tech Stack

- **Framework:** Spring Boot (Reactive / WebFlux stack)
- **Authentication:** Azure AD Client Credentials Flow (`Mail.Send` application permission)
- **Security:** Jasypt encryption for sensitive configuration properties
- **Data Transport:** Strict DTO-driven contracts utilizing Java records and Enum mappings

## Security & Secrets Management (Jasypt)

We use Jasypt to secure sensitive properties (such as keystore passwords and Azure credentials) in configuration files.

### 1. Create a Custom Certificate

It is recommended to generate your own local Java keystore for secure transport layers. Run the following command inside your `certs` directory:

```bash
keytool -genkeypair -alias send-email -keyalg RSA -keysize 2048 -storetype JKS -keystore keystore-local.jks -validity 365
```

### 2. Encrypt Values Using Jasypt

To encrypt new sensitive configuration strings (like your keystore password), execute the Jasypt Maven plugin:

```bash
mvn com.github.ulisesbocchio:jasypt-maven-plugin:4.0.4:encrypt-value \
  -Djasypt.encryptor.password=your-master-key \
  -Djasypt.plugin.value=your-new-keystore-password
```

### Default Development Fallbacks

- **Default Keystore Password:** `ENC(Ciiyk7d9ZSOsrBuIlcocNCNCHIBB72NisyuerGVnfQdPhdW1rkydNuA3CzRac3iWhWPurG4mNq/B5v34BAgUBQ==)`
- **Default Master Key:** `change-it` (Override this in production environments)

## Running Locally

To run the application locally, inject your master key and Azure App Registration credentials as Virtual Machine (VM) arguments or environment variables:

```bash
-Djasypt.encryptor.password=YOUR_MASTER_KEY
-DAZURE_CLIENT_ID=YOUR_CLIENT_ID
-DAZURE_CLIENT_SECRET=YOUR_CLIENT_SECRET
-DAZURE_TENANT_ID=YOUR_TENANT_ID
-Dapp.email.sender-upn=YOUR_EMAIL
```

## API Usage & Endpoints

### Send Email

- **Endpoint:** `POST /api/v1/email/send`
- **Content-Type:** `application/json`

#### Request Body Schema

```json
{
  "to": [
    "recipient@example.com"
  ],
  "cc": [
    "cc-recipient@example.com"
  ],
  "subject": "Automated Notification",
  "body": "This is a plain text or HTML message body.",
  "messageType": "Text",
  "priority": "NORMAL"
}
```

#### Field Specifications

| Field | Type | Required | Description |
|---|---|---|---|
| `to` | List | Yes | Target recipient email addresses. |
| `cc` | List | No | Carbon copy recipient addresses. |
| `subject` | String | Yes | The subject line of the email. |
| `body` | String | No | The core content of the message. |
| `messageType` | Enum (`Text`, `HTML`) | Yes | Specifies how Microsoft Graph renders the content. |
| `priority` | Enum (`NORMAL`, `HIGH`, `URGENT`) | No | Message dispatch priority level. |

## Microsoft Graph & Azure Reference

- [Microsoft Graph API Overview](https://learn.microsoft.com/en-us/graph/overview)
- [Application Endpoint Reference: Microsoft Graph Application Get](https://learn.microsoft.com/en-us/graph/api/application-get)
- [Permissions & Scopes Reference: Graph Permissions Reference](https://learn.microsoft.com/en-us/graph/permissions-reference)
- [Graph Metadata Repository: GitHub - msgraph-metadata](https://github.com/microsoftgraph/msgraph-metadata)