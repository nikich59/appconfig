package ru.nikich59.appconfig;

import java.lang.reflect.Field;

/**
 * Created by Nikita on 26.02.2018.
 */
public class ConfigValidator
{
	public boolean isConfigValid( Class configClass )
			throws ConfigValidationException
	{
		if ( ! configClass.isAnnotationPresent( ConfigObject.class ) )
		{
			throw new ConfigValidationException( ConfigValidationException.Reason.AnnotationNotPresent,
					"Config object: " + configClass.toString( ) + " is not annotated properly" );
		}

		for ( Field field : configClass.getDeclaredFields( ) )
		{
			if ( field.isAnnotationPresent( ParameterArray.class ) )
			{
				if ( ! isParameterArrayValid( field ) )
				{
					return false;
				}
			}
			else if ( field.isAnnotationPresent( Parameter.class ) )
			{
				if ( ! isParameterValid( field ) )
				{
					return false;
				}
			}
		}

		return true;
	}

	private boolean isParameterArrayValid( Field field )
			throws ConfigValidationException
	{
		ParameterArray parameterArray = field.getAnnotation( ParameterArray.class );

		if ( ! field.getType( ).isArray( ) )
		{
			throw new ConfigValidationException( ConfigValidationException.Reason.ParameterArrayIsNotArray,
					"Field: \'" + field.getName( ) + "\' is marked as parameter array but is not an array" );
		}

		Class requiredParameterArrayComponentType = parameterArray.parameterClass( );
		Class actualParameterArrayComponentType = field.getType( ).getComponentType( );

		if ( ! parameterArray.parameterClass( ).equals( field.getType( ).getComponentType( ) ) )
		{
			throw new ConfigValidationException( ConfigValidationException.Reason.ParameterArrayTypeNotMatch,
					"Parameter array component type required: \'" + requiredParameterArrayComponentType.toString( ) +
							"\', actual array component type: \'" + actualParameterArrayComponentType.toString( ) +
							"\'" );
		}

		return true;
	}

	private boolean isParameterValid( Field field )
			throws ConfigValidationException
	{
		if ( field.getType( ).isArray( ) )
		{
			throw new ConfigValidationException( ConfigValidationException.Reason.ParameterTypeNotMatch,
					"Field: \'" + field.getName( ) + "\' is not marked as array but array provided" );
		}

		return true;
	}
}
