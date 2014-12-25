package ak.akapi;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import static net.minecraftforge.common.config.Property.Type.*;

public class ConfigSavable extends Configuration
{
	public ConfigSavable(File file)
	{
		super(file);
	}
    public void set(String category, String key, int defaultValue)
    {
        set(category, key, Integer.toString(defaultValue), INTEGER);
    }
    public void set(String category, String key, boolean defaultValue)
    {
        set(category, key, Boolean.toString(defaultValue), BOOLEAN);
    }
    public void set(String category, String key, double defaultValue)
    {
        set(category, key, Double.toString(defaultValue), DOUBLE);
    }
    public void set(String category, String key, String defaultValue)
    {
        set(category, key, defaultValue, STRING);
    }
    public void set(String category, String key, String[] defaultValue)
    {
        set(category, key, defaultValue, STRING);
    }
    public void set(String category, String key, int[] arrays)
    {
    	String[] values = new String[arrays.length];
    	for(int i=0;i<values.length;i++)
    	{
    		values[i] = Integer.toString(arrays[i]);
    	}
    	set(category, key, values, INTEGER);
    }
    public void set(String category, String key, double[] arrays)
    {
    	String[] values = new String[arrays.length];
    	for(int i=0;i<values.length;i++)
    	{
    		values[i] = Double.toString(arrays[i]);
    	}
    	set(category, key, values, DOUBLE);
    }
    public void set(String category, String key, boolean[] arrays)
    {
    	String[] values = new String[arrays.length];
    	for(int i=0;i<values.length;i++)
    	{
    		values[i] = Boolean.toString(arrays[i]);
    	}
    	set(category, key, values, BOOLEAN);
    }
	public void set(String category, String key, HashSet set)
	{
		String[] values = new String[set.size()];
		Iterator it = set.iterator();
		int i = 0;
		while(it.hasNext())
		{
			values[i] = it.next().toString();
			i++;
		}
		set(category, key, values, STRING);
	}
	public void set(String category, String key, String str, Property.Type type)
	{
		ConfigCategory cat = getCategory(category);
		Map<String, Property> properties;
		Property prop = new Property(key, str, type);
		if (cat.containsKey(key))
		{
			properties = ObfuscationReflectionHelper.getPrivateValue(ConfigCategory.class, cat, "properties");
			properties.put(key, prop);
		}
	}
	public void set(String category, String key, String[] set, Property.Type type)
	{
		ConfigCategory cat = getCategory(category);
		Map<String, Property> properties;
		Property prop = new Property(key, set, type);
		if (cat.containsKey(key))
		{
			properties = ObfuscationReflectionHelper.getPrivateValue(ConfigCategory.class, cat, "properties");
			properties.put(key, prop);
		}
	}
}