package project.airbnb.clone.common.annotations;

import project.airbnb.clone.common.resolvers.CurrentGuestIdArgumentResolver;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 현재 사용자 ID를 얻을 수 있는 annotation
 * @see CurrentGuestIdArgumentResolver
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface CurrentGuestId {
    boolean required() default true;
}
