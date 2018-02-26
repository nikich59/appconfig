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
		IllegalAccessException
	}

	Reason reason;

	public ConfigParseException( Reason reason )
	{
		this.reason = reason;
	}

	public ConfigParseException( Reason reason, Exception other )
	{
		super( other );
	}

	public ConfigParseException( Reason reason, String message )
	{
		super( message );
	}
}
