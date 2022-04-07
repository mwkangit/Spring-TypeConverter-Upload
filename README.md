# Spring-TypeConverter-Upload



## Description

본 프로젝트는 2가지 프로젝트로 구성 된다. 1번째 프로젝트는 `typeconverter`이며 스프링에서 타입에 따라 변환 과정을 구현하여 상세하게 알아본다. 2번째 프로젝트는 `upload`이며 파일 업로드 및 다운로드를 구현하여 알아본다.



------



## Environment

![framework](https://img.shields.io/badge/Framework-SpringBoot-green)![framework](https://img.shields.io/badge/Language-java-b07219) 

Framework: `Spring Boot` 2.6.4

Project: `Gradle`

Packaging: `Jar`

IDE: `Intellij`

Template Engine: `Thymeleaf`

Dependencies: `Spring Web`, `Lombok`



------



## Installation



![Linux](https://img.shields.io/badge/Linux-FCC624?style=for-the-badge&logo=linux&logoColor=black) 

```
./gradlew build
cd build/lib
java -jar hello-spring-0.0.1-SNAPSHOT.jar
```



![Windows](https://img.shields.io/badge/Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white) 

```
gradlew build
cd build/lib
java -jar hello-spring-0.0.1-SNAPSHOT.jar
```



------



## Core Feature



**Project : typeconverter**

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

`Spring Basic Formatter`기능을 이용하여 형식 변환을 구현한 것이다. 어노테이션을 이용한 변환이며 `ConversionService`, `Formatter` 을 이용하여 구현하는 방법도 다루었다.



**Project : upload**

```java
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

`multipart/form-data` 로 요청된 이미지를 로컬에 저장을 구현한 것이다. 이후 파일을 사용자 파일명, 시스템 파일명으로 나눠서 처리했으며 다중 파일 요청, 첨부 파일 다운로드도 다루었다.



-----



## Demonstration Video



**Project : typeconverter**

![Spring-TypeConverter-Upload1](https://user-images.githubusercontent.com/79822924/162283649-4494b6f8-fad4-4f6f-9830-7ae031f93703.gif)



**Project : upload**

![Spring-TypeConverter-Upload2](https://user-images.githubusercontent.com/79822924/162283664-e11ebe68-4243-4c95-81be-efd22bffba69.gif)



------



## More Explanation

[Spring-typeconverter-Note.md](https://github.com/mwkangit/Spring-TypeConverter-Upload/blob/master/typeconverter/Spring-typeconverter.Note.md)
[Spring-upload-Note.md](https://github.com/mwkangit/Spring-TypeConverter-Upload/blob/master/upload/Spring-upload-Note.md)
