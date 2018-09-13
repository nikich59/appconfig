package ru.nikich59.appconfig;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ConfigWriterJSON
{
	public static JSONObject getJSONObject( Object config )
			throws IllegalAccessException
	{
		JSONObject jsonObject = new JSONObject( );

		Field[] fields = config.getClass( ).getFields( );

		for ( Field field : fields )
		{
			Parameter parameterAnnotation = field.getAnnotation( Parameter.class );

			if ( parameterAnnotation != null )
			{
				if ( parameterAnnotation.nested( ) == Parameter.NestedSource.Internal )
				{
					jsonObject.put( parameterAnnotation.name( ), getJSONObject( field.get( config ) ) );
				}
				else
				{
					jsonObject.put( parameterAnnotation.name( ), field.get( config ) );
				}
			}

			ParameterArray parameterArrayAnnotation = field.getAnnotation( ParameterArray.class );

			if ( parameterArrayAnnotation != null )
			{
				JSONArray jsonArray = new JSONArray( );

				Object value = field.get( config );

				List < Object > values = new ArrayList <>( );

				if ( value.getClass( ).isArray( ) )
				{
					for ( int i = 0; i < Array.getLength( value ); i += 1 )
					{
						values.add( Array.get( value, i ) );
					}
				}
				else
				{
					values.addAll( ( List ) value );
				}

				for ( Object o : values )
				{
					if ( field.getType( ).isPrimitive( ) )
					{
						jsonArray.add( o );
					}
					else
					{
						jsonArray.add( getJSONObject( o ) );
					}
				}

				jsonObject.put( parameterArrayAnnotation.name( ),
						jsonArray );
			}
		}

		return jsonObject;
	}
}









