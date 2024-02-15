# spring-embedded-redis-helper

Spring 용 Embedded Redis 라이브러리

`it.ozimov:embedded-redis` 라이브러리에서 제공하는 Redis Server 는 2버전대로 매우 낮고, Apple Silicon 에서도 작동하지 않는 문제가 있어서 직접 Redis Server를 빌드하여 바이너리 실행 파일을 변경하고, 쉽게 Spring 에서 이용할 수 있도록 하였습니다.

## 바이너리 빌드 대상

* Windows
  * https://github.com/redis-windows/redis-windows 에서 제공하는 exe 파일을 가져왔습니다.
* Unix
  * 주로 CI/CD를 통해 이용된다는 점을 고려하여 Docker OpenJDK Alpine 이미지에서 `apk add --update redis` 실행 후 빌드되는 `/usr/bin/redis-server` 를 가져왔습니다.
* MacOS
  * https://download.redis.io/releases/ 에서 다운로드 후 Intel, Apple Silicon 제품군에서 직접 빌드하여 가져왔습니다.
  * Intel(x86_64), Apple Silicon(aarch64) 둘 다 제공합니다.

## 모듈

* demo - 시연용
* spring-embedded-redis-helper - 설정 관련

# 사용법

해당 라이브러리를 clone 하여 `demo` 모듈에서 직접 시험해볼 수 있습니다.

애플리케이션을 실행한 후, http://localhost/swagger-ui/index.html 으로 접근하여 테스트를 해볼 수 있습니다.

## 설정 추가

사용하길 원하는 프로젝트에서 `@Import` 를 사용하여 `EmbeddedRedisConfiguration` 클래스를 불러옵니다.

프로필을 지정하여 사용할 경우 아래와 같이 사용할 수 있습니다.

```java
@Profile({"default", "test"})
@Configuration(proxyBeanMethods = false)
@Import(EmbeddedRedisConfiguration.class)
public class DemoEmbeddedRedisConfiguration {
}
```