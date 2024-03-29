package com.epam.training.microservicefoundation.resourceprocessor.web.client;

import com.epam.training.microservicefoundation.resourceprocessor.web.client.Server.Service;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

public class MockServerExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver {

  private Map<Service, MockServer> serverMap;
  private Properties properties;

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    Parameter parameter = parameterContext.getParameter();
    Server serverAnnotation = parameter.getDeclaredAnnotation(Server.class);
    return serverMap.containsKey(serverAnnotation.service());
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Parameter parameter = parameterContext.getParameter();
    Server serverAnnotation = parameter.getDeclaredAnnotation(Server.class);
    return serverMap.get(serverAnnotation.service());
  }


  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    for (MockServer mockServer : serverMap.values()) {
      mockServer.dispose();
    }
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    String basePort = properties.getProperty("web-client.base-port");
    MockServer mockServer = MockServer.newInstance(Integer.parseInt(basePort));
    serverMap = new HashMap<>();
    serverMap.put(Service.SONG, mockServer);
    serverMap.put(Service.RESOURCE, mockServer);
  }

  private Properties loadApplicationProperties() throws IOException {
    ClassPathResource resource = new ClassPathResource("application.properties");
    return PropertiesLoaderUtils.loadProperties(resource);
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    properties = loadApplicationProperties();
  }
}
