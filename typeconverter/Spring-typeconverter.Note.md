# Practice Setting



**Project : typeconverter**

```groovy
plugins {
	id 'org.springframework.boot' version '2.6.4'
	id 'io.spring.dependency-management' version '1.0.11.RELEASE'
	id 'java'
}

group = 'hello'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '11'

configurations {
	compileOnly {
		extendsFrom annotationProcessor
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
	implementation 'org.springframework.boot:spring-boot-starter-web'
	compileOnly 'org.projectlombok:lombok'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
	useJUnitPlatform()
}
```

- #### 스프링 타입 컨버터에 대해 배우게 된다.



# Type Converter



- #### 스프링은 자동으로 요청에 대해 원하는 타입으로 변환하는 작업을 지원한다.

- #### 스프링 타입 변환은 여러 곳에 적용된다.

  - #### 스프링 MVC 요청 파라미터

    - #### @RequestParam, @ModelAttribute, @PathVariable

  - #### @Value 등으로 YML 정보 읽기

  - #### XML에 넣은 스프링 빈 정보를 변환

  - #### 뷰를 렌더링 할 때



## Converter



```java
package org.springframework.core.convert.converter;

public interface Converter<S, T> {
	T convert(S source);
}
```

- #### 스프링에 추가적인 타입 변환이 필요하면 컨버터 인터페이스를 구현해서 등록하면 된다.

- #### 과거에는 `PropertyEditor` 라는 것으로 타입을 변환했다. `PropertyEditor` 는 동시성 문제가 있어서 타입을 변환할 때 마다 객체를 계속 생성해야 하는 단점이 있었다. `Converter`은 동시성 문제를 해결하며 확장에도 용이하다.



```java
@Slf4j
public class StringToIntegerConverter implements Converter<String, Integer> {

    @Override
    public Integer convert(String source) {
        log.info("convert source={}", source);
        return Integer.valueOf(source);
    }
    
}
```

- #### String 타입을 Integer 타입으로 변환하는 코드로 `Converter`를 구현한 것이다.

- #### 반대로 타입을 변환하는 것도 가능하다.



```java
// "127.0.0.1:8080"가 들어오면 Converter로 바로 IpPort 객체로 변환하고 싶을 때 이용
@Getter
@EqualsAndHashCode
public class IpPort {
    private String ip;
    private int port;

    public IpPort(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
}
```

- #### `@EqualsAndHashCode`는 롬복 기능으로 equals A, B 할 때 ip, port 같으면 true로 반환하는 것이다. 즉, 모든 필드를 사용해서 `equals()`, `hashCode()`를 생성하며 모든 필드의 값이 같다면 `a.equals(b)`의 결과가 참이 된다. 참조값이 달라도 필드값이 같으면 같은 것으로 간주하는 것이다.

- #### 테스트 시 `a.isEqualTo(b)` 메소드에 적용할 수 있다.



```java
// String을 IpPort 객체로 convert 한다.
@Slf4j
public class StringToIpPortConverter implements Converter<String, IpPort> {
    @Override
    public IpPort convert(String source) {
        log.info("convert source={}", source);
        // "127.0.0.1:8080" -> IpPort 객체
        String[] split = source.split(":");
        String ip = split[0];
        int port = Integer.parseInt(split[1]);
        return new IpPort(ip, port);
    }
}
```

```java
// ConverterTest
// String -> IpPort
@Test
void stringToIpPort(){
    StringToIpPortConverter converter = new StringToIpPortConverter();
    String source = "127.0.0.1:8080";
    IpPort result = converter.convert(source);
    assertThat(result).isEqualTo(new IpPort("127.0.0.1", 8080));
}
```

- #### String을 ip, port로 변환하는 컨버터 코드이다.

- #### 위의 IpPort 객체로 만들어서 반환한다.

