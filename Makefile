ROOT_DIR=..
DOCKER_DIR=./docker

backend:
	./mvnw clean package -DskipTests && docker compose -f compose.yaml up -d --no-deps --build backend
database:
	docker compose -f compose.yaml up -d --no-deps --build database

deploy-backend: backend
deploy-database: database
deploy: database backend
