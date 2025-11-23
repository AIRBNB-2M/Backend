package project.airbnb.clone.common.annotations;

import project.airbnb.clone.common.resolvers.CurrentMemberIdArgumentResolver;

import java.lang.annotation.*;

/**
 * 현재 사용자 ID를 얻을 수 있는 annotation
 * @see CurrentMemberIdArgumentResolver
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface CurrentMemberId {
    boolean required() default true;
}
