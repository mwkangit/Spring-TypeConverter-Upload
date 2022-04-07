# Practice Setting



**Project : upload**



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

- #### Multipart/form-data를 통한 업로드 및 다운로드에 대한 프로젝트이다.



# File Upload



- #### HTML 폼 전송 방식

  - #### `application/x-www-form-urlencoded`

  - #### `multipart/form-data`

- #### `application/x-www-form-urlencoded`은  html 폼 데이터를 서버에 전송하는 기본적인 방법으로 `enctype`이 없으면 `Content-Type: application/x-www-form-urlencoded`내용이 추가 되어 전송한다.

- #### 파일을 업로드 할 때에는 문자가 아닌 바이너리 데이터를 전송해야 하는데 `application/x-www-form-urlencoded`는 문자를 전송하는 방법이므로 파일 업로드에는 사용할 수 없다.

- #### `multipart/form-data`는 여러 종류의 데이터를 한번에 전송하는 데이터 형식으로 `enctype="multipart/form-data"`를 작성해야 한다.

- #### `Content-Disposition`이라는 항복별 헤더가 추가되어 있으며 부가 정보가 있다.

- #### 폼의 일반 데이터는 각 항목별로 문자가 전송되고 파일의 경우 파일 이름과 Content-Type이 추가되고 바이너리 데이터가 전송된다.



## Servlet File Upload



```java
@Slf4j
@Controller
@RequestMapping("/servlet/v1")
public class ServletUploadControllerV1 {

    @GetMapping("/upload")
    public String newFile(){
        return "upload-form";
    }

    @PostMapping("/upload")
    public String saveFileV1(HttpServletRequest request) throws ServletException, IOException {
        log.info("request={}", request);

        String itemName = request.getParameter("itemName");
        log.info("itemName={}", itemName);

        Collection<Part> parts = request.getParts();
        log.info("parts={}", parts);

        return "upload-form";
    }

}
```

```html
<!--upload-form.html-->
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="utf-8">
</head>
<body>
<div class="container">
    <div class="py-5 text-center">
        <h2>상품 등록 폼</h2>
    </div>
    <h4 class="mb-3">상품 입력</h4>
    <form th:action method="post" enctype="multipart/form-data">
        <ul>
            <li>상품명 <input type="text" name="itemName"></li>
            <li>파일<input type="file" name="file" ></li>
        </ul>
        <input type="submit"/>
    </form>
</div> <!-- /container -->
</body>
</html>
```

```
// http://localhost:8080/servlet/v1/upload 요청
// Request Body 내용
Content-Type: multipart/form-data; boundary=----xxxx

------xxxx
Content-Disposition: form-data; name="itemName"

Spring

------xxxx
Content-Disposition: form-data; name="file"; filename="test.data"
Content-Type: application/octet-stream

sdklajkljdf...
```

- #### `request.getParts()`는 `multipart/form-data` 전송에서 각각 나누어진 부분을 받아서 확인할 수 있다.



```properties
# application.properties
# size limit
spring.servlet.multipart.max-file-size=1MB
spring.servlet.multipart.max-request-size=10MB
```

- #### 큰 파일을 무제한 업로드하는 것은 원하지 않기 때문에 업로드 사이즈를 제한할 수 있다.

- #### 사이즈를 넘으면 `SizeLimitExceededException`예외가 발생한다.

- #### max-file-size : 파일 하나의 최대 사이즈, Default 1MB 이다.

- #### max-request-size : 멀티파트 요청 하나에 여러 파일을 업로드 할 수 있는데, 그 전체 합이다. Default 10MB 이다.



```properties
# application.properties
# multipart accept
spring.servlet.multipart.enabled=true
```

```
// false
request=org.apache.catalina.connector.RequestFacade@xxx
itemName=null
parts=[]
```

```
// true
request=org.springframework.web.multipart.support.StandardMultipartHttpServletRequest
itemName=Spring
parts=[ApplicationPart1, ApplicationPart2]
```



- #### 스프링의 `multipart/form-data`기능을 켜고 끌 수 있다.

- #### 끄면 톰캣이 사용하는 request 구현체인 `RequestFacade`(프록시)를 사용하며 내부 데이터가 비어있는 것을 볼 수 있다.

- #### 켜면 `StandardMultipartHttpServletRequest`로 request 구현체가 변경되며 내부 데이터를 확인 할 수 있다.

- #### Multipart 허용 시 DispatcherServlet은 `MultipartResolver`을 실행한다.

- #### 멀티파트 리졸버는 멀티파트 요청인 경우 서블릿 컨테이너가 전달하는 일반적인 `HttpServletRequest`를 `MultipartHttpServletRequest` 로 변환해서 반환한다.

