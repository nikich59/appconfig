import ru.nikich59.appconfig.*;

import java.util.List;

@Config
public class TestConfig
{
	@Parameter( name = "int" )
	public int i;

	@Parameter( name = "string" )
	public String s;

	@Parameter( name = "float" )
	public float f;

	@Parameter( name = "long" )
	public long l;

	@Parameter( name = "nested", nested = Parameter.NestedSource.Internal )
	public TestConfigNested nested;

	@ParameterArray( name = "nested_array", componentClass = TestConfigNested.class )
	public TestConfigNested[] nestedArray;

	@ParameterArray( name = "string_array", componentClass = String.class )
	public String[] stringArray;

	@Parameter( name = "class", superClass = List.class )
	public Class clazz;

	@Parameter( name = "nested_file", nested = Parameter.NestedSource.File )
	public TestConfigNested nestedFile;


	@BeforeParsing
	public void beforeParsing( ConfigParser configParser )
	{
		System.out.println( "\n   ---   Before parsing   ---   \n" );
	}

	public void print( )
	{
		System.out.println( "Int: " + i );
		System.out.println( "Float: " + f );
		System.out.println( "Long: " + l );
		System.out.println( "String: " + s );

		System.out.println( "Nested: " );
		if ( nested == null )
		{
			System.out.println( "\tnull" );
		}
		else
		{
			nested.print( );
		}

		System.out.println( "Nested array: " );
		if ( nestedArray == null )
		{
			System.out.println( "\tnull" );
		}
		else
		{
			System.out.println( "[" );
			for ( TestConfigNested testConfigNested : nestedArray )
			{
				testConfigNested.print( );
				System.out.println( "\t\t," );
			}
			System.out.println( "]" );
		}

		System.out.println( "String array: " );
		if ( stringArray == null )
		{
			System.out.println( "\tnull" );
		}
		else
		{
			System.out.println( "[" );
			for ( String s : stringArray )
			{
				System.out.println( "\t\'" + s + "\'" );
				System.out.println( "\t\t," );
			}
			System.out.println( "]" );
		}

		System.out.println( "Class: " );
		if ( clazz == null )
		{
			System.out.println( "\tnull" );
		}
		else
		{
			System.out.println( "\t" + clazz.toGenericString( ) );
		}

		System.out.println( "Nested file: " );
		if ( nestedFile == null )
		{
			System.out.println( "\tnull" );
		}
		else
		{
			nestedFile.print( );
		}
	}
}












