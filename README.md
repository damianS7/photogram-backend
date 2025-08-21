# 📷 Photogram

Just a simplified Instagram clone using Spring Boot and Vue.js

---

## Technologies

- **Java 21**
- **Spring Boot**
- **JWT (JSON Web Tokens)** for authentication
- **Java Mail Sender**
- **JUnit + Maven** for testing
- **Docker** to deploy
- **Vue 3** for frontend

---

## Configuration

Before starting the application, make sure to configure your environment

### Environment Variables

Config `application.yml` and set following variables

- `app.frontend.host` where your frontend will run such as localhost, photogram.local ...
- `app.frontend.port` port where frontend will run.

### /etc/hosts file

Add following line to your `/etc/hosts` file in case you want to use a custom domain instead localhost.

`192.168.XXX.XXX	photogram.local`

### .ENV file

Create a `.env` file in the root directory of the project.

- `DB_USER` and `DB_PASS` for your PostgreSQL database
- `JWT_SECRET_KEY` for your JWT secret key

```
DB_USER=admin
DB_PASS=123456
JWT_SECRET_KEY=1234567890
```

## 📦 Install and deployment

Make sure you have [Docker](https://www.docker.com/) installed.

Compile and run the application using Maven:

```
./mvnw clean package -DskipTests
```

Run the Docker containers:

```bash
docker-compose up --build
```

Or you can just deploy it with Makefile using the following command:

```bash
make deploy
```

This will build the database, mail and application containers.

## 🚀 Using the application

After containers are running, you can access the application at

- Backend: `http://localhost:8090` or `http://photogram.local:8090`
- Database: `http://localhost:5430` or `http://photogram.local:5430`
- Mailpit: `http://localhost:8025`
