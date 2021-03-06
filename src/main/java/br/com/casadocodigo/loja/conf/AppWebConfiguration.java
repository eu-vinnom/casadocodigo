package br.com.casadocodigo.loja.conf;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.datetime.DateFormatterRegistrar;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.google.common.cache.CacheBuilder;

import br.com.casadocodigo.loja.controllers.HomeController;
import br.com.casadocodigo.loja.dao.ProdutoDAO;
import br.com.casadocodigo.loja.dao.UsuarioDAO;
import br.com.casadocodigo.loja.infra.FileSaver;
import br.com.casadocodigo.loja.models.CarrinhoCompras;
import br.com.casadocodigo.loja.validation.UsuarioValidation;

@EnableWebMvc
@ComponentScan(basePackageClasses = { HomeController.class, ProdutoDAO.class,
    FileSaver.class, CarrinhoCompras.class, UsuarioValidation.class, UsuarioDAO.class})
@EnableCaching
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AppWebConfiguration extends WebMvcConfigurerAdapter {

  @Bean
  public InternalResourceViewResolver internalResourceViewResolver() {
    InternalResourceViewResolver resolver = new InternalResourceViewResolver();
    resolver.setPrefix("/WEB-INF/views/");
    resolver.setSuffix(".jsp");

    // resolver.setExposeContextBeansAsAttributes(true);
    resolver.setExposedContextBeanNames("carrinhoCompras");

    return resolver;
  }

  @Bean
  public MessageSource messageSource() {
    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
    messageSource.setBasename("/WEB-INF/messages");
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setCacheSeconds(1);
    return messageSource;
  }
  
  @Bean
  public FormattingConversionService mvcConversionService() {
    DefaultFormattingConversionService cs = new DefaultFormattingConversionService();
    DateFormatterRegistrar reg = new DateFormatterRegistrar();
       
    reg.setFormatter(new DateFormatter("yyyy-MM-dd"));
    reg.registerFormatters(cs);
    
    return cs;
  }

  @Bean
  public MultipartResolver multipartResolver() {
    return new StandardServletMultipartResolver();
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public CacheManager cacheManger() {
    CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder().maximumSize(100).expireAfterAccess(5,
        TimeUnit.MINUTES);
    GuavaCacheManager manager = new GuavaCacheManager();
    manager.setCacheBuilder(builder);
    return manager; // new ConcurrentMapCacheManager();
  }

  @Bean
  public ViewResolver contentNegotiationViewResolver(ContentNegotiationManager manager) {
    List<ViewResolver> viewResolvers = new ArrayList<>();
    viewResolvers.add(internalResourceViewResolver());
    viewResolvers.add(new JsonViewResolver());

    ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
    resolver.setViewResolvers(viewResolvers);
    resolver.setContentNegotiationManager(manager);
    return resolver;
  }

  @Override
  public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    configurer.enable();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new LocaleChangeInterceptor());
  }

  @Bean
  public LocaleResolver localeResolver() {
    return new CookieLocaleResolver();
  }

  @Bean
  public MailSender mailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost("seu servidor smtp");// por exemplo smtp.gmail.com
    mailSender.setUsername("seu email");
    mailSender.setPassword("sua senha");
    mailSender.setPort(587);

    Properties mailProperties = new Properties();
    mailProperties.setProperty("mail.smtp.auth", "true");
    mailProperties.setProperty("mail.smtp.starttls.enable", "true");
    mailSender.setJavaMailProperties(mailProperties);

    return mailSender;
  }

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    for (HttpMessageConverter<?> converter : converters) {
      if (converter instanceof MappingJackson2HttpMessageConverter) {
        MappingJackson2HttpMessageConverter jacksonConverter = (MappingJackson2HttpMessageConverter) converter;
        jacksonConverter.setPrettyPrint(true);
      }
    }
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
