import ru.nikich59.appconfig.Config;
import ru.nikich59.appconfig.Parameter;
import ru.nikich59.appconfig.ParentConfig;
import ru.nikich59.appconfig.Required;

@Config
public class TestConfigNested
{
	@ParentConfig
	public TestConfig parent;

	@Parameter(name = "name")
	public String name;

	@Required
	@Parameter(name = "value")
	public String value;

	public void print()
	{
		System.out.println( "\tParent string: " + parent.s );
		System.out.println( "\tName: " + name );
		System.out.println( "\tValue: " + value );
	}
}
