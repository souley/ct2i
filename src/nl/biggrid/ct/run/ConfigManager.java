/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.run;

import java.io.File;
import java.util.Vector;

import nl.biggrid.ct.ui.ParamDataEntity;
import nl.biggrid.ct.ui.ParamFileEntity;
import nl.biggrid.ct.ui.ProcessEntity;
import nl.biggrid.ct.ui.SettingsDialog;
import nl.biggrid.ct.ui.TomoEntity;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.commons.configuration.XMLConfiguration;

/**
 *
 * @author souley
 */
public class ConfigManager {
    static final String CONFIG_FILE = "config.xml";
    static final String DEFAULT_CONFIG_FILE = "defaultsettings.xml";
    static final String CURRENT_DIR = System.getProperty("user.dir");
    static final String USER_CT2I = ".ct2i";
    static final String USER_CT2I_PATH = CURRENT_DIR + File.separator + ".ct2i";
    static final String USER_CONFIG_FILE = USER_CT2I + File.separator + DEFAULT_CONFIG_FILE;
    static final String USER_CONFIG_FILE_PATH = USER_CT2I_PATH + File.separator + DEFAULT_CONFIG_FILE;

    static private ConfigManager _instance = null;

    CombinedConfiguration config = null;
    private boolean userConfigPresent = false;

    public String getString(final String key) {
        return config.getString(key);
    }

    public int getInt(final String key) {
        return config.getInt(key);
    }

    public void setProperty(final String key, final Object value) {
        config.setProperty(key, value);
    }

    protected ConfigManager() {
    }

    static public ConfigManager instance() {
        if (null == _instance) {
            _instance = new ConfigManager();
        }
        return _instance;
    }

    public void init(final String userConfig) {
        userConfigPresent = (new File(USER_CONFIG_FILE_PATH)).isFile();
        try
        {
            XMLConfiguration currentConfig = new XMLConfiguration(CONFIG_FILE);
            if (userConfig != null) {
                if (currentConfig != null) {
                    currentConfig.setProperty("xml(0)[@fileName]", userConfig);
                }
            }
            if (userConfigPresent) {
                currentConfig.setProperty("xml(1)[@fileName]", USER_CONFIG_FILE);
            } else {
                currentConfig.setProperty("xml(1)[@fileName]", DEFAULT_CONFIG_FILE);
            }
            currentConfig.save();
        } catch(ConfigurationException cex)
        {
            System.err.println("Cannot find configuration file: " + CONFIG_FILE);
            cex.printStackTrace();
        }        
        try {
            DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder(CONFIG_FILE);
            config = builder.getConfiguration(true);
        } catch (ConfigurationException cex) {
            System.err.println("Cannot load configuration file: " + CONFIG_FILE);
            cex.printStackTrace();
        }
    }

    public void save(final SettingsDialog settingsWin, final Vector<TomoEntity> entities) {
        settingsWin.saveSettings();
        for (TomoEntity e : entities) {
            if (e instanceof ProcessEntity) {
                setProperty("omnimatch.binary[@file]", ((ProcessEntity)e).getCommand());
            } else if (e instanceof ParamFileEntity) {
                ParamFileEntity pfe = (ParamFileEntity)e;
                String id = e.getId();
                if (id.equalsIgnoreCase("tomogram")) {
                    setProperty("omnimatch.tomogram[@file]", pfe.getURL());
                } else if (id.equalsIgnoreCase("template")) {
                    setProperty("omnimatch.template[@file]", pfe.getURL());
                } else if (id.equalsIgnoreCase("mask")) {
                    setProperty("omnimatch.mask[@file]", pfe.getURL());
                } else if (id.equalsIgnoreCase("psf")) {
                    setProperty("omnimatch.psf[@file]", pfe.getURL());
                }
            } else if (e instanceof ParamDataEntity) {
                ParamDataEntity pde = (ParamDataEntity)e;
                String id = e.getId();
                Vector<String> values = pde.getValues();
                if (id.equalsIgnoreCase("fourier")) {
                    setProperty("omnimatch.fourier.value", Integer.parseInt(values.elementAt(0)));
                } else if (id.equalsIgnoreCase("output")) {
                    setProperty("omnimatch.output.value", values.elementAt(0));
                } else {
                   setProperty("omnimatch." + id + ".start", values.elementAt(0));
                   setProperty("omnimatch." + id + ".end", values.elementAt(1));
                   setProperty("omnimatch." + id + ".inc", values.elementAt(2));
                }
            }
        }
        final String pfp = USER_CT2I_PATH;
        File ct2iDir = new File(pfp);
        if (!ct2iDir.isDirectory() && !ct2iDir.mkdirs()) {
            System.err.println("Cannot create directory '"+ct2iDir+"'");
            return;
        }
        try {
            XMLConfiguration defaultConfig = (XMLConfiguration)config.getConfiguration("default");
            if (defaultConfig != null) {
                defaultConfig.save(USER_CONFIG_FILE);
            }
        } catch (ConfigurationException cex) {
            System.out.println("Cannot save configuration files ... ");
            cex.printStackTrace();
        }
    }
}
