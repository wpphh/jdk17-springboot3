# JDK 17 + Spring Boot 3 学习项目

> 从 JDK 8 + Spring Boot 2 迁移到 JDK 17 + Spring Boot 3 的完整学习指南

## 快速启动

```bash
# 编译
mvn clean package

# 运行
mvn spring-boot:run

# 访问
# 应用:     http://localhost:8080
# Swagger:  http://localhost:8080/swagger-ui.html
# H2 控制台: http://localhost:8080/h2-console
#   JDBC URL: jdbc:h2:mem:testdb
#   User: sa  Password: (空)
```

## 推荐阅读顺序

| 顺序 | 文件 | 学什么 |
|------|------|--------|
| 1 | `model/types/AccountStatus.java` | **Sealed Interface + Record** — 密封接口+记录类 |
| 2 | `model/types/ApiResponse.java` | **代数数据类型** — sealed + record 联合使用 |
| 3 | `util/PatternMatchingDemo.java` | **JDK17 语法集中演示** — switch 表达式、pattern matching、text block |
| 4 | `model/BaseEntity.java` | **javax → jakarta** — 包名迁移 |
| 5 | `dto/*.java` | **Record 作为 DTO** — 替代 Lombok @Data |
| 6 | `service/impl/UserServiceImpl.java` | **构造器注入** — 替代 @Autowired 字段注入 |
| 7 | `config/SecurityConfig.java` | **SecurityFilterChain** — 替代 WebSecurityConfigurerAdapter |
| 8 | `exception/GlobalExceptionHandler.java` | **ProblemDetail** — RFC 7807 标准错误响应 |
| 9 | `http/ExternalApiClient.java` | **HTTP Interface Client** — 声明式 HTTP 客户端 |
| 10 | `autoconfig/AuditAutoConfiguration.java` | **@AutoConfiguration** — 自动配置新方式 |
| 11 | `test/` | **测试层** — @DataJpaTest, @WebMvcTest, sealed type 断言 |

---

## JDK 8 → JDK 17 核心变化

### 1. Record (JDK 16 正式引入)

```java
// JDK8: 冗长的 POJO / Lombok
public class UserDTO {
    private String name;
    private String email;
    // + getter, setter, equals, hashCode, toString ...
}

// JDK17: 一行搞定
public record UserDTO(String name, String email) {}
// 自动提供: 构造器, name(), email(), equals(), hashCode(), toString()
```

**本项目应用**: `dto/` 下所有请求/响应类都是 record

### 2. Sealed Interface (JDK 17)

```java
// JDK8: 普通 interface, 任何人都能实现
public interface Result<T> { ... }

// JDK17: sealed interface, 只允许指定的类实现
public sealed interface Result<T> permits Success, Failure {
    record Success<T>(T data) implements Result<T> {}
    record Failure(String error) implements Result<T> {}
}
```

**本项目应用**: `model/types/ApiResponse.java`, `model/types/AccountStatus.java`

### 3. Switch 表达式 (JDK 14)

```java
// JDK8: 穿透 switch, 需要 break
String desc;
switch (status) {
    case ACTIVE: desc = "活跃"; break;
    case DELETED: desc = "已删除"; break;
    default: desc = "未知";
}

// JDK17: switch 表达式, 无 break, 直接返回值 (适用于 enum)
String desc = switch (status) {
    case ACTIVE -> "活跃";
    case DELETED -> "已删除";
};
```

**本项目应用**: `UserResponse.fromEntity()` (enum 上的 switch 表达式)

**注意**: switch 表达式在 **enum** 上 JDK17 完全支持. 但在 **sealed type** 上做 pattern matching switch 需要 **JDK21** (JEP 441). 本项目在 sealed type 上使用 `instanceof` pattern matching 替代.

### 4. instanceof Pattern Matching (JDK 16)

