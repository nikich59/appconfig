package ru.nikich59.appconfig;

import com.google.common.io.CharStreams;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

/**
 * Created by prokoshev.n on 27.02.2018.
 */
public class ConfigParser
{
	public enum ConfigFormat
	{
		JSON,
		YAML
	}

	private ConfigMapParser configMapParser;

	public ConfigParser( )
	{
		configMapParser = new ConfigMapParser( this );
	}

	public ConfigParser( ClassLoader classLoader )
	{
		configMapParser = new ConfigMapParser( this, classLoader );
	}

	public void addClassLoader( ClassLoader classLoader )
	{
		configMapParser.addClassLoader( classLoader );
	}

	public Object parseMap( Class configClass, Map < String, Object > map )
			throws ConfigValidationException, ConfigParseException
	{
		return parseMap( configClass, map, null );
	}

	Object parseMap( Class configClass, Map < String, Object > map, Object parent )
			throws ConfigValidationException, ConfigParseException
	{
		return configMapParser.parseMap( configClass, map, parent );
	}

	public Object parseFile( Class configClass, File file )
			throws IOException, ConfigValidationException, ConfigParseException
	{
		return parseFile( configClass, file, null );
	}

	Object parseFile( Class configClass, File file, Object parent )
			throws IOException, ConfigValidationException, ConfigParseException
	{
		String fileExtension = FilenameUtils.getExtension( file.getAbsolutePath( ) );

		try ( FileReader fileReader = new FileReader( file ) )
		{
			switch ( fileExtension )
			{
				case "yml":
				case "yaml":
					return parseConfig( configClass, fileReader, ConfigFormat.YAML, parent );
				case "json":
				case "js":
					return parseConfig( configClass, fileReader, ConfigFormat.JSON, parent );
			}
		}

		throw new UnsupportedOperationException( "File extension: \'" + fileExtension +
				"\' is not supported" );
	}

	Object parseFile( Class configClass, File file, ConfigFormat configFormat, Object parent )
			throws IOException, ConfigValidationException, ConfigParseException
	{
		try ( FileReader fileReader = new FileReader( file ) )
		{
			return parseConfig( configClass, fileReader, configFormat, parent );
		}
	}

	public Object parseConfig( Class configClass, Reader configReader, ConfigFormat configFormat, Object parent )
			throws IOException, ConfigParseException, ConfigValidationException
	{
		return parseConfig( configClass, CharStreams.toString( configReader ), configFormat, parent );
	}

	public Object parseConfig( Class configClass, String configString, ConfigFormat configFormat, Object parent )
			throws ConfigValidationException, ConfigParseException
	{
		switch ( configFormat )
		{
			case JSON:
				try
				{
					return parseConfigJson( configClass, configString, parent );
				}
				catch ( ParseException e )
				{
					throw new ConfigParseException( ConfigParseException.Reason.SourceParseException, e );
				}
			case YAML:
				return parseConfigYaml( configClass, configString, parent );
		}

		throw new UnsupportedOperationException( "Format: \'" + configFormat.toString( ) +
				"\' is not supported" );
	}

	public Object parseConfigJson( Class configClass, String configString, Object parent )
			throws ParseException, ConfigParseException, ConfigValidationException
	{
		JSONParser jsonParser = new JSONParser( );

		JSONObject configJsonObject = ( JSONObject ) jsonParser.parse( configString );

		return configMapParser.parseMap( configClass, configJsonObject, parent );
	}

	public Object parseConfigYaml( Class configClass, String configString, Object parent )
			throws ConfigParseException, ConfigValidationException
	{
		Yaml yaml = new Yaml( );

		Map < String, Object > configYamlObject = yaml.load( configString );

		return configMapParser.parseMap( configClass, configYamlObject, parent );
	}
}
