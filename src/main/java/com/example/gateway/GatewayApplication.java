package com.example.gateway;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.cache.LoadBalancerCacheManager;
import org.springframework.cloud.loadbalancer.config.LoadBalancerZoneConfig;
import org.springframework.cloud.loadbalancer.core.CachingServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.DiscoveryClientServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ZonePreferenceServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.ServiceInstanceListSuppliers;
import org.springframework.context.ApplicationContext;
//import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableDiscoveryClient
@LoadBalancerClients(defaultConfiguration = CustomLoadBalancerConfiguration.class)
public class GatewayApplication {

	
//	@Value("${eureka.instance.metadataMap.zone}")
//	private String zone = "zone1";
	
	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}
	
	@RestController
	class SimpleController {
		@Autowired
		DiscoveryClient discoveryClient;
		
		@ResponseBody
		@GetMapping(value = "/{service}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
		public List<ServiceInstance> zone(@PathVariable("service") String service) {
			List<ServiceInstance> instances = discoveryClient.getInstances(service);
			instances.forEach(value -> {
				System.out.println("Value: "+ value);
				System.out.println("Metadata: "+ value.getMetadata() );
				System.out.println("InstanceId: "+value.getInstanceId());
			});
			return instances;
		}
	}
	
}

@Configuration
class CustomLoadBalancerConfiguration {
	@Bean
    public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(
            ConfigurableApplicationContext context) {
		return ServiceInstanceListSupplier.builder()
                .withDiscoveryClient()
                .withHealthChecks()
                .withZonePreference()
                .build(context);
    }
}

@Component
@Order(-2)
class GlobalErrorHandler extends AbstractErrorWebExceptionHandler {

	public GlobalErrorHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties,
			ApplicationContext applicationContext, ServerCodecConfigurer serverCodecConfigurer) {
		super(errorAttributes, resourceProperties, applicationContext);
		this.setMessageWriters(serverCodecConfigurer.getWriters());
		// TODO Auto-generated constructor stub
	}

	@Override
	protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
		// TODO Auto-generated method stub
		return RouterFunctions.route(RequestPredicates.all()
				, this::name);
	}
	
	private Mono<ServerResponse> name(ServerRequest request) {
		Map<String, Object> errorAttributes = getErrorAttributes(request, ErrorAttributeOptions.defaults());
		String valueString = errorAttributes.get("status").toString();
//		if (Integer.valueOf(valueString) !) {
//			
//		}
		System.out.println("Error Attributes: "+errorAttributes);
		System.out.println(valueString);
		return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(BodyInserters.fromValue(errorAttributes));
	}
	
}

