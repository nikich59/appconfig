package ru.nikich59.appconfig;


public class ConfigValidationException extends Exception
{
	enum Reason
	{
		AnnotationNotPresent,
		ParameterArrayTypeNotMatch,
		ParameterTypeNotMatch,
		ParameterArrayIsNotArray
	}

	private Reason reason;

	public ConfigValidationException( Reason reason )
	{
		this.reason = reason;
	}

	public ConfigValidationException( Reason reason, String message )
	{

		super( message );
		this.reason = reason;
	}
}