- #### 스프링은 용도에 따라 다양한 방식의 타입 컨버터를 제공한다.

  - #### Converter : 기본 타입 컨버터

  - #### ConverterFactory : 전체 클래스 계층 구조가 필요할 때 

  - #### GenericConverter : 정교한 구현, 대상 필드의 애노테이션 정보 사용 가능

  - #### ConditionalGenericConverter : 특정 조건이 참인 경우에만 실행

    [Converter](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#core-convert)

  - #### 스프링은 이미 문자, 숫자, 불린, Enum 등 일반적인 타입에 대한 컨버터가 기본으로 제공한다.



## ConversionService



```java
public interface ConversionService {
    
    boolean canConvert(@Nullable Class<?> sourceType, Class<?> targetType);
    
    boolean canConvert(@Nullable TypeDescriptor sourceType, TypeDescriptor targetType);
    
    <T> T convert(@Nullable Object source, Class<T> targetType);
Object convert(@Nullable Object source, @Nullable TypeDescriptor sourceType, TypeDescriptor targetType);

}
```

- #### 스프링은 개별 컨버터를 모아두고 그것들을 묶어서 편리하게 사용할 수 있는 기능인 `ConversionService`를 제공한다.

- #### 컨버전 서비스 인터페이스는 단순히 컨버팅이 가능한지 확인하는 기능과 컨버팅 기능을 제공한다.

- #### `DefaultConversionService`는  컨버전 서비스를 구현한 구현체 중 하나로 테스트 시 작성한 컨버터를 등록할 수 있다.

- #### 타입 컨버터들은 모두 컨버전 서비스 내부에 숨어서 제공되어 타입을 변환을 원하는 사용자는 컨버전 서비스 인터페이스에만 의존하면 된다. 물론 컨버전 서비스를 등록하는 부분과 사용하는 부분을 분리하고 의존관계 주입을 사용해야 한다.

- #### `DefaultConversionService`는 두 인터페이스를 구현했다.

  - #### `ConversionService` : 컨버터 사용에 초점(`canConvert()`, `convert()`)

  - #### `ConverterRegistry` : 컨버터 등록에 초점(`addConverter()`)

  - #### 이렇게 인터페이스를 분리하면 컨버터를 사용하는 클라이언트와 컨버터를 등록하고 관리하는 클라이언트의 관심사를 명확하게 구분할 수 있으며 이것을 `ISP`라고 한다.



```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 주석처리 우선순위 - 우선순위 때문에 지운다
        registry.addConverter(new StringToIntegerConverter());
        registry.addConverter(new IntegerToStringConverter());
        registry.addConverter(new StringToIpPortConverter());
        registry.addConverter(new IpPortToStringConverter());

    }
}
```

- #### `WebMvcConfigurer`은 스프링 MVC에 등록 시 반드시 사용하는 것이다.

- #### `WebMvcConfigurer`가 제공하는 `addFormatter()`를 사용해서 추가하고 싶은 컨버터를 등록하며 스프링은 내부에서 사용하는 `ConversionService`에 컨버터를 자동으로 추가해준다.

- #### 컨버터를 추가하면 추가한 컨버터가 기본 컨버터 보다 높은 우선 순위를 가진다.

- #### `@RequestParam`은 처리하는 `ArgumentResolver`인 `RequestParamMethodAgrumentResolver`에서 `ConversionService`를 사용해서 타입을 변환한다.



## View Converter Application



```java
@Controller
public class ConverterController {

    @GetMapping("/converter-view")
    public String converterView(Model model){
        model.addAttribute("number", 10000);
        model.addAttribute("ipPort", new IpPort("127.0.0.1", 8080));
        return "converter-view";
    }
    
}
```

```html
<!--converter-view.html-->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<ul>
    <li>${number}: <span th:text="${number}" ></span></li>
    <li>${{number}}: <span th:text="${{number}}" ></span></li>
    <li>${ipPort}: <span th:text="${ipPort}" ></span></li>
    <li>${{ipPort}}: <span th:text="${{ipPort}}" ></span></li>
</ul>
</body>
</html>
```

```
// http://localhost:8080/converter-view 요청
${number}: 10000
${{number}}: 10000
${ipPort}: hello.typeconverter.type.IpPort@59cb0946
${{ipPort}}: 127.0.0.1:8080
```

- #### 뷰에 컨버터를 적용하면 문자를 객체로 컨버팅하는 것이 아닌 객체를 문자로 컨버팅하는 작업을 확인 할 수 있다.

- #### 타임리프는 `${{...}}`를 사용하면 자동으로 컨버전 서비스를 사용해서 변환된 결과를 출력해준다.

  - #### 변수 표현식 : `${...}`

  - #### 컨버전 서비스 적용 : `${{...}}`

- #### `${number}`은 타임리프가 자동으로 숫자를 문자로 변환하기 때문에 `${{number}}`와 결과가 같다.



```java
// ConverterController
@GetMapping("/converter/edit")
public String converterForm(Model model){
    IpPort ipPort = new IpPort("127.0.0.1", 8080);
    Form form = new Form(ipPort);
    model.addAttribute("form", form);
    return "converter-form";
}

@PostMapping("/converter/edit")
public String converterEdit(@ModelAttribute Form form, Model model){
    IpPort ipPort = form.getIpPort();
    model.addAttribute("ipPort", ipPort);
    return "converter-view";
}

@Data
static class Form{
    private IpPort ipPort;

    public Form(IpPort ipPort) {
        this.ipPort = ipPort;
    }
}
```

```html
<!--converter-form.html-->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<form th:object="${form}" th:method="post">
    th:field <input type="text" th:field="*{ipPort}"><br/>
    th:value <input type="text" th:value="*{ipPort}">(보여주기 용도)<br/>
    <input type="submit"/>
</form>
</body>
</html>
```

- #### 타임리프의 `th:field`는 `id`, `name`을 출력하는 등 다양한 기능이 있는데 여기에 컨버전 서비스도 함께 적용된다. 즉, 자동으로 컨버터를 적용한다. 이때 `*{...}`를 사용한다.

- #### `th:value`는 컨버터를 적용하고 싶지 않을 때 이용한다.



## Formatter



- #### 객체를 특정한 포멧에 맞추어 문자로 출력하거나 또는 그 반대의 역할을 하는 것에 특화된 기능이 `Formatter`이다. 즉, 컨버터의 특별한 버전이라고 생각하면 된다.

- #### 포맷터를 이용하여 1000을 "1,000"으로 변환하고 날짜를 "2021-01-01 10:50:11" 와 같이 변환하며 날짜의 경우 Locale 현지화 정보가 될 수 있다.



```java
public interface Printer<T> {
    String print(T object, Locale locale);
}

public interface Parser<T> {
    T parse(String text, Locale locale) throws ParseException;
}

public interface Formatter<T> extends Printer<T>, Parser<T> {
}
```

- #### 포맷터는 객체를 문자로 변경하고 문자를 객체로 변경하는 두 가지 기능을 모두 수행한다.

- #### `String print(T object, Locale locale);`은 객체를 문자로 변경한다.

- #### `T parse(String text, Locale locale) throws ParseException;`은 문자를 객체로 변경한다.



```java
// formatter에 이용
@Slf4j
public class MyNumberFormatter implements Formatter<Number> {
    @Override
    public Number parse(String text, Locale locale) throws ParseException {
        log.info("text={}, locale='{}", text, locale);
        // "1,000" -> 1000
        // 1000 -> "1,000"
        NumberFormat format = NumberFormat.getInstance(locale);
        return format.parse(text);
    }

    @Override
    public String print(Number object, Locale locale) {
        log.info("object = {}, locale = {}", object, locale);
        return NumberFormat.getInstance(locale).format(object);
    }
}
```

- #### "1,000"을 "1000"으로, "1000"을 "1,000"으로 변환하는 포맷을 적용한 것이다.

- #### 1,000 처럼 숫자 중간의 쉼표를 적용하려면 자바가 기본으로 제공하는 `NumberFormat` 객체를 사용한다. 이 객체는 Locale 정보를 활용해서 나라별로 다른 숫자 포맷을 만들어준다.

- #### `parse()`를 사용해서 문자를 숫자로 변환한다. Number 타입은 Integer, Long과 같은 숫자 타입의 부모 클래스이다.

- #### `print()`를 사용해서 객체를 문자로 변환한다. 즉, 여기서는 Number 객체를 String으로 변환한다.

- #### `Converter`은 타입에 따라, `Formatter`은 포맷에 따라 반영한다. 즉, 포맷터는 타입이 1개 필요하며 default로 String 타입이다.

- #### 스프링은 용도에 따라 다양한 방식의 포맷터를 제공한다.

  - #### Formatter : 포맷터

  - #### AnnotationFormatterFactory : 필드의 타입이나 애노테이션 정보를 활용할 수 있는 포맷터

    [Formatter](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#format)



## ConversionService with Formatter



- #### 컨버전 서비스에는 컨버터만 등록할 수 있고 포맷터를 등록할 수는 없다. 포맷터는 객체를 문자로, 문자를 객체로 변환하는 특별한 컨버터일 뿐이다.

- #### 포맷터를 지원하는 컨버전 서비스를 사용하면 컨버전 서비스에 포맷터를 추가할 수 있다. 내부에서 어댑터 패턴을 사용해서 `Formatter`가 `Converter`처럼 동작하도록 한다.



```java
public class FormattingConversionServiceTest {

    @Test
    void formattingConversionService(){
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        // 컨버터 등록
        conversionService.addConverter(new StringToIpPortConverter());
        conversionService.addConverter(new IpPortToStringConverter());
        // 포멧터 등록
        conversionService.addFormatter(new MyNumberFormatter());

        // 컨버터 사용
        IpPort ipPort = conversionService.convert("127.0.0.1:8080", IpPort.class);
        assertThat(ipPort).isEqualTo(new IpPort("127.0.0.1", 8080));
        // 포멧터 사용
        assertThat(conversionService.convert(1000, String.class)).isEqualTo("1,000");
        assertThat(conversionService.convert("1,000", Long.class)).isEqualTo(1000L);

    }

}
```

- #### `FormattingConversionService`는 포맷터를 지원하는 컨버전 서비스이다. `ConversionService` 관련 기능을 상속받기 때문에 결과적으로 컨버터도 포맷터도 모두 등록할 수 있으며 사용시 모두 `convert()` 를 이용하면 된다.

- #### `DefaultFormattingConversionService`는 `FormattingConversionService`에 기본적인 통화, 숫자 관련 몇 가지 기본 포맷터를 추가해서 제공한다. 즉, `FormattingConversionService`, `ConversionService`, `ConvertRegistry`를 모두 구현하고 있다.

- #### 스프링 부트는 `DefaultFormattingConversionService`를 상속 받은 `WebConversionService`를 내부에서 사용한다.



```java
// WebConfig
@Override
public void addFormatters(FormatterRegistry registry) {
    // 주석처리 우선순위 - 우선순위 때문에 지운다
    //        registry.addConverter(new StringToIntegerConverter());
    //        registry.addConverter(new IntegerToStringConverter());
    registry.addConverter(new StringToIpPortConverter());
    registry.addConverter(new IpPortToStringConverter());

    // 추가
    registry.addFormatter(new MyNumberFormatter());
}
```

- #### 위 코드에 등록한 컨버터와 포맷터는 결국 모두 `DefaultFormattingConversionService`를 상속 받은 `WebConversionService`에 등록하고 사용하는 것이다.

- #### 위 코드에서 포맷터에서도 숫자에서 문자, 문자에서 숫자로 변경하기 때문에 앞선 컨버터는 주석처리 한다.

- #### 우선 순위는 컨버터가 포맷터보다 높다.



## Spring Basic Formatter



- #### 포맷터는 기본 형식이 지정되어 있기 때문에 객체의 각 필드마다 다른 형식으로 포맷을 지정하기는 어렵다. 즉, Number이어도 각 현식의 포맷터에 다른 형식으로 적용하기는 어렵다.



```java
@Controller
public class FormatterController {

    @GetMapping("/formatter/edit")
    public String formatterForm(Model model){
        Form form = new Form();
        form.setNumber(1000000);
        form.setLocalDateTime(LocalDateTime.now());
        model.addAttribute("form", form);
        return "formatter-form";
    }

    @PostMapping("/formatter/edit")
    public String formatterEdit(@ModelAttribute Form form){
        return "formatter-view";
    }

    @Data
    static class Form{
        @NumberFormat(pattern = "###,###")
        private Integer number;
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime localDateTime;
    }

}
```

```html
<!--formatter-form.html-->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<form th:object="${form}" th:method="post">
    number <input type="text" th:field="*{number}"><br/>
    localDateTime <input type="text" th:field="*{localDateTime}"><br/>
    <input type="submit"/>
</form>
</body>
</html>
```

```html
<!--formatter-view.html-->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<ul>
    <li>${form.number}: <span th:text="${form.number}" ></span></li>
    <li>${{form.number}}: <span th:text="${{form.number}}" ></span></li>
    <li>${form.localDateTime}: <span th:text="${form.localDateTime}" ></span></li>
    <li>${{form.localDateTime}}: <span th:text="${{form.localDateTime}}" ></span></li>
</ul>
</body>
</html>
```

```
${form.number}: 1000000
${{form.number}}: 1,000,000
${form.localDateTime}: 2022-04-07T00:09:08
${{form.localDateTime}}: 2022-04-07 00:09:08
```

- #### 스프링은 어노테이션 기반으로 원하는 형식을 지정해서 사용할 수 있는 유용한 포맷터를 제공한다.

  - #### `@NumberFormat` : 숫자 관련 형식 지정 포맷터 사용, `NumberFormatAnnotationFormatterFactory`

  - #### `@DateTimeFormat` : 날짜 관련 형식 지정 포맷터 사용, `Jsr310DateTimeFormatAnnotationFormatterFactory`

  [@NumberFormat, @DateTimeFormat](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#format-CustomFormatAnnotations[)

- #### 위 코드는 String으로 요청 받은 데이터를 Integer, LocalDateTime으로 변환한다. `@NumberFormat(pattern = "###,###")`은 1000000을 1,000,000로 변환한다. `@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")`은 String을 전형적인 LocalDateTime으로 변환하며 월은 분과 중복 방지를 위해 `MM`으로 작성하는 것이 표준이다.



## ConversionService Caution



- #### 메시지 컨버터(`HttpMessageConverter`)에는 컨버전 서비스가 적용되지 않는다.

- #### 특히 객체를 JSON으로 변환할 때 메시지 컨버터를 사용하면서 많이 오해하는데 `HttpMessageConverter`의 역할은 HTTP 메시지 바디의 내용을 객체로 변환하거나 객체를 HTTP 메시지 바디에 입력하는 것이다.

- #### 예를 들어, JSON 객체로 변환하는 메시지 컨버터는 내부에서 Jackson 같은 라이브러리를 사용한다. 객체를 JSON으로 변환한다면 그 결과는 이 라이브러리에 달린 것이다. 따라서 JSON 결과로 만들어지는 숫자나 날짜 포맷을 변경하고 싶으면 해당 라이브러리가 제공하는 설정을 통해서 포맷을 지정해야 한다. 이때는 Jackson Data Format을 이용한다. 결과적으로 이것은 컨버전 서비스와 전혀 관계 없다.

- #### 메시지 바디는 `HttpMessageConverter`를 이용하며 컨버전 서비스는 `@RequestParam`, `@ModelAttribute `@PathVariable`, 뷰 템플릿 등에서 사용할 수 있다.