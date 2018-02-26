package ru.nikich59.appconfig;

import java.lang.annotation.*;

@Target( ElementType.TYPE )
@Inherited
@Retention( RetentionPolicy.RUNTIME )
public @interface ConfigObject
{
}
