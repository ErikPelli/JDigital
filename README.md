![Logo](https://user-images.githubusercontent.com/46297387/212667556-5b899bb4-9abe-42aa-b1b4-691b87d7574c.png)

# JDigital
This project is based on [IpDigital](https://github.com/ErikPelli/IpDigital), a precedent PHP-based school group project,
replacing the core logic with a Java Spring API.

The goal of the whole project was to create a system for the management of non-compliances in an imaginary company,
thus allowing us to keep track of how the problems have been solved in past and solve those in the present through a
simple but effective web interface, leaving the employee enough freedom of choice on how to go to act on a problem that
presented himself.

## Frontend preview
![Dashboard](https://user-images.githubusercontent.com/46297387/212711664-ab500798-0b22-491b-93e3-d67dd62ec362.png)

## Old implementation
![Architecture](https://user-images.githubusercontent.com/46297387/212673244-31875a2a-3f68-4c7d-b557-6b0162f33e17.png)

- Bare metal deployment

### Frontend & Backend
- Bootstrap template
- Inline PHP code inside the template
- Served by NGINX

### API
- Standalone PHP app listening on port `8080`
- Interact directly with MySQL database using raw queries

The backend has been split into two parts:
- a part that exposes some REST API and returns a JSON result, after getting the data from the database
- a part that interfaces with the user, manages the sessions and calls the APIs to get the data to show

The APIs are exclusively for internal use and there is no authentication for their use (even if it should be done),
and allows us to separate the functionalities and speed up the development.

#### No frameworks & tests
When it was necessary to develop the old project, the time available was very limited and there was the constraint that
all the code had to be produced by the team members, without using frameworks (here is the school world...).
Therefore, there was no time to develop automatic tests to verify the correctness of the changes, and each time we hoped
that everything would work.

Over time, developers using the API required new ones, and code had to be adapted to add them.
Since all the code was custom, the final result was just 1000 lines of code, using simple solutions such as a router for the
REST APIs that I wrote from scratch using PHP standard functions, to match the HTTP request to the function to handle it.
However, code maintainability is affected by this and certainly a framework is updated over time, also avoiding security issues.

## Current implementation
![Architecture](https://user-images.githubusercontent.com/46297387/212685381-993ac958-19d7-4587-b9cf-d681389e0533.png)

- Docker deployment
- Use old Frontend & Backend, providing a drop-in replacement for old APIs

### Frontend & Backend
- Exactly the same as the previous one, but with Apache Web Server instead of Nginx

### API
The main part of this project is to provide APIs to the old PHP backend that are fully compatible with those that
are already being used, using the Spring framework with Java.

Covering all the edge cases was not trivial and took weeks of work, exceeding 5000 lines of code if we also count the tests.
Where possible, an attempt was made to replace features implemented alone in the old code with those provided by the framework,
such as auto-generated queries using Spring Data JPA.

In this case, there was no shortage of time and I tried to do a good job, writing all the tests that may be needed
to identify bugs in the future.

## Build & Run
Make sure no other copies of the app are running (`docker ps` and `docker rm -f <ids>`).

Start the application stack:
```
docker compose up
```

Open the web app in your browser:
```
# Check the default APP PORT in .env file
127.0.0.1:4444
# Username and password for default test user
admin@jdigital.com
1234
```
