package com.walmart.marketplace.infrastructure.gateway.util;

import com.walmart.marketplace.order.domain.entity.type.Vendor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ServiceFinder {

  private final ConfigurableApplicationContext applicationContext;

  protected static final Map<Vendor.VendorType, Vendor> defaultVendorCache =
      new EnumMap<>(Vendor.VendorType.class);

  public ServiceFinder(ConfigurableApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public <T> T getService(Class<T> service, Vendor vendor) {

    try {
      return qualifiedBeanOfType(service, vendor.getVendorId());
    } catch (NoSuchBeanDefinitionException ex) {
      return qualifiedBeanOfType(
          service, defaultVendorCache.get(vendor.getVendorType()).getVendorId());
    }
  }

  protected <T> T qualifiedBeanOfType(Class<T> beanType, String qualifier) {
    return BeanFactoryAnnotationUtils.qualifiedBeanOfType(
        applicationContext.getBeanFactory(), beanType, qualifier);
  }

  @Target({
    ElementType.CONSTRUCTOR,
    ElementType.FIELD,
    ElementType.METHOD,
    ElementType.TYPE,
    ElementType.PARAMETER
  })
  @Retention(RetentionPolicy.RUNTIME)
  @Qualifier("UBEREATS")
  public @interface UBEREATS {}

  @Target({
    ElementType.CONSTRUCTOR,
    ElementType.FIELD,
    ElementType.METHOD,
    ElementType.TYPE,
    ElementType.PARAMETER
  })
  @Retention(RetentionPolicy.RUNTIME)
  @Qualifier("JUSTEAT")
  public @interface JUSTEAT {}

  static {
    defaultVendorCache.put(Vendor.VendorType.UBEREATS, Vendor.UBEREATS);
    defaultVendorCache.put(Vendor.VendorType.JUSTEAT, Vendor.JUSTEAT);
  }
}