- #### 스프링이 제공하는 기본 멀티파트 리졸버는 `MultipartHttpServletRequest` 인터페이스를 구현한 `StandardMultipartHttpServletRequest`를 반환한다.



```properties
# application.properties
# multipart file save path
file.dir=/home/mwkang/Desktop/SpringStorage/mvc2/upload_repository/
```

- #### 파일 저장 경로를 설정한 것이며 `/`가 마지막에 반드시 포함되어야 한다.



```java
@Slf4j
@Controller
@RequestMapping("/servlet/v2")
public class ServletUploadControllerV2 {

    @Value("${file.dir}")
    private String fileDir;

    @GetMapping("/upload")
    public String newFile(){
        return "upload-form";
    }

    @PostMapping("/upload")
    public String saveFileV1(HttpServletRequest request) throws ServletException, IOException {
        log.info("request={}", request);

        String itemName = request.getParameter("itemName");
        log.info("itemName={}", itemName);

        Collection<Part> parts = request.getParts();
        log.info("parts={}",  parts);

        for (Part part : parts) {
            log.info("==== PART ====");
            // part 이름
            log.info("name={}", part.getName());
            // part 헤더명들
            Collection<String> headerNames = part.getHeaderNames();
            // part 헤더명들, 각 헤더명에 대한 값
            for (String headerName : headerNames) {
                log.info("header {}: {}", headerName, part.getHeader(headerName));
            }
            // 편의 메소드
            // content-disposition; filename
            // 없을 시 null 반환
            log.info("submittedFilename={}", part.getSubmittedFileName());
            log.info("size={}", part.getSize()); // part body size

            // 데이터 읽기
            InputStream inputStream = part.getInputStream();
            String body = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            log.info("body={}", body);

            // 이 부분은 파일 부붙에만 있으므로 if로 알아본다
            // 파일에 저장하기
            if(StringUtils.hasText(part.getSubmittedFileName())){
                String fullPath = fileDir + part.getSubmittedFileName();
                log.info("파일 저장 fullPath={}", fullPath);
                part.write(fullPath);
            }
        }

        return "upload-form";
    }

}
```

```
// http://localhost:8080/servlet/v2/upload 요청
==== PART ====
name=itemName
header content-disposition: form-data; name="itemName"
submittedFileName=null
size=7
body=상품A
==== PART ====
name=file
header content-disposition: form-data; name="file"; filename="스크린샷.png"
header content-type: image/png
submittedFileName=스크린샷.png
size=112384
body=qwlkjek2ljlese...
파일 저장 fullPath=/Users/kimyounghan/study/file/스크린샷.png
```

- #### `@Value`를 통해 파일 저장 경로를 가져올 수 있다.

- #### 멀티 파트의 각 데이터는 `part`로 나누어져서 담긴다.

- #### `part.getName()`을 통해 파트의 이름을 가져올 수 있다.

- #### `part.getHeaderNames()`를 통해 파트에 해당되는 헤더들을 String 타입으로 가져올 수 있다.

- #### `part.getHeader()`은 해당 헤더의 내용을 가져온다. 파일의 경우 `content-disposition`, `content-type` 헤더가 있다.

- #### `part.getSubmittedFileName()`은 저장할 파일의 이름을 가져온다. 없을 시 null을 반환한다.

- #### `part.getSize()`는 파일의 사이즈를 가져온다.

- #### `part.getInputStream()`은 part의 전송 데이터를 읽는다. 이때 `StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8)`를 사용하여 내용을 확인할 수 있다(바이트여서 알아보기는 힘들다).

- #### `part.write()` 매개변수로 파일 경로와 파일명을 입력하여 part의 파일을 경로에 저장한다.



## Spring File Upload



```java
// Spring에서 제공하는 upload 방법 - MultipartFile
@Slf4j
@Controller
@RequestMapping("/spring")
public class SpringUploadController {

    @Value("${file.dir}")
    private String fileDir;

    @GetMapping("/upload")
    public String newFile(){
        return "upload-form";
    }

    @PostMapping("/upload")
    public String saveFile(@RequestParam String itemName,
                           @RequestParam MultipartFile file, HttpServletRequest request) throws IOException {
        log.info("request = {}", request);
        log.info("itemName = {}", itemName);
        log.info("mulitpartFile = {}", file);

        if(!file.isEmpty()){
            String fullPath = fileDir + file.getOriginalFilename();
            log.info("파일 저장 fullPath = {}", fullPath);
            file.transferTo(new File(fullPath));
        }
        return "upload-form";
    }
}
```

