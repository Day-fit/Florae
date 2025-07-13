# Florae

**Florae** is a smart plant care application designed to make plant cultivation effortless and insightful. It leverages AI-based photo recognition, real-time sensor data, and a robust backend to help you care for your plants efficiently and scientifically.

> :book: **Full documentation is available at:** [https://deepwiki.com/Day-fit/Florae](https://deepwiki.com/Day-fit/Florae)

---

## Features

- **AI Plant Recognition:** Upload a photo, and Florae will recognize the plant species using advanced algorithms.
- **Plant Requirements Database:** Instantly access detailed requirements (light, water, humidity, temperature) for thousands of plants.
- **Sensor Integration:** Compare plant requirements with real-time data from ESP32-based sensors (temperature, humidity, soil moisture, etc.).
- **Automated Deficiency Detection:** Get tailored recommendations to address environmental deficiencies, such as under-watering or improper lighting.
- **Secure API & User Management:** Robust authentication and JWT token management, including blacklisting and revocation.
- **Caching & Performance:** Utilizes Redis for caching frequent queries (API keys, user details, daily reports) to provide fast responses.
- **Modular Architecture:** Well-structured codebase with separate modules for authentication, plant data, requirements, and reporting.
- **Self-hostable:** Includes code and configuration for self-hosting, with Docker environment support.

---

## Websites

- **Full Documentation:** [https://deepwiki.com/Day-fit/Florae](https://deepwiki.com/Day-fit/Florae)
- **API Endpoint:** [https://florae.dayfit.pl/api](https://florae.dayfit.pl/api)
- **Frontend:** [https://florae.dayfit.pl](https://florae.dayfit.pl)

---

## Project Structure

**Will change soon! Imigration to microservices is comming**
- **Backend:** Java (Spring Boot) application for API, authentication, data storage, and business logic.
- **Frontend:** Place your web application files in the `florae-frontend/` directory.
- **ESP32 Integration:** Code and resources for connecting ESP32-based sensors (see `Floralink` folder for firmware and hardware schematics).
- **Caching Layer:** Redis is used for caching frequent queries (API keys, user details, reports).
- **Database:** JPA repositories for persistent storage of users, plants, API keys, and requirements.

---

## Requirements

- **ESP32 Platform:** For sensor data collection; code and future PCB schematics are in the `Floralink` folder.
- **Docker Environment:** Recommended for hosting both backend and Redis.
- **Java 21+** (Spring Boot), **Redis**, **Git**, and optionally Node.js for frontend development.
- **Frontend:** Can be run as a static site or integrated with your own hosting.

---

## Quick Start

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Day-fit/Florae.git
   ```
2. **Set up Docker (recommended):**
   - Docker Compose files are included for easy backend and Redis setup.
3. **Install dependencies:**
   - Backend: `./mvnw install`
   - Frontend: See `florae-frontend/` for instructions.
4. **Configure environment variables:**
   - See `.env.example` or documentation for required variables (DB credentials, API keys, etc).
5. **Run the application:**
   - Backend: `./mvnw spring-boot:run` or via Docker Compose.
   - Frontend: Serve static files or run locally as documented in `florae-frontend/`.

---

## Contributing

Contributions, issues, and feature requests are welcome! Please see the [full documentation](https://deepwiki.com/Day-fit/Florae) for architectural details and contribution guidelines.

---

## License

This project is licensed under the [BSD-3-Clause](./LICENSE) license.

---

> For more detailed instructions, API docs, and advanced configuration, please visit [https://deepwiki.com/Day-fit/Florae](https://deepwiki.com/Day-fit/Florae).
