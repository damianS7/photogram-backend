ROOT_DIR=..
DOCKER_DIR=./docker

backend:
	./mvnw clean package -DskipTests && docker compose -f compose.yaml up -d --no-deps --build backend
database:
	docker compose -f compose.yaml up -d --no-deps --build database
mailpit:
	docker compose -f compose.yaml up -d --no-deps --build mailpit
deploy:
	./mvnw clean package -DskipTests && docker compose -f compose.yaml up -d --build

deploy-mail: mailpit
deploy-backend: backend
deploy-database: database
