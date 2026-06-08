# FlashGo

FlashGo is an instant delivery and local commerce fulfillment platform backend built with Spring Boot.

It is designed around four core roles:

- User: browse shops, place orders, pay, and track delivery
- Merchant: manage shops and products, accept or reject orders
- Rider: grab orders, pick up goods, and complete delivery
- Platform: monitor users, merchants, riders, orders, and settlement data

## Core Workflow

The V1 order lifecycle follows a single main path:

1. User places an order
2. User pays the order
3. Merchant accepts the order
4. Rider grabs the order
5. Rider picks up the goods
6. Rider completes delivery
7. Platform settles the order

## Current Scope

This repository currently focuses on the V1 backend foundation:

- Spring Boot monolith project structure
- Core order domain design
- Flyway database migrations
- Spring Security + JWT authentication
- Redis-based login state and permission pool design
- Initial auth APIs:
  - `POST /api/v1/auth/login`
  - `GET /api/v1/auth/me`
  - `POST /api/v1/auth/logout`

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Security
- MyBatis-Plus
- MySQL
- Flyway
- Redis
- RabbitMQ

## Repository Notes

- Sensitive runtime configuration files are excluded from Git
- Most planning and business design documents are not tracked by default
- The full original project description is preserved in:
  - [docs/project-overview.md](/Users/waylandite/Work/FlashGo/docs/project-overview.md)

## Next Steps

- Complete auth initialization data
- Implement V1 order APIs
- Add merchant, rider, and platform management flows
- Improve role-based permission enforcement
