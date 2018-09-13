import ru.nikich59.appconfig.ConfigWriterJSON;


public class Main
{
	public static void main( String[] args )
			throws Exception
	{
		TestConfigNested testConfigNested = new TestConfigNested( );
		testConfigNested.name = "NAMEEEEE";
		testConfigNested.value = "VALUEEEEEE";

		TestConfig testConfig = new TestConfig( );
		testConfig.clazz = String.class;
		testConfig.f = 1;
		testConfig.i = 2;
		testConfig.l = 4;
		testConfig.s = "STRINGGG";
		testConfig.nested = testConfigNested;

		TestConfigNested[] array = new TestConfigNested[ 2 ];
		array[ 0 ] = testConfigNested;
		array[ 1 ] = testConfigNested;
		testConfig.nestedArray = array;

		testConfig.nestedFile = testConfigNested;
		testConfig.stringArray = new String[]{ "STRING111", "String222" };

		System.out.println( ConfigWriterJSON.getJSONObject( testConfig ).toJSONString( ) );
	}
}