```java
// JDK8: 先判断再强转
if (obj instanceof User) {
    User user = (User) obj;
    return user.getName();
}

// JDK17: 一步到位
if (obj instanceof User user) {
    return user.getName();  // 直接使用 user, 无需强转
}
```

**本项目应用**: `PatternMatchingDemo.extractUserName()`, `AccountStatus.describe()`

### 5. Sealed Type 的处理方式

```java
// JDK17: 使用 instanceof pattern matching 处理 sealed type
if (status instanceof Active a) {
    return "活跃: " + a.since();
} else if (status instanceof Suspended s) {
    return "暂停: " + s.reason();
}

// JDK21: 可以直接用 switch 表达式匹配 sealed type
return switch (status) {
    case Active a   -> "活跃: " + a.since();
    case Suspended s -> "暂停: " + s.reason();
    case Deleted d   -> "已删除";
};
```

### 5. Text Block 文本块 (JDK 15)

```java
// JDK8: 字符串拼接
String sql = "SELECT u FROM User u " +
             "WHERE u.username LIKE :name " +
             "ORDER BY u.createdAt";

// JDK17: 三引号多行字符串
String sql = """
        SELECT u FROM User u
        WHERE u.username LIKE :name
        ORDER BY u.createdAt
        """;
```

**本项目应用**: `UserRepository.searchUsers()` 的 `@Query`, `PatternMatchingDemo.buildUserQuery()`

### 6. Stream.toList() (JDK 16)

```java
// JDK8
List<String> list = stream.collect(Collectors.toList());

// JDK17
List<String> list = stream.toList();  // 返回不可变 List
```

---

## Spring Boot 2 → Spring Boot 3 核心变化

### 1. javax → jakarta 命名空间 (最重要的变化!)

```java
// SB2: import javax.persistence.*;
// SB3: import jakarta.persistence.*;

// SB2: import javax.servlet.*;
// SB3: import jakarta.servlet.*;

// SB2: import javax.validation.*;
// SB3: import jakarta.validation.*;
```

**原因**: Java EE 移交给 Eclipse 基金会后更名为 Jakarta EE, 包名全部更改.
**影响**: 所有 JPA、Servlet、Validation 的 import 都要改.

### 2. WebSecurityConfigurerAdapter 已删除!

```java
// SB2: 继承适配器类
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/api/public/**").permitAll()
            .and()
        .csrf().disable();
    }
}

// SB3: 使用 SecurityFilterChain Bean + Lambda DSL
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated())
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
```

**关键方法名变化**:
- `authorizeRequests()` → `authorizeHttpRequests()`
- `antMatchers()` → `requestMatchers()`
- `.and()` 链式调用 → Lambda DSL

### 3. ProblemDetail — RFC 7807 标准错误响应 (Spring 6)

```java
// SB2: 自定义错误类
public class ErrorResponse {
    private int code;
    private String message;
    // ...
}

// SB3: ProblemDetail (内置, 符合 RFC 7807)
@ExceptionHandler(ResourceNotFoundException.class)
public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
}
```

**返回格式**:
```json
{
  "type": "about:blank",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "用户不存在: ID=123"
}
```

### 4. HTTP Interface Client (SB 3.1+)

```java
// SB2: RestTemplate (命令式)
RestTemplate restTemplate = new RestTemplate();
User user = restTemplate.getForObject("/api/users/1", User.class);

// SB3: 声明式接口
@HttpExchange("/api")
public interface UserClient {
    @GetExchange("/users/{id}")
    User getUser(@PathVariable Long id);
}
// Spring 自动生成实现, 通过 HttpServiceProxyFactory 注册为 Bean
```

### 5. RestClient (SB 3.2 新增)

```java
// SB2: RestTemplate
RestTemplate restTemplate = new RestTemplate();
String result = restTemplate.getForObject(url, String.class);

// SB3: RestClient (更现代的 API)
RestClient client = RestClient.builder().baseUrl("http://api.example.com").build();
String result = client.get().uri("/users/1").retrieve().body(String.class);
```

