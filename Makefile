MAVEN = ./mvnw

run:
	cd ./SpeedViolationService && $(MAVEN) spring-boot:run

build:
	cd ./SpeedViolationService && $(MAVEN) clean package -DskipTests

checkstyle:
	cd ./SpeedViolationService && $(MAVEN) validate

test:
	cd ./SpeedViolationService && $(MAVEN) test

ci: checkstyle test

act:
	act -j build

coverage:
	cd ./SpeedViolationService && $(MAVEN) clean test jacoco:report

verify:
	cd ./SpeedViolationService && $(MAVEN) verify

docker-build:
	docker build -t speed-violation-service .

docker-run:
	docker run -p 8080:8080 speed-violation-service

docker-test:
	docker build --target builder -t speed-violation-service-builder . && docker run --rm speed-violation-service-builder mvn test jacoco:report -B