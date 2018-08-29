# appconfig
This is a lightweight module to help you configure your application using JPA-style config objects.

<h1>Import this module:</h1>

- <b>Gradle</b>

```
allprojects {  
	repositories {  
		...  
		maven { url 'https://jitpack.io' }  
	}  
}  
```
```
dependencies {
	implementation 'com.github.nikich59:appconfig:-SNAPSHOT'
}
```

- <b>Maven</b>

```
<repositories>
	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
</repositories>
```
```
<dependency>
    <groupId>com.github.User</groupId>
    <artifactId>Repo</artifactId>
    <version>Tag</version>
</dependency>
```

<h1>Create your config:</h1>

- <b>Nested config class</b>

```java
@Config
public class TestConfigNested
{
	// You can access parent config if applicable.
	@ParentConfig
	public TestConfig parent;

	@Parameter(name = "name")
	public String name;

	// Some parameters may be required.
	@Required
	@Parameter(name = "value")
	public String value;
}
```

- <b>Containing config class</b>

```java
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

	// You can have nested configs.
	@Parameter( name = "nested", nested = Parameter.NestedSource.Internal )
	public TestConfigNested nested;

	//  You can have arrays of parameters of either basic types or of other config classes.
	@ParameterArray( name = "nested_array", componentClass = TestConfigNested.class )
	public TestConfigNested[] nestedArray;
	@ParameterArray( name = "string_array", componentClass = String.class )
	public String[] stringArray;
	
	//  You can have Class as parameter: in config file it must be specified with full classname,
	// superclass also may be pecified.
	@Parameter( name = "class", superClass = List.class )
	public Class clazz;

	// This nested config is in external file with relative path 'nested_file'.
	@Parameter( name = "nested_file", nested = Parameter.NestedSource.File )
	public TestConfigNested nestedFile;

	// Will be called before parsing.
	@BeforeParsing
	public void beforeParsing( ConfigParser configParser )
	{
		System.out.println( "\n   ---   Before parsing   ---   \n" );
	}
}
```

<h1>Parse config files:</h1>

```java
File testConfigJsonFile = new File( "test_config.json" );
// File testConfigYamlFile = new File( "test_config.yml" );

ClassLoader classLoader = ClassLoader.getSystemClassLoader( );

// You may specify classloader.
ConfigParser configParser = new ConfigParser( classLoader );

// ConfigParser will automatically decide which format to parse by using file extension.
TestConfig testConfig = ( TestConfig ) configParser.parseFile( TestConfig.class, testConfigJsonFile );
// TestConfig testConfig = ( TestConfig ) configParser.parseFile( TestConfig.class, testConfigYamlFile );
```
