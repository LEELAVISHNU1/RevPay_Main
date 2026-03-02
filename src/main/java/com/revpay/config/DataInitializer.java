package com.revpay.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.revpay.entity.Role;
import com.revpay.repository.RoleRepository;

@Configuration
public class DataInitializer {

	@Bean
	CommandLineRunner initRoles(RoleRepository roleRepository) {
		return args -> {

			if (roleRepository.findByRoleName("PERSONAL").isEmpty()) {
				Role personal = new Role();
				personal.setRoleName("PERSONAL");
				roleRepository.save(personal);
			}

			if (roleRepository.findByRoleName("BUSINESS").isEmpty()) {
				Role business = new Role();
				business.setRoleName("BUSINESS");
				roleRepository.save(business);
			}

			if (roleRepository.findByRoleName("ADMIN").isEmpty()) {
				Role admin = new Role();
				admin.setRoleName("ADMIN");
				roleRepository.save(admin);
			}

			System.out.println("Default roles inserted");
		};
	}
}
