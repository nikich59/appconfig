package ru.nikich59.appconfig;

/**
 * Created by Nikita on 26.02.2018.
 */
public class ConfigParseException extends Exception
{
	enum Reason
	{
		ValidationException,
		InstantiationException,
		FieldTypeException,
		ArrayComponentTypeException,
		RequiredFieldNotPresent,
		IllegalAccessException,
		SourceParseException,
		ClassNotFoundException,
		NestedLinkedConfigException,
		InitializationException
	}

	public ConfigParseException( Reason reason )
	{
		super( reason.toString() );
	}

	public ConfigParseException( Reason reason, Exception other )
	{
		super( reason.toString(), other );
	}

	public ConfigParseException( Reason reason, String message )
	{
		super( reason.toString() + ": " + message );
	}

	public ConfigParseException( Reason reason, String message, Exception other )
	{
		super( reason.toString() + ": " + message, other );
	}
}
