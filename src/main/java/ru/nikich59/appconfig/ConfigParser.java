package ru.nikich59.appconfig;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Created by Nikita on 26.02.2018.
 */
public class ConfigParser
{
	public Object parseMap( Class configClass, Map < String, Object > configObjectMap )
			throws ConfigParseException, ConfigValidationException
	{
		ConfigValidator configValidator = new ConfigValidator( );
		if ( ! configValidator.isConfigValid( configClass ) )
		{
			throw new ConfigParseException( ConfigParseException.Reason.ValidationException );
		}

		return parseMapAfterValidation( configClass, configObjectMap );
	}

	private Object parseMapAfterValidation( Class configClass, Map < String, Object > configObjectMap )
			throws ConfigParseException
	{
		Object configObject;

		try
		{
			configObject = configClass.newInstance( );
		}
		catch ( InstantiationException | IllegalAccessException e )
		{
			throw new ConfigParseException( ConfigParseException.Reason.InstantiationException, e );
		}

		for ( Field field : configClass.getDeclaredFields( ) )
		{
			parseField( field, configObject, configObjectMap );
		}

		return configObject;
	}

	private void parseField( Field field, Object configObject, Map < String, Object > configObjectMap )
			throws ConfigParseException
	{
		if ( field.isAnnotationPresent( Parameter.class ) )
		{
			parseParameter( field, configObject, configObjectMap );
		}
		else
		{
			parseParameterArray( field, configObject, configObjectMap );
		}
	}

	private void parseParameter( Field field, Object configObject, Map < String, Object > configObjectMap )
			throws ConfigParseException
	{
		boolean isFieldRequired = isRequired( field, configObjectMap );

		setParameter( field, configObject, configObjectMap, isFieldRequired );
	}

	private void parseParameterArray( Field field, Object configObject, Map < String, Object > configObjectMap )
			throws ConfigParseException
	{
		boolean isFieldRequired = isRequired( field, configObjectMap );

		ParameterArray parameterArrayInfo = field.getAnnotation( ParameterArray.class );

		setParameterArray(
				field,
				parameterArrayInfo.name( ),
				configObject,
				configObjectMap,
				isFieldRequired,
				parameterArrayInfo.parameterClass( ) );
	}

	private boolean isRequired(
			Field field,
			Map < String, Object > configObjectMap )
			throws ConfigParseException
	{
		if ( ! field.isAnnotationPresent( Required.class ) )
		{
			return false;
		}

		Required requiredInfo = field.getAnnotation( Required.class );

		for ( RequiredField requiredField : requiredInfo.fields( ) )
		{
			Object requiredFieldValue = configObjectMap.get( requiredField.name( ) );

			if ( requiredFieldValue == null )
			{
				return false;
				/*
				throw new ConfigParseException( ConfigParseException.Reason.RequiredFieldNotPresent,
						"Field: \'" + field.getName( ) + "\' is required but not provided" );*/
			}

			boolean isValueProvided = requiredField.values( ).length == 0;
			for ( String requiredValue : requiredField.values( ) )
			{
				if ( requiredValue.equals( requiredFieldValue.toString( ) ) )
				{
					isValueProvided = true;
				}
			}

			if ( isValueProvided )
			{
				return true;
/*
				runIfRequired.run( );

				try
				{
					field.set( configObject, requiredFieldValue );
				}
				catch ( IllegalAccessException e )
				{
					throw new ConfigParseException( ConfigParseException.Reason.IllegalAccessException, e );
				}*/
			}
		}

		return false;
	}

	private void setParameter(
			Field field,
			Object configObject,
			Map < String, Object > configObjectMap,
			boolean isRequired )
			throws ConfigParseException
	{
		Parameter parameter = field.getAnnotation( Parameter.class );
		String parameterName = parameter.name( );
		boolean isNested = parameter.nested( );

		if ( isRequired || configObjectMap.containsKey( parameterName ) )
		{
			Object parameterValue = configObjectMap.get( parameterName );

			if ( parameterValue.getClass( ).isArray( ) )
			{
				throw new ConfigParseException( ConfigParseException.Reason.FieldTypeException );
//				setParameterArray( field, parameterValue, configObject, configObjectMap, true );
			}

			if ( isNested )
			{
				setNestedParameter( field, parameterValue, configObject );
			}
			else
			{
				setFieldValue( configObject, field.getName( ), parameterValue );
			}
			/*
			if ( parameterValue instanceof Map )
			{
				setParameterMap( field, parameterValue, configObject, configObjectMap, true );
			}
			else
			{
				setFieldValue( configObject, field.getName( ), parameterValue );
			}*/
		}
	}

	private void setFieldValue( Object configObject, String fieldName, Object fieldValue )
			throws ConfigParseException
	{
		try
		{
			PropertyDescriptor propertyDescriptor =
					new PropertyDescriptor( fieldName, configObject.getClass( ) );

			propertyDescriptor.getWriteMethod( ).invoke( configObject, fieldValue );
		}
		catch ( IllegalAccessException | IntrospectionException | InvocationTargetException | IllegalArgumentException e )
		{
			throw new ConfigParseException( ConfigParseException.Reason.IllegalAccessException, e );
		}
	}

	private void setParameterArray(
			Field field,
			String name,
			Object configObject,
			Map < String, Object > configObjectMap,
			boolean isRequired,
			Class componentType
	)
			throws ConfigParseException
	{
		if ( isRequired || configObjectMap.containsKey( name ) )
		{
			Object[] values = ( Object[] ) configObjectMap.get( name );

			Object array = Array.newInstance( componentType, values.length );

			for ( int i = 0; i < values.length; i += 1 )
			{
				/*
				if ( ! ( values[ i ] instanceof Map ) )
				{
					throw new ConfigParseException( ConfigParseException.Reason.ArrayComponentTypeException,
							"Field: \'" + field.getName( ) + "\' is marked as array of type: \'" +
									componentType.toString( ) + "\' but actual type of element number " +
									String.valueOf( i ) + " is: \'" + values[ i ].getClass( ).toString( ) + "\'" );
				}*/

				if ( values[ i ] instanceof Map )
				{
					Map < String, Object > map = ( Map < String, Object > ) values[ i ];

					Object component;

					component = parseMapAfterValidation( componentType, map );

					Array.set( array, i, component );
				}
				else if ( values[ i ].getClass( ).getClass( ).isArray( ) )
				{
					throw new ConfigParseException( ConfigParseException.Reason.ArrayComponentTypeException );
				}
				else
				{
					Array.set( array, i, values[ i ] );
				}
			}

			setFieldValue( configObject, field.getName( ), array );
/*
			try
			{
				field.set( configObject, array );
			}
			catch ( IllegalAccessException e )
			{
				throw new ConfigParseException( ConfigParseException.Reason.IllegalAccessException, e );
			}*/
		}
	}

	private void setNestedParameter(
			Field field,
			Object parameterValue,
			Object configObject
	)
			throws ConfigParseException
	{
		if ( ! ( parameterValue instanceof Map ) )
		{
			throw new ConfigParseException( ConfigParseException.Reason.FieldTypeException );
		}

		Map < String, Object > map = ( Map < String, Object > ) parameterValue;

		Object nestedParameter = parseMapAfterValidation( field.getType( ), map );

		setFieldValue( configObject, field.getName( ), nestedParameter );
	}
}
