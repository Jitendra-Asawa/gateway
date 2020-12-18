package com.example.gateway;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.PathMatchConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurerComposite;

@Component
@EnableWebFlux
public class WebfluxConfigTest extends WebFluxConfigurerComposite{
	
	@Override
	public void configurePathMatching(PathMatchConfigurer configurer) {
		// TODO Auto-generated method stub
		super.configurePathMatching(configurer.setUseCaseSensitiveMatch(false));
	}
}
