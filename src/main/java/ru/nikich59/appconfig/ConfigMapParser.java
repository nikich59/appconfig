package ru.nikich59.appconfig;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Nikita on 26.02.2018.
 */
public class ConfigMapParser
{
	private List< ClassLoader > classLoaders = new ArrayList<>( );

	private ConfigParser parent;

	ConfigMapParser( ConfigParser parent )
	{
		this.parent = parent;

		classLoaders.add( ClassLoader.getSystemClassLoader( ) );
	}

	ConfigMapParser( ConfigParser parent, ClassLoader classLoader )
	{
		this.parent = parent;

		classLoaders.add( classLoader );
	}

	Object parseMap( Class configClass, Map< String, Object > configObjectMap, Object parent,
					 String pathFromParent )
			throws ConfigParseException, ConfigValidationException
	{
		Objects.requireNonNull( configClass );
		Objects.requireNonNull( configObjectMap );


		ConfigValidator configValidator = new ConfigValidator( );
		if ( !configValidator.isConfigValid( configClass ) )
		{
			throw new ConfigParseException( ConfigParseException.Reason.ValidationException );
		}

		return parseMapAfterValidation( configClass, configObjectMap, parent, pathFromParent );
	}

	void addClassLoader( ClassLoader classLoader )
	{
		classLoaders.add( classLoader );
	}

	private void initialize( Object configObject, Object parentConfig, String pathFromParent )
			throws IllegalAccessException, InvocationTargetException, ConfigParseException
	{
		setParent( configObject, parentConfig, pathFromParent );

		for ( Method method : configObject.getClass( ).getMethods( ) )
		{
			if ( method.isAnnotationPresent( BeforeParsing.class ) )
			{
				method.invoke( configObject, parent );
			}
		}
	}

	private Object parseMapAfterValidation( Class configClass, Map< String, Object > configObjectMap,
											Object parentConfig, String pathFromParent )
			throws ConfigParseException, ConfigValidationException
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

		try
		{
			initialize( configObject, parentConfig, pathFromParent );
		}
		catch ( IllegalAccessException | InvocationTargetException e )
		{
			throw new ConfigParseException( ConfigParseException.Reason.InitializationException );
		}

		List< Field > fields = new ArrayList<>( );

		for ( Field field : configClass.getDeclaredFields( ) )
		{
			if ( field.isAnnotationPresent( BeforeParsing.class ) )
			{
				fields.add( field );
			}
		}

		for ( Field field : configClass.getDeclaredFields( ) )
		{
			if ( !field.isAnnotationPresent( BeforeParsing.class ) )
			{
				fields.add( field );
			}
		}

		for ( Field field : fields )
		{
			parseField( field, configObject, configObjectMap );
		}

