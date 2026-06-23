# ===== 1단계: 빌드 =====
FROM gradle:8.10-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

# ===== 2단계: 실행 (가벼운 JRE 이미지) =====
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/app.jar app.jar
EXPOSE 8080

# ★ 메모리 함정 방지 ★
# JVM이 "컨테이너에 할당된 메모리"를 인식하도록 강제.
# 이 옵션이 없으면 JVM이 노드 전체 메모리 기준으로 힙을 잡아 OOMKilled 발생.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
