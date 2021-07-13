package com.pwr.web.controller;
/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Objects;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.client.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import com.pwr.web.model.CPC;
import java.util.Collections;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.access.prepost.PreAuthorize;
/**
 * @author Joe Grandja
 */
@Controller
public class MainController {

    private final OAuth2AuthorizedClientService authorizedClientService;

   // String CCURL = new String("https://CPC-OKTA.azurewebsites.net/Catalog?customerGroup=");
   String CCURL = new String("https://localhost:3002/Catalog?customerGroup=");

    public MainController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @RequestMapping("/ProductCatalog")
    public String CustomerOffer(Model model,OAuth2AuthenticationToken authentication,@AuthenticationPrincipal OAuth2User principal ) {


        // Create the Okta client registration
   /*     ClientRegistration CustomerOfferClientRegistration = ClientRegistration
                    .withRegistrationId("CustomerOffer")
                    .tokenUri("https://xom-poc.okta.com/oauth2/auspdolvnEZiFfZhu5d6/v1/token")
                    .clientId("0oarprsoofpoIXI4e5d6")
                    .clientSecret("2bnQyMQ0tA7bOWXpPXJnAU_nhyXsuKrFKOIbD0Z8")
                    .scope("pwrscope")
                    .authorizationGrantType(new AuthorizationGrantType("client_credentials"))
                    .build();
        */
        ClientRegistration CustomerOfferClientRegistration = ClientRegistration
                .withRegistrationId("CustomerOffer")
                .tokenUri("https://abacpoc.us.auth0.com/oauth/token")
                .clientId("DlMRAGyW3jPA9vgvwcAu36Sj1pVfswzB")
                .clientSecret("xQoMjYrJDyPbYPlyG0iPlN2J33rgouiQeqr_37JyTvHrfbM30vUjt2GmSYTYjmWm")
                .scope("read")
                .authorizationGrantType(new AuthorizationGrantType("client_credentials"))
                .build();
        // Create the client registration repository

         ClientRegistrationRepository customerClientRegistrationRepository = new InMemoryClientRegistrationRepository(CustomerOfferClientRegistration);



        // Create the authorized client service
        OAuth2AuthorizedClientService customerAuth2AuthorizedClientService = new InMemoryOAuth2AuthorizedClientService(customerClientRegistrationRepository);


        // Create the authorized client manager and service manager using the
        // beans created and configured above

            OAuth2AuthorizedClientProvider CustomerAuthorizedClientProvider =
                    OAuth2AuthorizedClientProviderBuilder.builder()
                            .clientCredentials()
                            .build();

            AuthorizedClientServiceOAuth2AuthorizedClientManager CustomerAuthorizedClientManager =
                    new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                            customerClientRegistrationRepository, customerAuth2AuthorizedClientService);


        CustomerAuthorizedClientManager.setAuthorizedClientProvider(CustomerAuthorizedClientProvider);





        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId("CustomerOffer")
                .principal("ACE")
                .build();

        // Perform the actual authorization request using the authorized client service and authorized client
        // manager. This is where the JWT is retrieved from the Okta servers.
        OAuth2AuthorizedClient authorizedClient = CustomerAuthorizedClientManager.authorize(authorizeRequest);

        // Get the token from the authorized client object
        OAuth2AccessToken accessToken = Objects.requireNonNull(authorizedClient).getAccessToken();


        Map<String,String> results =new HashMap<String,String>();
        List<CPC> CPCs =new ArrayList<CPC>();
        // Add the JWT to the RestTemplate headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken.getTokenValue());
        HttpEntity request = new HttpEntity(headers);

        if (principal.getAttribute("customergroup") != null ) {
            String requestURL = new String(CCURL.toString() + principal.getAttribute("customergroup"));

            // Make the actual HTTP GET request
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<List<CPC>> response = restTemplate.exchange(
                    requestURL,
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<List<CPC>>() {
                    }
            );


//        String result = response.getBody();
            CPCs = response.getBody();

        }
            model.addAttribute("results", CPCs);
            model.addAttribute("customergroup",principal.getAttribute("customergroup"));

            return "ProductCatalog";
    }


    @RequestMapping("/")
    public String login() {
        return "home";
    }

    @RequestMapping("/profile")
    public ModelAndView userDetails(OAuth2AuthenticationToken authentication) {
        return new ModelAndView("profile" , Collections.singletonMap("details", authentication.getPrincipal().getAttributes()));
    }
    private OAuth2AuthorizedClient getAuthorizedClient(OAuth2AuthenticationToken authentication) {
        return this.authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(), authentication.getName());
    }

    private ExchangeFilterFunction oauth2Credentials(OAuth2AuthorizedClient authorizedClient) {
        return ExchangeFilterFunction.ofRequestProcessor(
                clientRequest -> {
                    ClientRequest authorizedRequest = ClientRequest.from(clientRequest)
                            .header(HttpHeaders.AUTHORIZATION,
                                    "Bearer " + authorizedClient.getAccessToken().getTokenValue())
                            .build();
                    return Mono.just(authorizedRequest);
                });
    }


 @RequestMapping(value = "/oauth/revoke-token", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void logout(HttpServletRequest request) {
  //      String authHeader = request.getHeader("Authorization");
   //     if (authHeader != null) {
    //        String tokenValue = authHeader.replace("Bearer", "").trim();
     //       OAuth2AccessToken accessToken = tokenStore.readAccessToken(tokenValue);
      //      tokenStore.removeAccessToken(accessToken);
       // }

    }


    @RequestMapping("/OrderCreate")
    private String OrderCreate(Model model) {
        return "OrderCreate";
    }

    @RequestMapping("/OrderList")
    private String OrderList(Model model) {
        return "OrderList";
    }

    @RequestMapping("/InvoiceList")
    private String InvoiceList(Model model) {
        return "InvoiceList";
    }

    @RequestMapping("/SMS")
    private String SMS(Model model) {
        return "SMS";
    }

    @RequestMapping("/Logistic")
    private String Logistic(Model model) {
        return "Logistic";
    }

    @RequestMapping("/ASR")
    private String ASR(Model model) {
        return "ASR";
    }

    @RequestMapping("/Report")
    private String Report(Model model) {
        return "Report";
    }

    @RequestMapping("/ContactUs")
    private String ContactUs(Model model) {
        return "ContactUs";
    }

}