		return configObject;
	}

	private void parseField( Field field, Object configObject, Map< String, Object > configObjectMap )
			throws ConfigParseException, ConfigValidationException
	{
		boolean isFieldRequired = isFieldRequired( field, configObjectMap );

		if ( getParameterAnnotation( field ) != null )
		{
			parseParameter( field, configObject, configObjectMap, isFieldRequired );
		}
		else if ( getParameterArrayAnnotation( field ) != null )
		{
			parseParameterArray( field, configObject, configObjectMap, isFieldRequired );
		}
	}

	private Parameter getParameterAnnotation( Field field )
	{
		return field.getAnnotation( Parameter.class );
	}

	private ParameterArray getParameterArrayAnnotation( Field field )
	{
		return field.getAnnotation( ParameterArray.class );
	}

	private boolean isFieldRequired(
			Field field,
			Map< String, Object > configObjectMap
	)
			throws ConfigParseException
	{
		if ( !field.isAnnotationPresent( Required.class ) )
		{
			return false;
		}

		Required requiredInfo = field.getAnnotation( Required.class );

		if ( requiredInfo.fields( ).length == 0 )
		{
			return true;
		}

		for ( RequiredField requiredField : requiredInfo.fields( ) )
		{
			Object requiredFieldValue = configObjectMap.get( requiredField.name( ) );

			if ( requiredFieldValue == null )
			{
				return false;
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
			}
		}

		return false;
	}

	private void parseParameter(
			Field field,
			Object configObject,
			Map< String, Object > configObjectMap,
			boolean isRequired
	)
			throws ConfigParseException, ConfigValidationException
	{
		Parameter parameter = getParameterAnnotation( field );
		String parameterName = parameter.name( );

		if ( isRequired || configObjectMap.containsKey( parameterName ) )
		{
			Object parameterValue = configObjectMap.get( parameterName );

			if ( parameterValue == null )
			{
				throw new ConfigParseException( ConfigParseException.Reason.RequiredFieldNotPresent );
			}

			if ( parameterValue.getClass( ).isArray( ) )
			{
				throw new ConfigParseException( ConfigParseException.Reason.FieldTypeException );
			}

			if ( Parameter.NestedSource.Internal.equals( parameter.nested( ) ) )
			{
				setNestedParameter( field, parameterValue, configObject, parameter.name( ) );
			}
			else if ( Parameter.NestedSource.File.equals( parameter.nested( ) ) )
			{
				try
				{
					String filePath;

					if ( field.getDeclaringClass( ).getResource( parameterValue.toString( ) ) != null )
					{
						filePath = field.getDeclaringClass( ).getResource( parameterValue.toString( ) ).getPath( );
					}
					else
					{
						filePath = parameterValue.toString( );
					}

					File nestedConfigFile = new File( filePath );

					String pathFromParent = nestedConfigFile.getPath( );

					Object nestedParameter =
							parent.parseFile( field.getType( ), nestedConfigFile,
									configObject, pathFromParent );

					setFieldValue( configObject, field, nestedParameter );

				}
				catch ( IOException e )
				{
					throw new ConfigParseException( ConfigParseException.Reason.NestedLinkedConfigException, e );
				}
			}
			else
			{
				setFieldValue( configObject, field, parameterClassCast( field.getType( ), parameterValue, parameter ) );
			}
		}
	}

	private Object parameterClassCast( Class requiredType, Object source, Parameter parameter )
			throws ConfigParseException
	{
		if ( requiredType.isInstance( source ) )
		{
			return source;
		}

		if ( requiredType.equals( int.class ) ||
				requiredType.equals( Integer.class ) )
		{
			if ( source.getClass( ).equals( long.class ) ||
					source.getClass( ).equals( Long.class ) )
			{
				return ( new Long( ( long ) source ) ).intValue( );
			}

			return ( int ) source;
		}

		if ( requiredType.equals( float.class ) ||
				requiredType.equals( Float.class ) )
		{
			if ( source.getClass( ).equals( double.class ) ||
					source.getClass( ).equals( Double.class ) )
			{
				return ( new Double( ( double ) source ) ).floatValue( );
			}

			return ( float ) source;
		}

		if ( requiredType.equals( Class.class ) )
		{
			Class clazz = null;

			for ( ClassLoader classLoader : classLoaders )
			{
				try
				{
					clazz = classLoader.loadClass( source.toString( ) );

					if ( clazz != null )
					{
						break;
					}
				}
				catch ( ClassNotFoundException e )
				{
					// Ignoring exception because error is handled after trying to use all the class loaders.
				}
			}
			if ( clazz == null )
			{
				throw new ConfigParseException( ConfigParseException.Reason.ClassNotFoundException,
						source.toString( ) );
			}

			if ( !parameter.superClass( ).isAssignableFrom( clazz ) )
			{
				throw new ConfigParseException( ConfigParseException.Reason.FieldTypeException );
			}

			return clazz;
		}

		return source;
	}

	private void setFieldValue( Object configObject, Field field, Object fieldValue )
			throws ConfigParseException
	{
		try
		{
			field.set( configObject, fieldValue );
		}
		catch ( IllegalAccessException e )
		{
			String fieldName = field.getName( );

			try
			{
				PropertyDescriptor propertyDescriptor =
						new PropertyDescriptor( fieldName, configObject.getClass( ) );

				propertyDescriptor.getWriteMethod( ).invoke( configObject, fieldValue );
			}
			catch ( IllegalAccessException | IntrospectionException |
					InvocationTargetException | IllegalArgumentException ex )
			{
				throw new ConfigParseException( ConfigParseException.Reason.IllegalAccessException,
						"Cannot access field: \'" + field.getName( ) + "\'", ex );
			}
		}

	}

	private void parseParameterArray(
			Field field,
			Object configObject,
			Map< String, Object > configObjectMap,
			boolean isRequired
	)
			throws ConfigParseException, ConfigValidationException
	{
		ParameterArray parameterArray = getParameterArrayAnnotation( field );

		String arrayName = parameterArray.name( );

		Class componentClass = parameterArray.componentClass( );

		if ( isRequired || configObjectMap.containsKey( arrayName ) )
		{

			Object[] values;
			if ( configObjectMap.get( arrayName ).getClass( ).isArray( ) )
			{
				values = ( Object[] ) configObjectMap.get( arrayName );
			}
			else if ( configObjectMap.get( arrayName ) instanceof List )
			{
				values = ( ( List ) configObjectMap.get( arrayName ) ).toArray( );
			}
			else
			{
				throw new ConfigParseException( ConfigParseException.Reason.FieldTypeException,
						"Field: \'" + field.getName( ) + "\' is marked as array but not an array" );
			}

			Object array = Array.newInstance( componentClass, values.length );

			for ( int i = 0; i < values.length; i += 1 )
			{
				if ( values[ i ] instanceof Map )
				{
					Map< String, Object > map = ( Map< String, Object > ) values[ i ];

					Object component;

					component = parseMapAfterValidation( componentClass, map, configObject, "" );

					Array.set( array, i, component );
				}
				else if ( values[ i ].getClass( ).getClass( ).isArray( ) ||
						values[ i ] instanceof List )
				{
					throw new ConfigParseException( ConfigParseException.Reason.ArrayComponentTypeException );
				}
				else if ( componentClass.isInstance( values[ i ] ) )
				{
					Array.set( array, i, values[ i ] );
				}
				else
				{
					throw new ConfigParseException( ConfigParseException.Reason.ArrayComponentTypeException );
				}
			}

			setFieldValue( configObject, field, array );
		}
	}

	private void setNestedParameter(
			Field field,
			Object parameterValue,
			Object configObject,
			String pathFromParent
	)
			throws ConfigParseException, ConfigValidationException
	{
		if ( !( parameterValue instanceof Map ) )
		{
			throw new ConfigParseException( ConfigParseException.Reason.FieldTypeException );
		}

		Map< String, Object > map = ( Map< String, Object > ) parameterValue;

		Object nestedParameter = parseMapAfterValidation( field.getType( ), map, configObject, pathFromParent );

		setFieldValue( configObject, field, nestedParameter );
	}

	private void setParent( Object nestedConfig, Object parentConfig, String pathFromParent )
			throws ConfigParseException
	{
		for ( Field parentField : nestedConfig.getClass( ).getDeclaredFields( ) )
		{
			if ( parentField.isAnnotationPresent( ParentConfig.class ) )
			{
				if ( parentField.getType( ).equals( String.class ) )
				{
					setFieldValue( nestedConfig, parentField, pathFromParent );
				}
				else
				{
					setFieldValue( nestedConfig, parentField, parentConfig );
				}
			}
		}
	}
}
