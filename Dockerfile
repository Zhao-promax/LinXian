# 通用多阶段构建：一个 Dockerfile 构建任意一个可运行服务镜像。
# 用法（在仓库根目录执行）：
#   docker build --build-arg SERVICE_MODULE=linxian-order-service -t linxian-order-service .
#
# 说明：linxian-common / linxian-api 是共享库模块，由 -am 一并编译；不单独打镜像。
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY linxian-common ./linxian-common
COPY linxian-api ./linxian-api
ARG SERVICE_MODULE
COPY ${SERVICE_MODULE} ./${SERVICE_MODULE}
RUN mvn -pl ${SERVICE_MODULE} -am -DskipTests package -q

FROM eclipse-temurin:17-jre
ARG SERVICE_MODULE
WORKDIR /app
COPY --from=build /app/${SERVICE_MODULE}/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
