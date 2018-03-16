package ru.nikich59.appconfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter
{
	enum NestedSource
	{
		None,
		Internal,
		File
	}

	String name();

	NestedSource nested() default NestedSource.None;

	Class superClass() default Object.class;
}