```
// http://localhost:8080/spring/upload 요청
request=org.springframework.web.multipart.support.StandardMultipartHttpServletRequest@5c022dc6
itemName=상품A
multipartFile=org.springframework.web.multipart.support.StandardMultipartHttpServletRequest$StandardMultipartFile@274ba730
파일 저장 fullPath=/Users/kimyounghan/study/file/스크린샷.png
```

- #### `@RequestParam MultipartFile file`는 업로드하는 HTML 폼의 name에 맞추어 @RequestParam을 적용하면 된다. 추가로 @ModelAttribute 에서도 MultipartFile을 동일하게 사용할 수 있다. 즉, @ModelAttribute 폼 안에 MultipartFile 하나를 생성하면 된다.

- #### `file.getOriginalFilename()` : 업로드 파일 명을 반환한다.

- #### `file.transferTo(...)` : 파일 저장하는 것으로 내부에 `new File(fullPath)` 형식으로 경로를 지정하면 된다.



## File Upload Application



- #### 상품을 관리

  - #### 상품 이름

  - #### 첨부파일 하나

  - #### 이미지 파일 여러개

- #### 첨부파일을 업로드 다운로드 할 수 있다.

- #### 업로드한  이미지를 웹 브라우저에서 확인할 수 있다.



```java
@Data
public class Item {

    private long id;
    private String itemName;
    private UploadFile attachFile;
    private List<UploadFile> imageFiles;

}
```

```java
// 아이템 저장하는 영역
@Repository
public class ItemRepository {

    private final Map<Long, Item> store = new HashMap<>();
    private long sequence = 0L;

    public Item save(Item item){
        item.setId(++sequence);
        store.put(item.getId(), item);
        return item;
    }

    public Item findById(Long id){
        return store.get(id);
    }
}
```

```java
@Data
public class UploadFile {

    // 업로드한 파일명
    private String uploadFileName;
    // 시스템에 저장한 파일명
    private String storeFileName;

    public UploadFile(String uploadFileName, String storeFileName) {
        this.uploadFileName = uploadFileName;
        this.storeFileName = storeFileName;
    }
}
```

- #### 사용자들이 같은 파일명 입력 시 덮어쓰는 현상을 방지하기 위해 나눠서 처리하기 위해 `uploadFileName`, `storeFileName` 변수를 나누었다. 즉, 고객이 업로드한 파일명, 서버 내부에서 관리하는 파일명으로 나눈 것이다.



```java
@Component
public class FileStore {

    @Value("${file.dir}")
    private String fileDir;

    // 경로명 + 파일명
    public String getFullPath(String fileName){
        return fileDir + fileName;
    }

    // 이미지 여러개 업로드할 시 이용
    public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles) throws IOException {
        List<UploadFile> storeFileResult = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            if(!multipartFile.isEmpty()){
                storeFileResult.add(storeFile(multipartFile));
            }
        }
        return storeFileResult;
    }

    public UploadFile storeFile(MultipartFile multipartFile) throws IOException {
        if(multipartFile.isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        // image.png
        // 서버에 저장하는 파일명(중복 없는 파일명 생성). UUID로 저장해도 확장자(.png)는 남겨야 이미지인지 알 수 있다.
        String storeFileName = createStoreFileName(originalFilename);
        multipartFile.transferTo(new File(getFullPath(storeFileName)));
        return new UploadFile(originalFilename, storeFileName);


    }

    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        // 시스템에 저장할 파일명
        return uuid + "." + ext;
    }

    // 확장자만 꺼내는 함수
    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }


}
```

- #### `storeFiles()`메소드는 여러 이미지 파일을 담고 반환하는 역할을 한다.

- #### `storeFile`메소드는 UUID로 생성된 파일명으로 파일을 저장하고 기존의 파일명과 UUID로 생성된 파일명을 `UploadFile`객체에 담아서 반환한다.

- #### `createStoreFileName`메소드는 UUID로 시스템에 저장할 새로운 파일 명을 만든다.

- #### `extractExt`메소드는 파일의 확장자를 추출한다.



```java
@Data
public class ItemForm {

    private Long itemId;
    private String itemName;
    private MultipartFile attachFile;
    private List<MultipartFile> imageFiles;

}
```

- #### 상품 저장용 폼이다.

- #### `MultipartFile attachFile`은 멀티파트 @ModelAttribute에서 사용할 수 있다.

- #### `List<MultipartFile> imageFiles`는 이미지를 다중 업로드 하기 위해서 MultipartFile를 사용했다.



