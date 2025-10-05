# HW17: Docker + PostgreSQL (H2)

## H2 (��� ��-����������)
1) ������:  
   $ docker build -t hw17-docker .
2) ������:  
   $ docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=h2 hw17-docker
3) http://localhost:8080

## PostgreSQL (Docker Compose)
1) ������:  
   $ docker compose up --build
2) http://localhost:8080  
   ��: localhost:5432 (user: postgres / pass: postgres / db: librarydb)
3) ��������� � ��������� ������:  
   $ docker compose down -v

## ��������� Kubernetes
$ eval $(minikube docker-env)  
$ docker build -t hw17-docker:latest .  
$ kubectl apply -f k8s/  
$ minikube service library-service
