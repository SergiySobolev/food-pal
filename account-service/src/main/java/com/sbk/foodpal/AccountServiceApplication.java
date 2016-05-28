package com.sbk.foodpal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients
public class AccountServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountServiceApplication.class, args);
	}
}

@Component
class DummyUserDataCLR implements CommandLineRunner {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public void run(String... args) throws Exception {
        Stream.of("Serhii", "Yoni", "Nazar", "Yasnogor")
                .forEach(name -> accountRepository.save(new Account(name)));
        accountRepository.findAll().forEach(System.out::println);
        accountRepository.findByName("YasnoGor").forEach(System.out::println);
    }
}

@Component
@RefreshScope
@RestController
class MessageController {
    @Value("${message.greeting}")
    String greeting;

    @Value("${server.port}")
    int port;

    @Value("${configuration.projectName}")
    String projectName;

    @RequestMapping(value = "/msg", produces = "application/json")
    public List<String> msg(){
        List<String> env = Arrays.asList(
                "message.greeting is: " + greeting,
                "server.port is: " + port,
                "configuration.projectName is: " + projectName
        );
        return env;
    }

}

@RepositoryRestResource
interface AccountRepository extends JpaRepository<Account, Long> {

    @RestResource(path = "by-name")
    Collection<Account> findByName(@Param("name")String name);
}

@Entity
class Account {
    @Id
    @GeneratedValue
    private Long id;
    private String name;

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public Account(String name) {
        this.name = name;
    }

    public Account() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
