import org.junit.Test;
import ru.nikich59.appconfig.ConfigParser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by prokoshev.n on 27.02.2018.
 */


public class SimpleTest
{
	private Map < String, Object > map;

	private ClassLoader classLoader = ClassLoader.getSystemClassLoader( );

	public static void main( String[] args )
			throws Exception
	{
		SimpleTest simpleTest = new SimpleTest( );

		simpleTest.test( );
	}

	@Test
	public void test( )
			throws Exception
	{
		SimpleTest simpleTest = new SimpleTest( );

		simpleTest.testMap( );

		simpleTest.testJsonFile( );

		simpleTest.testYamlFile( );
	}

	private void initMap( )
	{
		map = new HashMap <>( );

		map.put( "int", 432 );
		map.put( "string", "string parameter" );
		map.put( "float", 3455.4f );
		map.put( "long", 656343 );

		Map < String, Object > nestedMap;
		nestedMap = new HashMap <>( );

		nestedMap.put( "name", "name1" );
		nestedMap.put( "value", "value1" );

		map.put( "nested", nestedMap );

		List < Object > stringArray = new ArrayList <>( );
		stringArray.add( "first  string in array" );
		stringArray.add( "second string in array" );
		stringArray.add( "third  string in array" );

		map.put( "string_array", stringArray );

		List < Map > nestedList = new ArrayList <>( );

		nestedMap = new HashMap <>( );
		nestedMap.put( "name", "nested array name 1" );
		nestedMap.put( "value", "nested array value 1" );

		nestedList.add( nestedMap );

		map.put( "nested_array", nestedList );
	}

	private void testJsonFile( )
			throws Exception
	{
		File testConfigJsonFile = new File( getClass( ).getResource( "test_config.json" ).getFile( ) );

		ConfigParser configParser = new ConfigParser( classLoader );

		TestConfig testConfig = ( TestConfig ) configParser.parseFile( TestConfig.class, testConfigJsonFile );

		System.out.println( "\n   ---   JSON file test:" );

		testConfig.print( );

		System.out.println( "   ---   :JSON file test\n" );
	}

	private void testYamlFile( )
			throws Exception
	{
		File testConfigYamlFile = new File( getClass( ).getResource( "test_config.yml" ).getFile( ) );

		ConfigParser configParser = new ConfigParser( classLoader );

		TestConfig testConfig = ( TestConfig ) configParser.parseFile( TestConfig.class, testConfigYamlFile );

		System.out.println( "\n   ---   YAML file test:" );

		testConfig.print( );

		System.out.println( "   ---   :YAML file test\n" );
	}

	private void testMap( )
			throws Exception
	{
		initMap( );

		ConfigParser configParser = new ConfigParser( classLoader );

		TestConfig testConfig = ( TestConfig ) configParser.parseMap( TestConfig.class, map );

		System.out.println( "\n   ---   Map test:" );

		testConfig.print( );

		System.out.println( "   ---   :Map test\n" );
	}
}











