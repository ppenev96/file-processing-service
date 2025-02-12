# File Processing Application

## To start the application must have Docker installed

## In the main folder there is file: docker-compose.yml, which could be started with docker-compose up command from Docker CLI

* In the Docker Compose file is Postgres DB, Redis, Kafka and File Processing Application. 
* The server (the created spring-boot application) has logic to download Maven and Java, and automatically start and connect to Posgres DB.

## If the application is started successfully, this curl could be used in Postman to call the API and I will add exmple.csv in the project main folder

* **Curl:**
  curl --location 'http://localhost:8080/files/upload' \
  --form 'file=@"/C:CSV.csv"'

## If there is needed Mock Server from Postman could be added and this variable changed in application.yml: 
external:
service:
base-url: https://202b27fd-cb6e-45fe-ba97-5549424307fe.mock.pstmn.io
receive-users-url: /users/receive
