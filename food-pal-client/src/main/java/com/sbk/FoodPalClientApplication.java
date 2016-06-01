package com.sbk;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@EnableZuulProxy
@SpringCloudApplication
@EnableFeignClients
public class FoodPalClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodPalClientApplication.class, args);
	}

	@Bean
	@LoadBalanced
	public RestTemplate getLoadBalanced() {
		return new RestTemplate();
	}

}

@FeignClient("account-service")
interface AccountsService {
    @RequestMapping(value = "/msg", produces = "application/json")
    List<String> msg();
}

class Account {
    private Long id;
    private String name;

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

@RestController
@RequestMapping("/accounts")
class RequestApiGatewayRestController {

    @Autowired
    private DiscoveryClient discoverClient;

    @Autowired
    private AccountsService reservationService;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(method = RequestMethod.GET, path = "/names")
    public Collection<String> getReservationName(){
        ParameterizedTypeReference<Resources<Account>> ptr =
                new ParameterizedTypeReference<Resources<Account>>() { };
        ResponseEntity<Resources<Account>> exchange =
                this.restTemplate.exchange("http://account-service/accounts",
                        HttpMethod.GET,
                        null,
                        ptr);
        return exchange
                .getBody()
                .getContent()
                .stream()
                .map(Account::getName)
                .collect(Collectors.toList());
    }

    @RequestMapping(method = RequestMethod.GET, path = "/msg")
    public List<String> msg(){
        return reservationService.msg();
    }

    @HystrixCommand(fallbackMethod = "getBackupGuide")
    @RequestMapping(method = RequestMethod.GET, path = "/musterror")
    public String haveToBeError() {
        return restTemplate.getForObject("http://account-service/musterror", String.class);
    }

    String getBackupGuide() {
        return "None available! Your backup guide is: Cookie";
    }
}

