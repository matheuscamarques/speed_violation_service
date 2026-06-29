run:
	cd ./SpeedViolationService && mvn spring-boot:run

build:
	cd ./SpeedViolationService && mvn clean package -DskipTests

test:
	cd ./SpeedViolationService && mvn test

coverage:
	cd ./SpeedViolationService && mvn clean test jacoco:report

docker-build:
	docker build -t speed-violation-service .

docker-run:
	docker run -p 8080:8080 speed-violation-service

docker-test:
	docker build --target builder -t speed-violation-service-builder . && docker run --rm speed-violation-service-builder mvn test jacoco:report -B