### 6. AutoConfiguration.imports 替代 spring.factories

```
# SB2: META-INF/spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.example.MyAutoConfiguration

# SB3: META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
com.example.MyAutoConfiguration
```

### 7. 构造器注入替代 @Autowired 字段注入

```java
// SB2 常见: 字段注入
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
}

// SB3 推荐: 构造器注入 (单构造器无需 @Autowired)
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

### 8. 默认值变化

| 配置项 | SB2 默认值 | SB3 默认值 |
|--------|-----------|-----------|
| `spring.sql.init.mode` | `always` | `NEVER` |
| Java 版本 | 8 | 17 |
| Jakarta EE 版本 | Java EE 8 (javax) | Jakarta EE 9+ (jakarta) |
| Spring Security | 5.x | 6.x |

---

## 项目结构说明

```
src/main/java/com/example/demo/
├── model/                  # JPA 实体 (用 class, 不能用 record)
│   ├── BaseEntity.java     # 基类: jakarta.persistence.*
│   ├── User.java           # 用户实体: @ManyToMany
│   ├── Role.java           # 角色实体
│   └── types/              # JDK17 特性类型
│       ├── AccountStatus.java   # sealed interface + record
│       └── ApiResponse.java     # 代数数据类型
├── dto/                    # Record DTO (不是 class!)
│   ├── UserCreateRequest.java
│   ├── UserUpdateRequest.java
│   ├── UserResponse.java
│   └── PageRequest.java
├── repository/             # Spring Data JPA
│   ├── UserRepository.java     # Text Block JPQL
│   └── RoleRepository.java
├── service/                # 业务层
│   ├── UserService.java
│   └── impl/UserServiceImpl.java  # 构造器注入
├── controller/             # REST 控制器
│   ├── UserController.java
│   └── AuthController.java
├── config/                 # 配置类
│   ├── SecurityConfig.java      # SecurityFilterChain
│   └── AppConfig.java           # RestClient + HTTP Client
├── autoconfig/             # SB3 自动配置
│   └── AuditAutoConfiguration.java
├── exception/              # 异常处理
│   ├── GlobalExceptionHandler.java  # ProblemDetail
│   ├── ResourceNotFoundException.java
│   └── BusinessException.java
├── http/                   # HTTP Interface Client
│   └── ExternalApiClient.java
└── util/                   # 工具类
    └── PatternMatchingDemo.java  # JDK17 语法演示
```

## 为什么 Record 不能做 JPA Entity?

| 特性 | Record | JPA Entity 要求 |
|------|--------|----------------|
| final | 自动 final | 需要非 final (Hibernate 代理需要继承) |
| 无参构造器 | 没有 | 必须有 (反射创建) |
| 字段 | final | 可变 (需要 setter) |
| 继承 | 不能继承其他类 | 需要继承 BaseEntity |

**结论**: `model/` 用 class, `dto/` 用 record, 各司其职.

---

## 学习检查清单

- [ ] 理解 Record 和 class 的区别及适用场景
- [ ] 能写出 sealed interface + record 的代数数据类型
- [ ] 会用 switch 表达式替代传统的 switch 语句
- [ ] 会用 instanceof pattern matching 替代显式强转
- [ ] 会用 Text Block 写多行 SQL/JSON
- [ ] 能将 SB2 项目的所有 javax.* import 改为 jakarta.*
- [ ] 能将 WebSecurityConfigurerAdapter 改为 SecurityFilterChain Bean
- [ ] 理解 ProblemDetail 并能自定义错误响应
- [ ] 会用构造器注入替代 @Autowired 字段注入
- [ ] 会声明 HTTP Interface Client 并注册为 Bean
- [ ] 知道 spring.factories 已被 AutoConfiguration.imports 替代
- [ ] 知道 spring.sql.init.mode 默认值已改为 NEVER