```java
@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;
    private final FileStore fileStore;

    @GetMapping("/items/new")
    public String newItem(@ModelAttribute ItemForm form){
        return "item-form";
    }

    @PostMapping("/items/new")
    public String saveItem(@ModelAttribute ItemForm form, RedirectAttributes redirectAttributes) throws IOException {
        // 파일 1개 저장
        UploadFile attachFile = fileStore.storeFile(form.getAttachFile());
        // 이미지 파일 여러개 저장
        List<UploadFile> storeImageFiles = fileStore.storeFiles(form.getImageFiles());

        // 데이터베이스에 저장
        Item item = new Item();
        item.setItemName(form.getItemName());
        item.setAttachFile(attachFile);
        item.setImageFiles(storeImageFiles);
        itemRepository.save(item);

        redirectAttributes.addAttribute("itemId", item.getId());

        return "redirect:/items/{itemId}";
    }

    @GetMapping("/items/{id}")
    public String items(@PathVariable Long id, Model model){
        Item item = itemRepository.findById(id);
        model.addAttribute("item", item);
        return "item-view";
    }

    // 이미지들 출력
    // 보안에 취약하다
    @ResponseBody
    @GetMapping("/images/{filename}")
    public Resource downloadImage(@PathVariable String filename) throws MalformedURLException {
        // file:/User/.../uuid.png
        // url이 가리키는 리소스를 찾아온다
        return new UrlResource("file:" + fileStore.getFullPath(filename));
    }

    // 첨부파일 다운로드
    @GetMapping("/attach/{itemId}")
    public ResponseEntity<Resource> downloadAttach(@PathVariable Long itemId) throws MalformedURLException {
        Item item = itemRepository.findById(itemId);
        String storeFileName = item.getAttachFile().getStoreFileName();
        String uploadFileName = item.getAttachFile().getUploadFileName();

        UrlResource resource = new UrlResource("file:" + fileStore.getFullPath(storeFileName));

        log.info("uploadFileName={}", uploadFileName);

        // 파일명 utf-8 후 contentDisposition으로 다운 받을 수 있게 만든다
        String encodedUploadFileName = UriUtils.encode(uploadFileName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);


    }

}
```

```html
<!--item-form.html-->
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="utf-8">
</head>
<body><div class="container">
    <div class="py-5 text-center">
        <h2>상품 등록</h2>
    </div>
    <form th:action method="post" enctype="multipart/form-data">
        <ul>
            <li>상품명 <input type="text" name="itemName"></li>
            <li>첨부파일<input type="file" name="attachFile" ></li>
            <li>이미지 파일들<input type="file" multiple="multiple"
                              name="imageFiles" ></li>
        </ul>
        <input type="submit"/>
    </form>
</div> <!-- /container -->
</body>
</html>
```

```html
<!--item-view.html-->
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="utf-8">
</head>
<body>
<div class="container">
    <div class="py-5 text-center"><h2>상품 조회</h2>
    </div>
    상품명: <span th:text="${item.itemName}">상품명</span><br/>
    첨부파일: <a th:if="${item.attachFile}" th:href="|/attach/${item.id}|" th:text="${item.getAttachFile().getUploadFileName()}" /><br/>
    <img th:each="imageFile : ${item.imageFiles}" th:src="|/images/${imageFile.getStoreFileName()}|" width="300" height="300"/>
</div> <!-- /container -->
</body>
</html>
```

- #### `newItem`, `saveItem`메소드는 파일을 저장하는 역할을 한다.

- #### `items`메소드는 뷰를 출력한다.

- #### `downloadImage`는 여러 이미지들을 출력한다. 이때 `UrlResource`를 사용하며 이것은 URL 경로가 가리키는 리소스를 찾아서 반환하는 것으로 여기서는 이미지 리소스를 반환한다.

- #### `downloadAttach`메소드는 첨부파일을 다운로드 가능하게 한다.

  - #### UrlResource를 바로 바디에 넣어서 반환하면 파일 내용만 출력하며 다운로드는 진행하지 않는다.

  - #### `UriUtils.encode(uploadFileName, StandardCharsets.UTF_8)`를 이용하여 사용자가 저장했던 파일명을 인코딩 한다.

  - #### `Content-Disposition` 헤더에 사용자 파일명을 입력하여 파일이 전송되었음을 알린다.

  - #### 이때 `\"`를 통해 `"`가 정상적으로 전송되도록 해야한다.

  - #### ResponseEntity 헤더에 작성한 `Content-Disposition`을 넣고 바디에 리소스를 넣어서 반환하면 다운로드가 진행된다.

- #### HTML에서 다중 파일 업로드 시 `multiple="multiple"`을 사용하면 된다. 이후에 ItemForm 클래스의 `List<MultipartFile> imageFiles`로 입력되게 된다.

- ####  HTML에서 첨부 파일은 링크로 걸어두고 이미지는 `<img>` 태그를 `th:each`를 통해 반복하여 출력한다.